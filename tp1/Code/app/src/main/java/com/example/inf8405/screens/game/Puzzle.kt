package com.example.inf8405.screens.game

import android.util.Log

class Puzzle(val blocks: List<Block>) {
    val grid = Grid()

    init {
        blocks.forEach { grid.addBlock(it) }
        grid.print()
    }

    fun moveBlock(block: Block, x: Float, y: Float) {
        val xMovement = if (block.width > block.height) x else 0f
        val yMovement = if (block.width < block.height) y else 0f

        val newX = block.x + xMovement
        val newY = block.y + yMovement

        grid.removeBlock(block)
        block.x = newX
        block.y = newY
        grid.addBlock(block)

        grid.print()
    }


    fun canMoveBlock(block: Block, x: Float, y: Float): Boolean {
        val newX = (block.x + x).toInt()
        val newY = (block.y + y).toInt()

        if (newX < 0 || newX + block.width > grid.getSize() || newY < 0 || newY + block.height > grid.getSize()) {
            Log.e("Grid", "Cannot move block ${block.id} out of grid")
            return false
        }

        for (i in newY until newY + block.height) {
            for (j in newX until newX + block.width) {
                if (grid.getBlockAt(i, j) != null && grid.getBlockAt(i, j) != block) {
                    Log.e("Grid", "Cannot move block ${block.id} to ($i, $j) because it is occupied by block ${grid.getBlockAt(i, j)?.id}")
                    return false
                }
            }
        }

        Log.d("Grid", "Block ${block.id} can be moved to ($newX, $newY)")
        return true

    }

    fun undoMove() {
        grid.undoMove()
    }

    fun isSolved(): Boolean {
        // check if the marked block has reached the right edge of the grid
        return false
    }

    fun canUndo(): Boolean {
        return grid.canUndo()
    }

    companion object {

        private val PUZZLE_1 = Puzzle(
            listOf(
                Block(0, 0f, 2f, 2, 1, true, Orientation.HORIZONTAL),

                Block(1, 0f, 0f, 3, 1, false, Orientation.HORIZONTAL),
                Block(2, 0f, 5f, 3, 1, false, Orientation.HORIZONTAL),
                Block(3, 4f, 3f, 2, 1, false, Orientation.HORIZONTAL),

                Block(4, 2f, 1f, 1, 3, false, Orientation.VERTICAL),
                Block(5, 0f, 3f, 1, 2, false, Orientation.VERTICAL),
                Block(6, 5f, 0f, 1, 3, false, Orientation.VERTICAL),
                Block(7, 4f, 4f, 1, 2, false, Orientation.VERTICAL),
            )
        )
        private val PUZZLE_2 = Puzzle(
            listOf(
                Block(0, 0f, 2f, 2, 1, true, Orientation.HORIZONTAL),

                Block(1, 0f, 3f, 2, 1, false, Orientation.HORIZONTAL),
                Block(2, 2f, 5f, 2, 1, false, Orientation.HORIZONTAL),

                Block(3, 2f, 1f, 1, 2, false, Orientation.VERTICAL),
                Block(4, 2f, 3f, 1, 2, false, Orientation.VERTICAL),
                Block(5, 3f, 1f, 1, 3, false, Orientation.VERTICAL),
                Block(6, 4f, 1f, 1, 3, false, Orientation.VERTICAL),
                Block(7, 1f, 4f, 1, 2, false, Orientation.VERTICAL),
            )
        )
        private val PUZZLE_3 = Puzzle(
            listOf(
                Block(0, 0f, 2f, 2, 1, true, Orientation.HORIZONTAL),

                Block(1, 1f, 0f, 2, 1, false, Orientation.HORIZONTAL),
                Block(2, 3f, 0f, 2, 1, false, Orientation.HORIZONTAL),
                Block(3, 0f, 4f, 3, 1, false, Orientation.HORIZONTAL),

                Block(4, 0f, 0f, 1, 2, false, Orientation.VERTICAL),
                Block(5, 2f, 1f, 1, 2, false, Orientation.VERTICAL),
                Block(6, 3f, 2f, 1, 3, false, Orientation.VERTICAL),
                Block(7, 4f, 2f, 1, 3, false, Orientation.VERTICAL),
            )
        )
        val PUZZLES = listOf(PUZZLE_1, PUZZLE_2, PUZZLE_3)
    }

}



