package org.fuusio.flx.app.feature.repl.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

import org.fuusio.flx.app.R

/**
 * A key of the Lisp REPL's symbol bar: [insertText] goes in at the caret, a pair wraps a selection
 * and, with no selection, leaves the caret [cursorOffsetFromStart] characters in.
 */
internal data class LispSymbol(
    val label: String,
    val insertText: String = label,
    val cursorOffsetFromStart: Int? = null,
    val isWrapPair: Boolean = false,
)

internal val LISP_SYMBOLS = listOf(
    LispSymbol("()", cursorOffsetFromStart = 1, isWrapPair = true),
    LispSymbol("[]", cursorOffsetFromStart = 1, isWrapPair = true),
    LispSymbol("{}", cursorOffsetFromStart = 1, isWrapPair = true),
    LispSymbol("\"", insertText = "\"\"", cursorOffsetFromStart = 1, isWrapPair = true),
    LispSymbol("'"),
    LispSymbol(":"),
    LispSymbol("."),
    LispSymbol(";"),
    LispSymbol("+"),
    LispSymbol("-"),
    LispSymbol("*"),
    LispSymbol("/"),
    LispSymbol("="),
    LispSymbol("<"),
    LispSymbol(">"),
    LispSymbol("!"),
    LispSymbol("⇥", insertText = "  "),
)

internal fun moveCaretLeft(current: TextFieldValue): TextFieldValue {
    val selection = current.selection
    val cursor = if (selection.collapsed) (selection.start - 1).coerceAtLeast(0) else selection.min
    return current.copy(selection = TextRange(cursor))
}

internal fun moveCaretRight(current: TextFieldValue): TextFieldValue {
    val selection = current.selection
    val cursor = if (selection.collapsed) (selection.end + 1).coerceAtMost(current.text.length) else selection.max
    return current.copy(selection = TextRange(cursor))
}

internal fun insertLispSymbol(current: TextFieldValue, symbol: LispSymbol): TextFieldValue {
    val text = current.text
    val start = current.selection.min
    val end = current.selection.max
    if (symbol.isWrapPair && start != end) {
        val wrapped = symbol.insertText.first() + text.substring(start, end) + symbol.insertText.last()
        return TextFieldValue(text.substring(0, start) + wrapped + text.substring(end), TextRange(start + wrapped.length))
    }
    val cursor = start + (symbol.cursorOffsetFromStart ?: symbol.insertText.length)
    return TextFieldValue(text.substring(0, start) + symbol.insertText + text.substring(end), TextRange(cursor))
}

@Composable
internal fun LispSymbolBar(
    onSymbolClick: (LispSymbol) -> Unit,
    onCaretLeft: () -> Unit,
    onCaretRight: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SymbolKey(onCaretLeft, enabled) {
            Icon(painterResource(R.drawable.arrow_back_24px), stringResource(R.string.repl_caret_left), Modifier.size(18.dp))
        }
        SymbolKey(onCaretRight, enabled) {
            Icon(painterResource(R.drawable.arrow_forward_24px), stringResource(R.string.repl_caret_right), Modifier.size(18.dp))
        }
        LISP_SYMBOLS.forEach { symbol ->
            SymbolKey({ onSymbolClick(symbol) }, enabled) {
                Text(
                    symbol.label,
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Composable
private fun SymbolKey(onClick: () -> Unit, enabled: Boolean, content: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.defaultMinSize(minWidth = 36.dp, minHeight = 32.dp),
    ) {
        Box(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), contentAlignment = Alignment.Center) { content() }
    }
}
