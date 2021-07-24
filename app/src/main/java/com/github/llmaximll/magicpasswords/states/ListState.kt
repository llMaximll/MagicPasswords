package com.github.llmaximll.magicpasswords.states

sealed class ListState {
    object SELECTED : ListState()
    object UNSELECTED : ListState()
}
