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
import org.fuusio.flx.core.error.ZeroDivisionException
import org.fuusio.flx.core.function.FunctionSpec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/** Whole-number arithmetic is exact Long arithmetic; Double only where a Double is involved or on overflow. */
class NumberArithmeticTest : FlxUnitTest() {

    private val beyondDouble = 9_007_199_254_740_993L // 2^53 + 1: not representable as a Double

    @Nested
    inner class Arithmetic {

        @Test
        fun `addition is exact beyond 2^53`() {
            assertEquals(beyondDouble, eval(FunctionSpec.ADDITION, beyondDouble, 0L))
        }

        @Test
        fun `addition switches to Double at the first Double`() {
            assertEquals(3.5, eval(FunctionSpec.ADDITION, 1, 2L, 0.5))
        }

        @Test
        fun `addition overflow gives a Double`() {
            assertEquals(Long.MAX_VALUE.toDouble() + 1.0, eval(FunctionSpec.ADDITION, Long.MAX_VALUE, 1L))
        }

        @Test
        fun `subtraction is exact beyond 2^53`() {
            assertEquals(beyondDouble, eval(FunctionSpec.SUBTRACTION, beyondDouble + 1, 1L))
        }

        @Test
        fun `single-argument subtraction negates`() {
            assertEquals(-5L, eval(FunctionSpec.SUBTRACTION, 5))
            assertEquals(-2.5, eval(FunctionSpec.SUBTRACTION, 2.5))
        }

        @Test
        fun `multiplication overflow gives a Double`() {
            val result = eval(FunctionSpec.MULTIPLICATION, 3_037_000_500L, 3_037_000_500L)
            assertEquals(3_037_000_500.0 * 3_037_000_500.0, result)
        }

        @Test
        fun `multiplication is exact for whole numbers`() {
            assertEquals(beyondDouble, eval(FunctionSpec.MULTIPLICATION, beyondDouble, 1))
        }
    }

    @Nested
    inner class Division {

        @Test
        fun `zero can be divided`() {
            assertEquals(0.0, eval(FunctionSpec.DIVISION, 0, 5))
        }

        @Test
        fun `dividing by zero fails`() {
            assertThrows(ZeroDivisionException::class.java) { eval(FunctionSpec.DIVISION, 5, 0) }
        }
    }

    @Nested
    inner class Comparison {

        @Test
        fun `whole numbers beyond 2^53 compare exactly`() {
            assertEquals(true, eval(FunctionSpec.LESSER_THAN, beyondDouble - 1, beyondDouble))
            assertEquals(false, eval(FunctionSpec.GREATER_THAN_OR_EQUAL, beyondDouble - 1, beyondDouble))
        }

        @Test
        fun `mixed comparisons still work`() {
            assertEquals(true, eval(FunctionSpec.LESSER_THAN, 1, 1.5, 2L))
        }

        @Test
        fun `max of negative numbers`() {
            assertEquals(-3L, eval(FunctionSpec.MAX, -5, -3, -4))
        }

        @Test
        fun `min keeps the type of the smallest argument`() {
            assertEquals(1.5, eval(FunctionSpec.MIN, 3, 1.5, 2L))
            assertEquals(1L, eval(FunctionSpec.MIN, 3, 1, 2.5))
        }
    }
}
