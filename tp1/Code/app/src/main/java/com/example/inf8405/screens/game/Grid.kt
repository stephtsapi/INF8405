package com.example.inf8405.screens.game

import android.util.Log

class Grid {
    private val gridSize = 6
    private val grid = Array(gridSize) { Array(gridSize) { null as Block? } }
    private val moveHistory = mutableListOf<Move>()

    fun getSize(): Int {
        return gridSize
    }

    fun print() {
        Log.d("Grid", "------- Grid -------")
        for (i in 0 until grid.size) {
            val row = StringBuilder()
            for (j in 0 until grid[i].size) {
                val block = grid[i][j]
                val value = block?.id?.toString() ?: "-"
                row.append("$value ")
            }
            Log.d("Grid", row.toString())
        }
    }

    fun addBlock(block: Block) {
        Log.d("Grid", "Adding block with id ${block.id} to grid")
        for (i in block.y.toInt() until (block.y.toInt() + block.height)) {
            for (j in block.x.toInt() until (block.x.toInt() + block.width)) {
                grid[i][j] = block
            }
        }
    }

    fun removeBlock(block: Block) {
        Log.d("Grid", "Removing block with id ${block.id} from grid")
        for (i in block.y.toInt() until (block.y.toInt() + block.height)) {
            for (j in block.x.toInt() until (block.x.toInt() + block.width)) {
                grid[i][j] = null
            }
        }
    }

    fun getBlockAt(x: Int, y: Int): Block? {
        if (x < 0 || x >= grid.size || y < 0 || y >= grid[0].size) {
            return null
        }
        return grid[x][y]
    }

    fun undoMove() {
        if (!canUndo()) {
            Log.e("GameGridView", "Move history is empty, can't undo")
            return
        }

        val lastMove = moveHistory.removeAt(moveHistory.size - 1)
        lastMove.undo()

        removeBlock(lastMove.block)
        addBlock(lastMove.block)
        print()
    }

    fun canUndo(): Boolean {
        return moveHistory.isNotEmpty()
    }
}
