package org.fuusio.flx.app.feature.settings.navigation

import androidx.compose.runtime.Composable
import org.fuusio.flx.app.feature.settings.presentation.SettingsProcessor
import org.fuusio.flx.app.feature.settings.ui.SettingsScreen
import org.fuusio.kide.di.get
import org.fuusio.kide.navigation.ScreenContext
import org.fuusio.kide.navigation.ScreenNavKey

/**
 * @param showsBackNavigation Whether the screen offers back navigation rather than the navigation
 *   drawer. The drawer reaches this screen as a destination in its own right and leaves this
 *   `false`; a screen that sends the user here to change something passes `true`, so the user
 *   lands back where they came from.
 */
data class SettingsNavKey(
    val showsBackNavigation: Boolean = false,
) : ScreenNavKey<SettingsProcessor> {
    override val serialKey: String = "settings"
    override fun createProcessor(): SettingsProcessor = get()
    override val screen: @Composable ((ScreenContext<SettingsProcessor>) -> Unit)
        get() = { ctx -> SettingsScreen(ctx, showsBackNavigation = showsBackNavigation) }

    override fun saveArgs(): String = showsBackNavigation.toString()

    override fun restoreArgs(args: String): ScreenNavKey<SettingsProcessor> =
        copy(showsBackNavigation = args.toBoolean())
}
