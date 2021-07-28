package com.github.llmaximll.magicpasswords.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import com.github.llmaximll.magicpasswords.MainActivityVM

object Animation {

    /**
     * Уменьшение и увеличение кнопки при нажатии
     */
    fun animateView(view: View, reverse: Boolean, zChange: Boolean, countValue: Float = 0.95f) {
        if (!reverse) {
            val animatorX = ObjectAnimator.ofFloat(view, "scaleX", countValue)
            val animatorY = ObjectAnimator.ofFloat(view, "scaleY", countValue)
            AnimatorSet().apply {
                playTogether(animatorX, animatorY)
                duration = 150
                start()
            }
            if (zChange) {
                view.animate().apply {
                    translationZ(10f)
                }.start()
            }
        } else {
            val animatorX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f)
            val animatorY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f)
            AnimatorSet().apply {
                playTogether(animatorX, animatorY)
                duration = 150
                start()
            }
            if (zChange) {
                view.animate().apply {
                    translationZ(0f)
                }.start()
            }
        }
    }

    fun animateMainMenu(
        viewModel: MainActivityVM,
        reverse: Boolean = false,
        clickableImageView: AppCompatImageView,
        bottomNavFragment: FragmentContainerView,
        bottomAppBarChevron: ImageView,
        bottomAppBarTitle: TextView
    ) {
        if (!reverse) {
            clickableImageView.animate().apply {
                alpha(1f)
                withStartAction {
                    clickableImageView.isVisible = true
                    clickableImageView.isClickable = true
                    clickableImageView.isFocusable = true
                }
            }
            bottomNavFragment.apply {
                animate().apply {
                    translationY(-150f)
                    alpha(1f)
                    withStartAction {
                        isVisible = true
                    }
                }
            }
            bottomAppBarChevron.animate().apply {
                rotation(180f)
                viewModel.bottomBarMenuDataFlow.value = true
            }
            bottomAppBarTitle.animate().apply {
                alpha(0f)
            }
        } else {
            clickableImageView.animate().apply {
                alpha(0f)
                withEndAction {
                    clickableImageView.isVisible = false
                    clickableImageView.isClickable = false
                    clickableImageView.isFocusable = false
                }
            }
            bottomNavFragment.apply {
                animate().apply {
                    translationY(100f)
                    alpha(0f)
                    withEndAction {
                        isVisible = false
                    }
                }
            }
            bottomAppBarChevron.animate().apply {
                rotation(0f)
                viewModel.bottomBarMenuDataFlow.value = false
            }
            bottomAppBarTitle.animate().apply {
                alpha(1f)
            }
        }
    }
}