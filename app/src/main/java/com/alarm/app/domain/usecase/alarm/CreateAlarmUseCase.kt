package com.alarm.app.domain.usecase.alarm

import com.alarm.app.domain.model.Alarm
import com.alarm.app.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * Use case for creating a new alarm.
 * 
 * Encapsulates the business logic required to add a new alarm to the system via the [AlarmRepository].
 *
 * @property alarmRepository The repository responsible for alarm data operations.
 */
class CreateAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    /**
     * Executes the use case to create a new alarm.
     * 
     * @param alarm The [Alarm] entity containing the data for the new alarm.
     * @return The unique ID generated for the newly created alarm.
     */
    suspend operator fun invoke(alarm: Alarm): Long {
        return alarmRepository.createAlarm(alarm)
    }
}
