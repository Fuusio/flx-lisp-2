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
import org.fuusio.flx.core.function.FunctionSpec
import org.fuusio.flx.core.macro.MacroSpec
import org.fuusio.flx.core.type.impl.ImmutableList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested

/** A list form caches how it is called; the cache must never outlive a change to the list. */
class CallPlanTest : FlxUnitTest() {

    @Nested
    inner class Test {

        @org.junit.jupiter.api.Test
        fun `same form evaluates the same way repeatedly`() {
            val ctx = ctx()
            val form = list(FunctionSpec.ADDITION, 1L, 2L)
            repeat(3) { assertEquals(3L, form.eval(ctx)) }
        }

        @org.junit.jupiter.api.Test
        fun `adding an argument after evaluation is seen`() {
            val ctx = ctx()
            val form = list(FunctionSpec.ADDITION, 1L, 2L) as ImmutableList
            assertEquals(3L, form.eval(ctx))
            form.add(4L)
            assertEquals(7L, form.eval(ctx))
        }

        @org.junit.jupiter.api.Test
        fun `changing a macro's arguments after evaluation is seen`() {
            val ctx = ctx()
            val form = list(MacroSpec.QUOTE, 1L) as ImmutableList
            assertEquals(1L, form.eval(ctx))
            form.remove(1L)
            form.add(2L)
            assertEquals(2L, form.eval(ctx))
        }

        @org.junit.jupiter.api.Test
        fun `emptied form evaluates to itself`() {
            val ctx = ctx()
            val form = list(FunctionSpec.ADDITION, 1L) as ImmutableList
            assertEquals(1L, form.eval(ctx))
            form.clear()
            assertEquals(form, form.eval(ctx))
        }

        @org.junit.jupiter.api.Test
        fun `loop body form is reused across iterations`() {
            assertEquals(55L, eval("(var s 0L) (for i 1 10 1 (= s (+ s i))) s"))
        }
    }
}
