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
import org.fuusio.flx.core.type.impl.ImmutableArray
import org.fuusio.flx.core.type.impl.ImmutableMap
import org.fuusio.flx.core.type.impl.ImmutableSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/** Collection literals: plain-value ones are copied in bulk, but every evaluation still gets its own copy. */
class LiteralTest : FlxUnitTest() {

    @Nested
    inner class Arrays {

        @Test
        fun `constant array evaluates to an equal new array each time`() {
            val ctx = ctx()
            val literal = array(1L, "a", 'c', true, Null)
            val first = literal.eval(ctx)
            val second = literal.eval(ctx)
            assertEquals(literal, first)
            assertNotSame(literal, first)
            assertNotSame(first, second)
        }

        @Test
        fun `modifying an evaluated array leaves the literal alone`() {
            val ctx = ctx()
            val literal = array(1L, 2L)
            (literal.eval(ctx) as ImmutableArray).add(3L)
            assertEquals(array(1L, 2L), literal.eval(ctx))
        }

        @Test
        fun `array with a symbol is evaluated`() {
            assertEquals(array(1L, 42L), eval("(val x 42L) [1L x]"))
        }

        @Test
        fun `array changed to hold a symbol is evaluated again`() {
            val ctx = ctx()
            val x = Symbol("x")
            ctx.set(x, 7L)
            val literal = array(1L, 2L) as ImmutableArray
            assertEquals(array(1L, 2L), literal.eval(ctx))
            literal.remove(2L)
            literal.add(x)
            assertEquals(array(1L, 7L), literal.eval(ctx))
        }

        @Test
        fun `nested array is copied, not shared`() {
            val ctx = ctx()
            val literal = array(1L, array(2L))
            val first = literal.eval(ctx) as ImmutableArray
            val second = literal.eval(ctx) as ImmutableArray
            assertNotSame(first.get(1), second.get(1))
        }
    }

    @Nested
    inner class Maps {

        @Test
        fun `constant map evaluates to an equal new map each time`() {
            val ctx = ctx()
            val literal = map(keyword("a"), 1L, keyword("b"), "x")
            val first = literal.eval(ctx)
            assertEquals(literal, first)
            assertNotSame(first, literal.eval(ctx))
        }

        @Test
        fun `map with symbol values is evaluated`() {
            val ctx = ctx()
            val x = Symbol("x")
            ctx.set(x, 5L)
            assertEquals(map("k", 5L), map("k", x).eval(ctx))
        }

        @Test
        fun `cleared map evaluates to an empty map`() {
            val ctx = ctx()
            val literal = map("k", 1L) as ImmutableMap
            literal.eval(ctx)
            literal.clear()
            assertEquals(map(), literal.eval(ctx))
        }
    }

    @Nested
    inner class Sets {

        @Test
        fun `constant set evaluates to an equal new set each time`() {
            val ctx = ctx()
            val literal = set(1L, 2L, "a")
            val first = literal.eval(ctx)
            assertEquals(literal, first)
            assertNotSame(first, literal.eval(ctx))
        }

        @Test
        fun `set changed to hold a symbol is evaluated again`() {
            val ctx = ctx()
            val x = Symbol("x")
            ctx.set(x, 9L)
            val literal = set(1L) as ImmutableSet
            literal.eval(ctx)
            literal.add(x)
            assertEquals(set(1L, 9L), literal.eval(ctx))
        }
    }

    private fun keyword(name: String) = org.fuusio.flx.Flx.getKeyword(name)
}
