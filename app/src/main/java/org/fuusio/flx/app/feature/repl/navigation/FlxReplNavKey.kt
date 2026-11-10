package org.fuusio.flx.app.feature.repl.navigation

import androidx.compose.runtime.Composable
import org.fuusio.flx.app.feature.repl.presentation.FlxReplProcessor
import org.fuusio.flx.app.feature.repl.ui.FlxReplScreen
import org.fuusio.kide.di.get
import org.fuusio.kide.navigation.ScreenContext
import org.fuusio.kide.navigation.ScreenNavKey

data object FlxReplNavKey : ScreenNavKey<FlxReplProcessor> {
    override val serialKey: String = "flx_repl"
    override fun createProcessor(): FlxReplProcessor = get()
    override val screen: @Composable ((ScreenContext<FlxReplProcessor>) -> Unit)
        get() = { ctx -> FlxReplScreen(ctx) }
}
