package com.github.llmaximll.magicpasswords.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.llmaximll.magicpasswords.OnBackPressedListener
import com.github.llmaximll.magicpasswords.adaptersholders.RemovedPasswordsListAdapter
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.databinding.FragmentRecycleBinBinding
import com.github.llmaximll.magicpasswords.vm.RecycleBinVM
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RecycleBinFragment : Fragment(),
    OnBackPressedListener {

    private lateinit var binding: FragmentRecycleBinBinding
    private lateinit var viewModel: RecycleBinVM
    private lateinit var cf: CommonFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cf = CommonFunctions.get()
        viewModel = cf.initViewModel(this, RecycleBinVM::class.java) as RecycleBinVM
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecycleBinBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAllPasswords()
    }

    override fun onBackPressed(): Boolean = true

    private fun getAllPasswords() {
        viewModel.getAllPasswords(1)
        lifecycleScope.launch {
            viewModel.passwordsList.collect { passwordsList ->
                setRecyclerView(passwordsList)
            }
        }
    }

    private fun setRecyclerView(passwordsList: List<PasswordInfo>) {
        val mutPasswordsList = mutableListOf<PasswordInfo>()
        mutPasswordsList.addAll(passwordsList)
        if (passwordsList.isNotEmpty()) {
            val rV = binding.passwordsRecyclerView
            rV.layoutManager = LinearLayoutManager(requireContext())
            val adapter = RemovedPasswordsListAdapter(mutPasswordsList, viewModel, requireContext(), binding.coordinatorLayout)
            rV.adapter = adapter
        }
    }

    companion object {
        fun newInstance(): RecycleBinFragment = RecycleBinFragment()
    }
}