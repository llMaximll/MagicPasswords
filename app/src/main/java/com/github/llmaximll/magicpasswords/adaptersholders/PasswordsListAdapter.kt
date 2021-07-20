package com.github.llmaximll.magicpasswords.adaptersholders

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.background.DeletePasswordWorker
import com.github.llmaximll.magicpasswords.utils.CommonFunctions
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.fragments.PasswordsListFragment
import com.github.llmaximll.magicpasswords.vm.PasswordsListVM
import java.util.*
import java.util.concurrent.TimeUnit

class PasswordsListAdapter(
    private val callbacks: PasswordsListFragment.Callbacks?,
    private var passwordsList: MutableList<PasswordInfo>,
    private val viewModel: PasswordsListVM,
    private val context: Context,
    private val parentView: View
) :
    RecyclerView.Adapter<PasswordsListHolder>(), ItemTouchHelperAdapter {

    private val workManager = WorkManager.getInstance(context)
    private var cf: CommonFunctions = CommonFunctions.get()
    private lateinit var view: View
    private lateinit var sp: SharedPreferences

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordsListHolder {
        view = LayoutInflater.from(parent.context)
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
        passwordsList.remove(password)
        notifyItemRemoved(position)
        val deleteFormat = sp.getInt(cf.spTimeDelete, CommonFunctions.TimeDeleteMonthSP)
        password.removed = 1
        password.removedDate = Calendar.getInstance().timeInMillis
        password.removedFormat = deleteFormat
        if (deleteFormat != CommonFunctions.TimeDeleteImmediatelySP) {
            viewModel.updatePassword(password)
            deletePasswordWorkManager(password.id.toString(), password.id.toString())
            fun cancelSnackBar() {
                password.removed = 0
                viewModel.updatePassword(password)
                workManager.cancelAllWorkByTag("${password.id}")
                passwordsList.add(position, password)
                notifyItemInserted(position)
            }
            cf.snackBar(parentView, "Пароль удален", true) { cancelSnackBar() }
        } else {
            viewModel.deletePassword(password)
            cf.snackBar(parentView, "Пароль удален", false)
        }
    }

    private fun deletePasswordWorkManager(passwordId: String, tag: String) {
        val duration = sp.getInt(cf.spTimeDelete, CommonFunctions.TimeDeleteMonthSP)
        val myData = Data.Builder().apply {
            putString("passwordId", passwordId)
        }.build()
        val myWorkRequest = OneTimeWorkRequestBuilder<DeletePasswordWorker>().apply {
            addTag(tag)
            setInputData(myData)
            when (duration) {
                CommonFunctions.TimeDeleteDaySP -> {
                    setInitialDelay(24, TimeUnit.HOURS)
                }
                CommonFunctions.TimeDeleteWeakSP -> {
                    setInitialDelay(7, TimeUnit.DAYS)
                }
                CommonFunctions.TimeDeleteMonthSP -> {
                    setInitialDelay(30, TimeUnit.DAYS)
                }
            }
        }.build()
        workManager.enqueue(myWorkRequest)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        sp = cf.getSharedPreferences(context)
    }
}