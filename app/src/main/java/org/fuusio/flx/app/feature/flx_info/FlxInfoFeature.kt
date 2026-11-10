package org.fuusio.flx.app.feature.flx_info

import android.content.Context
import org.fuusio.flx.app.FlxApplicationFeature
import org.fuusio.flx.app.feature.flx_info.navigation.FlxInfoNavKey
import org.fuusio.flx.app.feature.flx_info.presentation.FlxInfoProcessor
import org.fuusio.flx.app.feature.flx_info.presentation.FlxInfoViewState
import org.fuusio.kide.feature.KoinFeature
import org.fuusio.kide.navigation.ScreenNavKeyRegistry
import org.koin.dsl.module

object FlxInfoFeature : KoinFeature {

    override fun initialize() {
        ScreenNavKeyRegistry.register(FlxInfoNavKey)
    }

    override fun koinModule(applicationContext: Context) = module {

        includes(FlxApplicationFeature.koinModule(applicationContext))

        factory<FlxInfoViewState> { FlxInfoViewState() }

        factory { FlxInfoProcessor(get()) }
    }
}
