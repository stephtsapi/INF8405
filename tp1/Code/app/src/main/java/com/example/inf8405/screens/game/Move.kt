package com.example.inf8405.screens.game

import android.util.Log


class Move(val block: Block, private val from: Position, private val to: Position) {

    fun execute() {
        block.x = to.x.toFloat()
        block.y = to.y.toFloat()
        Log.d("Move", "Moving block ${block.id} from (${from.x}, ${from.y}) to (${to.x}, ${to.y})")
    }

    fun undo() {
        block.x = from.x.toFloat()
        block.y = from.y.toFloat()
        Log.d("Move", "Undoing move for block ${block.id}")
    }
}


