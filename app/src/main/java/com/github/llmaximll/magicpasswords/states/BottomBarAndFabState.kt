package com.github.llmaximll.magicpasswords.states

sealed class BottomBarAndFabState {
    object BottomBarOnFabOn: BottomBarAndFabState()
    object BottomBarOnFabOff: BottomBarAndFabState()
    object BottomBarOffFabOn: BottomBarAndFabState()
    object BottomBarOffFabOff: BottomBarAndFabState()
}
