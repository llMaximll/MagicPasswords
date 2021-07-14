package com.github.llmaximll.magicpasswords.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.fragments.ChangePasswordFragment
import com.github.llmaximll.magicpasswords.repositories.MagicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "ChangePasswordVM"

class ChangePasswordVM : ViewModel() {
    private val repository = MagicRepository.get()
    private val cf = CommonFunctions.get()
    private val passwordInfoDataFlow = MutableStateFlow<PasswordInfo?>(null)
    val passwordInfoFlow = passwordInfoDataFlow.asStateFlow()

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

    fun getPasswordInfo(idPassword: UUID) {
        viewModelScope.launch(Dispatchers.IO) {
            passwordInfoDataFlow.value = repository.getPasswordInfo(idPassword)
        }
    }

    fun checkFields(
        context: Context,
        name: String,
        password: String,
        password2:
        String,
        description: String
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
            description.isEmpty() -> {
                cf.toast(context,"Поле \"Описание\" пустое")
                return false
            }
        }
        if (password != password2) {
            cf.toast(context,"Пароли не совпадают")
            return false
        }
        return true
    }

    fun generatePassword(count: Int, passwordFormat: Int = ChangePasswordFragment.PASSWORD_FORMAT_WITHOUT_SPEC_SYMBOLS): String {
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