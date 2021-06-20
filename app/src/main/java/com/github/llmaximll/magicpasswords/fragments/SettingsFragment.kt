package com.github.llmaximll.magicpasswords.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.databinding.FragmentSettingsBinding
import com.github.llmaximll.magicpasswords.vm.SettingsVM

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var cf: CommonFunctions
    private lateinit var sp: SharedPreferences
    private lateinit var viewModel: SettingsVM
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
            val editor = sp.edit()
            editor.putBoolean(cf.spFingerPrint, isChecked)
            editor.apply()
            cf.toast(requireContext(), if (isChecked) "Выбран" else "Отозван")
        }
        binding.fingerprintTextView.setOnClickListener {
            binding.fingerprintSwitch.isChecked = !binding.fingerprintSwitch.isChecked
        }
        binding.resetButton.setOnClickListener {
            viewModel.createBottomSheetDialog(requireContext())
        }
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}