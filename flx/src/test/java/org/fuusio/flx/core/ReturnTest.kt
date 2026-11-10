package org.fuusio.flx.core

import org.fuusio.flx.FlxUnitTest
import org.fuusio.flx.core.type.Sequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** `(return value)` leaves the function it is in, from however deep inside its blocks. */
class ReturnTest : FlxUnitTest() {

    private fun values(source: String): kotlin.collections.List<Any> =
        (eval(source) as Sequence).let { result -> (0 until result.size()).map { result[it] } }

    private fun key(name: String) = Keyword.create(name)

    @Test
    fun `return in a loop ends the function, not just the loop`() {
        assertEquals(3, eval("(fun f [xs] (forEach x xs (when (> x 2) (return x))) -1) (f [1 2 3 4])"))
        assertEquals(-1, eval("(fun f [xs] (forEach x xs (when (> x 2) (return x))) -1) (f [1 2])"))
        assertEquals(5L, eval("(fun f [] (var i 0) (while true (++ i) (when (== i 5) (return i))) :never) (f)"))
        assertEquals(key("two"), eval("(fun f [] (var n 0) (repeat 5 (++ n) (when (== n 2) (return :two))) :never) (f)"))
        assertEquals('l', eval("""(fun f [s] (forEach c s (when (== c 'l') (return c))) :none) (f "hello")"""))
    }

    @Test
    fun `return passes through do, let, when-let, loop and try`() {
        assertEquals(listOf(key("pos"), key("neg")), values("(fun f [n] (let [a 1] (do (when (> n 0) (return :pos)))) :neg) [(f 1) (f -1)]"))
        assertEquals(key("done"), eval("(fun f [n] (loop [i 0] (when (== i n) (return :done)) (recur (+ i 1))) :never) (f 1000)"))
        // A return is not an error: catch does not see it.
        assertEquals(1, eval("(fun f [] (try (return 1) (catch err 2)) 3) (f)"))
        assertEquals(7, eval("(fun f [] (try-or (return 7) 8) 9) (f)"))
        assertEquals(2, eval("(fun f [] (try (/ 1 0) (catch err (return 2))) 3) (f)"))
    }

    @Test
    fun `return ends only the function it is in`() {
        assertEquals(listOf(key("small"), key("big")), values("(fun g [x] (when (> x 1) (return :big)) :small) (fun f [] [(g 1) (g 5)]) (f)"))
        // In a lambda, the lambda.
        assertEquals(listOf(1, key("two"), 3), values("(fun f [] (map (fn [x] (when (== x 2) (return :two)) x) [1 2 3])) (f)"))
    }

    @Test
    fun `a bare return gives nil`() {
        assertEquals(Null, eval("(fun f [] (return) 3) (f)"))
    }

    @Test
    fun `outside a function return ends its top-level form`() {
        assertEquals(5, eval("(return 5)"))
        assertEquals(2L, eval("(var n 0) (repeat 3 (++ n) (when (== n 2) (return n))) n"))
        assertEquals(key("next"), eval("(do (return 1) 2) :next"))
    }
}
