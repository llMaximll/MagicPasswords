package com.github.llmaximll.magicpasswords.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.repositories.MagicRepository
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

    fun generatePassword(count: Int): String {
        //Без спец. знаков
        val dict = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val rnd = Random()
        var password = StringBuilder()
        for (i in 0..count) {
            password.append(dict[rnd.nextInt(dict.length)])
        }
        return password.toString()
    }
}