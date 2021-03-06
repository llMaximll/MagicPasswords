package com.github.llmaximll.magicpasswords.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.MainActivity
import com.github.llmaximll.magicpasswords.databinding.FragmentSettingsBinding
import com.github.llmaximll.magicpasswords.utils.Common
import com.google.android.material.transition.platform.MaterialFadeThrough
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var sp: SharedPreferences
    private lateinit var viewModel: SettingsVM
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var fingerprint = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sp = Common.getSharedPreferences(requireContext())
        fingerprint = sp.getBoolean(Common.spFingerPrint, false)
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
        viewModel = Common.initViewModel(this, SettingsVM::class.java) as SettingsVM
        viewModel.initSharedPreferences(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.changeThemeFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED) //???????????????????? repeatOnLifecycle
                .collect {
                    when (it) {
                        0 -> {
                            activity?.setTheme(R.style.Theme_MagicPasswords)
                            activity?.recreate()
                            viewModel.nullChangeTheme()
                        }
                        1 -> {
                            activity?.setTheme(R.style.Theme_MagicPasswordsDay)
                            activity?.recreate()
                            viewModel.nullChangeTheme()
                        }
                        2 -> {
                            activity?.setTheme(R.style.Theme_MagicPasswordsNight)
                            activity?.recreate()
                            viewModel.nullChangeTheme()
                        }
                    }
                }
        }
        // Transition
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.material_motion_duration_medium_1).toLong()
        }
    }

    override fun onStart() {
        super.onStart()
        binding.fingerprintSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (viewModel.checkFingerprintCompatibility(requireContext())) {
                if (isChecked) {
                    showFingerprint()
                } else {
                    val editor = sp.edit()
                    editor.putBoolean(Common.spFingerPrint, false)
                    editor.apply()

                    Common.toast(requireContext(), "??????????????")
                }
            } else {
                binding.fingerprintSwitch.isChecked = false
                Common.toast(requireContext(), "???????????????????????????? ???????????????????????????? ???? ????????????????????????????")
            }
        }
        binding.fingerprintButton.setOnClickListener {
            binding.fingerprintSwitch.isChecked = !binding.fingerprintSwitch.isChecked
        }
        binding.resetButton.setOnClickListener {
            viewModel.createBottomSheetDialogResetPass(requireContext(), binding.root)
        }
        binding.changeThemeButton.setOnClickListener {
            viewModel.createBottomSheetDialogChangeTheme(requireContext(), binding.root)
        }
        binding.timeDeleteButton.setOnClickListener {
            viewModel.createBottomSheetDialogChangeTimeDelete(requireContext(), binding.root)
        }
        binding.clearDatabaseButton.setOnClickListener {
            viewModel.showClearDatabaseDialog(requireContext(), binding.root)
        }
        binding.backupButton.setOnClickListener {
            viewModel.showBackupDialog(requireContext(), activity as MainActivity, binding.root)
        }
    }

    private fun showFingerprint() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            biometricPrompt = BiometricPrompt(this@SettingsFragment,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)

                        val editor = sp.edit()
                        editor.putBoolean(Common.spFingerPrint, true)
                        editor.apply()
                        binding.fingerprintSwitch.isChecked = true

                        Common.toast(requireContext(),  "??????????????")
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        binding.fingerprintSwitch.isChecked = false
                        Common.toast(requireContext(), "?????????????? ???? ??????????????")
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        binding.fingerprintSwitch.isChecked = false
                    }
                })

            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("???????? ?? MagicPasswords")
                .setNegativeButtonText("????????????")
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }
}