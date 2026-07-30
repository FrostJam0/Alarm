package com.alarm.app.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarm.app.domain.model.Alarm
import com.alarm.app.domain.usecase.alarm.DeleteAlarmUseCase
import com.alarm.app.domain.usecase.alarm.GetAllAlarmsUseCase
import com.alarm.app.domain.usecase.alarm.ToggleAlarmUseCase
import com.alarm.app.scheduler.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the main screen displaying the list of alarms.
 * Exposes the state of alarms and provides actions to toggle or delete them.
 *
 * @property getAllAlarmsUseCase Use case to retrieve all alarms as a stream.
 * @property toggleAlarmUseCase Use case to enable or disable an alarm in the database.
 * @property deleteAlarmUseCase Use case to remove an alarm from the database.
 * @property alarmScheduler Scheduler to manage the system alarms when toggled or deleted.
 */
@HiltViewModel
class AlarmListViewModel @Inject constructor(
    private val getAllAlarmsUseCase: GetAllAlarmsUseCase,
    private val toggleAlarmUseCase: ToggleAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val alarms: StateFlow<List<Alarm>> = _alarms.asStateFlow()

    init {
        viewModelScope.launch {
            getAllAlarmsUseCase().collectLatest { list ->
                _alarms.value = list.sortedWith(compareBy({ it.hour }, { it.minute }))
            }
        }
    }

    /**
     * Toggles the enabled state of a specific alarm.
     * Updates the database and schedules or cancels the system alarm accordingly.
     *
     * @param alarm The alarm to be toggled.
     * @param isEnabled The new enabled state.
     */
    fun toggleAlarm(alarm: Alarm, isEnabled: Boolean) {
        viewModelScope.launch {
            toggleAlarmUseCase(alarm.id, isEnabled)
            if (isEnabled) {
                val updated = alarm.copy(isEnabled = true)
                alarmScheduler.schedule(updated)
            } else {
                alarmScheduler.cancel(alarm.id)
            }
        }
    }

    /**
     * Deletes the specified alarm from the database and cancels its scheduled trigger.
     *
     * @param alarm The alarm to be deleted.
     */
    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmScheduler.cancel(alarm.id)
            deleteAlarmUseCase(alarm)
        }
    }
}
