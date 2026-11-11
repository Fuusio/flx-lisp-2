package org.fuusio.flx.app.feature.flx_reference.navigation

import androidx.compose.runtime.Composable
import org.fuusio.flx.app.feature.flx_reference.presentation.FlxReferenceProcessor
import org.fuusio.flx.app.feature.flx_reference.ui.FlxReferenceScreen
import org.fuusio.kide.di.get
import org.fuusio.kide.navigation.ScreenContext
import org.fuusio.kide.navigation.ScreenNavKey

data object FlxReferenceNavKey : ScreenNavKey<FlxReferenceProcessor> {
    override val serialKey: String = "flx_reference"
    override fun createProcessor(): FlxReferenceProcessor = get()
    override val screen: @Composable ((ScreenContext<FlxReferenceProcessor>) -> Unit)
        get() = { ctx -> FlxReferenceScreen(ctx) }
}
