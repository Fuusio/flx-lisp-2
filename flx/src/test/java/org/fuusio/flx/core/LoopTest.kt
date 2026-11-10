/*
 * Copyright (C) 2016 - 2021 Marko Salmela
 *
 * http://fuusio.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fuusio.flx.core

import org.fuusio.flx.FlxUnitTest
import org.fuusio.flx.core.error.InvalidNumberOfArgsException
import org.fuusio.flx.core.error.StackDepthExceededException
import org.fuusio.flx.core.vm.FlxVM
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/** Iteration without stack growth (loop/recur), deep recursion reported as an FLX error, and loop variables. */
class LoopTest : FlxUnitTest() {

    @Nested
    inner class LoopRecur {

        @Test
        fun `loop with recur`() {
            assertEquals(55L, eval("(loop [i 0L sum 0L] (if-else (> i 10L) sum (recur (+ i 1L) (+ sum i))))"))
        }

        @Test
        fun `loop without recur returns its last form`() {
            assertEquals(3L, eval("(loop [a 1L b 2L] (+ a b))"))
        }

        @Test
        fun `recur in a function runs far deeper than the stack allows recursion`() {
            val result = eval("(fun sumTo [n acc] (if-else (< n 1L) acc (recur (- n 1L) (+ acc n)))) (sumTo 100000L 0L)")
            assertEquals(5_000_050_000L, result)
        }

        @Test
        fun `recur with the wrong number of values fails`() {
            assertThrows(InvalidNumberOfArgsException::class.java) {
                eval("(loop [i 0L] (if-else (> i 1L) i (recur (+ i 1L) 5L)))")
            }
        }

        @Test
        fun `closures created inside loop recur passes keep their own values`() {
            val result = eval("""
                (var fns [])
                (loop [i 1L]
                    (if-else (> i 3L)
                        (+ ((.get fns 0)) ((.get fns 1)) ((.get fns 2)))
                        (do
                            (.add fns (fn [] i))
                            (recur (+ i 1L)))))
            """)
            assertEquals(6L, result)
        }

        @Test
        fun `closures created inside nested let in loop recur keep their own values`() {
            val result = eval("""
                (var fns [])
                (loop [i 1L]
                    (if-else (> i 3L)
                        (+ ((.get fns 0)) ((.get fns 2)))
                        (do
                            (let [doubled (* i 2L)]
                                (.add fns (fn [] (+ i doubled))))
                            (recur (+ i 1L)))))
            """)
            assertEquals(12L, result)
        }

        @Test
        fun `closures created inside tail-recursive function keep their own values`() {
            val result = eval("""
                (var fns [])
                (fun collect [n]
                    (if-else (> n 3L)
                        (+ ((.get fns 0)) ((.get fns 1)) ((.get fns 2)))
                        (do
                            (.add fns (fn [] n))
                            (recur (+ n 1L)))))
                (collect 1L)
            """)
            assertEquals(6L, result)
        }
    }

    @Nested
    inner class StackDepth {

        @Test
        fun `unbounded recursion is an FLX error, not a crash`() {
            assertThrows(StackDepthExceededException::class.java) {
                eval("(fun down [n] (+ 1L (down (- n 1L)))) (down 1L)")
            }
        }

        @Test
        fun `a larger stack allows deeper recursion`() {
            val result = FlxVM.runWithStack(FlxVM.LARGE_STACK_SIZE) {
                eval("(fun depth [n] (if-else (< n 1L) 0L (+ 1L (depth (- n 1L))))) (depth 3000L)")
            }
            assertEquals(3000L, result)
        }

        @Test
        fun `large stack worker thread is reused and supports re-entrant calls`() {
            val firstThread = FlxVM.runWithStack(FlxVM.LARGE_STACK_SIZE) { Thread.currentThread() }
            val secondThread = FlxVM.runWithStack(FlxVM.LARGE_STACK_SIZE) {
                val nestedThread = FlxVM.runWithStack(FlxVM.LARGE_STACK_SIZE) { Thread.currentThread() }
                assertEquals(Thread.currentThread(), nestedThread)
                nestedThread
            }
            assertEquals(firstThread, secondThread)
        }
    }

    @Nested
    inner class LoopVariables {

        @Test
        fun `for does not overwrite a variable of the same name`() {
            assertEquals(100L, eval("(var i 100L) (for i 1 3 1 i) i"))
        }

        @Test
        fun `for still updates other variables`() {
            assertEquals(6L, eval("(var s 0L) (for i 1 3 1 (= s (+ s i))) s"))
        }

        @Test
        fun `for-each does not overwrite a variable of the same name`() {
            assertEquals(100L, eval("(var x 100L) (forEach x [1L 2L] x) x"))
        }

        @Test
        fun `for-each over a string`() {
            assertEquals(2L, eval("(var n 0L) (forEach c \"ab\" (++ n)) n"))
        }
    }
}
