package com.github.llmaximll.magicpasswords.adaptersholders

import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.vm.RecycleBinVM
import java.util.*

private const val TAG = "RemovedPasswordsListHolder"

class RemovedPasswordsListHolder(itemView: View, private val viewModel: RecycleBinVM)
    : RecyclerView.ViewHolder(itemView),
    View.OnTouchListener,
    View.OnLongClickListener
{
    private lateinit var passwordInfo: PasswordInfo
    private val nameTextView: TextView = itemView.findViewById(R.id.name_textView)
    private val descriptionTextView: TextView = itemView.findViewById(R.id.description_textView)
    private val dateTextView: TextView = itemView.findViewById(R.id.date_textView)
    private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
    private val cf = CommonFunctions.get()

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
        val minutes = currentCalendar.get(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH)
        dateTextView.text = "До удаления: ${30 - minutes} дней"
        if (viewModel.selected.value) {
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
        return if (!viewModel.selected.value) {
            viewModel.selected.value = true
            true
        } else {
            false
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                cf.animateView(itemView, false, zChange = true)
            }
            MotionEvent.ACTION_CANCEL -> {
                cf.animateView(itemView, true, zChange = true)
            }
            MotionEvent.ACTION_UP -> {
                cf.animateView(itemView, true, zChange = true)
                if (viewModel.selected.value) {
                    cf.log(TAG, "onClick()")
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