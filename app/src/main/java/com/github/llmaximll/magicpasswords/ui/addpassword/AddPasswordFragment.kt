package com.github.llmaximll.magicpasswords.ui.addpassword

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.transition.Slide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.llmaximll.magicpasswords.utils.Encryption
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.databinding.FragmentAddPasswordBinding
import com.github.llmaximll.magicpasswords.utils.Common
import com.google.android.material.transition.platform.MaterialContainerTransform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddPasswordFragment : Fragment() {

    private lateinit var binding: FragmentAddPasswordBinding
    private lateinit var viewModel: AddPasswordVM
    private var passwordFormat = PASSWORD_FORMAT_WITHOUT_SPEC_SYMBOLS
    private var difficultPassword = 15

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.addButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val password2 = binding.passwordEditText2.text.toString()
            val description = binding.descriptionEditText.text.toString()
            val address = binding.addressEditText.text.toString()
            if (viewModel.checkFields(requireContext(), name, password, password2)) {
                viewModel.addPassword(
                    PasswordInfo(
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
                    val newPassword = Encryption.encrypt(binding.messageEditText2.text.toString(), requireContext())
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
        viewModel = Common.initViewModel(this, AddPasswordVM::class.java) as AddPasswordVM
        // Другое
        binding.withoutRadioButton.isChecked = true
        //transition
        enterTransition = MaterialContainerTransform().apply {
            startView = requireActivity().findViewById(R.id.add_password_fab)
            endView = binding.cardView
            duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
            scrimColor = Color.TRANSPARENT
            containerColor = Color.WHITE
            startContainerColor = Color.parseColor("#03DAC5")
            endContainerColor = Color.WHITE
        }
        returnTransition = Slide().apply {
            duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
            addTarget(R.id.cardView)
        }
    }

    companion object {
        const val PASSWORD_FORMAT_WITHOUT_SPEC_SYMBOLS = 0
        const val PASSWORD_FORMAT_WITH_SPEC_SYMBOLS = 1
    }
}
