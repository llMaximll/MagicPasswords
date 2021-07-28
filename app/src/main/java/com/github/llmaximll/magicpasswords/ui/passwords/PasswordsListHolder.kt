package com.github.llmaximll.magicpasswords.ui.passwords

import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.states.ListState
import com.github.llmaximll.magicpasswords.utils.Animation

class PasswordsListHolder(
    itemView: View,
    private val viewModel: PasswordsListVM
) :
    RecyclerView.ViewHolder(itemView),
    View.OnTouchListener,
    View.OnLongClickListener {
    private lateinit var passwordInfo: PasswordInfo
    private val nameTextView: TextView = itemView.findViewById(R.id.name_textView)
    private val descriptionTextView: TextView = itemView.findViewById(R.id.description_textView)
    private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
    private var callbacks: PasswordsListFragment.Callbacks? = null

    init {
        itemView.setOnTouchListener(this)
        itemView.setOnLongClickListener(this)
    }

    fun bind(
        callbacks: PasswordsListFragment.Callbacks?,
        passwordInfo: PasswordInfo
    ) {
        this.callbacks = callbacks
        this.passwordInfo = passwordInfo
        nameTextView.text = passwordInfo.name
        descriptionTextView.text = passwordInfo.description
        //transition
        itemView.transitionName = "transition_$adapterPosition"
        if (viewModel.selectedDataFlow.value is ListState.SELECTED) {
            if (viewModel.setAllDataFlow.value) {
                checkBox.isChecked = true
            }
            var checked = false
            for (key in viewModel.selectedPasswordsMMap.keys) {
                if (key == adapterPosition) {
                    checked = true
                    checkBox.isChecked = true
                    break
                }
            }
            if (!checked) checkBox.isChecked = false
        }
    }

    fun setSelected(selected: Boolean) {
        if (selected) {
            checkBox.visibility = View.VISIBLE
        } else {
            checkBox.visibility = View.INVISIBLE
            checkBox.isChecked = false
        }
    }

    override fun onLongClick(v: View?): Boolean {
        return if (viewModel.selectedDataFlow.value is ListState.UNSELECTED) {
            viewModel.run {
                selectedPasswordsMMap[adapterPosition] = passwordInfo
                selectedDataFlow.value = ListState.SELECTED
            }
            true
        } else {
            false
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                Animation.animateView(itemView, false, zChange = true)
            }
            MotionEvent.ACTION_CANCEL -> {
                Animation.animateView(itemView, true, zChange = true)
            }
            MotionEvent.ACTION_UP -> {
                Animation.animateView(itemView, true, zChange = true)
                if (viewModel.selectedDataFlow.value is ListState.UNSELECTED) {
                    callbacks?.onPasswordsListFragmentChangePassword(
                        passwordInfo.id.toString(),
                        itemView
                    )
                }
                if (viewModel.selectedDataFlow.value is ListState.SELECTED) {
                    checkBox.isChecked = !checkBox.isChecked
                    if (checkBox.isChecked) {
                        viewModel.selectedPasswordsMMap[adapterPosition] = passwordInfo
                    } else {
                        viewModel.selectedPasswordsMMap.remove(adapterPosition)
                    }
                }
                v?.performClick()
            }
        }
        return false
    }
}