package zeiss.com.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class MeasurementPoint(val path: String, val module: String, val position: SCNVector3, val direction: SCNVector3): Parcelable {
    var characteristics: ArrayList<Characteristic> = arrayListOf()
}