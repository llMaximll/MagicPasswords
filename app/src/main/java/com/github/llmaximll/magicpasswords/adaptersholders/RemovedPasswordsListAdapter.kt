package com.github.llmaximll.magicpasswords.adaptersholders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
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
    RecyclerView.Adapter<RemovedPasswordsListHolder>(), ItemTouchHelperAdapter {

    private val workManager = WorkManager.getInstance(context)
    private lateinit var view: View
    private lateinit var password: PasswordInfo
    private var cf: CommonFunctions = CommonFunctions.get()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemovedPasswordsListHolder {
        view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rv_removed_passwords_list, parent, false)
        return RemovedPasswordsListHolder(view)
    }

    override fun onBindViewHolder(holder: RemovedPasswordsListHolder, position: Int) {
        val passwordInfo = passwordsList[position]
        password = passwordInfo
        holder.bind(passwordInfo)
    }

    override fun getItemCount(): Int = passwordsList.size

    override fun onItemDismiss(position: Int) {
        deletePasswordWorkManager("${password.id}")
        password.removed = 0
        password.removedDate = 0L
        viewModel.deletePassword(password)
    }

    private fun deletePasswordWorkManager(tag: String) {
        workManager.cancelAllWorkByTag(tag)
        cf.toast(context, "deletePasswordWorkManager")
    }
}