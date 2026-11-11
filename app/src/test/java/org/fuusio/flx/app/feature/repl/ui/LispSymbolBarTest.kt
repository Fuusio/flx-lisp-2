package org.fuusio.flx.app.feature.repl.ui

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class LispSymbolBarTest {

    private fun key(label: String) = LISP_SYMBOLS.first { it.label == label }

    @Test
    fun `a pair is inserted with the caret inside`() {
        val result = insertLispSymbol(TextFieldValue("(+ 1 ", TextRange(5)), key("()"))
        assertEquals("(+ 1 ()", result.text)
        assertEquals(TextRange(6), result.selection)
    }

    @Test
    fun `a pair wraps the selection`() {
        val result = insertLispSymbol(TextFieldValue("a b c", TextRange(2, 3)), key("[]"))
        assertEquals("a [b] c", result.text)
        assertEquals(TextRange(5), result.selection)
    }

    @Test
    fun `quotes get the caret between them and plain symbols advance it`() {
        assertEquals(TextRange(1), insertLispSymbol(TextFieldValue("", TextRange(0)), key("\"")).selection)
        val result = insertLispSymbol(TextFieldValue("(x", TextRange(2)), key(":"))
        assertEquals("(x:", result.text)
        assertEquals(TextRange(3), result.selection)
    }

    @Test
    fun `the caret moves one step and stops at the ends`() {
        assertEquals(TextRange(0), moveCaretLeft(TextFieldValue("abc", TextRange(0))).selection)
        assertEquals(TextRange(1), moveCaretLeft(TextFieldValue("abc", TextRange(2))).selection)
        assertEquals(TextRange(3), moveCaretRight(TextFieldValue("abc", TextRange(3))).selection)
        assertEquals(TextRange(2), moveCaretRight(TextFieldValue("abc", TextRange(1, 2))).selection)
    }
}
