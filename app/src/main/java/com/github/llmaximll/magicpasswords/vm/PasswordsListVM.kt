package com.github.llmaximll.magicpasswords.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.repositories.MagicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val KEY_RECYCLER_VIEW = "key_recycler_view"

class PasswordsListVM(state: SavedStateHandle) : ViewModel() {
    private val repository = MagicRepository.get()
    private val passwordsListDataFlow = MutableStateFlow<List<PasswordInfo>?>(null)
    val passwordsListFlow = passwordsListDataFlow.asStateFlow()

    private val savedStateHandle = state

    fun getAllPasswords(removed: Int = 0) {
        viewModelScope.launch(Dispatchers.IO) {
            passwordsListDataFlow.value = repository.getAllPasswords(removed)
        }
    }

    fun updatePassword(password: PasswordInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePassword(password)
        }
    }

    fun deletePassword(password: PasswordInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePasswordById(password.id)
        }
    }

    fun saveRecyclerViewState(mState: LinearLayoutManager.SavedState?) {
        savedStateHandle.set(KEY_RECYCLER_VIEW, mState)
    }

    fun getRecyclerViewState(): LinearLayoutManager.SavedState? {
        return savedStateHandle.get(KEY_RECYCLER_VIEW)
    }
}