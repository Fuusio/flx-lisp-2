package org.fuusio.flx.app.feature.repl.presentation

import org.fuusio.kide.presentation.ViewState

enum class TranscriptKind { INPUT, OUTPUT, ERROR, CONSOLE }

data class TranscriptEntry(val kind: TranscriptKind, val text: String)

data class FlxReplViewState(
    val source: String = "",
    val running: Boolean = false,
    val transcript: List<TranscriptEntry> = emptyList(),
    val history: List<String> = emptyList(),
    val historyPosition: Int = 0,
    val historyDraft: String = "",
) : ViewState
