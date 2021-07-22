package com.github.llmaximll.magicpasswords.vm

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.databinding.DialogRecoveryBackupBinding
import com.github.llmaximll.magicpasswords.fragments.ChangePasswordFragment
import com.github.llmaximll.magicpasswords.repositories.MagicRepository
import com.github.llmaximll.magicpasswords.utils.CommonFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "ChangePasswordVM"

class MainActivityVM : ViewModel() {
    private val repository = MagicRepository.get()
    private val cf = CommonFunctions.get()

    fun addPassword(passwordInfo: PasswordInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addPassword(passwordInfo)
        }
    }

    fun updatePassword(passwordInfo: PasswordInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePassword(passwordInfo)
        }
    }

    suspend fun getAllPasswords(): List<PasswordInfo> {
        return repository.getAllPasswords(0)
    }

    @SuppressLint("InflateParams")
    fun showRecoveryPassword(context: Context, passwordsList: List<PasswordInfo>) {
        val dialog = Dialog(context)
        val view = LayoutInflater
            .from(context)
            .inflate(
                R.layout.dialog_recovery_backup,
                null,
                false
            )
        val binding = DialogRecoveryBackupBinding.bind(view)

        binding.countPasswordsTextView.text = "Найдено: ${passwordsList.size} паролей."

        binding.yesButton.setOnClickListener {
            viewModelScope.launch(Dispatchers.IO) {
                repository.addAllPasswords(passwordsList)
            }
            cf.toast(context, "Пароли были добавлены")
            dialog.dismiss()
        }

        binding.noButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(binding.root)
        dialog.show()
    }

    fun checkFields(
        context: Context,
        name: String,
        password: String,
        password2: String
    ): Boolean {
        when {
            name.isEmpty() -> {
                cf.toast(context,"Поле \"Название\" пустое")
                return false
            }
            password.isEmpty() -> {
                cf.toast(context,"Поле \"Пароль\" пустое")
                return false
            }
            password2.isEmpty() -> {
                cf.toast(context,"Поле \"Пароль 2\" пустое")
                return false
            }
        }
        if (password != password2) {
            cf.toast(context,"Пароли не совпадают")
            return false
        }
        return true
    }

    fun generatePassword(
        count: Int,
        passwordFormat: Int = ChangePasswordFragment.PASSWORD_FORMAT_WITHOUT_SPEC_SYMBOLS
    ): String {
        //Без спец. знаков
        val dict = when (passwordFormat) {
            ChangePasswordFragment.PASSWORD_FORMAT_WITHOUT_SPEC_SYMBOLS -> {
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            }
            ChangePasswordFragment.PASSWORD_FORMAT_WITH_SPEC_SYMBOLS -> {
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\\/^&%$#@_-"
            } else -> {
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            }
        }
        val rnd = Random()
        val password = StringBuilder()
        for (i in 0..count) {
            password.append(dict[rnd.nextInt(dict.length)])
        }
        return password.toString()
    }
}