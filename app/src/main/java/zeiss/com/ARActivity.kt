package zeiss.com

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.serialization.internal.HashMapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import zeiss.com.fragments.CustomArFragment
import zeiss.com.helpers.MQTTHelper
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.math.abs

class ARActivity : AppCompatActivity() {
    lateinit var arFragment: CustomArFragment
    private var shouldAddModel = true
    lateinit var node: Node
    private var model: Node? = null
    var mqttHelper: MQTTHelper? = null

    val positions: List<Vector3> = listOf(Vector3(0.15f, 0.15f, -0.2f), Vector3(0f, 0.45f, 2.48f), Vector3(0.4f, 0.005f, -1.55f)) //, Vector3(), Vector3(), Vector3())



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        startMqtt()

        arFragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as CustomArFragment
        arFragment.planeDiscoveryController.hide()
        arFragment.arSceneView.scene.addOnUpdateListener(Scene.OnUpdateListener { this.onUpdateFrame(it) })

        // Register onTouch listener.
        arFragment.arSceneView.scene.setOnTouchListener { hitTestResult, motionEventTest ->
            // Handle one finger event for rotations.

            // Calculate delta values.
            val gestureDeltaX = abs(motionEventTest.x - (previousXLocation ?: 0.0f))
            val gestureDeltaY = abs(motionEventTest.y - (previousYLocation ?: 0.0f))

            // HACK: Ignore large changes to get rid of jumping issues.
            if (gestureDeltaX > 50 || gestureDeltaY > 50) {
                previousYLocation = motionEventTest.y
                previousXLocation = motionEventTest.x
                return@setOnTouchListener true
            }

            // Apply the quaternion rotation to the element.
            val rot = calculateRotationForDistance(motionEventTest.x, motionEventTest.y, node.localRotation)
            node.localRotation = rot

            val jsonObject =
                json {
                    "x" To rot.x
                    "y" To rot.y
                    "z" To rot.z
                    "w" To rot.w
                }

            mqttHelper?.publish(jsonObject)

            return@setOnTouchListener true

        }


    }

    private fun startMqtt() {
        mqttHelper = MQTTHelper(applicationContext)
        mqttHelper?.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {

            }

            override fun connectionLost(throwable: Throwable) {

            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                if (!topic.contains(mqttHelper?.clientId.toString())) {
                    val sdpJson = Json.parse(HashMapSerializer(String.serializer(), String.serializer()), mqttMessage.toString())
                    val quaternion: Quaternion = Quaternion(sdpJson["x"]!!.toFloat(),sdpJson["y"]!!.toFloat(),sdpJson["z"]!!.toFloat(),sdpJson["w"]!!.toFloat())
                    node.localRotation = quaternion
                }
                Log.w("Debug", mqttMessage.toString())
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {

            }
        })
    }
    fun json(build: JsonObjectBuilder.() -> Unit): JSONObject {
        return JsonObjectBuilder().json(build)
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun placeObject(arFragment: ArFragment, anchor: Anchor, uri: Uri) {
        ModelRenderable.builder()
            .setSource(arFragment.context!!, uri)
            .build()
            .thenAccept { modelRenderable -> addNodeToScene(arFragment, anchor, modelRenderable) }
            .exceptionally { throwable ->
                Toast.makeText(arFragment.context, "Error:" + throwable.message, Toast.LENGTH_LONG).show()
                null
            }
    }

    private fun addRedPointNodeToScene(md: ModelRenderable?, localPosition: Vector3) {
        val model2 = Node()
        model2!!.setParent(model)
        model2!!.renderable = md
        model2!!.localScale = Vector3(0.1f, 0.1f, 0.1f)
        model2!!.localPosition = localPosition

        node!!.addChild(model)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun onUpdateFrame(frameTime: FrameTime) {
        val frame = arFragment.arSceneView.arFrame

        // Currently the model is only updated once. The QR code therefore has to remain static.
        // However, if you stop moving the QR code again, the model will be repositioned again.
        val augmentedImages = frame!!.getUpdatedTrackables(AugmentedImage::class.java)
        for (augmentedImage in augmentedImages) {
            if (augmentedImage.trackingState == TrackingState.TRACKING) {
                if (augmentedImage.name == "qr" && shouldAddModel) {
                    placeObject(
                        arFragment,
                        augmentedImage.createAnchor(augmentedImage.centerPose),
                        Uri.parse("rocket.sfb")
                    )
                    shouldAddModel = false
                }
            }
        }
    }

    fun setupAugmentedImagesDb(config: Config, session: Session): Boolean {
        val augmentedImageDatabase: AugmentedImageDatabase
        val bitmap = loadAugmentedImage() ?: return false

        augmentedImageDatabase = AugmentedImageDatabase(session)
        augmentedImageDatabase.addImage("qr", bitmap)
        config.augmentedImageDatabase = augmentedImageDatabase
        return true
    }

    private fun loadAugmentedImage(): Bitmap? {
        try {
            assets.open("qr.png").use { `is` -> return BitmapFactory.decodeStream(`is`) }
        } catch (e: IOException) {
            Log.e("ImageLoad", "IO Exception", e)
        }

        return null
    }


    // TODO: THIS METHOD HARDCODES THE CENTER OF ORIGIN FOR THE ROTATION.
    private fun addNodeToScene(arFragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)
        node = Node()
        node.setParent(anchorNode)
        node.localPosition = Vector3(0f, 0.1f, 0f)
        node.localScale = Vector3(0.105f,0.105f,0.105f)
        arFragment.arSceneView.scene.addChild(anchorNode)

        model = Node()
        model!!.setParent(node)
        model!!.renderable = renderable
        model!!.localPosition = Vector3(0f, -0.1f, 0f)

        val quat = Quaternion.axisAngle(Vector3(0f, 1f, 0f), 180f)
        model!!.localRotation = quat

        positions.forEach {pos ->
            ModelRenderable.builder()
                .setSource(this, R.raw.redball)
                .build()
                .thenAccept {
                    addRedPointNodeToScene(it, pos)
                }.exceptionally {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(it.message)
                        .setTitle("error!")
                    val dialog = builder.create()
                    dialog.show()
                    return@exceptionally null
                }
        }
    }

    var previousXLocation: Float? = null
    var previousYLocation: Float? = null

    /**
     * x: x position of touch in screen
     * y: y position of touch in screen
     * curRotation: current rotation of andy
     *
     * Returns Quaternion for rotating andy into the correct position depending on
     * position of touch in screen.
     */
    fun calculateRotationForDistance(x: Float, y: Float, curRotation: Quaternion): Quaternion {
        val width = 1500f
        val height = 3000f

        val scalarMultiplier = 4.75f

        val gestureDeltaX = x - (previousXLocation ?: 0.0f)
        val gestureDeltaY = y - (previousYLocation ?: 0.0f)

        val yRadians = (gestureDeltaX / width) * scalarMultiplier * 180f / 3.14f
        val xRadians = (gestureDeltaY / height) * scalarMultiplier * 180f / 3.14f

        val quat1 = Quaternion.axisAngle(Vector3(1f, 0f, 0f), xRadians)
        val quat2 = Quaternion.axisAngle(Vector3(0f, 1f, 0f), yRadians)

        previousXLocation = x
        previousYLocation = y

        return Quaternion.multiply((Quaternion.multiply(quat1, quat2)), curRotation)

    }

}

class JsonObjectBuilder {
    private val deque: Deque<JSONObject> = ArrayDeque()

    fun json(build: JsonObjectBuilder.() -> Unit): JSONObject {
        deque.push(JSONObject())
        this.build()
        return deque.pop()
    }

    infix fun <T> String.To(value: T) {
        deque.peek().put(this, value)
    }
}