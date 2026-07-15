package com.alarm.app.domain.usecase.alarm

import com.alarm.app.domain.model.Alarm
import com.alarm.app.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * Use case for deleting an existing alarm.
 * 
 * Encapsulates the business logic required to remove an alarm from the system via the [AlarmRepository].
 *
 * @property alarmRepository The repository responsible for alarm data operations.
 */
class DeleteAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    /**
     * Executes the use case to delete an alarm.
     * 
     * @param alarm The [Alarm] entity to be permanently removed.
     */
    suspend operator fun invoke(alarm: Alarm) {
        // Typically, any scheduled work (like AlarmManager intents) should also be canceled 
        // by observing the repository changes or triggering an alarm scheduler from the ViewModel.
        alarmRepository.deleteAlarm(alarm)
    }
}
