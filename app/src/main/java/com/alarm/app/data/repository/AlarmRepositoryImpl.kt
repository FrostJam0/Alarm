package com.alarm.app.data.repository

import com.alarm.app.data.db.AlarmDao
import com.alarm.app.data.mapper.toDomain
import com.alarm.app.data.mapper.toEntity
import com.alarm.app.domain.model.Alarm
import com.alarm.app.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of the [AlarmRepository] interface.
 * Coordinates data operations for alarms between the data source (DAO) and the domain layer.
 */
class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao
) : AlarmRepository {

    /**
     * Retrieves a continuous flow of all alarms, mapped to domain models.
     *
     * @return A Flow emitting a list of [Alarm] objects.
     */
    override fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms().map { list -> list.map { it.toDomain() } }
    }

    /**
     * Fetches a single alarm by its ID.
     *
     * @param id The unique identifier of the alarm.
     * @return The [Alarm] domain model if found, null otherwise.
     */
    override suspend fun getAlarmById(id: Int): Alarm? {
        return alarmDao.getAlarmById(id)?.toDomain()
    }

    /**
     * Retrieves all alarms that are currently enabled.
     *
     * @return A list of enabled [Alarm] domain models.
     */
    override suspend fun getEnabledAlarms(): List<Alarm> {
        return alarmDao.getEnabledAlarms().map { it.toDomain() }
    }

    /**
     * Creates a new alarm in the repository.
     *
     * @param alarm The [Alarm] domain model to create.
     * @return The ID of the newly created alarm.
     */
    override suspend fun createAlarm(alarm: Alarm): Long {
        return alarmDao.insertAlarm(alarm.toEntity())
    }

    /**
     * Updates an existing alarm in the repository.
     *
     * @param alarm The [Alarm] domain model containing updated data.
     */
    override suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(alarm.toEntity())
    }

    /**
     * Deletes a specific alarm from the repository.
     *
     * @param alarm The [Alarm] domain model to delete.
     */
    override suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm.toEntity())
    }

    /**
     * Updates the enabled status of a specific alarm.
     *
     * @param id The ID of the alarm to update.
     * @param isEnabled The new enabled status.
     * @param updatedAt The timestamp of this update.
     */
    override suspend fun setAlarmEnabled(id: Int, isEnabled: Boolean, updatedAt: Long) {
        alarmDao.setEnabled(id, isEnabled, updatedAt)
    }
}
