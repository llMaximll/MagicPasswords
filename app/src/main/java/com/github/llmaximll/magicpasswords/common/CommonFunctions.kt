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
import androidx.work.WorkManager
import com.github.llmaximll.magicpasswords.BuildConfig
import com.google.android.material.snackbar.Snackbar

private const val SP_NAME = "sp_magic_passwords"

class CommonFunctions private constructor() {

    val spFirstLaunch = "sp_first_launch"
    val spPassword = "sp_password"
    val spFingerPrint = "sp_fingerprint"
    val spThemeApp = "sp_theme_app"

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

    fun snackBar(contextView: View, message: String, actionFunc: () -> Unit) {
        Snackbar.make(contextView, message, Snackbar.LENGTH_LONG).apply {
            setAction("Отмена") {
                actionFunc()
            }
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

    fun cancelAllWorkByTag(workManager: WorkManager, tagList: List<String>) {
        for(tag in tagList) {
            workManager.cancelAllWorkByTag(tag)
        }
    }

    fun changeFragment(
        supportFragmentManager: FragmentManager,
        @IdRes containerViewId: Int,
        fragment: Fragment,
        backStack: Boolean = false,
        backStackTag: String = "",
        animation: Boolean = false,
        transition: Boolean = false,
        sharedView: View? = null
    ) {
        supportFragmentManager
            .beginTransaction()
            .apply {
                if (backStack) {
                    addToBackStack(backStackTag)
                }
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
            .replace(containerViewId, fragment)
            .commit()
    }

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
        const val SystemTheme = 0
        const val LightTheme = 1
        const val DarkTheme = 2
    }
}