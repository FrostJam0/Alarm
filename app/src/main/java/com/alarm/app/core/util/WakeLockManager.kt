package com.alarm.app.core.util

import android.content.Context
import android.os.PowerManager

/**
 * A singleton utility object for managing CPU wake locks.
 *
 * This ensures that the device's CPU stays awake while critical alarm operations
 * (like starting the alarm service and ringing) are being executed, even if the screen is off.
 */
object WakeLockManager {
    private var wakeLock: PowerManager.WakeLock? = null

    /**
     * Acquires a partial wake lock if one is not already held.
     *
     * The wake lock is acquired with a timeout of 10 minutes to prevent battery drain
     * in case [release] is never called due to an error.
     *
     * @param context The context used to access the [PowerManager] system service.
     */
    fun acquire(context: Context) {
        if (wakeLock == null) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "AlarmApp::AlarmWakeLock"
            )
        }
        if (wakeLock?.isHeld == false) {
            wakeLock?.acquire(10 * 60 * 1000L /*10 minutes*/)
        }
    }

    /**
     * Releases the currently held wake lock, allowing the CPU to go back to sleep.
     */
    fun release() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }
}
