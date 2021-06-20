package com.github.llmaximll.magicpasswords.vm

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModel
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.google.android.material.bottomsheet.BottomSheetDialog

class SettingsVM : ViewModel() {

    private lateinit var sp: SharedPreferences
    private val cf = CommonFunctions.get()
    private lateinit var oldPassword2: String

    fun initSharedPreferences(context: Context) {
        sp = cf.getSharedPreferences(context)
        oldPassword2 = sp.getString(cf.spPassword, "null") ?: "null"
    }

    private fun saveNewPassword(newPassword: String) {
        val editor = sp.edit()
        editor.putString(cf.spPassword, newPassword)
        editor.apply()
    }

    fun createBottomSheetDialog(context: Context) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(
            R.layout.fragment_reset_password,
            null,
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
}