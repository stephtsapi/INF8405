package com.example.inf8405.screens.game

import android.app.Dialog
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.inf8405.R
import com.example.inf8405.databinding.FragmentGameBinding
import com.example.inf8405.databinding.FragmentSuccessPopupBinding


class GameFragment : Fragment() {

    private val viewModel: GameViewModel by viewModels()

    private lateinit var binding: FragmentGameBinding
    private lateinit var popupBinding: FragmentSuccessPopupBinding
    private lateinit var gameGridView: GameGridView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate view and obtain an instance of the binding class
        binding = FragmentGameBinding.inflate(inflater, container, false)
        popupBinding = FragmentSuccessPopupBinding.inflate(inflater, container, false)
        binding.gameViewModel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameGridView = binding.gameGridView as GameGridView
        viewModel.puzzle.value?.let { gameGridView.setPuzzle(it) }
        binding.previousButton.setOnClickListener {
            setPreviousPuzzle()
        }
        binding.nextButton.setOnClickListener {
            setNextPuzzle()
        }
        binding.resetButton.setOnClickListener {
            resetPuzzleView()
        }
    }

    private fun setPreviousPuzzle() {
        viewModel.setPreviousPuzzle()
        gameGridView.setPuzzle(viewModel.puzzle.value!!)
        setNavigationButtonVisibility()
        viewModel.resetMoveCounter()
    }

    private fun setNextPuzzle() {
        viewModel.setNextPuzzle()
        gameGridView.setPuzzle(viewModel.puzzle.value!!)
        setNavigationButtonVisibility()
        viewModel.resetMoveCounter()
    }
    private fun setNavigationButtonVisibility() {
        when (viewModel.currentPuzzleNumber.value) {
                1 -> {
                    binding.previousContainer.visibility = LinearLayout.INVISIBLE
                    binding.nextContainer.visibility = LinearLayout.VISIBLE
                }
                3 -> {
                    binding.previousContainer.visibility = LinearLayout.VISIBLE
                    binding.nextContainer.visibility = LinearLayout.INVISIBLE
                }
                else -> {
                    binding.previousContainer.visibility = LinearLayout.VISIBLE
                    binding.nextContainer.visibility = LinearLayout.VISIBLE
                }
        }
    }

    private fun resetPuzzleView() {
        viewModel.resetPuzzle()
        gameGridView.setPuzzle(viewModel.puzzle.value!!)
        viewModel.resetMoveCounter()
    }

    /**
     * Fade-out animation: https://stackoverflow.com/questions/18940412/override-dialog-dismiss-with-new-alphaanimation
     * Sound: https://stackoverflow.com/questions/18459122/play-sound-on-button-click-android
     * */
    fun displaySuccessDialog() {
        val dialog = Dialog(this.requireContext())
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val view = layoutInflater.inflate(R.layout.fragment_success_popup, null)
        dialog.setContentView(view)
        dialog.show()
        val mp = MediaPlayer.create(this.requireContext(), R.raw.success);
        mp.start();
        Handler(Looper.getMainLooper()).postDelayed({
            val fadeOut = AlphaAnimation(1f, 0f)
            fadeOut.duration = 500;
            view.animation = fadeOut
            view.startAnimation(fadeOut);
            fadeOut.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    dialog.dismiss()
                    if(viewModel.currentPuzzleNumber.value != 3)
                        setNextPuzzle()
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })

        }, 3000)
    }
}
