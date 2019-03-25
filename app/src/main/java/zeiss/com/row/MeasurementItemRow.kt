package zeiss.com.row

import android.graphics.Bitmap
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.measurement_row.view.*
import zeiss.com.R
import zeiss.com.model.MeasurementPoint

class MeasurementItemRow(public val m: MeasurementPoint,private val bm: Bitmap, public val operator: String, public val batch: String, public val date: String): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.measurement_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.image_view_measurement_row.setImageBitmap(bm)
        viewHolder.itemView.date_measurement_row.text = date
        viewHolder.itemView.operator_measurement_row


        viewHolder.itemView.info_measurement_row.text = "$operator\n$batch"
    }
}