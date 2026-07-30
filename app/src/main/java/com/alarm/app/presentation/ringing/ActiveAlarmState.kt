package com.alarm.app.presentation.ringing

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Singleton to hold the active alarm's volatile state (like tap count)
 * so it survives if the Activity is violently recreated to pierce the lock screen.
 */
object ActiveAlarmState {
    val clickCount = MutableStateFlow(0)
    val buttonPercentX = MutableStateFlow(0.5f)
    val buttonPercentY = MutableStateFlow(0.5f)
    
    fun reset() {
        clickCount.value = 0
        buttonPercentX.value = 0.5f
        buttonPercentY.value = 0.5f
    }
}
