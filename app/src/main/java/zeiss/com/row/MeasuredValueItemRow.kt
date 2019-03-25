package zeiss.com.row

import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.measured_value_row.view.*
import zeiss.com.R

class MeasuredValueItemRow: Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.measured_value_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.type_measured_value_row
        viewHolder.itemView.info_measured_value_row
        viewHolder.itemView.number_measured_value_row
    }
}