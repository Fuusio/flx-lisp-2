package org.fuusio.flx.core

import org.fuusio.flx.FlxUnitTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** `e` and `pi` have values, and are still names a program may use for its own. */
class ConstantNamesTest : FlxUnitTest() {

    @Test
    fun `e and pi are the constants where the program does not say otherwise`() {
        assertEquals(Math.PI * 2, eval("(* 2 pi)"))
        assertEquals(Math.E, eval("e"))
        assertEquals(Math.E, eval("(fun f [] e) (f)"))
    }

    @Test
    fun `catch e is the error`() {
        assertEquals("caught: Divided by zero.", eval("""(try (/ 1 0) (catch e (str "caught: " e)))"""))
    }

    @Test
    fun `a parameter, a let or a val named e or pi is the program's`() {
        assertEquals(8L, eval("(fun f [e] (* e 2)) (f 4)"))
        assertEquals(3, eval("(let [e 3] e)"))
        assertEquals(6L, eval("(val pi 5) (+ pi 1)"))
        assertEquals(6L, eval("(var e 1) (= e 5) (+ e 1)"))
        assertEquals(6L, eval("(var total 0) (forEach e [1 2 3] (= total (+ total e))) total"))
        // And outside that binding it is the constant again.
        assertEquals(Math.E, eval("(fun f [e] e) (f 1) e"))
    }
}
