package org.fuusio.flx.core

import org.fuusio.flx.FlxUnitTest
import org.fuusio.flx.core.error.FlxException
import org.fuusio.flx.core.type.Sequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/** `and` and `or` stop at the argument that decides them; `if` takes an else branch. */
class ShortCircuitAndIfTest : FlxUnitTest() {

    @Test
    fun `and stops at the first false argument`() {
        assertEquals(false, eval("(and false (/ 1 0))"))
        assertEquals(false, eval("(val xs []) (and (isNotEmpty xs) (> (first xs) 0))"))
        assertEquals(2L, eval("(var n 0) (and (do (++ n) true) (do (++ n) nil) (do (++ n) true)) n"))
    }

    @Test
    fun `or stops at the first true argument`() {
        assertEquals(true, eval("(or true (/ 1 0))"))
        assertEquals(2L, eval("(var n 0) (or (do (++ n) nil) (do (++ n) 7) (do (++ n) true)) n"))
    }

    @Test
    fun `and and or still evaluate what they need, and answer as before`() {
        assertThrows(FlxException::class.java) { eval("(and true (/ 1 0))") }
        assertThrows(FlxException::class.java) { eval("(or false (/ 1 0))") }
        assertEquals(true, eval("(and)"))
        assertEquals(false, eval("(or)"))
        assertEquals(true, eval("(and 1 \"a\" true)"))
        assertEquals(false, eval("(and 1 nil 3)"))
        assertEquals(true, eval("(or nil 0)"))
        assertEquals(false, eval("(or nil false)"))
    }

    @Test
    fun `and and or are still functions as values`() {
        assertEquals(false, eval("(reduce and [true true false])"))
        val mapped = eval("(map and [true false] [true true])") as Sequence
        assertEquals(listOf<Any>(true, false), (0 until mapped.size()).map { mapped[it] })
    }

    @Test
    fun `if takes an else branch`() {
        assertEquals(1, eval("(if true 1 2)"))
        assertEquals(2, eval("(if false 1 2)"))
        assertEquals("posnot", eval("""(fun f [x] (if (> x 0) "pos" "not")) (str (f 1) (f -1))"""))
        // Only the chosen branch is evaluated.
        assertEquals(1, eval("(if true 1 (/ 1 0))"))
        assertEquals(1, eval("(if true 1)"))
        // Without an else branch a false condition gives nil, as `when` does.
        assertEquals(Null, eval("(if false 1)"))
        assertEquals("fallback", eval("""(?: (if false 1) "fallback")"""))
        assertThrows(FlxException::class.java) { eval("(if true 1 2 3)") }
    }
}
