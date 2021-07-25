package com.github.llmaximll.magicpasswords.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.adaptersholders.RemovedPasswordsListAdapter
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.databinding.FragmentRecycleBinBinding
import com.github.llmaximll.magicpasswords.states.ListState
import com.github.llmaximll.magicpasswords.utils.CommonFunctions
import com.github.llmaximll.magicpasswords.vm.RecycleBinVM
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "RecycleBinFragment"

class RecycleBinFragment : Fragment() {

    private lateinit var binding: FragmentRecycleBinBinding
    private lateinit var viewModel: RecycleBinVM
    private lateinit var cf: CommonFunctions
    private lateinit var adapter: RemovedPasswordsListAdapter
    private var recyclerViewState: Parcelable? = null

    private val backPressedDispatcher = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            this@RecycleBinFragment.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cf = CommonFunctions.get()
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
        viewModel = cf.initViewModel(this, RecycleBinVM::class.java) as RecycleBinVM
        //Другое
        recyclerViewState = viewModel.getRecyclerViewState()

        setToolBar()
        if (viewModel.passwordsList.isEmpty()) {
            getAllPasswords()
        } else {
            setRecyclerView(viewModel.passwordsList)
        }
        isSelectedFragment()
        cf.log(TAG, "selectedPasswordsMMap=${viewModel.selectedPasswordsMMap.values.size}")
    }

    override fun onPause() {
        super.onPause()
        recyclerViewState = binding.passwordsRecyclerView.layoutManager?.onSaveInstanceState()
        viewModel.saveRecyclerViewState(recyclerViewState as LinearLayoutManager.SavedState?)
    }

    private fun onBackPressed() {
        backPressedDispatcher.isEnabled = false
        if (viewModel.selectedDataFlow.value is ListState.SELECTED) {
            viewModel.selectedDataFlow.value = ListState.UNSELECTED
        } else {
            requireActivity().onBackPressed()
        }
    }

    private fun getAllPasswords() {
        viewModel.getAllPasswords(1)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.passwordsListFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { passwordsList ->
                if (passwordsList != null) {
                    viewModel.passwordsList.clear()
                    viewModel.passwordsList.addAll(passwordsList.toMutableList())
                    setRecyclerView(passwordsList)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setToolBar(toolBar: androidx.appcompat.widget.Toolbar = binding.toolBar) {
        toolBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete_passwords -> {
                    if (viewModel.selectedPasswordsMMap.isNotEmpty()) {
                        viewModel.deletePasswords(viewModel.selectedPasswordsMMap, requireContext())
                        viewModel.passwordsList.removeAll(viewModel.selectedPasswordsMMap.values)
                        setRecyclerView(viewModel.passwordsList)
                        viewModel.selectedDataFlow.value = ListState.UNSELECTED
                    } else {
                        cf.toast(requireContext(), "Не выбраны элементы списка")
                    }
                }
                R.id.recover_passwords -> {
                    if (viewModel.selectedPasswordsMMap.isNotEmpty()) {
                        viewModel.passwordsList.removeAll(viewModel.selectedPasswordsMMap.values)
                        viewModel.selectedPasswordsMMap.values.forEach { pass ->
                            pass.removed = 0
                            pass.removedDate = 0L
                        }
                        viewModel.recoverPasswords(viewModel.selectedPasswordsMMap, requireContext())
                        setRecyclerView(viewModel.passwordsList)
                        viewModel.selectedDataFlow.value = ListState.UNSELECTED
                    } else {
                        cf.toast(requireContext(), "Не выбраны элементы списка")
                    }
                }
                R.id.select_all -> {
                    viewModel.setAllDataFlow.value = true
                }
            }
            true
        }
    }

    private fun setRecyclerView(passwordsList: List<PasswordInfo>) {
        val rV = binding.passwordsRecyclerView
        rV.layoutManager = LinearLayoutManager(requireContext())
        rV.layoutManager?.onRestoreInstanceState(recyclerViewState)
        adapter = RemovedPasswordsListAdapter(
            passwordsList.toMutableList(),
            viewModel,
            requireContext()
        )
        rV.adapter = adapter
    }
    /**
     * В зависимости от переменной selected функция выполняет то или иное состояние фрагмента (внешний вид)
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun isSelectedFragment() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedDataFlow
                        .collect { state ->
                            binding.toolBar.menu.run {
                                findItem(R.id.delete_passwords).isVisible = state is ListState.SELECTED
                                findItem(R.id.recover_passwords).isVisible = state is ListState.SELECTED
                                findItem(R.id.select_all).isVisible = state is ListState.SELECTED
                            }
                            if (state is ListState.UNSELECTED) {
                                viewModel.selectedPasswordsMMap.clear()
                                viewModel.setAllDataFlow.value = false
                            }
                            if (this@RecycleBinFragment::adapter.isInitialized) {
                                adapter.notifyDataSetChanged()
                            }
                        }
                }
                launch {
                    viewModel.setAllDataFlow
                        .collect { setAll ->
                            if (setAll) {
                                viewModel.passwordsList.forEachIndexed { index, passwordInfo ->
                                    viewModel.selectedPasswordsMMap[index] = passwordInfo
                                }
                            }
                            if (this@RecycleBinFragment::adapter.isInitialized) {
                                adapter.notifyDataSetChanged()
                            }
                        }
                }
            }
        }
    }
}