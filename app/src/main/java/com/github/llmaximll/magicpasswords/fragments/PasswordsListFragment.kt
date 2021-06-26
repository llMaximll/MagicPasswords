package com.github.llmaximll.magicpasswords.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.adaptersholders.PasswordsListAdapter
import com.github.llmaximll.magicpasswords.adaptersholders.SimpleItemTouchHelperCallback
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.databinding.FragmentPasswordsListBinding
import com.github.llmaximll.magicpasswords.vm.PasswordsListVM
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val TAG = "PasswordsListFragment"

class PasswordsListFragment : Fragment() {

    interface Callbacks {
        fun onPasswordsListFragment(fragment: String, idPassword: String, sharedView: View?)
    }

    private lateinit var binding: FragmentPasswordsListBinding
    private lateinit var cf: CommonFunctions
    private lateinit var viewModel: PasswordsListVM
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
        getAllPasswords()
        //transition
        postponeEnterTransition()
        binding.coordinatorLayout.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun getAllPasswords() {
        viewModel.getAllPasswords(0)
        lifecycleScope.launch {
            viewModel.passwordsList.collect { passwordsList ->
                setRecyclerView(passwordsList)
            }
        }
    }

    private fun setToolBar(toolBar: Toolbar = binding.toolBar) {
        toolBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.add -> {
                    callbacks?.onPasswordsListFragment("add", "null", null)
                }
                R.id.settings -> {
                    callbacks?.onPasswordsListFragment("settings", "null", null)
                }
                R.id.recycle_bin -> {
                    callbacks?.onPasswordsListFragment("recycle bin", "null", null)
                }
            }
            true
        }
    }

    private fun setRecyclerView(passwordsList: List<PasswordInfo>) {
        val mutPasswordsList = mutableListOf<PasswordInfo>()
        mutPasswordsList.addAll(passwordsList)
        if (passwordsList.isNotEmpty()) {
            val rV = binding.passwordsRecyclerView
            rV.layoutManager = LinearLayoutManager(requireContext())
            val adapter = PasswordsListAdapter(callbacks, mutPasswordsList, viewModel, requireContext(), binding.coordinatorLayout)
            rV.adapter = adapter
            val callback = SimpleItemTouchHelperCallback(adapter)
            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(rV)
        }
    }

    companion object {
        fun newInstance(): PasswordsListFragment {
            return PasswordsListFragment()
        }
    }
}