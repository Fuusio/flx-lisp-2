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

import org.fuusio.flx.Flx
import org.fuusio.flx.FlxUnitTest
import org.fuusio.flx.core.error.InvalidArgException
import org.fuusio.flx.core.macro.MacroSpec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/** `let`, `fn` and `fun` prepared once per call site, argument evaluation, and slimmer scopes. */
class PreparedFormsTest : FlxUnitTest() {

    @Nested
    inner class Let {

        @Test
        fun `let hides an outer variable instead of changing it`() {
            assertEquals(1L, eval("(var x 1L) (let [x 2L] x) x"))
        }

        @Test
        fun `let can reuse the name of an outer val`() {
            assertEquals(2L, eval("(val x 1L) (let [x 2L] x)"))
        }

        @Test
        fun `binding values are evaluated in the enclosing scope`() {
            assertEquals(1L, eval("(var a 1L) (let [a 10L b a] b)"))
        }

        @Test
        fun `a name bound twice takes the last value`() {
            assertEquals(2L, eval("(let [x 1L x 2L] x)"))
        }

        @Test
        fun `let evaluated repeatedly at one site`() {
            assertEquals(30L, eval("(var s 0L) (for i 1 5 1 (let [d (* i 2L)] (= s (+ s d)))) s"))
        }

        @Test
        fun `invalid bindings fail on every evaluation`() {
            val ctx = ctx()
            val form = list(MacroSpec.LET, array(1L, 2L), 3L)
            repeat(2) { assertThrows(InvalidArgException::class.java) { form.eval(ctx) } }
        }
    }

    @Nested
    inner class Functions {

        @Test
        fun `fn evaluated repeatedly at one site makes working closures`() {
            assertEquals(12L, eval("(var s 0L) (for i 1 3 1 (= s (+ s ((fn [y] (* y 2L)) i)))) s"))
        }

        @Test
        fun `closures from one fn site keep their own values`() {
            val result = eval("""
                (var fs [])
                (for i 1 3 1 (.add fs (let [k i] (fn [] k))))
                (+ ((.get fs 0)) ((.get fs 2)))
            """)
            assertEquals(4L, result)
        }

        @Test
        fun `fun defined inside a loop`() {
            assertEquals(3L, eval("(var r 0L) (for i 1 3 1 (do (fun g [] i) (= r (+ (g) 0L)))) r"))
        }
    }

    @Nested
    inner class Scopes {

        @Test
        fun `program is found from nested scopes`() {
            initVM()
            val nested = Flx.ctx.create().create()
            assertSame(Flx.ctx.program, nested.program)
        }

        @Test
        fun `args default to empty`() {
            assertTrue(ctx().args.isEmpty())
        }
    }

    @Nested
    inner class LoopAndBuiltins {

        @Test
        fun `loop evaluated repeatedly at one site`() {
            val result = eval("""
                (var total 0L)
                (for k 1 4 1
                    (= total (+ total (loop [i 0L s 0L] (if-else (> i k) s (recur (+ i 1L) (+ s i)))))))
                total
            """)
            assertEquals(20L, result)
        }

        @Test
        fun `invalid loop bindings fail on every evaluation`() {
            val ctx = ctx()
            val form = list(MacroSpec.LOOP, array(1L, 2L), 3L)
            repeat(2) { assertThrows(InvalidArgException::class.java) { form.eval(ctx) } }
        }

        @Test
        fun `rand produces distinct values within the same millisecond`() {
            val distinctCount = eval("""
                (val s #{})
                (repeat 20 (.add s (rand)))
                (.size s)
            """) as Long
            assertTrue(distinctCount > 1L)
        }

        @Test
        fun `toMap converts JSON keys directly without invoking parser`() {
            val result = eval("""
                (val m (.toMap (.toJson "{\"name\":\"Ada\",\":role\":\":admin\",\"spaced key\":42}")))
                [(.get m "name") (.get m :role) (.get m "spaced key")]
            """)
            assertEquals(array("Ada", Flx.getKeyword("admin"), 42L), result)
        }

        @Test
        fun `map and filter over collections`() {
            assertEquals(list(2L, 4L, 6L), eval("(map (fn [x] (* x 2L)) [1L 2L 3L])"))
            assertEquals(list(11L, 22L, 33L), eval("(map + [1L 2L 3L] (10L 20L 30L))"))
            assertEquals(list(2L, 4L), eval("(.filter [1L 2L 3L 4L] (fn [x] (.isEven x)))"))
        }
    }
}
