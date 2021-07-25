package com.github.llmaximll.magicpasswords.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.github.llmaximll.magicpasswords.OnBackPressedListener
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.activities.MainActivity
import com.github.llmaximll.magicpasswords.adaptersholders.PasswordsListAdapter
import com.github.llmaximll.magicpasswords.adaptersholders.SimpleItemTouchHelperCallback
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.databinding.FragmentPasswordsListBinding
import com.github.llmaximll.magicpasswords.states.ListState
import com.github.llmaximll.magicpasswords.states.SearchState
import com.github.llmaximll.magicpasswords.utils.CommonFunctions
import com.github.llmaximll.magicpasswords.vm.PasswordsListVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "PasswordsListFragment"
private const val KEY_RECYCLER_VIEW = "key_recycler_view"

class PasswordsListFragment : Fragment(),
    OnBackPressedListener {

    interface Callbacks {
        fun onPasswordsListFragment(fragment: String, idPassword: String, sharedView: View?)
    }

    private lateinit var binding: FragmentPasswordsListBinding
    private lateinit var cf: CommonFunctions
    private lateinit var viewModel: PasswordsListVM
    private lateinit var adapter: PasswordsListAdapter
    private var recyclerViewState: Parcelable? = null
    private var callbacks: Callbacks? = null

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

        setToolBar()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = cf.initViewModel(this, PasswordsListVM::class.java) as PasswordsListVM
        recyclerViewState = viewModel.getRecyclerViewState()
        if (viewModel.passwordsList.isEmpty()) {
            getAllPasswords()
        } else {
            setRecyclerView(viewModel.passwordsList)
        }
        isSelectedFragment()
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
                    (activity as? MainActivity)?.replaceMainFragments(MainActivity.REPLACE_ON_ADD_FRAGMENT)
                    v.performClick()
                }
            }
            true
        }
        cf.log(TAG, "passwordsList=${viewModel.passwordsList.size}")
        cf.log(TAG, "selectedPasswordsMMap=${viewModel.selectedPasswordsMMap.size}")
    }

    override fun onPause() {
        super.onPause()
        recyclerViewState = binding.passwordsRecyclerView.layoutManager?.onSaveInstanceState()
        viewModel.saveRecyclerViewState(recyclerViewState as LinearLayoutManager.SavedState?)
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    fun getAllPasswords() {
        viewModel.getAllPasswords(0)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.passwordsListFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { passwordsList ->
                    if (passwordsList != null) {
                        viewModel.passwordsList.clear()
                        viewModel.passwordsList.addAll(passwordsList)
                        setRecyclerView(passwordsList)
                    }
                }
        }
    }

    private fun setToolBar(toolBar: Toolbar = binding.toolBar) {
        toolBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settings -> {
                    callbacks?.onPasswordsListFragment(
                        "settings",
                        "null",
                        null
                    )
                }
                R.id.recycle_bin -> {
                    callbacks?.onPasswordsListFragment(
                        "recycle bin",
                        "null",
                        null
                    )
                }
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
                viewModel.searchDataFlow.value = SearchState.ACTIVE
                if (newText != null) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.getRelevantPasswords(newText)
                        viewModel.searchDataFlow.value = SearchState.INACTIVE
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
            viewModel,
            requireContext(),
            binding.coordinatorLayout
        )
        rV.adapter = adapter
        val callback = SimpleItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rV)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun isSelectedFragment() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedDataFlow
                        .collect { state ->
                            binding.toolBar.menu.run {
                                findItem(R.id.settings).isVisible = state is ListState.UNSELECTED
                                findItem(R.id.recycle_bin).isVisible = state is ListState.UNSELECTED
                                findItem(R.id.delete_passwords).isVisible = state is ListState.SELECTED
                                findItem(R.id.select_all).isVisible = state is ListState.SELECTED
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
                    viewModel.searchDataFlow
                        .collect { search ->
                            if (search is SearchState.ACTIVE) {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.passwordsRecyclerView.visibility = View.GONE
                            } else {
                                binding.progressBar.visibility = View.GONE
                                binding.passwordsRecyclerView.visibility = View.VISIBLE
                            }
                        }
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return if (viewModel.selectedDataFlow.value is ListState.SELECTED) {
            viewModel.selectedDataFlow.value = ListState.UNSELECTED
            false
        } else {
            true
        }
    }

    companion object {
        fun newInstance(): PasswordsListFragment {
            return PasswordsListFragment()
        }
    }
}