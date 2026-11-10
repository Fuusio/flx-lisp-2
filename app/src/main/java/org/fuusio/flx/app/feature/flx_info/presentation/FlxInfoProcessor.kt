package org.fuusio.flx.app.feature.flx_info.presentation

import org.fuusio.flx.app.domain.usecase.GetAppVersionUseCase
import org.fuusio.kide.di.get
import org.fuusio.kide.presentation.Action
import org.fuusio.kide.presentation.PresentationProcessor
import org.fuusio.kide.presentation.SideEffect
import org.fuusio.kide.presentation.useCase

class FlxInfoProcessor(initialState: FlxInfoViewState) :
    PresentationProcessor<FlxInfoIntent, FlxInfoViewState, SideEffect>(initialState) {

    override suspend fun map(intent: FlxInfoIntent): Action<FlxInfoViewState, SideEffect>? =
        when (intent) {
            is FlxInfoScreenLaunched -> useCase {
                val version = get<GetAppVersionUseCase>()()
                reduce { copy(version = version) }
            }
        }
}
