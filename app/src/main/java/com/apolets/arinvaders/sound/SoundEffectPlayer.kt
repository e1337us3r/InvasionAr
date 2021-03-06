package com.apolets.arinvaders.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.apolets.arinvaders.R
import java.util.concurrent.ThreadLocalRandom

enum class SoundEffects(val effectName: Int, val volumeLevel: Float = 1f, var id: Int = 0, val effectId: String = "") {
    LASER(R.raw.laser, 0.5f),
    EXPLOSION(R.raw.bomb),
    EARTH_HIT_1(R.raw.scream_1, 1.0f, 0, "planetEffect"),
    EARTH_HIT_2(R.raw.scream_2, 1.0f, 0, "planetEffect"),
    EARTH_HIT_3(R.raw.scream_3, 1.0f, 0, "planetEffect")
}

object SoundEffectPlayer {

    private val soundPool: SoundPool
    private val random = ThreadLocalRandom.current()
    private val earthEffects = mutableListOf<SoundEffects>()

    init {

        val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build()

        soundPool = SoundPool.Builder()
                .setMaxStreams(20)
                .setAudioAttributes(audioAttributes)
                .build()

        initEarthEffects()
    }

    fun loadAllEffects(context: Context) {
        SoundEffects.values().forEach {
            it.id = soundPool.load(context, it.effectName, 1)
        }
    }

    fun playEffect(soundEffect: SoundEffects) {
        if (soundEffect.id != 0)
            soundPool.play(soundEffect.id, soundEffect.volumeLevel, soundEffect.volumeLevel, 1, 0, 1f)
    }

    // it seems a bit clumsy and unnecessary, as the effects enum is static.
    // maybe you can think of a better solution
    private fun initEarthEffects() {
        SoundEffects.values().forEach {

            if (it.effectId == "planetEffect") {
                earthEffects.add(it)
            }
        }
    }

    fun randomEarthEffect(): SoundEffects {

        return earthEffects[random.nextInt(earthEffects.size)]
    }

}