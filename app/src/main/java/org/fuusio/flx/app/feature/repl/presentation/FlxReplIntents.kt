package org.fuusio.flx.app.feature.repl.presentation

import org.fuusio.flx.app.feature.repl.domain.Evaluation
import org.fuusio.kide.presentation.ViewIntent

sealed interface FlxReplIntent : ViewIntent

data class SourceChanged(val source: String) : FlxReplIntent
data object Run : FlxReplIntent
data object Reset : FlxReplIntent
data object ClearOutput : FlxReplIntent
data object Previous : FlxReplIntent
data object Next : FlxReplIntent

internal data class EvaluationFinished(val result: Evaluation) : FlxReplIntent
internal data class ResetFinished(val error: String?) : FlxReplIntent
