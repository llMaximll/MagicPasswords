package com.github.llmaximll.magicpasswords.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.databinding.FragmentSettingsBinding
import com.github.llmaximll.magicpasswords.vm.SettingsVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var cf: CommonFunctions
    private lateinit var sp: SharedPreferences
    private lateinit var viewModel: SettingsVM
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var fingerprint = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cf = CommonFunctions.get()
        sp = cf.getSharedPreferences(requireContext())
        fingerprint = sp.getBoolean(cf.spFingerPrint, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (fingerprint) {
            binding.fingerprintSwitch.isChecked = true
        }
        viewModel = cf.initViewModel(this, SettingsVM::class.java) as SettingsVM
        viewModel.initSharedPreferences(requireContext())
    }

    override fun onStart() {
        super.onStart()
        binding.fingerprintSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (viewModel.checkFingerprintCompatibility(requireContext())) {
                if (isChecked) {
                    showFingerprint()
                } else {
                    val editor = sp.edit()
                    editor.putBoolean(cf.spFingerPrint, false)
                    editor.apply()

                    cf.toast(requireContext(), "Отозван")
                }
            } else {
                binding.fingerprintSwitch.isChecked = false
                cf.toast(requireContext(), "Биометрическая аутентификация не поддерживается")
            }
        }
        binding.fingerprintTextView.setOnClickListener {
            binding.fingerprintSwitch.isChecked = !binding.fingerprintSwitch.isChecked
        }
        binding.resetButton.setOnClickListener {
            viewModel.createBottomSheetDialog(requireContext())
        }
    }

    private fun showFingerprint() {
        lifecycleScope.launch(Dispatchers.Main) {
            biometricPrompt = BiometricPrompt(this@SettingsFragment,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)

                        val editor = sp.edit()
                        editor.putBoolean(cf.spFingerPrint, true)
                        editor.apply()
                        binding.fingerprintSwitch.isChecked = true

                        cf.toast(requireContext(),  "Успешно")
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        binding.fingerprintSwitch.isChecked = false
                        cf.toast(requireContext(), "Попытка не удалась")
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        binding.fingerprintSwitch.isChecked = false
                    }
                })

            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Вход в MagicPasswords")
                .setNegativeButtonText("Отмена")
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}