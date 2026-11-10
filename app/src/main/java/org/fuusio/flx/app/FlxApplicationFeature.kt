package org.fuusio.flx.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.fuusio.flx.app.adapter.app_info.AppInfoDataSource
import org.fuusio.flx.app.adapter.app_info.AppInfoRepositoryImpl
import org.fuusio.flx.app.adapter.settings.SettingsRepositoryImpl
import org.fuusio.flx.app.adapter.values.ValuesDataSource
import org.fuusio.flx.app.domain.adapter.app_info.AppInfoRepository
import org.fuusio.flx.app.domain.adapter.settings.SettingsRepository
import org.fuusio.flx.app.domain.usecase.GetAppVersionUseCase
import org.fuusio.flx.app.domain.usecase.GetThemeModeFlowUseCase
import org.fuusio.flx.app.domain.usecase.GetThemeModeUseCase
import org.fuusio.flx.app.domain.usecase.SetThemeModeUseCase
import org.fuusio.flx.app.framework.app_info.AppInfoDataSourceImpl
import org.fuusio.flx.app.framework.values.ValuesDataSourceImpl
import org.fuusio.kide.feature.KoinApplicationFeature
import org.koin.core.module.Module
import org.koin.dsl.module

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flx")

/** The application-wide dependencies: data sources, repositories and use cases shared by features. */
object FlxApplicationFeature : KoinApplicationFeature {

    override fun koinModule(applicationContext: Context): Module = module {

        // Framework

        single<DataStore<Preferences>> { applicationContext.dataStore }
        single<ValuesDataSource> { ValuesDataSourceImpl(get()) }
        single<AppInfoDataSource> { AppInfoDataSourceImpl() }

        // Adapters

        single<SettingsRepository> { SettingsRepositoryImpl(get()) }
        single<AppInfoRepository> { AppInfoRepositoryImpl(get()) }

        // Use cases

        factory<GetAppVersionUseCase> {
            GetAppVersionUseCase { get<AppInfoRepository>().getVersion() }
        }
        factory<GetThemeModeUseCase> {
            GetThemeModeUseCase { get<SettingsRepository>().getThemeMode() }
        }
        factory<GetThemeModeFlowUseCase> {
            GetThemeModeFlowUseCase { get<SettingsRepository>().themeMode }
        }
        factory<SetThemeModeUseCase> {
            SetThemeModeUseCase { themeMode -> get<SettingsRepository>().setThemeMode(themeMode) }
        }
    }
}
