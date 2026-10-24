package com.openpos.guardian.core.di

import com.openpos.guardian.data.repository.AuditRepositoryImpl
import com.openpos.guardian.data.repository.DeviceInfoRepositoryImpl
import com.openpos.guardian.data.repository.KioskRepositoryImpl
import com.openpos.guardian.data.repository.LauncherRepositoryImpl
import com.openpos.guardian.data.repository.SettingsRepositoryImpl
import com.openpos.guardian.domain.repository.AuditRepository
import com.openpos.guardian.domain.repository.DeviceInfoRepository
import com.openpos.guardian.domain.repository.KioskRepository
import com.openpos.guardian.domain.repository.LauncherRepository
import com.openpos.guardian.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuditRepository(impl: AuditRepositoryImpl): AuditRepository

    @Binds
    @Singleton
    abstract fun bindDeviceInfoRepository(impl: DeviceInfoRepositoryImpl): DeviceInfoRepository

    @Binds
    @Singleton
    abstract fun bindKioskRepository(impl: KioskRepositoryImpl): KioskRepository

    @Binds
    @Singleton
    abstract fun bindLauncherRepository(impl: LauncherRepositoryImpl): LauncherRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
