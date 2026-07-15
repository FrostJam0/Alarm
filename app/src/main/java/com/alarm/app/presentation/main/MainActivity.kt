package com.alarm.app.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.alarm.app.presentation.navigation.AppNavGraph
import com.alarm.app.presentation.theme.AlarmTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * The main activity of the application.
 * Sets up the app theme, installs the splash screen, and initializes the main navigation graph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is starting.
     * Installs the splash screen and sets the main content using Jetpack Compose.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     * then this Bundle contains the data it most recently supplied.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        setContent {
            AlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph()
                }
            }
        }
    }
}
