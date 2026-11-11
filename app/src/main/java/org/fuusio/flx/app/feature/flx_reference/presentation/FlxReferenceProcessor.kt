package org.fuusio.flx.app.feature.flx_reference.presentation

import org.fuusio.flx.app.domain.usecase.GetFlxReferenceHtmlUseCase
import org.fuusio.kide.di.get
import org.fuusio.kide.presentation.Action
import org.fuusio.kide.presentation.PresentationProcessor
import org.fuusio.kide.presentation.SideEffect
import org.fuusio.kide.presentation.useCase
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

class FlxReferenceProcessor(initialState: FlxReferenceViewState) :
    PresentationProcessor<FlxReferenceIntent, FlxReferenceViewState, SideEffect>(initialState) {

    override suspend fun map(intent: FlxReferenceIntent): Action<FlxReferenceViewState, SideEffect>? =
        when (intent) {
            is FlxReferenceScreenLaunched -> useCase {
                val html = try {
                    get<GetFlxReferenceHtmlUseCase>()()
                } catch (error: Exception) {
                    if (error is CancellationException) throw error
                    Timber.w(error, "The FLX reference could not be read")
                    null
                }
                reduce { copy(html = html, hasFailed = html == null) }
            }
        }
}
