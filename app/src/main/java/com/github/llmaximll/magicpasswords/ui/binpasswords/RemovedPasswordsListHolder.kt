package com.github.llmaximll.magicpasswords.ui.binpasswords

import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.states.ListState
import com.github.llmaximll.magicpasswords.utils.Animation
import com.github.llmaximll.magicpasswords.utils.Common
import java.util.*

class RemovedPasswordsListHolder(
    itemView: View,
    private val viewModel: RecycleBinVM
)
    : RecyclerView.ViewHolder(itemView),
    View.OnTouchListener,
    View.OnLongClickListener
{
    private lateinit var passwordInfo: PasswordInfo
    private val nameTextView: TextView = itemView.findViewById(R.id.name_textView)
    private val descriptionTextView: TextView = itemView.findViewById(R.id.description_textView)
    private val dateTextView: TextView = itemView.findViewById(R.id.date_textView)
    private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

    init {
        itemView.apply {
            setOnTouchListener(this@RemovedPasswordsListHolder)
            setOnLongClickListener(this@RemovedPasswordsListHolder)
        }
    }

    fun bind(passwordInfo: PasswordInfo) {
        this.passwordInfo = passwordInfo
        nameTextView.text = passwordInfo.name
        descriptionTextView.text = passwordInfo.description
        val calendar = Calendar.getInstance()
        calendar.time = Date(passwordInfo.removedDate)
        val currentCalendar = Calendar.getInstance()
        when (passwordInfo.removedFormat) {
            Common.TimeDeleteDaySP -> {
                val hours = currentCalendar.get(Calendar.HOUR_OF_DAY) - calendar.get(Calendar.HOUR_OF_DAY)
                dateTextView.text = "До удаления: ${24 - hours} часов"
            }
            Common.TimeDeleteWeakSP -> {
                val days = currentCalendar.get(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH)
                dateTextView.text = "До удаления: ${7 - days} дней"
            }
            Common.TimeDeleteMonthSP -> {
                val minutes = currentCalendar.get(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH)
                dateTextView.text = "До удаления: ${30 - minutes} дней"
            }
        }
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