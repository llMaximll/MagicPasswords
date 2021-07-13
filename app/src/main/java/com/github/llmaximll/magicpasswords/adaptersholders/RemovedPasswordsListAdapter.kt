package com.github.llmaximll.magicpasswords.adaptersholders

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.vm.RecycleBinVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RemovedPasswordsListAdapter(
    private var passwordsList: MutableList<PasswordInfo>,
    private val viewModel: RecycleBinVM,
    private val context: Context,
    private val setAll: Boolean
) :
    RecyclerView.Adapter<RemovedPasswordsListHolder>() {

    private val workManager = WorkManager.getInstance(context)
    private lateinit var view: View
    private lateinit var password: PasswordInfo
    private var cf: CommonFunctions = CommonFunctions.get()
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemovedPasswordsListHolder {
        view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rv_removed_passwords_list, parent, false)
        return RemovedPasswordsListHolder(view, viewModel)
    }

    override fun onBindViewHolder(holder: RemovedPasswordsListHolder, position: Int) {
        val passwordInfo = passwordsList[position]
        password = passwordInfo
        holder.bind(passwordInfo, position)
    }

    override fun getItemCount(): Int = passwordsList.size

    private fun deletePasswordWorkManager(tag: String) {
        workManager.cancelAllWorkByTag(tag)
        cf.toast(context, "deletePasswordWorkManager")
    }

    override fun onViewAttachedToWindow(holder: RemovedPasswordsListHolder) {
        super.onViewAttachedToWindow(holder)
        scope.launch {
            viewModel.selected.collect {
                holder.setSelected(it)
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        scope.cancel()
    }
}