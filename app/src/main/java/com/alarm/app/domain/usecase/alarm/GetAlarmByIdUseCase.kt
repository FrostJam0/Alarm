package com.alarm.app.domain.usecase.alarm

import com.alarm.app.domain.model.Alarm
import com.alarm.app.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * Use case for retrieving a specific alarm by its unique identifier.
 * 
 * Encapsulates the business logic required to fetch a single alarm from the [AlarmRepository].
 *
 * @property alarmRepository The repository responsible for alarm data operations.
 */
class GetAlarmByIdUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    /**
     * Executes the use case to fetch an alarm.
     * 
     * @param id The unique identifier of the alarm to retrieve.
     * @return The [Alarm] matching the given ID, or null if it does not exist.
     */
    suspend operator fun invoke(id: Int): Alarm? {
        return alarmRepository.getAlarmById(id)
    }
}
