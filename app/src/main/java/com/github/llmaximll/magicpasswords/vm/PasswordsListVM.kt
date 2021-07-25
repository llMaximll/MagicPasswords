package com.github.llmaximll.magicpasswords.vm

import android.content.Context
import android.text.Editable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.repositories.MagicRepository
import com.github.llmaximll.magicpasswords.states.ListState
import com.github.llmaximll.magicpasswords.states.SearchState
import com.github.llmaximll.magicpasswords.utils.CommonFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

private const val KEY_RECYCLER_VIEW = "key_recycler_view"

class PasswordsListVM(state: SavedStateHandle) : ViewModel() {
    private val repository = MagicRepository.get()
    private val cf = CommonFunctions.get()
    private val savedStateHandle = state
    private val passwordsListDataFlow = MutableStateFlow<List<PasswordInfo>?>(null)
    val passwordsListFlow = passwordsListDataFlow.asStateFlow()
    val setAllDataFlow = MutableStateFlow(false)
    /**
     * Переменная [selectedDataFlow] показывает состояние фрагмента:
     * false - стандартное, true - какой-то элемент списка выделен
     */
    val selectedDataFlow = MutableStateFlow<ListState>(ListState.UNSELECTED)
    val selectedPasswordsMMap = mutableMapOf<Int, PasswordInfo>()

    val passwordsList = mutableListOf<PasswordInfo>()

    val searchDataFlow = MutableStateFlow<SearchState>(SearchState.INACTIVE)

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

    fun deletePasswords(mMap: MutableMap<Int, PasswordInfo>, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val sp = cf.getSharedPreferences(context)
            val deleteFormat = sp.getInt(cf.spTimeDelete, CommonFunctions.TimeDeleteMonthSP)
            if (deleteFormat != CommonFunctions.TimeDeleteImmediatelySP) {
                for (i in mMap.values) {
                    i.apply {
                        removed = 1
                        removedDate = Calendar.getInstance().timeInMillis
                        removedFormat = deleteFormat
                    }
                }
                val count = repository.updateAllPasswords(mMap.values.toList())
                withContext(Dispatchers.Main) {
                    if (sp.getInt(cf.spTimeDelete, CommonFunctions.TimeDeleteMonthSP) !=
                        CommonFunctions.TimeDeleteImmediatelySP) {
                        cf.toast(context, "Добавлено в корзину: $count")
                    } else {
                        cf.toast(context, "Удалено: $count")
                    }
                }
            } else {
                repository.deleteAllPasswords(mMap.values.toList())
            }
        }
    }

    suspend fun getRelevantPasswords(query: String?) {
        if (query.isNullOrBlank()) {
            repository.getAllPasswords(0).let {
                passwordsListDataFlow.value = it
            }
        } else {
            val sanitizedQuery = sanitizeSearchQuery("*$query*")
            repository.getRelevantPasswords(sanitizedQuery).let {
                passwordsListDataFlow.value = it
            }
        }
    }

    private fun sanitizeSearchQuery(query: String?): String {
        if (query == null) {
            return ""
        }
        val queryWithEscapedQuotes = query.replace(Regex.fromLiteral("\""), "\"\"")
        return "*\"$queryWithEscapedQuotes\"*"
    }

    fun saveRecyclerViewState(mState: LinearLayoutManager.SavedState?) {
        savedStateHandle.set(KEY_RECYCLER_VIEW, mState)
    }

    fun getRecyclerViewState(): LinearLayoutManager.SavedState? {
        return savedStateHandle.get(KEY_RECYCLER_VIEW)
    }
}