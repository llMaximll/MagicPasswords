package com.github.llmaximll.magicpasswords.fragments.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.llmaximll.magicpasswords.databinding.BottomSheetChangeThemeBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ChangeThemeBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetChangeThemeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetChangeThemeBinding.inflate(layoutInflater)
        return binding.root
    }

    companion object {
        fun newInstance(): ChangeThemeBottomSheet = ChangeThemeBottomSheet()
    }
}