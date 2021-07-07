package com.github.llmaximll.magicpasswords.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.repositories.MagicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecycleBinVM : ViewModel() {
    private val repository = MagicRepository.get()
    private val _passwordsList = MutableStateFlow<List<PasswordInfo>>(listOf())
    val passwordsList = _passwordsList.asStateFlow()
    /**
     * Переменная [selected] показывает состояние фрагмента
     * false - стандартное, true - какой-то элемент списка выделен
     */
    val selected = MutableStateFlow(false)
    val selectedPasswordsMMap = mutableMapOf<Int, PasswordInfo>()

    fun getAllPasswords(removed: Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            _passwordsList.value = repository.getAllPasswords(removed)
        }
    }

    private fun recoverPassword(password: PasswordInfo, context: Context) {
        val workManager = WorkManager.getInstance(context)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePassword(password)
            workManager.cancelAllWorkByTag("${password.id}")
        }
    }

    fun recoverPasswords(mMap: MutableMap<Int, PasswordInfo>, context: Context) {
        repeat(mMap.size) {
            recoverPassword(mMap.values.elementAt(it), context)
        }
    }

    fun deletePassword(password: PasswordInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePassword(password)
        }
    }

    fun deletePasswords(mMap: MutableMap<Int, PasswordInfo>) {
        repeat(mMap.size) {
            deletePassword(mMap.values.elementAt(it))
        }
    }
}