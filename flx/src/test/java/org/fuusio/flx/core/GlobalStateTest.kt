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
import org.fuusio.flx.core.function.FunctionSpec
import org.fuusio.flx.core.type.impl.ImmutableList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/** Built-ins answer for themselves, and definitions stay in the scope that made them. */
class GlobalStateTest : FlxUnitTest() {

    @Nested
    inner class BuiltinsAsValues {

        @Test
        fun `built-in used as a value evaluates to itself`() {
            assertEquals(FunctionSpec.MAX, eval("max"))
        }

        @Test
        fun `built-in passed to map`() {
            assertEquals(ImmutableList.create(4L, 5L), eval("(map max [1L 5L] [4L 2L])"))
        }

        @Test
        fun `variable shadows a built-in's name`() {
            assertEquals(5L, eval("(let [max 5L] max)"))
        }

        @Test
        fun `variable set to null shadows a built-in's name`() {
            assertEquals(Null, eval("(let [max nil] max)"))
        }

        @Test
        fun `root context holds no built-in bindings`() {
            initVM()
            assertFalse(Flx.ctx.isBound(Flx.getParsedSymbol("max")))
        }
    }

    @Nested
    inner class Definitions {

        @Test
        fun `function defined inside a function stays local`() {
            eval("(fun outer [] (do (fun inner [] 1L) (inner))) (outer)")
            assertFalse(Flx.ctx.isBound(Symbol("inner")))
        }

        @Test
        fun `top-level function is still callable`() {
            assertEquals(6L, eval("(fun f [x] (* x 2L)) (f 3L)"))
        }
    }

    @Nested
    inner class Interning {

        @Test
        fun `same keyword while in use`() {
            initVM()
            val keyword = Flx.getKeyword("colour")
            assertSame(keyword, Flx.getKeyword("colour"))
        }

        @Test
        fun `same symbol while in use`() {
            initVM()
            val symbol = Flx.getSymbol("someName")
            assertSame(symbol, Flx.getSymbol("someName"))
        }
    }
}
