package com.github.llmaximll.magicpasswords.adaptersholders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.vm.RecycleBinVM

class RemovedPasswordsListAdapter(
    private var passwordsList: MutableList<PasswordInfo>,
    private val viewModel: RecycleBinVM,
    private val context: Context,
    private val parentView: View
) :
    RecyclerView.Adapter<RemovedPasswordsListHolder>() {

    private var cf: CommonFunctions = CommonFunctions.get()
    private lateinit var view: View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemovedPasswordsListHolder {
        view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rv_passwords_list, parent, false)
        return RemovedPasswordsListHolder(view)
    }

    override fun onBindViewHolder(holder: RemovedPasswordsListHolder, position: Int) {
        val passwordInfo = passwordsList[position]
        holder.bind(passwordInfo)
    }

    override fun getItemCount(): Int = passwordsList.size
}