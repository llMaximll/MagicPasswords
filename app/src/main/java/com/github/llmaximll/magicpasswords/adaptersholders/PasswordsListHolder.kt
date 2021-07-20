package com.github.llmaximll.magicpasswords.adaptersholders

import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.utils.CommonFunctions
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.fragments.PasswordsListFragment

class PasswordsListHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnTouchListener {
    private lateinit var passwordInfo: PasswordInfo
    private val nameTextView: TextView = itemView.findViewById(R.id.name_textView)
    private val descriptionTextView: TextView = itemView.findViewById(R.id.description_textView)
    private val cf = CommonFunctions.get()
    private var callbacks: PasswordsListFragment.Callbacks? = null

    init {
        itemView.setOnTouchListener(this)
    }

    fun bind(callbacks: PasswordsListFragment.Callbacks?, passwordInfo: PasswordInfo) {
        this.callbacks = callbacks
        this.passwordInfo = passwordInfo
        nameTextView.text = passwordInfo.name
        descriptionTextView.text = passwordInfo.description
        //transition
        itemView.transitionName = "transition_$adapterPosition"
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
                Log.i("TAG", "position=$adapterPosition")
                callbacks?.onPasswordsListFragment("add", passwordInfo.id.toString(), itemView)
                v?.performClick()
            }
        }
        return true
    }
}