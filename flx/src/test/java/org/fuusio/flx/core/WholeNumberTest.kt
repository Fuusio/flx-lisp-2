package org.fuusio.flx.core

import org.fuusio.flx.FlxUnitTest
import org.fuusio.flx.core.type.Map
import org.fuusio.flx.core.type.Sequence
import org.fuusio.flx.core.type.Set
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * A whole number is an Int when written as a literal and a Long when computed. They are one
 * number: as map keys, as set elements, to `distinct`, to `==` on collections, and to the
 * functions whose arguments are declared INT or LONG.
 */
class WholeNumberTest : FlxUnitTest() {

    @Test
    fun `a map finds a key whether it was written or computed`() {
        assertEquals("one", eval("""(get {1 "one"} (+ 0 1))"""))
        assertEquals("one", eval("""(get {(+ 0 1) "one"} 1)"""))
        assertEquals("one", eval("""(val m (assoc {} (+ 0 1) "one")) (get m 1)"""))
        assertEquals(true, eval("(containsKey {1 :a} (+ 0 1))"))
        assertEquals(true, eval("(containsKey (.put {} (- 3 1) :a) 2)"))
    }

    @Test
    fun `a map holds a whole-number key once`() {
        assertEquals(1, (eval("(assoc {1 :a} (+ 0 1) :b)") as Map).size())
        assertEquals(Keyword.create("b"), eval("(get (assoc {1 :a} (+ 0 1) :b) 1)"))
        assertEquals(1, (eval("(merge {1 :a} {(+ 0 1) :b})") as Map).size())
        assertEquals(1, (eval("(dissoc {1 :a 2 :b} (+ 1 1))") as Map).size())
        assertEquals(1, (eval("{1 :a (+ 0 1) :b}") as Map).size())
    }

    @Test
    fun `a set holds a whole number once`() {
        assertEquals(2, (eval("#{1 2 (+ 1 1)}") as Set).size())
        assertEquals(1, (eval("(conj #{1} (+ 0 1))") as Set).size())
        assertEquals(true, eval("(.remove #{1 2} (- 2 1))"))
        assertEquals(false, eval("(.add #{1 2} (- 2 1))"))
    }

    @Test
    fun `distinct and equality take a written and a computed number as the same`() {
        assertEquals(3, (eval("(distinct [1 (+ 0 1) 2 2.0])") as Sequence).size())
        assertEquals(true, eval("(== [1 2] [(+ 0 1) (+ 1 1)])"))
        assertEquals(true, eval("(== {:a 1} {:a (+ 0 1)})"))
        assertEquals(true, eval("(== [[1] {:k [2]}] [[(+ 0 1)] {:k [(+ 1 1)]}])"))
        assertEquals(false, eval("(== [1 2] [1 2.5])"))
        assertEquals(false, eval("(== [1 2] [1 2 3])"))
        // Equal collections are the same key.
        assertEquals("found", eval("""(get {[1 2] "found"} [(+ 0 1) 2])"""))
    }

    @Test
    fun `isZero is true of a computed zero`() {
        assertEquals(true, eval("(.isZero (- 1 1))"))
        assertEquals(true, eval("(.isZero 0)"))
        assertEquals(false, eval("(.isZero (+ 1 1))"))
    }

    @Test
    fun `functions that take whole numbers take written ones`() {
        assertEquals("el", eval("""(.substring "hello" 1 3)"""))
        assertEquals("el", eval("""(.substring "hello" (+ 0 1) (+ 1 2))"""))
        assertEquals(true, eval("(.isOdd 3)"))
        assertEquals(true, eval("(.isOdd (+ 1 2))"))
        assertEquals(2, (eval("(.subArray [1 2 3 4] 1 3)") as Sequence).size())
        assertEquals(8L, (eval("(.shl 1 (+ 1 2))") as Number).toLong())
    }
}
