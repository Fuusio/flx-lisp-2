package org.fuusio.flx.app.feature.repl

import android.content.Context
import org.fuusio.flx.app.feature.repl.data.LocalFlxRuntime
import org.fuusio.flx.app.feature.repl.domain.FlxRuntime
import org.fuusio.flx.app.feature.repl.navigation.FlxReplNavKey
import org.fuusio.flx.app.feature.repl.presentation.FlxReplProcessor
import org.fuusio.kide.feature.KoinFeature
import org.fuusio.kide.navigation.ScreenNavKeyRegistry
import org.koin.dsl.module

object FlxReplFeature : KoinFeature {

    override fun initialize() {
        ScreenNavKeyRegistry.register(FlxReplNavKey)
    }

    override fun koinModule(applicationContext: Context) = module {
        // Each REPL screen gets a private session of its own.
        factory<FlxRuntime> { LocalFlxRuntime() }
        factory {
            FlxReplProcessor(
                runtime = get(),
                preferences = applicationContext.getSharedPreferences("flx_repl", Context.MODE_PRIVATE),
            )
        }
    }
}
