package zeiss.com.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
public class Characteristic(val path: String,val module: String, val position: SCNVector3, val direction: SCNVector3): Parcelable {
    var nominalValue: Double? = null
    var measuredValue: Double? = null
    var upperLimit: Double? = null
    var lowerLimit: Double? = null

    constructor(path: String,
                module: String,
                position: SCNVector3,
                direction: SCNVector3,
                nominalValue: Double?,
//                measuredValue: Double?,
                upperLimit: Double?,
                lowerLimit: Double?): this(path, module, position, direction) {
        this.nominalValue = nominalValue
//        this.measuredValue = measuredValue
        this.upperLimit = upperLimit
        this.lowerLimit = lowerLimit
    }

}