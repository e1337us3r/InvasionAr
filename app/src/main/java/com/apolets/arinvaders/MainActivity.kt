package com.apolets.arinvaders

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.apolets.arinvaders.sound.Maestro
import com.apolets.arinvaders.sound.Music
import com.apolets.arinvaders.sound.SoundEffectPlayer
import com.apolets.arinvaders.sound.SoundEffects
import com.apolets.arinvaders.static.Configuration
import com.viro.core.*
import com.viro.core.Vector
import java.io.IOException
import java.io.InputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var viroView: ViroViewARCore
    lateinit var arScene: ARScene
    lateinit var earthObject: Object3D

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viroView = ViroViewARCore(this, object : ViroViewARCore.StartupListener {
            override fun onSuccess() {
                displayScene()
            }

            override fun onFailure(error: ViroViewARCore.StartupError?, errorMessage: String?) {
                Log.d(Configuration.DEBUG_TAG, "Error initializing AR: $errorMessage")
            }

        })


        //Use this view as the main view instead of an xml layout file.
        setContentView(viroView)
        SoundEffectPlayer.loadAllEffects(this)

    }


    //Spawns earth, initializes spawn loop
    private fun startGame(earthPosition: Vector) {

        earthObject = spawnEarth(earthPosition)
        ShipManager.instance.setMainActivity(this)

        setAttackListener()

        ShipManager.instance.spawnWaveOfShips(7)
        //ShipManager.instance.spawnLoop.start()

        Maestro.playMusic(this, Music.BATTLE, true)
    }

    private fun displayScene() {

        // Create the 3D AR scene, and display the point cloud
        arScene = ARScene()
        arScene.displayPointCloud(true)

        // Create a TrackedPlanesController to visually display identified planes .
        val controller = PlanesController()

        // Spawn a 3D Droid on the position where the user has clicked on a tracked plane.
        controller.addOnPlaneClickListener(object : ClickListener {
            override fun onClick(i: Int, node: Node, clickPosition: Vector) {
                Log.d(Configuration.DEBUG_TAG, "On plane click")

                //Remove plane detection and click listener
                controller.removeOnPlaneClickListener(this)
                arScene.setListener(null)
                arScene.displayPointCloud(false)


                startGame(clickPosition)

            }

            override fun onClickState(i: Int, node: Node, clickState: ClickState, vector: Vector) {
                //No-op
            }
        })

        arScene.setListener(controller)

        viroView.scene = arScene


    }

    private fun spawnEarth(position: Vector): Object3D {
        // Create a droid on the surface
        val bot = getBitmapFromAsset("earth_ball.jpg")
        val object3D = Object3D()
        object3D.setPosition(position)

        arScene.rootNode.addChildNode(object3D)

        // Load the Android model asynchronously.
        object3D.loadModel(viroView.viroContext, Uri.parse("file:///android_asset/earth_ball.obj"), Object3D.Type.OBJ, object : AsyncObject3DListener {
            override fun onObject3DLoaded(obj: Object3D, type: Object3D.Type) {
                // When the model is loaded, set the texture associated with this OBJ
                val objectTexture = Texture(bot, Texture.Format.RGBA8, false, false)
                val material = Material()

                material.diffuseTexture = objectTexture

                obj.geometry.materials = Arrays.asList(material)

                //Model has to be scaled down programmatically since it is too big
                obj.setScale(Vector(0.005, 0.005, 0.005))

                Log.d(Configuration.DEBUG_TAG, "Model loaded")


            }

            override fun onObject3DFailed(s: String) {
                Log.d(Configuration.DEBUG_TAG, "Model load failed :$s")
            }
        })

        //Give the earth node an identifier so it is distinguished from ships
        object3D.name = Configuration.EARTH_NODE_NAME

        Log.d(Configuration.DEBUG_TAG, "Earth coordinates: x:${object3D.positionRealtime.x}, y:${object3D.positionRealtime.y}, z:${object3D.positionRealtime.z} root: ${arScene.rootNode.positionRealtime}")
        return object3D
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setAttackListener() {


        viroView.setOnTouchListener { view, motionEvent ->
            //create hit test
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                Log.d(Configuration.DEBUG_TAG, "Screen touch listener")
                SoundEffectPlayer.playEffect(SoundEffects.LASER)
            }

            true
        }


    }

    private fun obtainScreenCenterMotionEvent(): MotionEvent {

        val screenCenter = getScreenCenter()

        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100
        val x = screenCenter.x.toFloat()
        val y = screenCenter.y.toFloat()

        val metaState = 0
        return MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                x,
                y,
                metaState)
    } // end obtainScreenCenterMotionEvent

    private fun getScreenCenter(): android.graphics.Point {

        val mainView = findViewById<View>(android.R.id.content)
        return android.graphics.Point(mainView.width / 2, mainView.height / 2)
    }

    fun getBitmapFromAsset(assetName: String): Bitmap? {
        val assetManager = this.resources.assets
        val imageStream: InputStream
        try {
            imageStream = assetManager.open(assetName)
        } catch (exception: IOException) {
            Log.d(Configuration.DEBUG_TAG, "Unable to find image [" + assetName + "] in assets! Error: "
                    + exception.message)
            return null
        }
        val bitmap = BitmapFactory.decodeStream(imageStream)
        imageStream.close()

        return bitmap
    }

    private inner class PlanesController : ARScene.Listener {


        private val surfaces = HashMap<String, Node>()
        private val planeClickListeners = HashSet<ClickListener>()


        fun addOnPlaneClickListener(listener: ClickListener) {
            planeClickListeners.add(listener)
        }

        fun removeOnPlaneClickListener(listener: ClickListener) {
            if (planeClickListeners.contains(listener)) {
                planeClickListeners.remove(listener)
            }
        }

        override fun onTrackingInitialized() {

        }

        override fun onTrackingUpdated(p0: ARScene.TrackingState?, p1: ARScene.TrackingStateReason?) {
        }

        override fun onAmbientLightUpdate(p0: Float, p1: Vector?) {
        }

        override fun onAnchorUpdated(arAnchor: ARAnchor?, p1: ARNode?) {
            if (arAnchor?.type == ARAnchor.Type.PLANE) {
                val planeAnchor = arAnchor as ARPlaneAnchor

                // Update the mesh surface geometry
                val node = surfaces[arAnchor.anchorId]
                val plane = node!!.geometry as Surface
                val dimensions = planeAnchor.extent
                plane.width = dimensions.x
                plane.height = dimensions.z
            }
        }

        override fun onAnchorFound(arAnchor: ARAnchor?, arNode: ARNode?) {

            // Spawn a visual plane if a PlaneAnchor was found
            if (arAnchor?.type == ARAnchor.Type.PLANE) {
                val planeAnchor = arAnchor as ARPlaneAnchor

                // Create the visual geometry representing this plane
                val dimensions = planeAnchor.extent
                val plane = Surface(1f, 1f)
                plane.width = dimensions.x
                plane.height = dimensions.z

                // Set a default material for this plane.
                val material = Material()
                material.diffuseColor = Color.parseColor("#eff1f4")
                material.blendMode = Material.BlendMode.ALPHA
                plane.materials = Arrays.asList(material)

                // Attach it to the node
                val planeNode = Node()
                planeNode.geometry = plane
                planeNode.setRotation(Vector(-Math.toRadians(90.0), 0.0, 0.0))
                planeNode.setPosition(planeAnchor.center)

                // Attach this planeNode to the anchor's arNode
                arNode?.addChildNode(planeNode)
                surfaces[arAnchor.getAnchorId()] = planeNode

                // Attach click listeners to be notified upon a plane onClick.
                planeNode.clickListener = object : ClickListener {
                    override fun onClick(i: Int, node: Node, vector: Vector) {
                        for (listener in planeClickListeners) {
                            listener.onClick(i, node, vector)
                            //Remove detected planes from scene
                            arNode?.removeAllChildNodes()
                        }
                    }

                    override fun onClickState(i: Int, node: Node, clickState: ClickState, vector: Vector) {
                        //No-op
                    }
                }


            }

        }

        override fun onAnchorRemoved(arAnchor: ARAnchor?, p1: ARNode?) {
            surfaces.remove(arAnchor?.anchorId)
        }
    }


    // Lifecycle methods
    override fun onStart() {
        super.onStart()
        viroView.onActivityStarted(this)
    }

    override fun onResume() {
        super.onResume()
        viroView.onActivityResumed(this)
    }

    override fun onPause() {
        super.onPause()
        viroView.onActivityPaused(this)
    }

    override fun onStop() {
        super.onStop()
        viroView.onActivityStopped(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        viroView.onActivityDestroyed(this)
    }
}
