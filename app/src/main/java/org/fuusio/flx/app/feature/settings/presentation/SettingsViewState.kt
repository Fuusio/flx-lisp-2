package org.fuusio.flx.app.feature.settings.presentation

import org.fuusio.flx.app.domain.entity.ThemeMode
import org.fuusio.kide.presentation.ViewState

data class SettingsViewState(
    val themeMode: ThemeMode = ThemeMode.DEFAULT,
) : ViewState
