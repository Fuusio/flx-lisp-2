package org.fuusio.flx.app.feature.settings.presentation

import org.fuusio.flx.app.domain.entity.ThemeMode
import org.fuusio.kide.presentation.ViewIntent

sealed interface SettingsIntent : ViewIntent

data object SettingsScreenLaunched : SettingsIntent

data class ThemeModeSelected(val themeMode: ThemeMode) : SettingsIntent
