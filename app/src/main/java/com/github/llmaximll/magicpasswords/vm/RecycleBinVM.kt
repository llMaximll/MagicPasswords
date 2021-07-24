package com.github.llmaximll.magicpasswords.vm

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkManager
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.repositories.MagicRepository
import com.github.llmaximll.magicpasswords.states.ListState
import com.github.llmaximll.magicpasswords.utils.CommonFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "RecycleBinVM"
private const val KEY_RECYCLER_VIEW_REMOVED = "key_recycler_view"

class RecycleBinVM(state: SavedStateHandle) : ViewModel() {
    private val cf = CommonFunctions.get()
    private val repository = MagicRepository.get()
    private val savedStateHandle = state
    private val passwordsListDataFlow = MutableStateFlow<List<PasswordInfo>?>(null)
    val passwordsListFlow = passwordsListDataFlow.asStateFlow()
    val setAllDataFlow = MutableStateFlow(false)

    /**
     * Переменная [selectedDataFlow] показывает состояние фрагмента
     * false - стандартное, true - какой-то элемент списка выделен
     */
    val selectedDataFlow = MutableStateFlow<ListState>(ListState.UNSELECTED)
    val selectedPasswordsMMap = mutableMapOf<Int, PasswordInfo>()

    var passwordsList = mutableListOf<PasswordInfo>()

    fun getAllPasswords(removed: Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            passwordsListDataFlow.value = repository.getAllPasswords(removed)
        }
    }

    fun recoverPasswords(mMap: MutableMap<Int, PasswordInfo>, context: Context) {
        val workManager = WorkManager.getInstance(context)
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.updateAllPasswords(mMap.values.toList())
            val tagList = mutableListOf<String>()
            for (el in mMap.values) {
                tagList += "{$el}"
            }
            cf.cancelAllWorkByTag(workManager, tagList)
            withContext(Dispatchers.Main) {
                cf.toast(context, "Восстановлено: $count")
            }
        }
    }

    fun deletePasswords(mMap: MutableMap<Int, PasswordInfo>, context: Context) {
        val workManager = WorkManager.getInstance(context)
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.deleteAllPasswords(mMap.values.toList())
            val tagList = mutableListOf<String>()
            for (el in mMap.values) {
                tagList += "{$el}"
            }
            cf.cancelAllWorkByTag(workManager, tagList)
            withContext(Dispatchers.Main) {
                cf.toast(context, "Удалено: $count")
            }
        }
    }

    fun saveRecyclerViewState(mState: LinearLayoutManager.SavedState?) {
        savedStateHandle.set(KEY_RECYCLER_VIEW_REMOVED, mState)
    }

    fun getRecyclerViewState(): LinearLayoutManager.SavedState? {
        return savedStateHandle.get(KEY_RECYCLER_VIEW_REMOVED)
    }
}