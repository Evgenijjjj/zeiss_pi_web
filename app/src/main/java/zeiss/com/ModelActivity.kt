package zeiss.com

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.MenuItem
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import kotlinx.android.synthetic.main.activity_model.*
import java.util.concurrent.CompletableFuture
import kotlin.math.abs

class ModelActivity : AppCompatActivity() {

    lateinit var scene: Scene
    private var rocket: Node? = null
    private var redPoint: Node? = null
    private var model: Node? = null
    private var model2: Node? = null
    private var previousXLocation: Float? = null
    private var previousYLocation: Float? = null

    // Scaling related listeners.
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mScaleFactor = 1f
    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (detector.currentSpan < 400f) {
                // This may be helpful in the future. It gives a rough estimate on when the
                // pinch (scale gesture) should actually handled as pan.
                // return true
            }

            // Update scale factor.
            mScaleFactor *= detector.scaleFactor

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f))

            // We invert the scale to make the pinch gesture more intuitive.
            val adjustedScale = 1 / mScaleFactor

            // Update the camera position, zooming is implemented by changing the distance on the
            // z-axis.
            if (rocket != null) {
                val oldPos = scene.camera.localPosition
                scene.camera.localPosition = Vector3(oldPos.x, oldPos.y, adjustedScale)
            }

            return true
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        button_to_augmented.setOnClickListener {
            val intent = Intent(this, ARActivity::class.java)
            startActivity(intent)
        }

        scene = sceneView.scene

        // Render the object to the scene.

        // WARNING! MOCK DATA! TODO: Add real source!
        // points: rocket body, top, wing
        val positions: List<Vector3> = listOf(Vector3(0.15f, 0.15f, -0.2f), Vector3(0f, 0.45f, 2.48f), Vector3(0.4f, 0.005f, -1.55f)) //, Vector3(), Vector3(), Vector3())
        renderObjects(R.raw.rocket, R.raw.redball, positions)

        // Setup scale detector.
        mScaleDetector = ScaleGestureDetector(this@ModelActivity, scaleListener)

        // Setup camera.
        val camera = scene.camera
        camera.localPosition = Vector3(0f, 0.0f, 1f)

        // Initialize drag locations (required to prevent jumping, could probably be improved).
        var previousDragLocationX = 0.0f
        var previousDragLocationY = 0.0f

        // Register onTouchListener that is called automagically upon touch events afterwards.
        scene.setOnTouchListener { _, motionEventTest ->

            // Make sure that the ScaleDetector was set up properly.
            mScaleDetector?.onTouchEvent(motionEventTest)

            // Only proceed if model has been set up.
            if (rocket == null) {
                return@setOnTouchListener true
            }

            // Implement 3 finger pan.
            if (motionEventTest.pointerCount == 3) {

                // This exits the listener when we are not dealing with MOVE events.
                // This prevents some weird 'jumping issues'.
                if (motionEventTest.action != MotionEvent.ACTION_MOVE) {
                    previousDragLocationX = motionEventTest.x
                    previousDragLocationY = motionEventTest.y
                    return@setOnTouchListener true
                }

                // TODO: Update hardcoded values with screen width and height.
                val deltaX = (motionEventTest.x - (previousDragLocationX)) / 1500f
                val deltaY = (motionEventTest.y - (previousDragLocationY)) / 3000f

                // Update previous values.
                previousDragLocationX = motionEventTest.x
                previousDragLocationY = motionEventTest.y


                // HACK: Ignore large changes to get rid of jumping issues.
                if (deltaX > 50 || deltaY > 50) {
                    previousYLocation = motionEventTest.y
                    previousXLocation = motionEventTest.x
                    return@setOnTouchListener true
                }

                // Add the calculated delta values to our current position.
                val oldPos = scene.camera.localPosition
                scene.camera.localPosition = Vector3(oldPos.x - deltaX, oldPos.y + deltaY, oldPos.z)



                return@setOnTouchListener true
            }

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
            val rot = calculateRotationForDistance(motionEventTest.x, motionEventTest.y, rocket!!.localRotation)
            rocket!!.localRotation = rot

            return@setOnTouchListener true
        }

    }

    /**
     * x: x position of touch in screen
     * y: y position of touch in screen
     * curRotation: current rotation of rocket
     *
     * Returns Quaternion for rotating rocket into the correct position depending on
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

    private fun renderObjects(objectPathRocket: Int, objectPathPoint: Int, positions: List<Vector3>) {
        ModelRenderable.builder()
            .setSource(this, objectPathRocket)
            .build()
            .thenAccept {
                addRocketNodeToScene(it)
            }
            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message)
                    .setTitle("error!")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }

        positions.forEach {pos ->
            ModelRenderable.builder()
                .setSource(this, objectPathPoint)
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

    private fun addRocketNodeToScene(md: ModelRenderable?) {
        rocket = Node().apply {
            setParent(scene)
            localPosition = Vector3(0f, 0.1f, 0f)
            localScale = Vector3(0.2f, 0.2f, 0.2f)
            name = "Rocket"
        }

        // HACK: This is needed in order to adjust the center of rotation
        // Otherwise it's always located at the bottom of the character.
        model = Node()
        model!!.setParent(rocket)
        model!!.renderable = md
        model!!.localPosition = Vector3(0f, -0.1f, 0f)

        scene.addChild(rocket)
    }

    private fun addRedPointNodeToScene(md: ModelRenderable?, localPosition: Vector3) {
        model2 = Node()
        model2!!.setParent(model)
        model2!!.renderable = md
        model2!!.localScale = Vector3(0.1f, 0.1f, 0.1f)
        model2!!.localPosition = localPosition

        model2?.setOnTapListener { hitTestResult, motionEvent ->
            startActivity(Intent(this, MeasuredValueInfoActivity::class.java))
        }
        rocket!!.addChild(model)

    }

    override fun onPause() {
        super.onPause()
        sceneView.pause()
    }

    override fun onResume() {
        super.onResume()
        sceneView.resume()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            super.onBackPressed()
            return true
        }
        return false
    }
}
