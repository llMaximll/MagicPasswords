package com.github.llmaximll.magicpasswords.ui.addpassword

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.ui.changepassword.ChangePasswordFragment
import com.github.llmaximll.magicpasswords.repositories.MagicRepository
import com.github.llmaximll.magicpasswords.utils.Common
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AddPasswordVM : ViewModel() {
    private val repository = MagicRepository.get()

    fun addPassword(passwordInfo: PasswordInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addPassword(passwordInfo)
        }
    }

    fun checkFields(
        context: Context,
        name: String,
        password: String,
        password2: String
    ): Boolean {
        when {
            name.isEmpty() -> {
                Common.toast(context,"Поле \"Название\" пустое")
                return false
            }
            password.isEmpty() -> {
                Common.toast(context,"Поле \"Пароль\" пустое")
                return false
            }
            password2.isEmpty() -> {
                Common.toast(context,"Поле \"Пароль 2\" пустое")
                return false
            }
        }
        if (password != password2) {
            Common.toast(context,"Пароли не совпадают")
            return false
        }
        return true
    }

    fun generatePassword(
        count: Int,
        passwordFormat: Int = ChangePasswordFragment.PASSWORD_FORMAT_WITHOUT_SPEC_SYMBOLS
    ): String {
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