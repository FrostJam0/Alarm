package com.alarm.app.domain.usecase.alarm

import com.alarm.app.domain.model.Alarm
import com.alarm.app.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving a reactive stream of all alarms.
 * 
 * Provides a [Flow] that continuously emits the latest list of alarms whenever the underlying data
 * in the [AlarmRepository] changes. Useful for populating the main alarm list UI.
 *
 * @property alarmRepository The repository responsible for alarm data operations.
 */
class GetAllAlarmsUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    /**
     * Executes the use case to fetch all alarms as a Flow.
     * 
     * @return A [Flow] emitting a list of all [Alarm] entities.
     */
    operator fun invoke(): Flow<List<Alarm>> {
        return alarmRepository.getAllAlarms()
    }
}
