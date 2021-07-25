package com.github.llmaximll.magicpasswords.states

sealed class SearchState {
    object ACTIVE: SearchState()
    object INACTIVE: SearchState()
}
