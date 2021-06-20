package com.github.llmaximll.magicpasswords.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.repositories.MagicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PasswordsListVM : ViewModel() {
    private val repository = MagicRepository.get()
    private val _passwordsList = MutableStateFlow<List<PasswordInfo>>(listOf())
    val passwordsList = _passwordsList.asStateFlow()

    fun getAllPasswords() {
        viewModelScope.launch(Dispatchers.IO) {
            _passwordsList.value = repository.getAllPasswords()
        }
    }

    fun deletePassword(password: PasswordInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePassword(password)
        }
    }
}