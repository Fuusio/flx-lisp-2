package org.fuusio.flx.core

import org.fuusio.flx.FlxUnitTest
import org.fuusio.flx.core.error.BuiltinFailedException
import org.fuusio.flx.core.error.FlxException
import org.fuusio.flx.core.error.ParseException
import org.fuusio.flx.core.error.ResultTooLargeException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** A built-in given values it cannot work with fails with an FLX error that says what was wrong. */
class JavaErrorsTest : FlxUnitTest() {

    private fun failure(source: String): String =
        assertThrows(BuiltinFailedException::class.java) { eval(source) }.message

    @Test
    fun `an index out of range says the index and the size`() {
        assertEquals("'nth' failed: index -1 is out of range for a size of 3", failure("(nth [1 2 3] -1)"))
        assertEquals("'.get' failed: index 7 is out of range for a size of 3", failure("(.get [1 2 3] 7)"))
        assertEquals("'.charAt' failed: index 9 is out of range for a size of 3", failure("""(.charAt "abc" 9)"""))
        assertEquals("'removeAt' failed: index 5 is out of range for a size of 2", failure("(removeAt [1 2] 5)"))
        assertEquals("'.substring' failed: the range 2 to 99 is out of range for a length of 5", failure("""(.substring "hello" 2 99)"""))
    }

    @Test
    fun `text that is not a number or a pattern says so`() {
        assertEquals("'.toInt' failed: \"12x\" is not a number", failure("""(.toInt "12x")"""))
        assertTrue(failure("""(.matches "abc" "(")""").startsWith("'.matches' failed: \"(\" is not a valid regular expression"))
    }

    @Test
    fun `values a function cannot work with are named`() {
        assertEquals("'randInt' failed: bound must be greater than origin", failure("(randInt 5 1)"))
        assertTrue(failure("(clamp 5 10 1)").startsWith("'clamp' failed: cannot coerce value to an empty range"))
    }

    @Test
    fun `the error is an FLX error that try catches with its message`() {
        assertEquals("'nth' failed: index 9 is out of range for a size of 1", eval("(try (nth [1] 9) (catch err err))"))
        assertEquals("fallback", eval("""(try-or (.toInt "x") "fallback")"""))
        // As a value, too: the function called by another.
        assertThrows(FlxException::class.java) { eval("""(map .toInt ["1" "x"])""") }
    }

    @Test
    fun `FLX errors and return pass through unchanged`() {
        assertEquals("Divided by zero.", eval("(try (/ 1 0) (catch err err))"))
        assertEquals(2, eval("(fun f [] (map (fn [x] (return x)) [1]) 2) (f)"))
    }

    @Test
    fun `first and last of an empty string are nil, as of an empty array`() {
        assertEquals(Null, eval("""(.first "")"""))
        assertEquals(Null, eval("""(.last "")"""))
        assertEquals('a', eval("""(.first "abc")"""))
        assertEquals('c', eval("""(.last "abc")"""))
    }

    @Test
    fun `parse reports text that is not FLX, and gives nil for no form`() {
        assertThrows(ParseException::class.java) { eval("""(parse "(+ 1 2")""") }
        assertEquals(Null, eval("""(parse "")"""))
        assertEquals(3L, eval("""(eval (parse "(+ 1 2)"))"""))
    }

    @Test
    fun `a result too large to build is refused, not built`() {
        assertThrows(ResultTooLargeException::class.java) { eval("(range 0 1000000000)") }
        assertThrows(ResultTooLargeException::class.java) { eval("""(.repeat "ab" 1000000000)""") }
        assertEquals("ababab", eval("""(.repeat "ab" 3)"""))
        assertEquals("", eval("""(.repeat "ab" -1)"""))
        assertEquals(5, (eval("(range 5)") as org.fuusio.flx.core.type.Sequence).size())
    }
}
