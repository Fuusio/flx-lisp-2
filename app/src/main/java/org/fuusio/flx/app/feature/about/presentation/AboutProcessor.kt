package org.fuusio.flx.app.feature.about.presentation

import org.fuusio.flx.app.domain.usecase.GetAppVersionUseCase
import org.fuusio.kide.di.get
import org.fuusio.kide.presentation.Action
import org.fuusio.kide.presentation.PresentationProcessor
import org.fuusio.kide.presentation.SideEffect
import org.fuusio.kide.presentation.useCase

class AboutProcessor(initialState: AboutViewState) :
    PresentationProcessor<AboutIntent, AboutViewState, SideEffect>(initialState) {

    override suspend fun map(intent: AboutIntent): Action<AboutViewState, SideEffect>? =
        when (intent) {
            is AboutScreenLaunched -> useCase {
                val version = get<GetAppVersionUseCase>()()
                reduce { copy(version = version) }
            }
        }
}
