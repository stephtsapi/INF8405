package com.example.inf8405.screens.game

enum class Orientation {
    HORIZONTAL, VERTICAL
}

data class Block(
    val id: Int,
    var x: Float,
    var y: Float,
    val width: Int,
    val height: Int,
    val isMarked: Boolean,
    var orientation: Orientation
)

