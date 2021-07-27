package com.github.llmaximll.magicpasswords.binpasswords

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.model.PasswordInfo
import com.github.llmaximll.magicpasswords.states.ListState

class RemovedPasswordsListAdapter(
    private var passwordsList: MutableList<PasswordInfo>,
    private val viewModel: RecycleBinVM
) :
    RecyclerView.Adapter<RemovedPasswordsListHolder>() {

    private lateinit var view: View
    private lateinit var password: PasswordInfo

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemovedPasswordsListHolder {
        view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rv_removed_passwords_list, parent, false)
        return RemovedPasswordsListHolder(view, viewModel)
    }

    override fun onBindViewHolder(holder: RemovedPasswordsListHolder, position: Int) {
        val passwordInfo = passwordsList[position]
        password = passwordInfo
        holder.bind(passwordInfo)
    }

    override fun getItemCount(): Int = passwordsList.size

    override fun onViewAttachedToWindow(holder: RemovedPasswordsListHolder) {
        super.onViewAttachedToWindow(holder)
        holder.setSelected(viewModel.selectedDataFlow.value is ListState.SELECTED)
    }
}