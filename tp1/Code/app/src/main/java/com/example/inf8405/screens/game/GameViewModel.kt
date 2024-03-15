package com.example.inf8405.screens.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {

    private val _moveCounter = MutableLiveData<Int>()
    val moveCounter: LiveData<Int>
        get() = _moveCounter

    private val _currentPuzzleNumber = MutableLiveData<Int>()
    val currentPuzzleNumber: MutableLiveData<Int>
        get() = _currentPuzzleNumber

    private val _isSolved = MutableLiveData<Boolean>()
    val isSolved: LiveData<Boolean>
        get() = _isSolved

    private val _puzzle = MutableLiveData<Puzzle>()
    val puzzle: LiveData<Puzzle>
        get() = _puzzle

    private val _minMoveCount = MutableLiveData<Int>()
    val minMoveCount: LiveData<Int> = _minMoveCount

    private val _gameWon = MutableLiveData<Boolean>()
    val gameWon: LiveData<Boolean> = _gameWon


    init {
        _puzzle.value = Puzzle(Puzzle.PUZZLES[0].blocks.map{it.copy()})

        _currentPuzzleNumber.value = 1
        _moveCounter.value = 0
        _gameWon.value = false
    }


    fun undoMove() {
        _puzzle.value!!.undoMove()
        _moveCounter.value = _moveCounter.value?.minus(1)
    }

    fun canUndo(): Boolean {
        return _puzzle.value!!.canUndo()
    }

    fun resetMoveCounter() {
        _moveCounter.value = 0
    }

    fun resetPuzzle() {
        _puzzle.value = Puzzle(Puzzle.PUZZLES[_currentPuzzleNumber.value!!.minus(1)].blocks.map{it.copy()}) //Deep copy
        resetMoveCounter()
    }

    fun setPreviousPuzzle() {
        _currentPuzzleNumber.value = _currentPuzzleNumber.value?.minus(1)
        _puzzle.value = Puzzle(Puzzle.PUZZLES[_currentPuzzleNumber.value!!.minus(1)].blocks.map{it.copy()})
    }

    fun setNextPuzzle() {
        _currentPuzzleNumber.value = _currentPuzzleNumber.value?.plus(1)
        _puzzle.value = Puzzle(Puzzle.PUZZLES[_currentPuzzleNumber.value!!.minus(1)].blocks.map{it.copy()})
    }

    private fun checkIfSolved() {
        // TODO
        //If solved : appeler la fonction displaySuccessDialog de GameFragment.kt
    }
}

