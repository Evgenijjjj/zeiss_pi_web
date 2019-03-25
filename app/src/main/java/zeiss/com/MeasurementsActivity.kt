package zeiss.com

import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_measurements.*
import zeiss.com.model.MeasurementPoint
import zeiss.com.row.MeasurementItemRow
import android.graphics.BitmapFactory



class MeasurementsActivity : AppCompatActivity() {
    private lateinit var adapter: GroupAdapter<ViewHolder>
    private lateinit var measurements: ArrayList<MeasurementPoint>
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measurements)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.measurements)

        measurements = intent.getParcelableArrayListExtra<MeasurementPoint>(getString(R.string.measurementsExtra))

        bitmap = BitmapFactory.decodeResource(
            this.resources,
            R.drawable.hardcoded_img
        )

        for (m in measurements)
            Log.d("vdsvsv", m.toString())

        adapter = GroupAdapter()
        measurements_recycler_view.adapter = adapter

        adapter.setOnItemClickListener { item, view ->
            val row = item as MeasurementItemRow
            val i = Intent(this, MeasurementInfoActivity::class.java)
            i.putExtra(getString(R.string.dateExtra), row.date)
            i.putExtra(getString(R.string.operatorExtra), row.operator)
            i.putExtra(getString(R.string.batchExtra), row.batch)
            i.putExtra(getString(R.string.measurementExtra), row.m)

            startActivity(i)
        }

        fillRecyclerView()
    }

    private fun fillRecyclerView() {
        // demo
        val o1 = "30, Williams"
        val o2 = "23, Schulze"
        val batchArr = listOf<String>("non", "dolor", "quos", "autem", "ipsa", "corrupti", "molestias")

        val d1 = getString(R.string._11_july_2017)
        val d2 = getString(R.string._30_november_2017)

        for (i in 0..(measurements.size - 1)) {
            val m = measurements[i]

            val operator: String = if (i % 3 == 0) o1
            else o2

            val date: String = if (i > 70) d1
            else d2

            adapter.add(MeasurementItemRow(m, bitmap, operator, batchArr[i % 6], date))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            super.onBackPressed()
            return true
        }
        return false
    }
}
