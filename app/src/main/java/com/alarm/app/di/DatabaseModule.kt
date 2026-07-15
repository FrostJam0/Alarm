package com.alarm.app.di

import android.content.Context
import androidx.room.Room
import com.alarm.app.data.db.AlarmDao
import com.alarm.app.data.db.AlarmDatabase
import com.alarm.app.data.db.QrCodeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger-Hilt module responsible for providing Room database and Data Access Object (DAO) instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides a singleton instance of the [AlarmDatabase].
     *
     * @param context The application context provided by Hilt.
     * @return The initialized Room [AlarmDatabase] instance.
     */
    @Provides
    @Singleton
    fun provideAlarmDatabase(
        @ApplicationContext context: Context
    ): AlarmDatabase {
        return Room.databaseBuilder(
            context,
            AlarmDatabase::class.java,
            "alarm.db"
        ).build()
    }

    /**
     * Provides a singleton instance of [AlarmDao] for accessing alarm-related data.
     *
     * @param database The [AlarmDatabase] instance.
     * @return The [AlarmDao] extracted from the database.
     */
    @Provides
    @Singleton
    fun provideAlarmDao(database: AlarmDatabase): AlarmDao {
        return database.alarmDao()
    }

    /**
     * Provides a singleton instance of [QrCodeDao] for accessing QR code-related data.
     *
     * @param database The [AlarmDatabase] instance.
     * @return The [QrCodeDao] extracted from the database.
     */
    @Provides
    @Singleton
    fun provideQrCodeDao(database: AlarmDatabase): QrCodeDao {
        return database.qrCodeDao()
    }
}
