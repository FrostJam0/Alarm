package com.alarm.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

/**
 * DataStore wrapper for managing application-wide preferences.
 * Handles storage and retrieval of state flags such as battery optimization prompts
 * and currently ringing alarm identifiers.
 */
@Singleton
class AppPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        /** Key for tracking if the user has been prompted about battery optimization. */
        val KEY_BATTERY_OPT_PROMPTED = booleanPreferencesKey("battery_opt_prompted")
        /** Key for storing the ID of the currently ringing alarm. */
        val KEY_CURRENTLY_RINGING_ALARM_ID = intPreferencesKey("currently_ringing_alarm_id")
    }

    /**
     * A flow emitting whether the user has been prompted to disable battery optimizations.
     */
    val batteryOptPrompted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_BATTERY_OPT_PROMPTED] ?: false
    }

    /**
     * Sets the state of whether the battery optimization prompt has been shown.
     *
     * @param prompted True if the user has been prompted, false otherwise.
     */
    suspend fun setBatteryOptPrompted(prompted: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_BATTERY_OPT_PROMPTED] = prompted
        }
    }

    /**
     * Retrieves the ID of the alarm that is currently ringing, if any.
     *
     * @return The ID of the currently ringing alarm, or null if none is ringing.
     */
    suspend fun getCurrentlyRingingAlarmId(): Int? {
        val prefs = dataStore.data.first()
        return prefs[KEY_CURRENTLY_RINGING_ALARM_ID]
    }

    /**
     * Sets the ID of the alarm that is currently ringing.
     * Pass null to indicate that no alarm is ringing.
     *
     * @param id The ID of the alarm, or null to clear.
     */
    suspend fun setCurrentlyRingingAlarmId(id: Int?) {
        dataStore.edit { prefs ->
            if (id == null) {
                prefs.remove(KEY_CURRENTLY_RINGING_ALARM_ID)
            } else {
                prefs[KEY_CURRENTLY_RINGING_ALARM_ID] = id
            }
        }
    }
}
