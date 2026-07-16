package com.alarm.app.presentation.ringing

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.alarm.app.data.datastore.AppPreferencesDataStore
import com.alarm.app.service.AlarmService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The Activity presented to the user when an alarm is actively ringing.
 *
 * This activity handles keeping the screen on, showing over the lock screen,
 * and intercepting hardware buttons to prevent accidental dismissal of the alarm
 * without completing the required QR code scan.
 */
@AndroidEntryPoint
class RingingActivity : ComponentActivity() {

    private val viewModel: RingingViewModel by viewModels()

    @Inject
    lateinit var dataStore: AppPreferencesDataStore

    /** Tracks whether the alarm is being legitimately dismissed. */
    private var isDismissing = false

    /** The alarm ID extracted from the launching intent. */
    private var alarmId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }

        super.onCreate(savedInstanceState)

        alarmId = intent?.getIntExtra(
            com.alarm.app.core.constants.AlarmConstants.EXTRA_ALARM_ID, -1
        ) ?: -1

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RingingScreen(
                        viewModel = viewModel,
                        onDismiss = {
                            stopAlarmAndFinish()
                        }
                    )
                }
            }
        }
    }

    private fun stopAlarmAndFinish() {
        isDismissing = true

        val stopIntent = Intent(this, AlarmService::class.java)
        stopService(stopIntent)

        lifecycleScope.launch {
            dataStore.setCurrentlyRingingAlarmId(null)
            finish()
        }
    }

    /**
     * Called when the user presses Home or opens Recents.
     * Re-launches this activity so the alarm screen can't be escaped.
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isDismissing) {
            relaunchSelf()
        }
    }

    /**
     * Secondary safety net: if anything pulls this activity off-screen
     * (e.g. incoming call overlay, system dialog) and the alarm hasn't
     * been dismissed, re-launch immediately.
     */
    override fun onStop() {
        super.onStop()
        if (!isDismissing) {
            relaunchSelf()
        }
    }

    private fun relaunchSelf() {
        val relaunchIntent = Intent(this, RingingActivity::class.java).apply {
            putExtra(com.alarm.app.core.constants.AlarmConstants.EXTRA_ALARM_ID, alarmId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(relaunchIntent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN -> true
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN -> true
            else -> super.onKeyUp(keyCode, event)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // No-op to prevent exiting without scanning
    }
}
