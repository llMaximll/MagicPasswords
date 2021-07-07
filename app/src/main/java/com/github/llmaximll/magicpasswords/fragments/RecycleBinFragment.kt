package com.github.llmaximll.magicpasswords.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.llmaximll.magicpasswords.OnBackPressedListener
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.adaptersholders.RemovedPasswordsListAdapter
import com.github.llmaximll.magicpasswords.adaptersholders.SimpleItemTouchHelperCallbackRemoved
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
    private var passwordsList = mutableListOf<PasswordInfo>()

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
        setToolBar()
        getAllPasswords()
        setSelectedFragment()
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
        lifecycleScope.launch {
            viewModel.passwordsList.collect { passwordsList ->
                val passwordsMList = mutableListOf<PasswordInfo>()
                passwordsMList.addAll(passwordsList)
                this@RecycleBinFragment.passwordsList = passwordsMList
                setRecyclerView(passwordsList)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setToolBar(toolBar: androidx.appcompat.widget.Toolbar = binding.toolBar) {
        toolBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete_passwords -> {
                    if (viewModel.deletedPasswordsMMap.isNotEmpty()) {
                        viewModel.deletePasswords(viewModel.deletedPasswordsMMap)
                        passwordsList.removeAll(viewModel.deletedPasswordsMMap.values)
                        setRecyclerView(passwordsList)
                        viewModel.selected.value = false
                    } else {
                        cf.toast(requireContext(), "Не выбраны элементы списка")
                    }
                }
            }
            true
        }
    }

    private fun setRecyclerView(passwordsList: List<PasswordInfo>) {
        val mutPasswordsList = mutableListOf<PasswordInfo>()
        mutPasswordsList.addAll(passwordsList)
        val rV = binding.passwordsRecyclerView
        rV.layoutManager = LinearLayoutManager(requireContext())
        adapter = RemovedPasswordsListAdapter(mutPasswordsList, viewModel, requireContext())
        rV.adapter = adapter
        val callback = SimpleItemTouchHelperCallbackRemoved(adapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rV)
    }

    /**
     * В зависимости от переменной viewModel.selected функция выполняет то или иное состояние фрагмента (внешний вид)
     */
    private fun setSelectedFragment() {
        lifecycleScope.launch {
            viewModel.selected.collect {
                cf.toast(requireContext(), "selected=$it")
                binding.toolBar.menu.findItem(R.id.delete_passwords).isVisible = it
                if (!it) viewModel.deletedPasswordsMMap.clear()
            }
        }
    }

    companion object {
        fun newInstance(): RecycleBinFragment = RecycleBinFragment()
    }
}