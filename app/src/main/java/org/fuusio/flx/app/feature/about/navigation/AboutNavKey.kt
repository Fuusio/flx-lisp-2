package org.fuusio.flx.app.feature.about.navigation

import androidx.compose.runtime.Composable
import org.fuusio.flx.app.feature.about.presentation.AboutProcessor
import org.fuusio.flx.app.feature.about.ui.AboutScreen
import org.fuusio.kide.di.get
import org.fuusio.kide.navigation.ScreenContext
import org.fuusio.kide.navigation.ScreenNavKey

data object AboutNavKey : ScreenNavKey<AboutProcessor> {
    override val serialKey: String = "about"
    override fun createProcessor(): AboutProcessor = get()
    override val screen: @Composable ((ScreenContext<AboutProcessor>) -> Unit)
        get() = { ctx -> AboutScreen(ctx) }
}
