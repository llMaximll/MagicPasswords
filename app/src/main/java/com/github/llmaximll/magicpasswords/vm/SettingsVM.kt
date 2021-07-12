package com.github.llmaximll.magicpasswords.vm

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.fragments.dialogs.ChangeThemeBottomSheet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class SettingsVM : ViewModel() {
    private lateinit var sp: SharedPreferences
    private lateinit var oldPassword2: String
    private val cf = CommonFunctions.get()
    private val changeThemeDataFlow = MutableStateFlow<Int?>(null)
    val changeThemeFlow = changeThemeDataFlow.asStateFlow()

    fun nullChangeTheme() {
        changeThemeDataFlow.value = null
    }

    fun initSharedPreferences(context: Context) {
        sp = cf.getSharedPreferences(context)
        oldPassword2 = sp.getString(cf.spPassword, "null") ?: "null"
    }

    private fun saveNewPassword(newPassword: String) {
        val editor = sp.edit()
        editor.putString(cf.spPassword, newPassword)
        editor.apply()
    }

    fun createBottomSheetDialogResetPass(context: Context, rootView: ViewGroup) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(
            R.layout.bottom_sheet_reset_password,
            rootView,
            false
        )

        val oldPassword: EditText = view.findViewById(R.id.old_password)
        val newPassword: EditText = view.findViewById(R.id.new_password)
        val secondPassword: EditText = view.findViewById(R.id.second_password)
        val okButton: Button = view.findViewById(R.id.ok_button)

        okButton.setOnClickListener {
            if (checkFields(
                    oldPassword.text.toString(),
                    newPassword.text.toString(),
                    secondPassword.text.toString(),
                    context
                )
            ) {
                saveNewPassword(newPassword.text.toString())
                dialog.dismiss()
                cf.toast(context, "Пароль изменен")
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    fun createBottomSheetDialogChangeTheme(context: Context, rootView: ViewGroup, fragmentManager: FragmentManager) {
//        val dialog = BottomSheetDialog(context)
//        val view = LayoutInflater.from(context).inflate(
//            R.layout.bottom_sheet_change_theme,
//            rootView,
//            false
//        )
//
//        val systemThemeButton: Button = view.findViewById(R.id.system_theme_button)
//        val lightThemeButton: Button = view.findViewById(R.id.light_theme_button)
//        val darkThemeButton: Button = view.findViewById(R.id.dark_theme_button)
//
//        systemThemeButton.setOnClickListener {
//            val editor = sp.edit()
//            editor.putInt(cf.spThemeApp, CommonFunctions.SystemTheme)
//            editor.apply()
//            changeThemeDataFlow.value = CommonFunctions.SystemTheme //0
//            dialog.dismiss()
//        }
//        lightThemeButton.setOnClickListener {
//            val editor = sp.edit()
//            editor.putInt(cf.spThemeApp, CommonFunctions.LightTheme)
//            editor.apply()
//            changeThemeDataFlow.value = CommonFunctions.LightTheme //1
//            dialog.dismiss()
//        }
//        darkThemeButton.setOnClickListener {
//            val editor = sp.edit()
//            editor.putInt(cf.spThemeApp, CommonFunctions.DarkTheme)
//            editor.apply()
//            changeThemeDataFlow.value = CommonFunctions.DarkTheme //2
//            dialog.dismiss()
//        }
//
//        dialog.setContentView(view)
//        dialog.show()
        val bottomSheet = ChangeThemeBottomSheet.newInstance()
        bottomSheet.show(fragmentManager, "ChangeThemeBottomSheet")
    }

    private fun checkFields(
        oldPassword: String,
        newPassword: String,
        secondPassword: String,
        context: Context
    ): Boolean {
        when {
            oldPassword != oldPassword2 -> {
                cf.toast(context, "Неверный старый пароль")
                return false
            }
            newPassword.length < 4 -> {
                cf.toast(context, "Длина пароля не может быть меньше 4")
                return false
            }
            newPassword != secondPassword -> {
                cf.toast(context, "Пароли не совпадают")
                return false
            }
        }

        return true
    }

    fun checkFingerprintCompatibility(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        }
        return false
    }
}