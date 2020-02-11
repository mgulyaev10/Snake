package com.helpfulproduction.snake

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class GameThread(
    private val gamesEventListener: GamesEventListener,
    private val gameFieldProvider: GameFieldProvider,
    private val directionProvider: Game.DirectionProvider
) : HandlerThread("GAME_THREAD", Process.THREAD_PRIORITY_FOREGROUND) {

    private val snake: Deque<Int> = ArrayDeque<Int>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val field = gameFieldProvider.getField()

    private var gameStarted = true
    private var firstTurn = true

    override fun run() {
        while (gameStarted) {
            val changedPositions = if (firstTurn) {
                val headPosition = Random.nextInt(gameFieldProvider.getSizeOfField())
                field[headPosition] = 0
                snake.addFirst(headPosition)
                val eatPosition = generateNewEat()
                firstTurn = false
                val changedPositions = listOf(headPosition, eatPosition)
                changedPositions
            } else {
                val changedPositions = go()
                changedPositions
            }

            mainHandler.postAtFrontOfQueue {
                gamesEventListener.onTurn(changedPositions, snake.size - 1)
            }
             Thread.sleep(250)
        }
    }


    private fun go(): List<Int> {
        val head = newHeadPosition()
        val inThisSquareNow = field[head]
        var isEaten = false
        val changedPositions: ArrayList<Int>?
        when (inThisSquareNow) {
            0 -> changedPositions = onHeadNextPosition(head, saveTail = false)
            1 -> {
                mainHandler.postAtFrontOfQueue{
                    gamesEventListener.loseGame()
                }
                gameStarted = false
                changedPositions = arrayListOf()
            }
            2 -> {
                changedPositions = onHeadNextPosition(head, saveTail = true)
                isEaten = true
            }
            else -> throw RuntimeException("No such type of square")
        }

        if (isEaten) {
            changedPositions.add(generateNewEat())
        }
        return changedPositions
    }

    private fun newHeadPosition(): Int {
        val direction = directionProvider.resolveCurrentDirection()
        return when (direction) {
            Game.Direction.UP -> {
                val pos = snake.first - GameActivity.COLUMNS
                if (pos < 0) pos + GameActivity.COLUMNS * GameActivity.ROWS else pos
            }
            Game.Direction.DOWN -> {
                val pos = snake.first + GameActivity.COLUMNS
                if (pos >= GameActivity.COLUMNS * GameActivity.ROWS) pos - GameActivity.COLUMNS * GameActivity.ROWS else pos
            }
            Game.Direction.LEFT -> {
                val pos = snake.first - 1
                if (pos == -1 || pos / GameActivity.ROWS != snake.first / GameActivity.ROWS) snake.first + GameActivity.COLUMNS - 1 else pos
            }
            Game.Direction.RIGHT -> {
                val pos = snake.first + 1
                if (pos / GameActivity.ROWS != snake.first / GameActivity.ROWS) snake.first - GameActivity.COLUMNS + 1 else pos
            }
        }
    }

    private fun onHeadNextPosition(position: Int, saveTail: Boolean): ArrayList<Int> {
        field[position] = 1
        snake.addFirst(position)
        var tailPosition: Int? = null
        if (!saveTail) {
            tailPosition = snake.pollLast()
        }
        val positionsChanged = arrayListOf(position)
        tailPosition?.let {tail ->
            field[tail] = 0
            positionsChanged.add(tail)
        }
        return positionsChanged
    }

    private fun generateNewEat(): Int {
        var eat: Int
        do {
            eat = Random.nextInt(gameFieldProvider.getSizeOfField())
        } while (!gameFieldProvider.isAvailable(eat))
        field[eat] = 2
        return eat
    }
}

