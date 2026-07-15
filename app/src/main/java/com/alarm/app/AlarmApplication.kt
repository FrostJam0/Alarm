package com.alarm.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The main application class for the Alarm app.
 *
 * This class is annotated with [HiltAndroidApp] to trigger Hilt's code generation,
 * including a base class for the application that serves as the application-level
 * dependency container. It acts as the entry point for the Dagger-Hilt dependency
 * injection setup.
 */
@HiltAndroidApp
class AlarmApplication : Application()
