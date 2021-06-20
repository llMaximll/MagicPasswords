package com.github.llmaximll.magicpasswords.fragments

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.databinding.FragmentChangePasswordBinding
import com.github.llmaximll.magicpasswords.vm.ChangePasswordVM
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

private const val ARG_ID_PASSWORD = "arg_id_password"
private const val ARG_TRANSITION_NAME = "arg_transition_name"

class ChangePasswordFragment : Fragment() {

    interface Callbacks {
        fun onChangePasswordFragment()
    }

    private lateinit var binding: FragmentChangePasswordBinding
    private lateinit var viewModel: ChangePasswordVM
    private lateinit var cf: CommonFunctions
    private lateinit var idPassword: UUID
    private lateinit var transitionName: String
    private var callbacks: Callbacks? = null
    private var name = ""
    private var password = ""
    private var description = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cf = CommonFunctions.get()
        //arguments
        val id = arguments?.getString(ARG_ID_PASSWORD)
        transitionName = arguments?.getString(ARG_TRANSITION_NAME) ?: "transition_null"
            if (id != "null") {
            idPassword = UUID.fromString(id)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
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
        binding.okButton.setOnClickListener {
                name = binding.nameEditText.text.toString()
                password = binding.passwordEditText.text.toString()
                val password2 = binding.passwordEditText2.text.toString()
                description = binding.descriptionEditText.text.toString()
            if (viewModel.checkFields(requireContext(), name, password, password2, description)) {
                viewModel.addPassword(PasswordInfo(name = name, password = password, description = description))
                callbacks?.onChangePasswordFragment()
                cf.toast(requireContext(), "OK")
            }
        }
        binding.changeButton.setOnClickListener {
            name = binding.nameEditText.text.toString()
            password = binding.passwordEditText.text.toString()
            val password2 = binding.passwordEditText2.text.toString()
            description = binding.descriptionEditText.text.toString()
            if (viewModel.checkFields(requireContext(), name, password, password2, description)) {
                viewModel.updatePassword(PasswordInfo(id = idPassword, name = name, password = password, description = description))
                callbacks?.onChangePasswordFragment()
                cf.toast(requireContext(), "Обновить")
            }
        }
        binding.passwordToggleCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                binding.passwordEditText.inputType = 129
            } else {
                binding.passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
        binding.passwordToggleCheckBox2.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                binding.passwordEditText2.inputType = 129
            } else {
                binding.passwordEditText2.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
        binding.generateButton.setOnClickListener {
            val password = viewModel.generatePassword(10)
            binding.passwordEditText.setText(password)
            binding.passwordEditText2.setText(password)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = cf.initViewModel(this, ChangePasswordVM::class.java) as ChangePasswordVM
        if (this::idPassword.isInitialized) {
            restorePassword()
        }
        //transition
        binding.scrollView.transitionName = transitionName
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun restorePassword() {
        viewModel.getPasswordInfo(idPassword)
        lifecycleScope.launch {
            viewModel.passwordInfo.collect {
                binding.nameEditText.setText(it?.name)
                binding.passwordEditText.setText(it?.password)
                binding.passwordEditText2.setText(it?.password)
                binding.descriptionEditText.setText(it?.description)
            }
        }
        binding.okButton.visibility = View.GONE
        binding.changeButton.visibility = View.VISIBLE
    }

    companion object {
        fun newInstance(idPassword: String, transitionName: String): ChangePasswordFragment {
            val args = Bundle().apply {
                putString(ARG_ID_PASSWORD, idPassword)
                putString(ARG_TRANSITION_NAME, transitionName)
            }
            return ChangePasswordFragment().apply {
                arguments = args
            }
        }
    }
}