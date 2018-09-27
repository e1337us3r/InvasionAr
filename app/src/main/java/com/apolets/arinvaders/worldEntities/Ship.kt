package com.apolets.arinvaders.worldEntities

import android.util.Log
import com.apolets.arinvaders.static.Configuration
import com.viro.core.AnimationTimingFunction
import com.viro.core.AnimationTransaction
import com.viro.core.Object3D
import com.viro.core.Vector
import java.util.*
import kotlin.math.pow

// you only need a companion object if you want to refer to these
// from outside the class (I guess... not sure if this is good code or not)
const val DEFAULT_MOVE_SPEED = 10
const val DEFAULT_HP = 1
const val DEFAULT_UNSCALED_MIN_DMG = 100000000L
const val DEFAULT_UNSCALED_MAX_DMG = 200000000L

enum class ShipType(val modelName: String, val speed: Int, val hp: Int, val dmgScaleValue: Float) {
    UFO("CUPIC_FYINGSAUCER.sfb", DEFAULT_MOVE_SPEED, DEFAULT_HP, 1.0f),
    FIGHTER("SciFi_Fighter_AK5.sfb", DEFAULT_MOVE_SPEED, DEFAULT_HP, 1.3f)
    // TODO: add more ships as we get more models
}

private val rGen = Random()

private fun randomizedDmgValue(dmgScaleValue: Float): Long {

    val min = DEFAULT_UNSCALED_MIN_DMG
    val max = DEFAULT_UNSCALED_MAX_DMG

    // with the default scale value (1.0), dmg will be between 100 - 200 million ppl per ufo hit.
    // the formula is pretty clunky atm, but we don't really need something more elaborate
    return (rGen.nextFloat() * (max - min) + min * dmgScaleValue).toLong()
}

class Ship(
        // ships default to their type's hp and speed, but these can be varied manually if needed
        val type: ShipType = ShipType.UFO,
        val speed: Int = type.speed,
        var hp: Int = type.hp,
        val dmg: Long = randomizedDmgValue(ShipType.UFO.dmgScaleValue)) : Object3D() {

    /*companion object {
        val renderables = mutableMapOf<ShipType, ModelRenderable>()
    }*/

    // each ship has a unique identifier, to enable easy tracking
    val id = java.util.UUID.randomUUID().toString()

    // called when the laser hits the ship from the middle of the screen
    /*fun onTouchNode(hitTestResult: HitTestResult, mEvent: MotionEvent) {

        // since playerAttack in MainActivity already calls damageShip, this
        // method is useless atm. but let's preserve it for now in case we want to
        // use it for something (or refactor MainActivity)

        if (hitTestResult.node == null) return
    }*/

    fun damageShip(dmg: Int) {
        Log.d(Configuration.DEBUG_TAG, "Ship damaged.")
        this.hp -= dmg
        if (this.hp <= 0) {
            destroyShip(false)
        }
    }

    fun destroyShip(destroyedByTheEarth: Boolean) {

        if (destroyedByTheEarth) {

            // TODO: play nuke explosion sound / effect?
        } else {
            // TODO: play explosion animation
            //SoundEffectPlayer.playEffect(SoundEffects.EXPLOSION)
        }
        this.dispose()

        Log.d(Configuration.DEBUG_TAG, "A ship was destroyed.")
    }

    fun attack(earthPosition: Vector) {

        val distanceFactor = calculateDistanceFactor(this.positionRealtime, earthPosition)
        Log.d(Configuration.DEBUG_TAG, "factor: $distanceFactor, time: ${3000 * distanceFactor}")

        AnimationTransaction.begin()
        AnimationTransaction.setAnimationDuration((3000 * distanceFactor).toLong())
        AnimationTransaction.setTimingFunction(AnimationTimingFunction.EaseOut)
        AnimationTransaction.setListener {
            Log.d(Configuration.DEBUG_TAG, "Animation complete")
            destroyShip(true)
        }
        this.setPosition(earthPosition)
        AnimationTransaction.commit()
    }

    private fun calculateDistanceFactor(start: Vector, end: Vector): Double {
        return Math.sqrt((end.x - start.x).pow(2).toDouble() + (end.y - start.y).pow(2).toDouble() + (end.z - start.z).pow(2))
    }
}