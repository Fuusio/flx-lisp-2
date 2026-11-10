package org.fuusio.flx.app.feature.settings.presentation

import org.fuusio.flx.app.domain.usecase.GetThemeModeUseCase
import org.fuusio.flx.app.domain.usecase.SetThemeModeUseCase
import org.fuusio.kide.di.get
import org.fuusio.kide.presentation.Action
import org.fuusio.kide.presentation.PresentationProcessor
import org.fuusio.kide.presentation.SideEffect
import org.fuusio.kide.presentation.useCase

class SettingsProcessor(initialState: SettingsViewState) :
    PresentationProcessor<SettingsIntent, SettingsViewState, SideEffect>(initialState) {

    override suspend fun map(intent: SettingsIntent): Action<SettingsViewState, SideEffect>? =
        when (intent) {
            is SettingsScreenLaunched -> useCase {
                val themeMode = get<GetThemeModeUseCase>()()
                reduce { copy(themeMode = themeMode) }
            }

            is ThemeModeSelected -> useCase {
                get<SetThemeModeUseCase>()(intent.themeMode)
                reduce { copy(themeMode = intent.themeMode) }
            }
        }
}
