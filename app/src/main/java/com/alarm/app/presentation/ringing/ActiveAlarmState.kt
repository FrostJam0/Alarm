package com.alarm.app.presentation.ringing

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Singleton to hold the active alarm's volatile state (like tap count)
 * so it survives if the Activity is violently recreated to pierce the lock screen.
 */
object ActiveAlarmState {
    val clickCount = MutableStateFlow(0)
    
    fun reset() {
        clickCount.value = 0
    }
}
