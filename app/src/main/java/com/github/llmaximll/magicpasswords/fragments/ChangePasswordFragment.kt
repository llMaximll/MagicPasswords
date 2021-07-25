package com.github.llmaximll.magicpasswords.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.llmaximll.magicpasswords.Encryption
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.databinding.FragmentChangePasswordBinding
import com.github.llmaximll.magicpasswords.utils.CommonFunctions
import com.github.llmaximll.magicpasswords.vm.ChangePasswordVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

private const val TAG = "ChangePasswordFragment"

class ChangePasswordFragment : Fragment() {

    private lateinit var binding: FragmentChangePasswordBinding
    private lateinit var viewModel: ChangePasswordVM
    private lateinit var cf: CommonFunctions
    private lateinit var idPassword: UUID
    private var name = ""
    private var password = ""
    private var description = ""
    private var passwordFormat = PASSWORD_FORMAT_WITHOUT_SPEC_SYMBOLS
    private var difficultPassword = 15

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cf = CommonFunctions.get()
        //arguments
        idPassword = try {
            UUID.fromString(arguments?.getString(ARG_ID_PASSWORD))
        } catch (e: Exception) {
            cf.toast(requireContext(), "Ошибка загрузки пароля")
            requireActivity().onBackPressed()
            UUID.randomUUID()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.changeButton.setOnClickListener {
            name = binding.nameEditText.text.toString()
            password = binding.passwordEditText.text.toString()
            val password2 = binding.passwordEditText2.text.toString()
            description = binding.descriptionEditText.text.toString()
            val address = binding.addressEditText.text.toString()
            if (viewModel.checkFields(requireContext(), name, password, password2)) {
                viewModel.updatePassword(
                    PasswordInfo(
                        id = idPassword,
                        name = name,
                        password = password,
                        description = description,
                        address = address,
                        messagePassword = binding.messageRadioButton.isChecked
                    ))
                requireActivity().onBackPressed()
            }
        }
        binding.passwordToggleCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                binding.passwordEditText.inputType = 129
                binding.passwordEditText2.inputType = 129
            } else {
                binding.passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.passwordEditText2.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
        binding.generateButton.setOnClickListener {
            if (!binding.messageRadioButton.isChecked) {
                val password = viewModel.generatePassword(difficultPassword, passwordFormat)
                binding.passwordEditText.setText(password)
                binding.passwordEditText2.setText(password)
            } else {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val enc = Encryption()
                    val newPassword = enc.encrypt(binding.messageEditText2.text.toString(), requireContext())
                    withContext(Dispatchers.Main) {
                        binding.passwordEditText.setText("$newPassword")
                        binding.passwordEditText2.setText("$newPassword")
                        difficultPassword = newPassword?.length ?: 0
                        binding.countSymbolsTextView.hint = "Количество знаков: ${newPassword?.length}"
                        binding.difficultSeekBar.progress = difficultPassword
                    }
                }
            }
        }
        binding.difficultSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress != 0) {
                    difficultPassword = progress
                    val newPassword = viewModel.generatePassword(difficultPassword, passwordFormat)
                    binding.countSymbolsTextView.hint = "Количество знаков: $progress"
                    binding.passwordEditText.setText(newPassword)
                    binding.passwordEditText2.setText(newPassword)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.without_radioButton -> {
                    passwordFormat = PASSWORD_FORMAT_WITHOUT_SPEC_SYMBOLS
                    binding.messageInputLayout.isEnabled = false
                    binding.difficultSeekBar.isEnabled = true
                    binding.countSymbolsTextView.hint = "Количество знаков: $difficultPassword"
                    binding.passwordEditText.isEnabled = true
                    binding.passwordEditText2.isEnabled = true
                }
                R.id.with_radioButton2 -> {
                    passwordFormat = PASSWORD_FORMAT_WITH_SPEC_SYMBOLS
                    binding.messageInputLayout.isEnabled = false
                    binding.difficultSeekBar.isEnabled = true
                    binding.countSymbolsTextView.hint = "Количество знаков: $difficultPassword"
                    binding.passwordEditText.isEnabled = true
                    binding.passwordEditText2.isEnabled = true
                }
                R.id.message_radioButton -> {
                    binding.messageInputLayout.isEnabled = true
                    binding.difficultSeekBar.isEnabled = false
                    binding.passwordEditText.isEnabled = false
                    binding.passwordEditText2.isEnabled = false
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = cf.initViewModel(this, ChangePasswordVM::class.java) as ChangePasswordVM
        if (this::idPassword.isInitialized) {
            restorePassword()
        }
        //Другое
        binding.withoutRadioButton.isChecked = true
    }

    private fun restorePassword() {
        viewModel.getPasswordInfo(idPassword)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.passwordInfoFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    binding.nameEditText.setText(it?.name)
                    binding.passwordEditText.setText(it?.password)
                    binding.passwordEditText2.setText(it?.password)
                    binding.descriptionEditText.setText(it?.description)
                    binding.addressEditText.setText(it?.address)
                    difficultPassword = it?.password?.length ?: 0
                    binding.countSymbolsTextView.hint = "Количество знаков: $difficultPassword"
                    if (it?.messagePassword == true) {
                        binding.messageRadioButton.isChecked = true
                        val en = Encryption()
                        binding.messageEditText2.setText(en.decrypt(it.password, requireContext()))
                    }
                }
        }
        binding.changeButton.visibility = View.VISIBLE
    }

    companion object {
        const val PASSWORD_FORMAT_WITHOUT_SPEC_SYMBOLS = 0
        const val PASSWORD_FORMAT_WITH_SPEC_SYMBOLS = 1
        // Аргументы
        const val ARG_ID_PASSWORD = "arg_id_password"
    }
}
