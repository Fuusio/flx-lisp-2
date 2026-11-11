package org.fuusio.flx.app.feature.repl.ui

import android.content.ClipData
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.fuusio.flx.app.R
import org.fuusio.flx.app.feature.repl.presentation.ClearOutput
import org.fuusio.flx.app.feature.repl.presentation.FlxReplIntent
import org.fuusio.flx.app.feature.repl.presentation.FlxReplProcessor
import org.fuusio.flx.app.feature.repl.presentation.FlxReplViewState
import org.fuusio.flx.app.feature.repl.presentation.Next
import org.fuusio.flx.app.feature.repl.presentation.Previous
import org.fuusio.flx.app.feature.repl.presentation.Reset
import org.fuusio.flx.app.feature.repl.presentation.Run
import org.fuusio.flx.app.feature.repl.presentation.SourceChanged
import org.fuusio.flx.app.feature.repl.presentation.TranscriptEntry
import org.fuusio.flx.app.feature.repl.presentation.TranscriptKind
import org.fuusio.flx.app.ui.theme.FlxTheme
import org.fuusio.kide.navigation.ScreenContext

/** A few one-liners to try, shown while the transcript is empty. */
private val EXAMPLES = listOf(
    "(+ 1 2 3)",
    "(fun square [x] (* x x))",
    "(map square [1 2 3 4])",
    "(println \"Hello, FLX!\")",
)

@Composable
fun FlxReplScreen(ctx: ScreenContext<FlxReplProcessor>) {
    FlxReplScreenContent(
        state = ctx.processor.states.collectAsStateWithLifecycle().value,
        onMenu = ctx.callback(ScreenContext.MENU),
        onDispatch = ctx.processor::dispatch,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlxReplScreenContent(
    state: FlxReplViewState,
    onMenu: () -> Unit,
    onDispatch: (FlxReplIntent) -> Unit,
) {
    val listState = rememberLazyListState()
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    // The field's text is the state's source; the selection is only the screen's, for the symbol bar.
    var field by remember { mutableStateOf(TextFieldValue(state.source, TextRange(state.source.length))) }
    LaunchedEffect(state.source) {
        if (state.source != field.text) field = TextFieldValue(state.source, TextRange(state.source.length))
    }
    LaunchedEffect(state.transcript.size) {
        if (state.transcript.isNotEmpty()) listState.animateScrollToItem(state.transcript.lastIndex)
    }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.repl_title)) },
            navigationIcon = {
                IconButton(onClick = onMenu) {
                    Icon(painterResource(R.drawable.menu_24px), stringResource(R.string.open_menu))
                }
            },
        )
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LazyColumn(Modifier.fillMaxWidth().weight(1f), state = listState) {
                if (state.transcript.isEmpty()) {
                    item(key = "welcome") {
                        ReplWelcome(onExample = { onDispatch(SourceChanged(it)) })
                    }
                }
                items(state.transcript) { entry ->
                    val color = when (entry.kind) {
                        TranscriptKind.INPUT -> MaterialTheme.colorScheme.primary
                        TranscriptKind.OUTPUT -> MaterialTheme.colorScheme.tertiary
                        TranscriptKind.ERROR -> MaterialTheme.colorScheme.error
                        TranscriptKind.CONSOLE -> Color.Unspecified
                    }
                    Text(
                        entry.text,
                        color = color,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            OutlinedTextField(
                value = field,
                onValueChange = { field = it; onDispatch(SourceChanged(it.text)) },
                modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp).onPreviewKeyEvent { event ->
                    if (event.key == Key.Enter && !event.isShiftPressed && !event.isCtrlPressed &&
                        !event.isAltPressed && !event.isMetaPressed
                    ) {
                        if (event.type == KeyEventType.KeyDown && !state.running) onDispatch(Run)
                        true
                    } else {
                        false
                    }
                },
                label = { Text(stringResource(R.string.repl_hint_input)) },
                trailingIcon = if (state.source.isNotEmpty()) {
                    {
                        IconButton(onClick = { onDispatch(SourceChanged("")) }) {
                            Icon(
                                painter = painterResource(R.drawable.close_24px),
                                contentDescription = stringResource(R.string.repl_clear_input),
                            )
                        }
                    }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (!state.running) onDispatch(Run) }),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            )
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Button(onClick = { onDispatch(Run) }, enabled = !state.running && state.source.isNotBlank()) {
                    Text(stringResource(R.string.repl_run))
                }
                IconButton(
                    onClick = { onDispatch(Previous) },
                    enabled = !state.running && state.historyPosition > 0,
                ) {
                    Icon(painterResource(R.drawable.arrow_upward_24px), stringResource(R.string.repl_previous))
                }
                IconButton(
                    onClick = { onDispatch(Next) },
                    enabled = !state.running && state.historyPosition < state.history.size,
                ) {
                    Icon(painterResource(R.drawable.arrow_downward_24px), stringResource(R.string.repl_next))
                }
                IconButton(onClick = { onDispatch(ClearOutput) }) {
                    Icon(painterResource(R.drawable.delete_24px), stringResource(R.string.repl_clear))
                }
                IconButton(onClick = { onDispatch(Reset) }, enabled = !state.running) {
                    Icon(painterResource(R.drawable.restart_alt_24px), stringResource(R.string.repl_reset))
                }
                IconButton(
                    onClick = {
                        val text = state.transcript.joinToString("\n") { it.text }
                        scope.launch {
                            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("FLX console", text)))
                        }
                    },
                    enabled = state.transcript.isNotEmpty(),
                ) {
                    Icon(painterResource(R.drawable.content_copy_24px), stringResource(R.string.repl_copy_console))
                }
            }
            LispSymbolBar(
                onSymbolClick = { symbol ->
                    field = insertLispSymbol(field, symbol)
                    onDispatch(SourceChanged(field.text))
                },
                onCaretLeft = { field = moveCaretLeft(field) },
                onCaretRight = { field = moveCaretRight(field) },
                enabled = !state.running,
            )
            if (state.running) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ReplWelcome(onExample: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.repl_welcome), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.repl_examples_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EXAMPLES.forEach { example ->
                SuggestionChip(
                    onClick = { onExample(example) },
                    label = { Text(example, fontFamily = FontFamily.Monospace) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyReplPreview() = FlxTheme { FlxReplScreenContent(FlxReplViewState(), {}, {}) }

@Preview(showBackground = true)
@Composable
private fun ReplPreview() = FlxTheme {
    FlxReplScreenContent(
        FlxReplViewState(
            transcript = listOf(
                TranscriptEntry(TranscriptKind.INPUT, "> (* 6 7)"),
                TranscriptEntry(TranscriptKind.OUTPUT, "⇨ 42"),
            ),
        ),
        {},
        {},
    )
}
