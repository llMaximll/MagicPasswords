package com.github.llmaximll.magicpasswords.adaptersholders

import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.data.PasswordInfo

class RemovedPasswordsListHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnTouchListener {
    private lateinit var passwordInfo: PasswordInfo
    private val nameTextView: TextView = itemView.findViewById(R.id.name_textView)
    private val descriptionTextView: TextView = itemView.findViewById(R.id.description_textView)
    private val cf = CommonFunctions.get()

    init {
        itemView.setOnTouchListener(this)
    }

    fun bind(passwordInfo: PasswordInfo) {
        this.passwordInfo = passwordInfo
        nameTextView.text = passwordInfo.name
        descriptionTextView.text = passwordInfo.description
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
                v?.performClick()
            }
        }
        return true
    }
}