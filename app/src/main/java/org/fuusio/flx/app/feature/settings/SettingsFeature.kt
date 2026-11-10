package org.fuusio.flx.app.feature.settings

import android.content.Context
import org.fuusio.flx.app.FlxApplicationFeature
import org.fuusio.flx.app.feature.settings.navigation.SettingsNavKey
import org.fuusio.flx.app.feature.settings.presentation.SettingsProcessor
import org.fuusio.flx.app.feature.settings.presentation.SettingsViewState
import org.fuusio.kide.feature.KoinFeature
import org.fuusio.kide.navigation.ScreenNavKeyRegistry
import org.koin.dsl.module

object SettingsFeature : KoinFeature {

    override fun initialize() {
        ScreenNavKeyRegistry.register(SettingsNavKey())
    }

    override fun koinModule(applicationContext: Context) = module {

        includes(FlxApplicationFeature.koinModule(applicationContext))

        factory<SettingsViewState> { SettingsViewState() }

        factory { SettingsProcessor(get()) }
    }
}
