package com.alarm.app.domain.repository

import com.alarm.app.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing [Alarm] entities.
 * 
 * Defines the contract for data operations related to alarms, abstracting the underlying data source (e.g., local database).
 * Implementations of this interface handle the actual data persistence and retrieval.
 */
interface AlarmRepository {
    /**
     * Retrieves a reactive stream of all alarms in the system.
     * 
     * @return A [Flow] emitting a list of all [Alarm]s. The flow updates automatically when the data changes.
     */
    fun getAllAlarms(): Flow<List<Alarm>>

    /**
     * Retrieves a specific alarm by its unique identifier.
     * 
     * @param id The unique identifier of the alarm.
     * @return The [Alarm] with the specified ID, or null if no such alarm exists.
     */
    suspend fun getAlarmById(id: Int): Alarm?

    /**
     * Retrieves a list of all currently enabled alarms.
     * 
     * Useful for rescheduling alarms after a device reboot or application restart.
     * 
     * @return A list containing all alarms where [Alarm.isEnabled] is true.
     */
    suspend fun getEnabledAlarms(): List<Alarm>

    /**
     * Creates a new alarm in the repository.
     * 
     * @param alarm The [Alarm] instance to be saved.
     * @return The unique identifier (ID) assigned to the newly created alarm.
     */
    suspend fun createAlarm(alarm: Alarm): Long

    /**
     * Updates an existing alarm in the repository.
     * 
     * @param alarm The [Alarm] instance containing updated data. The alarm is identified by its [Alarm.id].
     */
    suspend fun updateAlarm(alarm: Alarm)

    /**
     * Deletes an alarm from the repository.
     * 
     * @param alarm The [Alarm] instance to be removed.
     */
    suspend fun deleteAlarm(alarm: Alarm)

    /**
     * Updates the enabled state of a specific alarm.
     * 
     * @param id The unique identifier of the alarm to update.
     * @param isEnabled The new enabled state for the alarm.
     * @param updatedAt The timestamp indicating when this update occurred.
     */
    suspend fun setAlarmEnabled(id: Int, isEnabled: Boolean, updatedAt: Long)
}
