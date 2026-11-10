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
package org.fuusio.flx.core.component

import org.fuusio.flx.FlxUnitTest
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CalculatorTest : FlxUnitTest() {

    private lateinit var calculator: Calculator

    @BeforeEach
    fun before() {
        calculator = Calculator()
    }

    @Nested
    inner class TestDecimalPoint {

        @org.junit.jupiter.api.Test
        fun `12dot3`() {
            input1()
            input2()
            inputDecimalPoint()
            val output = input3()
            Assertions.assertEquals(12.3, output)
        }

        @org.junit.jupiter.api.Test
        fun `12dot`() {
            input1()
            input2()
            val output = inputDecimalPoint()
            Assertions.assertEquals(12.0, output)
        }
    }

    @Nested
    inner class TestToggleSign {

        @org.junit.jupiter.api.Test
        fun `12 +- = -12`() {
            input1()
            input2()
            val output = inputToggleSign()
            Assertions.assertEquals(-12.0, output)
        }

        @org.junit.jupiter.api.Test
        fun `12 +- +- = 12`() {
            input1()
            input2()
            Assertions.assertEquals(-12.0, inputToggleSign())
            Assertions.assertEquals(12.0, inputToggleSign())
        }
    }

    @Nested
    inner class TestPercentage {

        @org.junit.jupiter.api.Test
        fun `2  % = 0,02`() {
            input2()
            inputPercentage()
            assertOutput(0.02)
        }

        @org.junit.jupiter.api.Test
        fun `5  % * 10 = 0,5`() {
            input5()
            inputPercentage()
            inputMultiplication()
            input1()
            input0()
            assertOutput(0.5)
        }
    }

    @Nested
    inner class TestClear {

        @org.junit.jupiter.api.Test
        fun `1  2  C`() {
            input1()
            input2()
            inputClear()
            assertOutput(Calculator.ZERO_OUTPUT)
        }
    }

    @Nested
    inner class TestClearEntry {

        @org.junit.jupiter.api.Test
        fun `1  2  +  2 4 CE 1 8 = 30`() {
            input1()
            input2()
            inputAddition()
            input2()
            input4()
            inputClearEntry()
            input1()
            input8()
            assertOutput(30.0)
        }
    }

    @Nested
    inner class TestAddition {

        @org.junit.jupiter.api.Test
        fun `1 + 2 = 3`() {
            input1()
            inputAddition()
            input2()
            assertOutput(3.0)
        }

        @org.junit.jupiter.api.Test
        fun `1 + 2 + 3 = 6`() {
            input1()
            inputAddition()
            input2()
            inputAddition()
            input3()
            assertOutput(6.0)
        }

        @org.junit.jupiter.api.Test
        fun `0,1 + 0,05 + 0,1 = 0,25`() {
            inputDecimalPoint()
            input1()
            inputAddition()
            inputDecimalPoint()
            input0()
            input5()
            inputAddition()
            inputDecimalPoint()
            input1()
            assertOutput(0.25)
        }
    }

    @Nested
    inner class TestSubtraction {

        @org.junit.jupiter.api.Test
        fun `1 - 2 = -1`() {
            input1()
            inputSubtraction()
            input2()
            assertOutput(-1)
        }

        @org.junit.jupiter.api.Test
        fun `6 - 4 - 1 = 1`() {
            input6()
            inputSubtraction()
            input4()
            inputSubtraction()
            input1()
            assertOutput(1)
        }
    }

    @Nested
    inner class TestMultiplication {

        @org.junit.jupiter.api.Test
        fun `2 * 2 = 4`() {
            input2()
            inputMultiplication()
            input2()
            assertOutput(4)
        }

        @org.junit.jupiter.api.Test
        fun `6 * 4 * 2 = 48`() {
            input6()
            inputMultiplication()
            input4()
            inputMultiplication()
            input2()
            assertOutput(48)
        }
    }

    @Nested
    inner class TestDivision {

        @org.junit.jupiter.api.Test
        fun `4000 div 2 = 2000`() {
            input4()
            input0()
            input0()
            input0()
            inputDivision()
            input2()
            assertOutput(2000)
        }

        @org.junit.jupiter.api.Test
        fun `300 div 0 = NaN`() {
            input3()
            input0()
            input0()
            inputDivision()
            input0()
            assertOutput(Double.NaN)
        }

        @org.junit.jupiter.api.Test
        fun `20 div 5 div 2 = 2`() {
            input2()
            input0()
            inputDivision()
            input5()
            inputDivision()
            input2()
            assertOutput(2)
        }
    }

    private fun assertOutput(value: Double) {
        Assertions.assertEquals(value, calculator.inputEquals())
    }

    private fun assertOutput(value: Int) {
        assertOutput(value.toDouble())
    }

    private fun inputToggleSign(): Output = calculator.inputToggleSign()

    private fun inputClear(): Output = calculator.inputClear()

    private fun inputClearEntry(): Output = calculator.inputClearEntry()

    private fun inputAddition(): Output = calculator.inputAddition()

    private fun inputSubtraction(): Output = calculator.inputSubtraction()

    private fun inputMultiplication(): Output = calculator.inputMultiplication()

    private fun inputPercentage(): Output = calculator.inputPercentage()

    private fun inputDivision(): Output = calculator.inputDivision()

    private fun inputDecimalPoint(): Output = calculator.inputDecimalPoint()

    private fun input0(): Output = calculator.input0()

    private fun input1(): Output = calculator.input1()

    private fun input2(): Output = calculator.input2()

    private fun input3(): Output = calculator.input3()

    private fun input4(): Output = calculator.input4()

    private fun input5(): Output = calculator.input5()

    private fun input6(): Output = calculator.input6()

    private fun input7(): Output = calculator.input7()

    private fun input8(): Output = calculator.input8()

    private fun input9(): Output = calculator.input9()

}