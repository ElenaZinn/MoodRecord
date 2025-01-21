package com.example.emorecord

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import com.example.emorecord.R
import com.example.emorecord.databinding.FragmentMoodBinding
import com.example.emorecord.viewmodel.MoodViewModel
import kotlinx.coroutines.launch

// MoodFragment.kt
class MoodFragment : Fragment() {
    private lateinit var binding: FragmentMoodBinding
    private val viewModel: MoodViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_mood,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        setupObservers()
        return binding.root
    }


    private fun updateProgress() {
        val happy = viewModel.happyCount.value ?: 0
        val sad = viewModel.sadCount.value ?: 0
        val total = happy + sad
        if (total > 0) {
            val progress = (happy.toFloat() / total * 100).toInt()
            binding.progressBar.progress = progress
        }
    }

    private fun animateEmoji(view: View) {
        view.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(200)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }

    private fun animateProgressBar(oldProgress: Int, newProgress: Int) {
        ValueAnimator.ofInt(oldProgress, newProgress).apply {
            duration = 300
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener { animator ->
                binding.progressBar.progress = animator.animatedValue as Int
            }
            start()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.progressPercent.collect { progress ->
                animateProgressBar(binding.progressBar.progress, progress)
            }
        }
    }

    fun onHappyClick(card: View, emojiView: View) {
        viewModel.onHappyClick()
        animateEmoji(emojiView)
    }

    fun onSadClick(card: View, emojiView: View) {
        viewModel.onSadClick()
        animateEmoji(emojiView)
    }
}