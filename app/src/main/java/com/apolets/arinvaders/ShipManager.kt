package com.apolets.arinvaders

import android.net.Uri
import android.util.Log
import com.apolets.arinvaders.static.Configuration
import com.apolets.arinvaders.worldEntities.Ship
import com.apolets.arinvaders.worldEntities.ShipType
import com.viro.core.*
import com.viro.core.Vector
import java.util.*

/*
* Controls the collective operations regarding Ships
* (creation as waves, tracking, etc).
* @author Ville Lohkovuori, Sinan <just put your surname here>
* */

class ShipManager private constructor() {

    private lateinit var earthObject: Object3D
    val spawnLoop = SpawnLoop(this)
    private lateinit var mainActivity: MainActivity

    init {
    }

    private object Holder {

        val INSTANCE = ShipManager()
    }

    companion object {

        val instance: ShipManager by lazy { Holder.INSTANCE }
        const val DEFAULT_NUM_OF_SHIPS_IN_WAVE = 15
        const val DEFAULT_MIN_SPAWN_DIST = 2F
        const val DEFAULT_MAX_SPAWN_DIST = 2.5F
        const val DEFAULT_WAVE_LENGTH_MS = 8000L
    }

    fun setMainActivity(activity: MainActivity) {
        mainActivity = activity
    }

    private val rGen = Random(System.currentTimeMillis())

    fun spawnShip(shipType: ShipType) {

        val spawnCoord = randomCoord()

        val ship = createShipObject()

        ship.setPosition(spawnCoord)

        ship.name = "ENEMY"

        mainActivity.arScene.rootNode.addChildNode(ship)


        ship.attack(mainActivity.earthObject.positionRealtime)
    }


    private fun createShipObject(): Ship {
        // Create a droid on the surface
        val bot = mainActivity.getBitmapFromAsset("ufo.png")
        val ship = Ship()

        // Load the Android model asynchronously.
        ship.loadModel(mainActivity.viroView.viroContext, Uri.parse("file:///android_asset/ufo.obj"), Object3D.Type.OBJ, object : AsyncObject3DListener {
            override fun onObject3DLoaded(obj: Object3D, type: Object3D.Type) {
                // When the model is loaded, set the texture associated with this OBJ
                val objectTexture = Texture(bot, Texture.Format.RGBA8, false, false)
                val material = Material()

                material.diffuseTexture = objectTexture

                obj.geometry.materials = Arrays.asList(material)

                //Model has to be scaled down programmatically since it is too big
                obj.setScale(Vector(0.0005, 0.0005, 0.0005))

                Log.d(Configuration.DEBUG_TAG, "Model loaded")


            }

            override fun onObject3DFailed(s: String) {
                Log.d(Configuration.DEBUG_TAG, "Model load failed :$s")
            }
        })


        return ship
    }


    // we could add different spawn patterns (chosen by enum perhaps).
    //  without any arguments, spawns the default number of UFOs
    fun spawnWaveOfShips(numOfShips: Int = DEFAULT_NUM_OF_SHIPS_IN_WAVE, shipType: ShipType = ShipType.UFO) {
        for (item in 0..numOfShips) {

            spawnShip(shipType)
        }
    }



    private fun randomCoord(minDist: Float = DEFAULT_MIN_SPAWN_DIST,
                            maxDist: Float = DEFAULT_MAX_SPAWN_DIST): Vector {
        var sign = if (rGen.nextBoolean()) 1 else -1

        val x = (rGen.nextFloat() * (maxDist - minDist) + minDist) * sign

        sign = if (rGen.nextBoolean()) 1 else -1

        val z = (rGen.nextFloat() * (maxDist - minDist) + minDist) * sign

        val y = rGen.nextFloat() * maxDist
        Log.d(Configuration.DEBUG_TAG, "random UFO coordinates: x:$x, y:$y, z:$z ")
        return Vector(x, y, z)
    }
}