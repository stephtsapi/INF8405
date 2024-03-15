package com.example.inf8405.screens.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.inf8405.R
import kotlin.math.min

class GameGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val gridSize = 6
    private val paint: Paint = Paint()
    private lateinit var puzzle: Puzzle
    private var blockSize: Float = 0f

    private var selectedBlock: Block? = null
    private var startX: Float = 0f
    private var startY: Float = 0f


    fun setPuzzle(puzzle: Puzzle) {
        this.puzzle = puzzle
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null) return

        drawGrid(canvas)
        drawBlocks(canvas)
    }

    private fun drawGrid(canvas: Canvas) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        val gridPaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
        }

        // draw horizontal lines
        for (i in 0 until gridSize) {
            val y = i * blockSize
            canvas.drawLine(0f, y, width, y, gridPaint)
        }

        // draw vertical lines
        for (i in 0 until gridSize) {
            val x = i * blockSize
            canvas.drawLine(x, 0f, x, height, gridPaint)
        }
    }

    private fun drawBlocks(canvas: Canvas) {

        val blocks = puzzle.blocks
        for (block in blocks) {
            paint.color = if (block.isMarked) Color.RED else Color.BLACK
            paint.alpha = 200
            paint.isAntiAlias = true
            val blockRes = if(block.isMarked) R.drawable.marked_block else R.drawable.normal_block
            val blockDrawable = context.getDrawable(blockRes)

            val left = block.x * blockSize
            val top = block.y * blockSize
            val right = left + block.width * blockSize
            val bottom = top + block.height * blockSize

            blockDrawable!!.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
            blockDrawable.draw(canvas)
//            canvas.drawRect(left, top, right, bottom, paint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = min(measuredWidth, measuredHeight)
        blockSize = size / gridSize.toFloat()
        setMeasuredDimension(size, size)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> handleActionDown(event)
            MotionEvent.ACTION_MOVE -> handleActionMove(event)
            MotionEvent.ACTION_UP -> handleActionUp(event)
        }

        return true
    }

    private fun handleActionDown(event: MotionEvent) {
        startX = event.x
        startY = event.y

        val col = (event.x / blockSize).toInt()
        val row = (event.y / blockSize).toInt()

        selectedBlock = puzzle.grid.getBlockAt(row, col)

        if (selectedBlock != null) {
            Log.d("GameGridView", "Tapped on block ${selectedBlock!!.id}")
        }
    }

    private fun handleActionMove(event: MotionEvent) {
        if (selectedBlock == null) return

        val dx = event.x - startX
        val dy = event.y - startY
        val newX = dx / blockSize
        val newY = dy / blockSize

        if (puzzle.canMoveBlock(selectedBlock!!, newX, newY)) {
            puzzle.moveBlock(selectedBlock!!, newX, newY)
            invalidate()
            startX = event.x
            startY = event.y
        }
    }


    private fun handleActionUp(event: MotionEvent) {
        selectedBlock = null
    }
}
