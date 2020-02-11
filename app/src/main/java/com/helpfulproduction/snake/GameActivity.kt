package com.helpfulproduction.snake

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.util.TypedValue
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.RuntimeException
import java.util.*

class GameActivity: AppCompatActivity(), GamesEventListener {

    private var gameField: RecyclerView? = null
    private var up: Button? = null
    private var down: Button? = null
    private var left: Button? = null
    private var right: Button? = null
    private var back: Button? = null
    private var gameAdapter: SquareAdapter? = null
    private var score: TextView? = null

    private lateinit var game: Game

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        initializeField()
        initializeButtons()

        score = findViewById(R.id.score)

        game = Game(this, gameAdapter!!, this)
        game.start(this)
    }

    private fun initializeField() {
        gameField = findViewById(R.id.game_field)
        gameAdapter = SquareAdapter( )

        gameField?.apply {
            val manager = GridLayoutManager(this@GameActivity, COLUMNS, RecyclerView.VERTICAL, false)?.apply {}
            layoutManager = manager
            adapter = gameAdapter
        }
        val count = gameField?.itemDecorationCount ?: 0
        for (i in 0 until count) {
            gameField?.removeItemDecorationAt(i)
        }

        gameField?.addItemDecoration(GameFieldItemDecoration())
    }

    private fun initializeButtons() {
        up = findViewById(R.id.up)
        down = findViewById(R.id.down)
        left = findViewById(R.id.left)
        right = findViewById(R.id.right)
        back = findViewById(R.id.back)

        up?.setOnClickListener {
            if (game.isChangeDirectionAvaliable()) {
                game.changeDirection(Game.Direction.UP)
            }
        }
        down?.setOnClickListener {
            if (game.isChangeDirectionAvaliable()) {
                game.changeDirection(Game.Direction.DOWN)
            }
        }
        left?.setOnClickListener {
            if (game.isChangeDirectionAvaliable()) {
                game.changeDirection(Game.Direction.LEFT)
            }
        }
        right?.setOnClickListener {
            if (game.isChangeDirectionAvaliable()) {
                game.changeDirection(Game.Direction.RIGHT)
            }
        }

        back?.setOnClickListener {
            finish()
        }
    }

    override fun onPlayAgain() {
        game.start(this)
    }

    override fun onFinish() {
        finish()
    }

    override fun onTurn(changedPositions: List<Int>, score: Int) {
        changedPositions.forEach { position ->
            gameAdapter?.notifyItemChanged(position)
        }
        this.score?.text = "$score очков"
    }

    override fun loseGame() {
        AlertDialog.Builder(this)
            .setTitle("You are loser!")
            .setMessage("Play Again?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                onPlayAgain()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                onFinish()
            }
            .setCancelable(false)
            .show()
    }

    private class SquareHolder(imageView: ImageView): RecyclerView.ViewHolder(imageView)

    private class SquareAdapter: RecyclerView.Adapter<SquareHolder>(), GameFieldProvider {

        private val field: Array<Int> = Array(ROWS * COLUMNS) { 0 }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SquareHolder {
            val params = ViewGroup.LayoutParams(dp(parent.context, 10), dp(parent.context, 10))

            val view = ImageView(parent.context)
            view.layoutParams = params
            view.setPadding(view.left, dp(view.context, 1), view.right, dp(view.context, 1))
            return SquareHolder(view)
        }

        override fun onBindViewHolder(holder: SquareHolder, position: Int) {
            (holder.itemView as ImageView).setImageDrawable(resolveDrawable(holder.itemView.context, position))
        }

        override fun isAvailable(position: Int): Boolean = field[position] == 0

        override fun getItemCount() = COLUMNS * ROWS

        override fun onEatResolved(position: Int) {
            field[position] = 2
            notifyItemChanged(position)
        }

        override fun getSizeOfField(): Int = itemCount

        override fun getField(): Array<Int> {
            return field
        }

        private fun resolveDrawable(context: Context, position: Int): Drawable {
            val res = when (field[position]) {
                0 -> R.color.empty_square
                1 -> R.color.snake_square
                else -> R.color.eat_square
            }
            return ColorDrawable(ContextCompat.getColor(context, res))
        }

    }

    private class GameFieldItemDecoration: RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.top = 3
            outRect.bottom = 3
            outRect.left = 3
            outRect.right = 3
        }
    }

    companion object {
        const val COLUMNS = 15
        const val ROWS = 15

        fun dp(context: Context, dp: Int): Int{
            val r = context.resources
            val dpFloat = dp.toFloat()
            val px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpFloat,
                r.displayMetrics
            )
            return px.toInt()
        }
    }
}

interface GameFieldProvider {
    fun onEatResolved(position: Int)
    fun getSizeOfField(): Int
    fun isAvailable(position: Int): Boolean
    fun getField(): Array<Int>
}

interface GamesEventListener {
    fun onPlayAgain()
    fun onFinish()
    fun loseGame()
    fun onTurn(changedPositions: List<Int>, score: Int)
}