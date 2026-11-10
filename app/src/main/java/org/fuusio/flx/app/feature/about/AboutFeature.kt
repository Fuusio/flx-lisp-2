package org.fuusio.flx.app.feature.about

import android.content.Context
import org.fuusio.flx.app.FlxApplicationFeature
import org.fuusio.flx.app.feature.about.navigation.AboutNavKey
import org.fuusio.flx.app.feature.about.presentation.AboutProcessor
import org.fuusio.flx.app.feature.about.presentation.AboutViewState
import org.fuusio.kide.feature.KoinFeature
import org.fuusio.kide.navigation.ScreenNavKeyRegistry
import org.koin.dsl.module

object AboutFeature : KoinFeature {

    override fun initialize() {
        ScreenNavKeyRegistry.register(AboutNavKey)
    }

    override fun koinModule(applicationContext: Context) = module {

        includes(FlxApplicationFeature.koinModule(applicationContext))

        factory<AboutViewState> { AboutViewState() }

        factory { AboutProcessor(get()) }
    }
}
