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
package org.fuusio.flx.core.extension

import org.fuusio.flx.FlxUnitTest
import org.fuusio.flx.core.isDouble
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DoubleExtensionsTest : FlxUnitTest() {

    @Nested
    inner class TestIsZero {

        @org.junit.jupiter.api.Test
        fun `zero values`() {
            val zeros: Array<Number> = arrayOf(0.0, 0)
            zeros.forEach { number -> Assertions.assertTrue(number.toDouble().isZero()) }
        }

        @org.junit.jupiter.api.Test
        fun `non-zero values`() {
            val nonZeros = arrayOf(10.0, 1.234, 0.000001, 0.000000000001)
            nonZeros.forEach { double -> Assertions.assertFalse(double.isZero()) }
        }

    }

    @Nested
    inner class TestHasDecimals {

        @org.junit.jupiter.api.Test
        fun `decimal numbers`() {
            val decimalNumbers: Array<Double> = arrayOf(0.00001, 12.34, 2.000044, 12100.1)
            decimalNumbers.forEach { double -> Assertions.assertTrue(double.hasDecimals()) }
        }

        @org.junit.jupiter.api.Test
        fun `non-decimal numbers`() {
            val nonDecimalNumbers: Array<Double> = arrayOf(2.0, 12.0)
            nonDecimalNumbers.forEach { double -> Assertions.assertFalse(double.hasDecimals()) }
        }
    }
}