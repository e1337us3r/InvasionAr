package com.apolets.arinvaders.game

import com.apolets.arinvaders.ShipManager
import com.apolets.arinvaders.sound.Maestro


/*
* Class for managing the player and game-related operations.
*
* */

class GameManager private constructor() {

    init {
        // do stuff when GameManager.instance is assigned
    }

    private object Holder {
        val INSTANCE = GameManager()
    }

    // I don't claim to understand this, but it makes this class a Singleton
    companion object {
        val instance: GameManager by lazy { Holder.INSTANCE }
    }

    private val shipManager = ShipManager.instance
    var score = 0

    fun startGameSession() {
        shipManager.spawnLoop.start()
    }

    fun endGameSession() {

        Maestro.stopMusic() // we can do this here... need to start in MainActivity due to context issues though
        shipManager.spawnLoop.stop()
    }

} // end class