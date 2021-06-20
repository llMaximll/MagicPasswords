package com.github.llmaximll.magicpasswords.adaptersholders

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.fragments.PasswordsListFragment
import com.github.llmaximll.magicpasswords.vm.PasswordsListVM

class PasswordsListAdapter(
    private val callbacks: PasswordsListFragment.Callbacks?,
    private var passwordsList: MutableList<PasswordInfo>,
    private val viewModel: PasswordsListVM, private val context: Context
) :
    RecyclerView.Adapter<PasswordsListHolder>(), ItemTouchHelperAdapter {

    private var cf: CommonFunctions = CommonFunctions.get()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordsListHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rv_passwords_list, parent, false)
        return PasswordsListHolder(view)
    }

    override fun onBindViewHolder(holder: PasswordsListHolder, position: Int) {
        val passwordInfo = passwordsList[position]
        holder.bind(callbacks, passwordInfo)
    }

    override fun getItemCount(): Int = passwordsList.size

    override fun onItemDismiss(position: Int) {
        val password = passwordsList[position]
        viewModel.deletePassword(password)
        passwordsList.remove(password)
        notifyItemRemoved(position)
        cf.toast(context, "Пароль удален")
    }
}