package com.alarm.app.domain.usecase.alarm

import com.alarm.app.domain.model.Alarm
import com.alarm.app.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * Use case for updating an existing alarm.
 * 
 * Encapsulates the business logic required to modify an alarm's details in the system.
 * This is used when an alarm's time, label, recurrence, or associated QR code is changed by the user.
 *
 * @property alarmRepository The repository responsible for alarm data operations.
 */
class UpdateAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    /**
     * Executes the use case to update an alarm.
     * 
     * @param alarm The fully updated [Alarm] entity to be saved.
     */
    suspend operator fun invoke(alarm: Alarm) {
        alarmRepository.updateAlarm(alarm)
    }
}
