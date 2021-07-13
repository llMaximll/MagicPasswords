package com.github.llmaximll.magicpasswords.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.llmaximll.magicpasswords.OnBackPressedListener
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.adaptersholders.RemovedPasswordsListAdapter
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.databinding.FragmentRecycleBinBinding
import com.github.llmaximll.magicpasswords.vm.RecycleBinVM
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val TAG = "RecycleBinFragment"

class RecycleBinFragment : Fragment(),
    OnBackPressedListener {

    private lateinit var binding: FragmentRecycleBinBinding
    private lateinit var viewModel: RecycleBinVM
    private lateinit var cf: CommonFunctions
    private lateinit var adapter: RemovedPasswordsListAdapter
    private var recyclerViewState: Parcelable? = null
    private var passwordsList = mutableListOf<PasswordInfo>()

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
        getAllPasswords()
        setSelectedFragment()
    }

    override fun onPause() {
        super.onPause()
        recyclerViewState = binding.passwordsRecyclerView.layoutManager?.onSaveInstanceState()
        viewModel.saveRecyclerViewState(recyclerViewState as LinearLayoutManager.SavedState?)
    }

    override fun onBackPressed(): Boolean {
        return if (viewModel.selected.value) {
            viewModel.selected.value = false
            false
        } else {
            true
        }
    }

    private fun getAllPasswords() {
        viewModel.getAllPasswords(1)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.passwordsListFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { passwordsList ->
                if (passwordsList != null) {
                    val passwordsMList = mutableListOf<PasswordInfo>()
                    passwordsMList.addAll(passwordsList)
                    this@RecycleBinFragment.passwordsList = passwordsMList
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
                        passwordsList.removeAll(viewModel.selectedPasswordsMMap.values)
                        setRecyclerView(passwordsList)
                        viewModel.selected.value = false
                    } else {
                        cf.toast(requireContext(), "Не выбраны элементы списка")
                    }
                }
                R.id.recover_passwords -> {
                    if (viewModel.selectedPasswordsMMap.isNotEmpty()) {
                        passwordsList.removeAll(viewModel.selectedPasswordsMMap.values)
                        for (pass in viewModel.selectedPasswordsMMap.values) {
                            pass.removed = 0
                            pass.removedDate = 0L
                        }
                        viewModel.recoverPasswords(viewModel.selectedPasswordsMMap, requireContext())
                        setRecyclerView(passwordsList)
                    } else {
                        cf.toast(requireContext(), "Не выбраны элементы списка")
                    }
                }
                R.id.select_all -> {
                    setRecyclerView(passwordsList, true)
                }
            }
            true
        }
    }

    private fun setRecyclerView(passwordsList: List<PasswordInfo>, setAll: Boolean = false) {
        val mutPasswordsList = mutableListOf<PasswordInfo>()
        mutPasswordsList.addAll(passwordsList)
        val rV = binding.passwordsRecyclerView
        rV.layoutManager = LinearLayoutManager(requireContext())
        rV.layoutManager?.onRestoreInstanceState(recyclerViewState)
        adapter = RemovedPasswordsListAdapter(mutPasswordsList, viewModel, requireContext(), setAll)
        rV.adapter = adapter
    }
    /**
     * В зависимости от переменной viewModel.selected функция выполняет то или иное состояние фрагмента (внешний вид)
     */
    private fun setSelectedFragment() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selected
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    binding.toolBar.menu.findItem(R.id.delete_passwords).isVisible = it
                    binding.toolBar.menu.findItem(R.id.recover_passwords).isVisible = it
                    binding.toolBar.menu.findItem(R.id.select_all).isVisible = it
                    if (!it) viewModel.selectedPasswordsMMap.clear()
            }
        }
    }

    companion object {
        fun newInstance(): RecycleBinFragment = RecycleBinFragment()
    }
}