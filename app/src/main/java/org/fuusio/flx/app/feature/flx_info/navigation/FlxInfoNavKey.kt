package org.fuusio.flx.app.feature.flx_info.navigation

import androidx.compose.runtime.Composable
import org.fuusio.flx.app.feature.flx_info.presentation.FlxInfoProcessor
import org.fuusio.flx.app.feature.flx_info.ui.FlxInfoScreen
import org.fuusio.kide.di.get
import org.fuusio.kide.navigation.ScreenContext
import org.fuusio.kide.navigation.ScreenNavKey

data object FlxInfoNavKey : ScreenNavKey<FlxInfoProcessor> {
    override val serialKey: String = "flx_info"
    override fun createProcessor(): FlxInfoProcessor = get()
    override val screen: @Composable ((ScreenContext<FlxInfoProcessor>) -> Unit)
        get() = { ctx -> FlxInfoScreen(ctx) }
}
