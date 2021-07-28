package com.github.llmaximll.magicpasswords

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.databinding.DialogRecoveryBackupBinding
import com.github.llmaximll.magicpasswords.repositories.MagicRepository
import com.github.llmaximll.magicpasswords.states.BottomBarAndFabState
import com.github.llmaximll.magicpasswords.utils.Common
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivityVM : ViewModel() {
    private val repository = MagicRepository.get()

    val bottomBarAndFabStateDataFlow =
        MutableStateFlow<BottomBarAndFabState>(BottomBarAndFabState.BottomBarOnFabOn)

    val bottomBarMenuDataFlow = MutableStateFlow(false)

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
            Common.toast(context, "Пароли были добавлены")
            dialog.dismiss()
        }

        binding.noButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(binding.root)
        dialog.show()
    }
}