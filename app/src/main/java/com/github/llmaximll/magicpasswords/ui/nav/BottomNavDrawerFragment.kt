package com.github.llmaximll.magicpasswords.ui.nav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.llmaximll.magicpasswords.databinding.BottomNavDrawerFragmentBinding

class BottomNavDrawerFragment : Fragment() {

    interface Callbacks {
        fun onMenuItemClicked(button: Int)
    }

    var binding: BottomNavDrawerFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomNavDrawerFragmentBinding.inflate(inflater)
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        binding?.listPasswordsButton?.setOnClickListener {
            (context as Callbacks).onMenuItemClicked(PASSWORDS_ITEM)
        }
        binding?.settingsButton?.setOnClickListener {
            (context as Callbacks).onMenuItemClicked(SETTINGS_ITEM)
        }
        binding?.recycleBinButton?.setOnClickListener {
            (context as Callbacks).onMenuItemClicked(RECYCLE_BIN_ITEM)
        }
    }

    companion object {
        const val PASSWORDS_ITEM = 1
        const val SETTINGS_ITEM = 2
        const val RECYCLE_BIN_ITEM = 3
    }
}

