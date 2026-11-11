package org.fuusio.flx.core

import org.fuusio.flx.FlxUnitTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** A program may define a function or parameter with a built-in's name; its calls then reach that. */
class ShadowedBuiltinTest : FlxUnitTest() {

    @Test
    fun `a defined function shadows a built-in of the same name`() {
        assertEquals(42L, eval("(fun sum [a b] (+ a b)) (sum 40 2)"))
    }

    @Test
    fun `a call written before the definition reaches it afterwards`() {
        assertEquals(6L, eval("(fun twice [x] (sum x x)) (fun sum [a b] (+ a b)) (twice 3)"))
    }

    @Test
    fun `a parameter shadows a built-in only inside its function`() {
        assertEquals(7L, eval("(fun f [min] (min 3 4)) (fun min [a b] (+ a b)) (f min)"))
        assertEquals(3L, eval("(fun g [max] 1) (g 0) (min 3 4)"))
    }

    @Test
    fun `built-ins still work when nothing shadows them`() {
        assertEquals(6L, eval("(sum [1 2 3])"))
    }
}
