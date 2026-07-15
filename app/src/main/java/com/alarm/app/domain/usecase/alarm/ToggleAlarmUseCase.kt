package com.alarm.app.domain.usecase.alarm

import com.alarm.app.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * Use case for toggling the enabled state of an alarm.
 * 
 * Provides a lightweight method to quickly turn an alarm on or off without updating the entire entity.
 * It also automatically updates the `updatedAt` timestamp to ensure accurate modification tracking.
 *
 * @property alarmRepository The repository responsible for alarm data operations.
 */
class ToggleAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    /**
     * Executes the use case to toggle an alarm's enabled state.
     * 
     * @param id The unique identifier of the alarm to toggle.
     * @param isEnabled The new state (true for enabled, false for disabled).
     */
    suspend operator fun invoke(id: Int, isEnabled: Boolean) {
        alarmRepository.setAlarmEnabled(id, isEnabled, System.currentTimeMillis())
    }
}
