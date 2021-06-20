package com.github.llmaximll.magicpasswords.common

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.github.llmaximll.magicpasswords.BuildConfig

private const val SP_NAME = "sp_magic_passwords"

class CommonFunctions private constructor() {

    val spFirstLaunch = "sp_first_launch"
    val spPassword = "sp_password"
    val spFingerPrint = "sp_fingerprint"

    fun toast(context: Context, message: String) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_LONG,
        ).apply {
            setGravity(Gravity.TOP, 0, 0)
            show()
        }
    }

    fun log(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }

    fun <T: ViewModel> initViewModel(owner: ViewModelStoreOwner, modelClass: Class<T>): ViewModel {
        return ViewModelProvider(owner).get(modelClass)
    }

    fun changeFragment(
        supportFragmentManager: FragmentManager,
        @IdRes containerViewId: Int,
        fragment: Fragment,
        backStack: Boolean = false,
        animation: Boolean = false,
        transition: Boolean = false,
        sharedView: View? = null
    ) {
        supportFragmentManager
            .beginTransaction()
            .replace(containerViewId, fragment)
            .apply {
                if (backStack) addToBackStack(null)
                if (animation) setCustomAnimations(
                    android.R.animator.fade_in,
                    android.R.animator.fade_out,
                    android.R.animator.fade_in,
                    android.R.animator.fade_out,
                )
                if (transition && sharedView != null) {
                    addSharedElement(sharedView, sharedView.transitionName)
                }
            }
            .commit()
    }

    fun animateView(view: View, reverse: Boolean, zChange: Boolean) {
        if (!reverse) {
            val animatorX = ObjectAnimator.ofFloat(view, "scaleX", 0.95f)
            val animatorY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f)
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

    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private var INSTANCE: CommonFunctions? = null

        fun init() {
            if (INSTANCE == null) {
                INSTANCE = CommonFunctions()
            }
        }

        fun get(): CommonFunctions {
            return requireNotNull(INSTANCE) {
                "CommonFunctions should be initialized"
            }
        }
    }
}