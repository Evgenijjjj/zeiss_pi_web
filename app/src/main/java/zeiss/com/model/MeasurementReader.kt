package zeiss.com.model

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.DataFormatException

class DataFrame(fileName: String, sep: String = ",", ctx: Context) {
    var header: List<String> = arrayListOf()
    var rows: List<List<String>> = arrayListOf()
    val ncol: Int
        get() = header.size
    val nrow: Int
        get() = rows.size

    init {
        val isr = InputStreamReader(ctx.assets.open(fileName))
        val br = BufferedReader(isr)

        val rowsWithHeader = br.readLines().filter { it != "" }.map {
            it.split(sep).map {
                if ("\".*\"".toRegex().matches(it))
                {it.subSequence(1, it.length - 1).toString()}
                else
                {it.toString()}
            }
        }
        header = rowsWithHeader[0]
        rows = rowsWithHeader.drop(1)
    }

    fun select(names: Set<String>) {
        rows = rows.map { it.filterIndexed {index, s ->  header[index] in names}}
        header = header.filter { it in names}
        if (!HashSet(header).equals(names)) {
            throw IllegalArgumentException("Can't find columns")
        }
    }

    fun rename(oldName: String, newName: String) {
        header = header.map { if (it == oldName) {newName} else {it}  }
    }

    fun row(i: Int): Map<String, String> {
        return mapOf(*rows[i].mapIndexed { index, s -> Pair(header[index], s) }.toTypedArray())
    }
}

// Struct with the value codes of the Zeiss files
class RelevantValueCodes() {
    val module = "K2342"
    val nominalValue = "K2101"
//    val measuredValue = "K1" Have this column in mesuarement file but do not have in table with all data.
    val upperLimit = "K2111"
    val lowerLimit = "K2110"
    val directionX = "K2540"
    val directionY = "K2541"
    val directionZ = "K2542"
    val positionX = "K2543"
    val positionY = "K2544"
    val positionZ = "K2545"
    val path = "Path"
    val valueCodesInspectionPlan = arrayListOf(path, module, directionX, directionY, directionZ, positionX, positionY, positionZ)
    val valueCodesMeasurement = arrayListOf(nominalValue, upperLimit, lowerLimit)
    val allValueCodes = valueCodesInspectionPlan.union(valueCodesMeasurement)
}

class MesuarementReader(private val ctx: Context) {
    val valueCodes = RelevantValueCodes()

    fun readFromCSV(file: String): Pair<ArrayList<MeasurementPoint>, ArrayList<Characteristic>> {
        val measurementPoints = arrayListOf<MeasurementPoint>()
        val characteristics = arrayListOf<Characteristic>()

        var df = DataFrame(fileName = file, ctx = ctx)

        if (df.ncol < valueCodes.allValueCodes.size) {
            // Check if german file, they use ";" as seperator and "," as number point
            df = DataFrame(file, ".", ctx)

            // if "," used, rplace it with "."
            df.rows.map { it.map { it.replace(',', '.') } }
        }

        // Take only codes or first word for columns
        df.header = df.header.map { it.split(' ')[0] }

        // choose only columns we need
        try {
            df.select(valueCodes.allValueCodes)
        } catch (e: Exception) {
            throw DataFormatException("Can't find obligatory columns")
        }

        // Since naming format is inconsistent, search for 'Path' or 'pfad'
        if ("Pfad" in df.header) {
            df.rename("Pfad", "Path")
        }

        var row_num = 0
        while (row_num < df.nrow) {
            val cur_row = df.row(row_num)
            if (row_num != df.nrow - 1) {
                val next_row = df.row(row_num + 1)
                if (next_row[valueCodes.path].toString().startsWith(cur_row[valueCodes.path].toString())) {
                    // Generate MeasurementPoint and add it
                    val positionVector = SCNVector3(
                        cur_row[valueCodes.positionX].toString().toDouble(),
                        cur_row[valueCodes.positionY].toString().toDouble(),
                        cur_row[valueCodes.positionZ].toString().toDouble()
                    )
                    val directionVector = SCNVector3(
                        cur_row[valueCodes.directionX].toString().toDouble(),
                        cur_row[valueCodes.directionY].toString().toDouble(),
                        cur_row[valueCodes.directionZ].toString().toDouble()
                    )
                    val currentMeasurementPoint = MeasurementPoint(
                        cur_row[valueCodes.path].toString(),
                        cur_row[valueCodes.module].toString(),
                        positionVector, directionVector
                    )

                    measurementPoints.add(currentMeasurementPoint)

                    row_num += 1
                    var row_to_add = df.row(row_num)

                    while (row_num < df.nrow &&
                        row_to_add[valueCodes.path].toString().startsWith(cur_row[valueCodes.path].toString())
                    ) {
                        // append all characteristics to MPoint and set line to the line after the last charac. of the MPoint

                        val positionVectorChar = SCNVector3(
                            cur_row[valueCodes.positionX].toString().toDouble(),
                            cur_row[valueCodes.positionY].toString().toDouble(),
                            cur_row[valueCodes.positionZ].toString().toDouble()
                        )
                        val directionVectorChar = SCNVector3(
                            cur_row[valueCodes.directionX].toString().toDouble(),
                            cur_row[valueCodes.directionY].toString().toDouble(),
                            cur_row[valueCodes.directionZ].toString().toDouble()
                        )

                        measurementPoints.last().characteristics.add(
                            Characteristic(
                                row_to_add[valueCodes.path].toString(),
                                row_to_add[valueCodes.module].toString(),
                                positionVectorChar, directionVectorChar,
                                row_to_add[valueCodes.nominalValue].toString().toDouble(),
//                                row_to_add[valueCodes.measuredValue].toString().toDouble(),
                                row_to_add[valueCodes.upperLimit].toString().toDouble(),
                                row_to_add[valueCodes.lowerLimit].toString().toDouble()
                            )
                        )
                        row_num += 1
                        if (row_num < df.nrow) {
                            row_to_add = df.row(row_num)
                        }
                    }
                    continue
                }
            }
            val positionVector = SCNVector3(
                cur_row[valueCodes.positionX].toString().toDouble(),
                cur_row[valueCodes.positionY].toString().toDouble(),
                cur_row[valueCodes.positionZ].toString().toDouble()
            )
            val directionVector = SCNVector3(
                cur_row[valueCodes.directionX].toString().toDouble(),
                cur_row[valueCodes.directionY].toString().toDouble(),
                cur_row[valueCodes.directionZ].toString().toDouble()
            )
            characteristics.add(
                Characteristic(
                cur_row[valueCodes.path].toString(),
                cur_row[valueCodes.module].toString(),
                positionVector, directionVector,
                cur_row[valueCodes.nominalValue].toString().toDouble(),
//                cur_row[valueCodes.measuredValue].toString().toDouble(),
                cur_row[valueCodes.upperLimit].toString().toDouble(),
                cur_row[valueCodes.lowerLimit].toString().toDouble()

                ))
            row_num += 1
        }

        return Pair(measurementPoints, characteristics)
    }
}
