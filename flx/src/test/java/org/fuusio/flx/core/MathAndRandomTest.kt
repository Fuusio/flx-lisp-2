package org.fuusio.flx.core

import org.fuusio.flx.FlxUnitTest
import org.fuusio.flx.core.type.impl.ImmutableArray
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MathAndRandomTest : FlxUnitTest() {

    @Test
    fun `clamp and coerceIn preserve Long for whole numbers and Double for floating point`() {
        assertEquals(0L, eval("(clamp -5L 0L 10L)"))
        assertEquals(5L, eval("(clamp 5L 0L 10L)"))
        assertEquals(10L, eval("(clamp 25L 0L 10L)"))
        assertEquals(10L, eval("(.coerceIn 25L 0L 10L)"))

        assertEquals(0.5, eval("(clamp 0.5 0.0 1.0)") as Double, 1e-9)
        assertEquals(1.0, eval("(.coerceIn 2.5 0.0 1.0)") as Double, 1e-9)
    }

    @Test
    fun `atan2 and hypot compute 2D and 3D vector angles and magnitudes`() {
        assertEquals(Math.PI / 4.0, eval("(atan2 1.0 1.0)") as Double, 1e-9)
        assertEquals(5.0, eval("(hypot 3.0 4.0)") as Double, 1e-9)
        assertEquals(13.0, eval("(hypot 3.0 4.0 12.0)") as Double, 1e-9)
    }

    @Test
    fun `rand and randInt produce values within bounds`() {
        repeat(20) {
            val r = eval("(rand 10.0)") as Double
            assertTrue(r >= 0.0 && r < 10.0)

            val i1 = eval("(randInt 5L)") as Long
            assertTrue(i1 in 0L until 5L)

            val i2 = eval("(randInt 10L 15L)") as Long
            assertTrue(i2 in 10L until 15L)
        }
    }

    @Test
    fun `randChoice returns an element from a sequence or set and nil when empty`() {
        assertEquals(Null, eval("(randChoice [])"))
        assertEquals(Null, eval("(randChoice #{})"))

        val allowed = setOf("a", "b", "c")
        repeat(20) {
            assertTrue(eval("(randChoice [\"a\" \"b\" \"c\"])") in allowed)
            assertTrue(eval("(randChoice (\"a\" \"b\" \"c\"))") in allowed)
            assertTrue(eval("(randChoice #{\"a\" \"b\" \"c\"})") in allowed)
        }
    }

    @Test
    fun `shuffle returns a new array with the same elements without mutating the original`() {
        val result = eval("""
            (val orig [1L 2L 3L 4L 5L])
            (val shuf (shuffle orig))
            [orig shuf]
        """) as ImmutableArray
        val orig = result[0] as ImmutableArray
        val shuf = result[1] as ImmutableArray
        assertEquals(array(1L, 2L, 3L, 4L, 5L), orig)
        assertEquals(setOf(1L, 2L, 3L, 4L, 5L), shuf.elements.toSet())
        assertEquals(5, shuf.size())
    }
}

