package com.alarm.app.di

import com.alarm.app.data.repository.AlarmRepositoryImpl
import com.alarm.app.data.repository.QrCodeRepositoryImpl
import com.alarm.app.domain.repository.AlarmRepository
import com.alarm.app.domain.repository.QrCodeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger-Hilt module responsible for binding repository interfaces to their concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds the [AlarmRepositoryImpl] to the [AlarmRepository] interface.
     *
     * @param impl The concrete implementation of the repository.
     * @return The bound [AlarmRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindAlarmRepository(
        impl: AlarmRepositoryImpl
    ): AlarmRepository

    /**
     * Binds the [QrCodeRepositoryImpl] to the [QrCodeRepository] interface.
     *
     * @param impl The concrete implementation of the repository.
     * @return The bound [QrCodeRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindQrCodeRepository(
        impl: QrCodeRepositoryImpl
    ): QrCodeRepository
}
