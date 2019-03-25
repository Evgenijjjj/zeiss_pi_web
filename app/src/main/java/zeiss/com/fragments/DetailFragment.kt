package zeiss.com.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import kotlinx.android.synthetic.main.detail_fragment.*
import zeiss.com.MeasurementsActivity
import zeiss.com.ModelActivity
import zeiss.com.R
import zeiss.com.model.Characteristic
import zeiss.com.model.MeasurementPoint
import zeiss.com.model.MesuarementReader

class DetailFragment : Fragment(), View.OnClickListener {
    private lateinit var scene: Scene
    private var andy: Node? = null
    private var model: Node? = null
    private lateinit var data: Pair<ArrayList<MeasurementPoint>, ArrayList<Characteristic>>

    val positions: List<Vector3> = listOf(Vector3(0.15f, 0.15f, -0.2f), Vector3(0f, 0.45f, 2.48f), Vector3(0.4f, 0.005f, -1.55f)) //, Vector3(), Vector3(), Vector3())
    val pos_models : ArrayList<Node?> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.detail_fragment, container, false)

        /**
         * Here are all the information view from detail_fragment
         *
         * Further in the code do not need to write 'findViewById<TextView>', just use characteristics_count_tv.text = "356" !!
         */
        v.findViewById<TextView>(R.id.characteristics_count_tv)//.text = "365"
        v.findViewById<TextView>(R.id.measurement_points_count_tv)
        v.findViewById<TextView>(R.id.measurement_count_tv)

        val sampleNumber = 16
        v.findViewById<TextView>(R.id.last_day_measurements_tv).text = "Last day, 1 measurements"
        v.findViewById<TextView>(R.id.last_week_measurements_tv).text = "Last week, $sampleNumber measurements"


        /**
         * Charts
         * There can use Canvas to draw statistics in bitmap, then use chart.setImageBitmap(...)
         */
        v.findViewById<ImageView>(R.id.latest_measurements_chart)
        v.findViewById<ImageView>(R.id.last_day_measurements_chart)
        v.findViewById<ImageView>(R.id.last_week_measurements_chart)
        v.findViewById<com.google.ar.sceneform.SceneView>(R.id.rotating_model_view)


        for (child in v.touchables)
            child.setOnClickListener(this)


        val mr = MesuarementReader(activity!!)
        data = mr.readFromCSV("SGRAufbau3_InspectionPlan.csv")

        var characteristicsCount = 0
        for (d in data.first)
            characteristicsCount += d.characteristics.size

        Log.d("vsdvsdv", data.second.size.toString())

        for (mp in data.first)
            Log.d("vsdvsdv", "${mp.characteristics.size}")


        v.findViewById<TextView>(R.id.characteristics_count_tv).text = characteristicsCount.toString()
        v.findViewById<TextView>(R.id.measurement_points_count_tv).text = "0"
        v.findViewById<TextView>(R.id.measurement_count_tv).text = data.first.size.toString()

        return v
    }

    /**
     *  Here you can track all clicks from detail_fragment
     */
    override fun onClick(v: View?) {
        when (v) {
            measurement_points_btn -> Toast.makeText(activity, "Coming Soon", Toast.LENGTH_LONG).show()
            all_measurements_btn -> {
                val i = Intent(activity, MeasurementsActivity::class.java)
                i.putExtra(getString(R.string.measurementsExtra), data.first)
                startActivity(i)
            }
            latest_measurements_btn -> Toast.makeText(activity, "Coming Soon", Toast.LENGTH_LONG).show()
            last_day_measurements_btn -> Toast.makeText(activity, "Coming Soon", Toast.LENGTH_LONG).show()
            last_week_measurements_btn -> Toast.makeText(activity, "Coming Soon", Toast.LENGTH_LONG).show()
            rotating_model_view -> {
                Log.d("Detail", "3d thing clicked")
                val intent = Intent(activity, ModelActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (model == null) initModel()
    }

    private fun initModel() {
        scene = rotating_model_view.scene
        renderObject(R.raw.rocket)

        scene.camera.localPosition = Vector3(0f, -0.1f, 0.4f)
        scene.camera.localRotation = Quaternion(0.2f, 0f, 0f, 1.1f)
        scene.camera.worldRotation = Quaternion(0.2f, 0f, 0f, 1.1f)
        scene.camera.localScale = Vector3(0.8f, 0.8f, 0.4f)

        RotatingAsyncTask().execute()
    }

    private fun renderObject(objectPath: Int) {
        ModelRenderable.builder()
            .setSource(activity, objectPath)
            .build()
            .thenAccept {
                addNodeToScene(it)
            }
            .exceptionally {
                val builder = AlertDialog.Builder(activity!!)
                builder.setMessage(it.message)
                    .setTitle("error!")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }


        // TODO: the points don't show up for some reason
        positions.forEach {pos ->
            ModelRenderable.builder()
                .setSource(activity, R.raw.redball)
                .build()
                .thenAccept {
                    addRedPointNodeToScene(it, pos)
                }.exceptionally {
//                    val builder = AlertDialog.Builder(activity)
//                    builder.setMessage(it.message)
//                        .setTitle("error!")
//                    val dialog = builder.create()
//                    dialog.show()
                    return@exceptionally null
                }
        }
    }

    private fun addRedPointNodeToScene(md: ModelRenderable?, localPosition: Vector3) {
        val model2 = Node()
        model2!!.setParent(model)
        model2!!.renderable = md
        model2!!.localScale = Vector3(0.1f, 0.1f, 0.1f)
        model2!!.localPosition = localPosition

        model!!.addChild(model2)

        pos_models.add(model2)
    }

    private fun addNodeToScene(md: ModelRenderable?) {
        md?.let {
            andy = Node().apply {
                setParent(scene)
                localScale = Vector3(0.08f, 0.08f, 0.08f)
                name = "Android"
            }

            model = Node()
            model!!.setParent(andy)
            model!!.renderable = md
            scene.addChild(andy)
        }
    }

    override fun onPause() {
        super.onPause()
        rotating_model_view.pause()
    }

    override fun onResume() {
        super.onResume()
        rotating_model_view.resume()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class RotatingAsyncTask : AsyncTask<Void, Float, Void>() {
        private var v1: Float = 0.2f

        override fun doInBackground(vararg params: Void?): Void? {
            while (true) {
                if (v1 % 360 == 0f) v1 = 0f
                v1 += 0.2f
                publishProgress(v1)
                Thread.sleep(10)
            }
        }

        override fun onProgressUpdate(vararg values: Float?) {
            super.onProgressUpdate(*values)
            if (values[0] != null) {
                model?.apply { localRotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), values[0]!!) }
            }
        }
    }
}