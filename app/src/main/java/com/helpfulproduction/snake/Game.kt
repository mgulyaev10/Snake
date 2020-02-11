package com.helpfulproduction.snake

import android.content.Context

class Game(
    private val context: Context,
    private val gameFieldProvider: GameFieldProvider,
    gamesEventListener: GamesEventListener
) {

    private var isChangeDirectionAvaliable = true

    var direction: Direction = Direction.RIGHT
    private val directionProvider = object: DirectionProvider {
        override fun resolveCurrentDirection(): Direction {
            isChangeDirectionAvaliable = true
            return direction
        }
    }

    val gameThread = GameThread(gamesEventListener, gameFieldProvider, directionProvider)

    fun start(context: Context) {
        val lose = false

        gameThread.start()
    }

    fun changeDirection(newDirection: Direction) {
        if (newDirection == Direction.UP && direction == Direction.DOWN || newDirection == Direction.DOWN && direction == Direction.UP
            || newDirection == Direction.LEFT && direction == Direction.RIGHT || newDirection == Direction.RIGHT && direction == Direction.LEFT) {
            return
        } else {
            direction = newDirection
            isChangeDirectionAvaliable = false
        }
    }

    fun isChangeDirectionAvaliable() = isChangeDirectionAvaliable


    enum class Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    interface DirectionProvider {
        fun resolveCurrentDirection(): Direction
    }
}