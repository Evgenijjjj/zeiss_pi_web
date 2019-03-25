package zeiss.com

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_measurement_info.*
import zeiss.com.model.MeasurementPoint
import zeiss.com.row.MeasuredValueItemRow

class MeasurementInfoActivity : AppCompatActivity() {
    private lateinit var adapter: GroupAdapter<ViewHolder>
    private lateinit var measurement: MeasurementPoint

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measurement_info)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.measurement)

        val date = intent.getStringExtra(getString(R.string.dateExtra))
        val operator = intent.getStringExtra(getString(R.string.operatorExtra))
        val batch = intent.getStringExtra(getString(R.string.batchExtra))
        measurement = intent.getParcelableExtra<MeasurementPoint>(getString(R.string.measurementExtra))



        date_measurement_info.text = date
        operator_measurement_info.text = operator
        batch_measurement_info.text = batch
        view_all_attr_tv_meas_info.text = "View All Attributes (${measurement.characteristics.size})"
        view_all_attr_measurement_info
        measurement_info_recycler_view

        adapter = GroupAdapter()
        measurement_info_recycler_view.adapter = adapter

        adapter.setOnItemClickListener { item, view ->
            startActivity(Intent(this, MeasuredValueInfoActivity::class.java))
        }

        fillAdapter()
    }

    private fun fillAdapter() {
        //demo
        for (i in 0..100)
            adapter.add(MeasuredValueItemRow())
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            super.onBackPressed()
            return true
        }
        return false
    }
}
