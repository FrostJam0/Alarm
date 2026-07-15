package com.alarm.app.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarm.app.domain.model.Alarm
import com.alarm.app.domain.usecase.alarm.CreateAlarmUseCase
import com.alarm.app.domain.usecase.alarm.UpdateAlarmUseCase
import com.alarm.app.scheduler.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the creation and modification of alarms.
 * Handles the interaction with use cases for saving alarms and scheduling them.
 *
 * @property createAlarmUseCase Use case to insert a new alarm into the database.
 * @property updateAlarmUseCase Use case to update an existing alarm.
 * @property alarmScheduler Scheduler to set or cancel the system alarms based on the alarm state.
 */
@HiltViewModel
class AlarmCreateViewModel @Inject constructor(
    private val createAlarmUseCase: CreateAlarmUseCase,
    private val updateAlarmUseCase: UpdateAlarmUseCase,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    /**
     * Saves an alarm. If the alarm is new (id == 0), it creates a new entry and schedules it.
     * If it exists, it updates the entry and reschedules it.
     *
     * @param alarm The alarm data to be saved.
     */
    fun saveAlarm(alarm: Alarm) {
        viewModelScope.launch {
            if (alarm.id == 0) {
                val id = createAlarmUseCase(alarm)
                alarmScheduler.schedule(alarm.copy(id = id.toInt()))
            } else {
                updateAlarmUseCase(alarm)
                alarmScheduler.schedule(alarm)
            }
        }
    }
}
