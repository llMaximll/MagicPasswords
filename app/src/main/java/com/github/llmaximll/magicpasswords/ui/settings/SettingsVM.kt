package com.github.llmaximll.magicpasswords.ui.settings

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.MainActivity
import com.github.llmaximll.magicpasswords.databinding.BottomSheetChangeTimeDeleteBinding
import com.github.llmaximll.magicpasswords.databinding.DialogBackupBinding
import com.github.llmaximll.magicpasswords.databinding.DialogClearDatabaseBinding
import com.github.llmaximll.magicpasswords.databinding.DialogDangerImmediatelyTimeBinding
import com.github.llmaximll.magicpasswords.repositories.MagicRepository
import com.github.llmaximll.magicpasswords.utils.Common
import com.github.llmaximll.magicpasswords.utils.Storage
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsVM : ViewModel() {
    private lateinit var sp: SharedPreferences
    private lateinit var oldPassword2: String
    private lateinit var deleteFormatDialog: Dialog
    private val repository = MagicRepository.get()
    private val changeThemeDataFlow = MutableStateFlow<Int?>(null)
    val changeThemeFlow = changeThemeDataFlow.asStateFlow()

    fun nullChangeTheme() {
        changeThemeDataFlow.value = null
    }

    fun initSharedPreferences(context: Context) {
        sp = Common.getSharedPreferences(context)
        oldPassword2 = sp.getString(Common.spPassword, "null") ?: "null"
    }

    private fun saveNewPassword(newPassword: String) {
        val editor = sp.edit()
        editor.putString(Common.spPassword, newPassword)
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
                Common.toast(context, "Пароль изменен")
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    fun createBottomSheetDialogChangeTheme(context: Context, rootView: ViewGroup) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(
            R.layout.bottom_sheet_change_theme,
            rootView,
            false
        )

        val systemThemeButton: Button = view.findViewById(R.id.system_theme_button)
        val lightThemeButton: Button = view.findViewById(R.id.light_theme_button)
        val darkThemeButton: Button = view.findViewById(R.id.dark_theme_button)

        systemThemeButton.setOnClickListener {
            val editor = sp.edit()
            editor.putInt(Common.spThemeApp, Common.SystemThemeSP)
            editor.apply()
            changeThemeDataFlow.value = Common.SystemThemeSP //0
            dialog.dismiss()
        }
        lightThemeButton.setOnClickListener {
            val editor = sp.edit()
            editor.putInt(Common.spThemeApp, Common.LightThemeSP)
            editor.apply()
            changeThemeDataFlow.value = Common.LightThemeSP //1
            dialog.dismiss()
        }
        darkThemeButton.setOnClickListener {
            val editor = sp.edit()
            editor.putInt(Common.spThemeApp, Common.DarkThemeSP)
            editor.apply()
            changeThemeDataFlow.value = Common.DarkThemeSP //1
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    fun createBottomSheetDialogChangeTimeDelete(context: Context, rootView: ViewGroup) {
        deleteFormatDialog = BottomSheetDialog(context)
        val view = LayoutInflater
            .from(context).inflate(
                R.layout.bottom_sheet_change_time_delete,
                rootView,
                false
            )
        val binding = BottomSheetChangeTimeDeleteBinding.bind(view)

        binding.immediatelyButton.setOnClickListener {
            showDangerImmediatelyTimeDialog(context, rootView)
        }

        binding.dayButton.setOnClickListener {
            val editor = sp.edit()
            editor.putInt(Common.spTimeDelete, Common.TimeDeleteDaySP)
            editor.apply()
            deleteFormatDialog.dismiss()
        }

        binding.weakButton.setOnClickListener {
            val editor = sp.edit()
            editor.putInt(Common.spTimeDelete, Common.TimeDeleteWeakSP)
            editor.apply()
            deleteFormatDialog.dismiss()
        }

        binding.monthButton.setOnClickListener {
            val editor = sp.edit()
            editor.putInt(Common.spTimeDelete, Common.TimeDeleteMonthSP)
            editor.apply()
            deleteFormatDialog.dismiss()
        }

        deleteFormatDialog.setContentView(view)
        deleteFormatDialog.show()
    }

    private fun showDangerImmediatelyTimeDialog(context: Context, rootView: ViewGroup) {
        val dialog = Dialog(context)
        val view = LayoutInflater
            .from(context).inflate(
                R.layout.dialog_danger_immediately_time,
                rootView,
                false
            )
        val binding = DialogDangerImmediatelyTimeBinding.bind(view)

        binding.yesButton.setOnClickListener {
            val editor = sp.edit()
            editor.putInt(Common.spTimeDelete, Common.TimeDeleteImmediatelySP)
            editor.apply()
            dialog.dismiss()
            deleteFormatDialog.dismiss()
        }

        binding.noButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    fun showClearDatabaseDialog(context: Context, rootView: ViewGroup) {
        val dialog = Dialog(context)
        val view = LayoutInflater
            .from(context).inflate(
                R.layout.dialog_clear_database,
                rootView,
                false
            )
        val binding = DialogClearDatabaseBinding.bind(view)

        binding.yesButton.setOnClickListener {
            viewModelScope.launch(Dispatchers.IO) {
                repository.clearAllDatabase()
            }
            dialog.dismiss()
        }

        binding.noButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    fun showBackupDialog(context: Context, activity: MainActivity, rootView: ViewGroup) {
        val dialog = Dialog(context)
        val view = LayoutInflater
            .from(context).inflate(
                R.layout.dialog_backup,
                rootView,
                false
            )
        val binding = DialogBackupBinding.bind(view)

        binding.yesButton.setOnClickListener {
            val sp = Common.getSharedPreferences(context)
            val editor = sp.edit()
            editor.putBoolean(Common.spBackupEncryption, true)
            editor.apply()
            backupPasswords(context, activity)
            dialog.dismiss()
        }

        binding.noButton.setOnClickListener {
            val sp = Common.getSharedPreferences(context)
            val editor = sp.edit()
            editor.putBoolean(Common.spBackupEncryption, false)
            editor.apply()
            backupPasswords(context, activity)
            dialog.dismiss()
        }

        binding.recoveryTextView.setOnClickListener {
            dialog.dismiss()
            activity.openDocumentResultLauncher.launch(activity.openDocumentIntent)
        }

        dialog.setContentView(binding.root)
        dialog.show()
    }

    private fun backupPasswords(context: Context, activity: MainActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    if (Storage.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        activity.createDocumentResultLauncher.launch(activity.createDocumentIntent)
                    } else {
                        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        Storage.requestPermissions(
                            activity,
                            permissions,
                            WRITE_EXTERNAL_STORAGE_REQUEST_PERMISSION
                        )
                    }
                } else {
                    activity.createDocumentResultLauncher.launch(activity.createDocumentIntent)
                }
            }
        }
    }

    private fun checkFields(
        oldPassword: String,
        newPassword: String,
        secondPassword: String,
        context: Context
    ): Boolean {
        when {
            oldPassword != oldPassword2 -> {
                Common.toast(context, "Неверный старый пароль")
                return false
            }
            newPassword.length < 4 -> {
                Common.toast(context, "Длина пароля не может быть меньше 4")
                return false
            }
            newPassword != secondPassword -> {
                Common.toast(context, "Пароли не совпадают")
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

    companion object {
        private const val WRITE_EXTERNAL_STORAGE_REQUEST_PERMISSION = 1
    }
}