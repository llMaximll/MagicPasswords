package com.github.llmaximll.magicpasswords.adaptersholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.fragments.PasswordsListFragment
import com.github.llmaximll.magicpasswords.states.ListState
import com.github.llmaximll.magicpasswords.vm.PasswordsListVM

class PasswordsListAdapter(
    private val callbacks: PasswordsListFragment.Callbacks?,
    private var passwordsList: MutableList<PasswordInfo>,
    private val viewModel: PasswordsListVM) :
    RecyclerView.Adapter<PasswordsListHolder>() {

    private lateinit var view: View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordsListHolder {
        view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rv_passwords_list, parent, false)
        return PasswordsListHolder(view, viewModel)
    }

    override fun onBindViewHolder(holder: PasswordsListHolder, position: Int) {
        val passwordInfo = passwordsList[position]
        holder.bind(callbacks, passwordInfo)
    }

    override fun getItemCount(): Int = passwordsList.size

    override fun onViewAttachedToWindow(holder: PasswordsListHolder) {
        super.onViewAttachedToWindow(holder)
        holder.setSelected(viewModel.selectedDataFlow.value is ListState.SELECTED)
    }
}