package zeiss.com

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.android.synthetic.main.activity_measured_value_info.*

class MeasuredValueInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measured_value_info)
        title = "Measured Value"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /**
         * Views...
         */
        date_range_measured_value_info.text = getString(R.string._30_november_2017)
        line_chart_measured_value_info

        segments_measured_value_info.isSelected = true

        button1.isSelected = true

        min_measured_value_info.text = "-0.002"
        max_measured_value_info.text = "1.05"
        range_measured_value_info.text = "1.15"

        mean_measured_value_info.text = "0.01"
        sigma_measured_value_info.text = "0.005"
        outlier_measured_value_info.text = "2.05"

        measurements_measured_value_info

        nominal_value_measured_value_info.text = "1.023"
        lower_limit_measured_value_info.text = "-0.004"
        upper_limit_measured_value_info.text = "1.015"

        all_attr_measured_value_info

        getLineChar()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            super.onBackPressed()
            return true
        }
        return false
    }

    private fun getLineChar(){
        val monthNames: Array<String> = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep","Oct",
            "Nov", "Dec")

        val lineEntries = mutableListOf<Entry>()
        lineEntries.add(Entry(0f, 7f))
        lineEntries.add(Entry(1f, 2f))
        lineEntries.add(Entry(2f, 3f))
        lineEntries.add(Entry(3f, 4f))
        lineEntries.add(Entry(4f, 2f))
        lineEntries.add(Entry(5f, 3f))
        lineEntries.add(Entry(6f, 1f))
        lineEntries.add(Entry(7f, 5f))
        lineEntries.add(Entry(8f, 7f))
        lineEntries.add(Entry(9f, 6f))
        lineEntries.add(Entry(10f, 10f))
        lineEntries.add(Entry(11f, 5f))

        val lineDataSet = LineDataSet(lineEntries, "")

        line_chart_measured_value_info.animateY(1000)
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
            lineDataSet.setDrawFilled(true)

        val lineData = LineData(lineDataSet)

        line_chart_measured_value_info.description.text = ""
        line_chart_measured_value_info.xAxis.position = XAxis.XAxisPosition.BOTH_SIDED
        line_chart_measured_value_info.data = lineData

        val xAxis = line_chart_measured_value_info.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 12f
        xAxis.textColor = Color.RED
        xAxis.setValueFormatter(IndexAxisValueFormatter(monthNames))

        line_chart_measured_value_info.invalidate()
    }
}
