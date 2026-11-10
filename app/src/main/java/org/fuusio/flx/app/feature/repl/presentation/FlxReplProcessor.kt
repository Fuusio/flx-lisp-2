package org.fuusio.flx.app.feature.repl.presentation

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.fuusio.flx.app.feature.repl.domain.Evaluation
import org.fuusio.flx.app.feature.repl.domain.FlxRuntime
import org.fuusio.kide.presentation.Action
import org.fuusio.kide.presentation.PresentationProcessor
import org.fuusio.kide.presentation.SideEffect
import org.fuusio.kide.presentation.reduce

class FlxReplProcessor(
    private val runtime: FlxRuntime,
    private val preferences: SharedPreferences,
) : PresentationProcessor<FlxReplIntent, FlxReplViewState, SideEffect>(
    FlxReplViewState(source = preferences.getString(KEY_DRAFT, "") ?: ""),
) {
    private fun saveDraft(source: String) {
        preferences.edit { putString(KEY_DRAFT, source) }
    }

    override suspend fun map(intent: FlxReplIntent): Action<FlxReplViewState, SideEffect>? = when (intent) {
        is SourceChanged -> {
            saveDraft(intent.source)
            reduce { copy(source = intent.source, historyPosition = history.size, historyDraft = intent.source) }
        }
        ClearOutput -> reduce { copy(transcript = emptyList()) }
        Previous -> {
            if (state.running || state.historyPosition == 0) null else {
                val index = state.historyPosition - 1
                val draft = if (state.historyPosition == state.history.size) state.source else state.historyDraft
                val source = state.history[index]
                saveDraft(source)
                reduce { copy(source = source, historyPosition = index, historyDraft = draft) }
            }
        }
        Next -> {
            if (state.running || state.historyPosition >= state.history.size) null else {
                val index = state.historyPosition + 1
                val source = state.history.getOrNull(index) ?: state.historyDraft
                saveDraft(source)
                reduce { copy(source = source, historyPosition = index) }
            }
        }
        Run -> {
            // Reserve the operation in the sequential intent loop, before launching work. Otherwise,
            // rapid Run/Reset intents can both pass a busy check in separate async actions.
            val source = state.source
            if (state.running || source.isBlank()) null else {
                saveDraft("")
                processorScope.launch {
                    val result = try {
                        runtime.evaluate(source)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Evaluation("", error = e.message ?: "Execution failed")
                    }
                    dispatch(EvaluationFinished(result))
                }
                reduce {
                    val commands = (history + source).takeLast(MAX_HISTORY)
                    copy(
                        source = "", running = true, history = commands, historyPosition = commands.size,
                        historyDraft = "", transcript = transcript + TranscriptEntry(TranscriptKind.INPUT, "> $source"),
                    )
                }
            }
        }
        Reset -> if (state.running) null else {
            processorScope.launch {
                try {
                    runtime.reset()
                    dispatch(ResetFinished(null))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    dispatch(ResetFinished(e.message ?: "Reset failed"))
                }
            }
            reduce { copy(running = true) }
        }
        is EvaluationFinished -> reduce {
            val entries = buildList {
                if (intent.result.output.isNotEmpty()) {
                    add(TranscriptEntry(TranscriptKind.CONSOLE, intent.result.output.trimEnd('\n', '\r')))
                }
                intent.result.error?.let { add(TranscriptEntry(TranscriptKind.ERROR, "⇨ Error: $it")) }
                    ?: intent.result.result?.let { add(TranscriptEntry(TranscriptKind.OUTPUT, "⇨ $it")) }
            }
            copy(running = false, transcript = transcript + entries)
        }
        is ResetFinished -> {
            if (intent.error == null) {
                reduce { copy(running = false, transcript = emptyList(), history = emptyList(), historyPosition = 0, historyDraft = source) }
            } else {
                reduce { copy(running = false, transcript = transcript + TranscriptEntry(TranscriptKind.ERROR, "⇨ Error: ${intent.error}")) }
            }
        }
    }

    // A private session belongs to this processor; do not retain it after the navigation host closes.
    init {
        processorScope.coroutineContext[Job]?.invokeOnCompletion {
            CoroutineScope(Dispatchers.IO).launch { runtime.reset() }
        }
    }

    private companion object {
        const val KEY_DRAFT = "draft"
        const val MAX_HISTORY = 64
    }
}
