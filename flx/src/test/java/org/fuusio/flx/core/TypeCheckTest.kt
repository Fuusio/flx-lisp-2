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

import org.fuusio.flx.core.type.ArgModifier
import org.fuusio.flx.core.type.ArgSpec
import org.fuusio.flx.core.type.CoreType
import org.fuusio.flx.core.type.SignatureSpec
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/** The argument checks that run on every built-in call. */
class TypeCheckTest {

    @Nested
    inner class CoreTypes {

        @Test
        fun `primitive-backed types accept boxed values`() {
            assertTrue(CoreType.INT.isInstance(5))
            assertTrue(CoreType.LONG.isInstance(5L))
            assertTrue(CoreType.DOUBLE.isInstance(1.5))
            assertTrue(CoreType.BOOLEAN.isInstance(true))
            assertTrue(CoreType.CHAR.isInstance('a'))
            assertTrue(CoreType.NUMBER.isInstance(5))
            assertTrue(CoreType.NUMBER.isInstance(5L))
        }

        @Test
        fun `types reject other values`() {
            assertFalse(CoreType.INT.isInstance(5.0))
            assertFalse(CoreType.LONG.isInstance(5.0))
            assertFalse(CoreType.NUMBER.isInstance("5"))
            assertFalse(CoreType.STRING.isInstance('a'))
        }

        @Test
        fun `any accepts everything`() {
            assertTrue(CoreType.ANY.isInstance(Null))
            assertTrue(CoreType.ANY.isInstance("x"))
        }
    }

    @Nested
    inner class Signatures {

        private fun signature(vararg argSpecs: ArgSpec) = SignatureSpec(0, arrayOf(*argSpecs))

        @Test
        fun `fixed arguments`() {
            val s = signature(ArgSpec(CoreType.STRING, "s"), ArgSpec(CoreType.NUMBER, "n"))
            assertTrue(s.checkArgs(arrayOf<Any>("a", 1L)))
            assertFalse(s.checkArgs(arrayOf<Any>(1L, "a")))
            assertFalse(s.checkArgs(arrayOf<Any>("a")))
            assertFalse(s.checkArgs(arrayOf<Any>("a", 1L, 2L)))
        }

        @Test
        fun `vararg uses the last type for the rest`() {
            val s = signature(ArgSpec(CoreType.NUMBER, "n", ArgModifier.VARARG))
            assertTrue(s.checkArgs(arrayOf<Any>()))
            assertTrue(s.checkArgs(arrayOf<Any>(1L, 2, 3.0)))
            assertFalse(s.checkArgs(arrayOf<Any>(1L, "x")))
        }

        @Test
        fun `repeatable arguments cycle through their types`() {
            val s = signature(
                ArgSpec(CoreType.STRING, "name"),
                ArgSpec(CoreType.SYMBOL, "key", ArgModifier.REPEATABLE),
                ArgSpec(CoreType.NUMBER, "value", ArgModifier.REPEATABLE),
            )
            val k = Symbol("k")
            assertTrue(s.checkArgs(arrayOf<Any>("n", k, 1L)))
            assertTrue(s.checkArgs(arrayOf<Any>("n", k, 1L, k, 2L)))
            assertFalse(s.checkArgs(arrayOf<Any>("n", k, 1L, k)))
            assertFalse(s.checkArgs(arrayOf<Any>("n", k, 1L, 2L, k)))
        }

        @Test
        fun `all-any signature checks only the count`() {
            val s = signature(ArgSpec(CoreType.ANY, "a"), ArgSpec(CoreType.ANY, "b"))
            assertTrue(s.checkArgs(arrayOf<Any>(Null, "x")))
            assertFalse(s.checkArgs(arrayOf<Any>(Null)))
        }
    }
}
