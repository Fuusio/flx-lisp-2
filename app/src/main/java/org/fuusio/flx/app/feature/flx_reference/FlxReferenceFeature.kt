package org.fuusio.flx.app.feature.flx_reference

import android.content.Context
import org.fuusio.flx.app.FlxApplicationFeature
import org.fuusio.flx.app.feature.flx_reference.navigation.FlxReferenceNavKey
import org.fuusio.flx.app.feature.flx_reference.presentation.FlxReferenceProcessor
import org.fuusio.flx.app.feature.flx_reference.presentation.FlxReferenceViewState
import org.fuusio.kide.feature.KoinFeature
import org.fuusio.kide.navigation.ScreenNavKeyRegistry
import org.koin.dsl.module

object FlxReferenceFeature : KoinFeature {

    override fun initialize() {
        ScreenNavKeyRegistry.register(FlxReferenceNavKey)
    }

    override fun koinModule(applicationContext: Context) = module {

        includes(FlxApplicationFeature.koinModule(applicationContext))

        factory<FlxReferenceViewState> { FlxReferenceViewState() }

        factory { FlxReferenceProcessor(get()) }
    }
}
