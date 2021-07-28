package com.github.llmaximll.magicpasswords.utils

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
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.github.llmaximll.magicpasswords.BuildConfig
import com.github.llmaximll.magicpasswords.background.DeletePasswordWorker
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.TimeUnit

object Common {

    private const val SP_NAME = "sp_magic_passwords"

    const val spFirstLaunch = "sp_first_launch"
    const val spPassword = "sp_password"
    const val spFingerPrint = "sp_fingerprint"
    const val spThemeApp = "sp_theme_app"
    const val spTimeDelete = "sp_time_delete"
    const val spSecretKey = "sp_secret_key"
    const val spBackupEncryption = "sp_backup_encryption"

    const val SystemThemeSP = 0
    const val LightThemeSP = 1
    const val DarkThemeSP = 2
    const val TimeDeleteImmediatelySP = 0
    const val TimeDeleteDaySP = 1
    const val TimeDeleteWeakSP = 2
    const val TimeDeleteMonthSP = 3

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

    fun snackBar(contextView: View, message: String, action: Boolean, actionFunc: () -> Unit? = {  }) {
        Snackbar.make(contextView, message, Snackbar.LENGTH_LONG).apply {
            if (action) {
                setAction("Отмена") {
                    actionFunc()
                }
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

    fun deletePasswordWorkManager(
        context: Context,
        passwordsIdList: List<String>
    ) {
        val workManager = WorkManager.getInstance(context)
        val sp = getSharedPreferences(context)
        val duration = sp.getInt(spTimeDelete, TimeDeleteMonthSP)
        for (id in passwordsIdList) {
            val myData = Data.Builder().apply {
                putString("passwordId", id)
            }.build()
            val myWorkRequest = OneTimeWorkRequestBuilder<DeletePasswordWorker>().apply {
                addTag(id)
                setInputData(myData)
                when (duration) {
                    TimeDeleteDaySP -> {
                        setInitialDelay(24, TimeUnit.HOURS)
                    }
                    TimeDeleteWeakSP -> {
                        setInitialDelay(7, TimeUnit.DAYS)
                    }
                    TimeDeleteMonthSP -> {
                        setInitialDelay(30, TimeUnit.DAYS)
                    }
                }
            }.build()
            workManager.enqueue(myWorkRequest)
        }
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

    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }
}