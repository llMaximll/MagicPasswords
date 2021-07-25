package com.github.llmaximll.magicpasswords.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.activities.MainActivity
import com.github.llmaximll.magicpasswords.adaptersholders.PasswordsListAdapter
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.databinding.FragmentPasswordsListBinding
import com.github.llmaximll.magicpasswords.states.ListState
import com.github.llmaximll.magicpasswords.utils.CommonFunctions
import com.github.llmaximll.magicpasswords.vm.PasswordsListVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val TAG = "PasswordsListFragment"

class PasswordsListFragment : Fragment() {

    interface Callbacks {
        fun onPasswordsListFragmentChangePassword(idPassword: String)
        fun onPasswordsListFragmentAddPassword()
    }

    private lateinit var binding: FragmentPasswordsListBinding
    private lateinit var cf: CommonFunctions
    private lateinit var adapter: PasswordsListAdapter
    private var recyclerViewState: Parcelable? = null
    private var callbacks: Callbacks? = null

    lateinit var viewModel: PasswordsListVM

    private val backPressedDispatcher = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            this@PasswordsListFragment.onBackPressed()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cf = CommonFunctions.get()
        cf.log(TAG, "onCreate()")
        //fragment transition
        exitTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.fade)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPasswordsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = cf.initViewModel(this, PasswordsListVM::class.java) as PasswordsListVM
        recyclerViewState = viewModel.getRecyclerViewState()
        if (viewModel.passwordsList.isEmpty()) {
            viewModel.getAllPasswords(0)
        } else {
            setRecyclerView(viewModel.passwordsList)
        }
        setToolBar()
        isSelectedFragment()
        // Back button
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedDispatcher)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onStart() {
        super.onStart()
        binding.addPasswordFab.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    cf.animateView(v, reverse = false, zChange = false, .8f)
                }
                MotionEvent.ACTION_UP -> {
                    cf.animateView(v, reverse = true, zChange = false, .8f)
                    callbacks?.onPasswordsListFragmentAddPassword()
                    v.performClick()
                }
            }
            true
        }
    }

    private fun onBackPressed() {
        backPressedDispatcher.isEnabled = false
        if (viewModel.selectedDataFlow.value is ListState.SELECTED) {
            viewModel.selectedDataFlow.value = ListState.UNSELECTED
        } else {
            requireActivity().onBackPressed()
        }
    }

    override fun onPause() {
        recyclerViewState = binding.passwordsRecyclerView.layoutManager?.onSaveInstanceState()
        viewModel.saveRecyclerViewState(recyclerViewState as LinearLayoutManager.SavedState?)
        super.onPause()
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun setToolBar(toolBar: Toolbar = binding.toolBar) {
        toolBar.setNavigationIcon(R.drawable.ic_menu)
        toolBar.setNavigationOnClickListener {
            (requireActivity() as MainActivity).binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        toolBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete_passwords -> {
                    if (viewModel.selectedPasswordsMMap.isNotEmpty()) {
                        viewModel.deletePasswords(viewModel.selectedPasswordsMMap, requireContext())
                        val idList = mutableListOf<String>()
                        viewModel.selectedPasswordsMMap.values.forEach {
                            idList.add("${it.id}")
                        }
                        cf.deletePasswordWorkManager(
                            requireContext(),
                            idList
                        )
                        viewModel.passwordsList.removeAll(viewModel.selectedPasswordsMMap.values)
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
        val searchItem = toolBar.menu?.findItem(R.id.search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.getRelevantPasswords(newText)
                    }
                }
                return true
            }
        })
    }

    private fun setRecyclerView(passwordsList: List<PasswordInfo>) {
        val rV = binding.passwordsRecyclerView
        rV.layoutManager = LinearLayoutManager(requireContext())
        //Восстановление состояния RecyclerView
        binding.passwordsRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)

        adapter = PasswordsListAdapter(
            callbacks,
            passwordsList.toMutableList(),
            viewModel
        )
        rV.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun isSelectedFragment() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedDataFlow
                        .collect { state ->
                            binding.toolBar.menu.run {
                                findItem(R.id.delete_passwords).isVisible = state is ListState.SELECTED
                                findItem(R.id.select_all).isVisible = state is ListState.SELECTED
                            }
                            if (state is ListState.SELECTED) {
                                backPressedDispatcher.isEnabled = true
                            }
                            if (state is ListState.UNSELECTED) {
                                viewModel.selectedPasswordsMMap.clear()
                                viewModel.setAllDataFlow.value = false
                            }
                            if (this@PasswordsListFragment::adapter.isInitialized) {
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
                            if (this@PasswordsListFragment::adapter.isInitialized) {
                                adapter.notifyDataSetChanged()
                            }
                        }
                }
                launch {
                    viewModel.passwordsListFlow
                        .collect { passwordsList ->
                            if (passwordsList != null) {
                                viewModel.passwordsList.clear()
                                viewModel.passwordsList.addAll(passwordsList)
                                setRecyclerView(passwordsList)
                            }
                        }
                }
            }
        }
    }
}