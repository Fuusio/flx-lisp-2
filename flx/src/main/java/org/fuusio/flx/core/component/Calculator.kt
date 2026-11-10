package org.fuusio.flx.core.component

import org.fuusio.flx.core.extension.isZero

typealias Input = String
typealias Output = Double

@Suppress("MemberVisibilityCanBePrivate", "unused")
class Calculator {
    var operator: Operator = Operator.NONE
    var operand1: Double = 0.0
    var operand2: Double = 0.0
    var hasOperand1: Boolean = false
    var hasOperand2: Boolean = false

    private var output: Output = ZERO_OUTPUT

    private var isDecimalPointInputed = false
    private var isDivisionByZero = false

    var value: Double = 0.0
    var input: Input = EMPTY_INPUT

    private fun output(output: Output): Output {
        this.output = output
        return output
    }

    fun input0(): Output = inputDigit(0)
    fun input1(): Output = inputDigit(1)
    fun input2(): Output = inputDigit(2)
    fun input3(): Output = inputDigit(3)
    fun input4(): Output = inputDigit(4)
    fun input5(): Output = inputDigit(5)
    fun input6(): Output = inputDigit(6)
    fun input7(): Output = inputDigit(7)
    fun input8(): Output = inputDigit(8)
    fun input9(): Output = inputDigit(9)

    fun inputDigit(digit: Int): Output =
        when {
            input.length < MAX_DIGITS_LENGTH -> {
                if (input.length == 1 && input[0] == '0') {
                    input = EMPTY_INPUT
                }
                input += digit.toString()
                output(input.toDouble())
            }
            else -> output(input.toDouble())
        }

    fun inputAddition(): Output = inputOperator(Operator.ADDITION)
    fun inputSubtraction(): Output = inputOperator(Operator.SUBTRACTION)
    fun inputMultiplication(): Output = inputOperator(Operator.MULTIPLICATION)
    fun inputDivision(): Output = inputOperator(Operator.DIVISION)

    fun inputPercentage(): Output {
        value = when {
            hasOperand1 -> {
                operand1 / 100.0
            }
            input.isNotEmpty() -> {
                operand1 = inputToOperand()
                operand1 / 100.0
            }
            else -> {
                this.operator = Operator.NONE
                return output(ZERO_OUTPUT)
            }
        }
        operand1 = value
        hasOperand1 = true
        setCurrentOperator(Operator.NONE)
        return output(value)
    }

    fun inputDecimalPoint(): Output =
        when {
            isDecimalPointInputed -> output
            input.isEmpty() -> {
                input = "0."
                output(input.toDouble())
            }
            input.length < MAX_DIGITS_LENGTH -> {
                input += DECIMAL_POINT
                isDecimalPointInputed = true
                output(input.toDouble())
            }
            else -> output
        }

    private fun inputOperator(operator: Operator): Output {
        val output = when {
            hasOperand1 && input.isNotEmpty() -> {
                operand2 = inputToOperand()
                hasOperand2 = true
                value = applyOperator()
                operand1 = value
                hasOperand1 = true
                setCurrentOperator(operator)
                operand1
            }
            hasOperand1 -> {
                setCurrentOperator(operator)
                output
            }
            input.isNotEmpty() -> {
                operand1 = inputToOperand()
                hasOperand1 = true
                setCurrentOperator(operator)
                operand1
            }
            else -> ZERO_OUTPUT
        }
        return output(output)
    }

    private fun setCurrentOperator(operator: Operator) {
        this.operator = operator
        input = EMPTY_INPUT
        isDecimalPointInputed = false
    }

    fun inputEquals(): Output {
        operand2 = when {
            !hasOperand1 && input.isEmpty() -> return output(ZERO_OUTPUT)
            !hasOperand1 && input.isNotEmpty() -> {
                operand1 = input.toDouble()
                return output(operand1)
            }
            input.isEmpty() -> operand1
            else -> inputToOperand()
        }
        hasOperand2 = true
        value = applyOperator()
        operator = Operator.NONE
        operand1 = value
        hasOperand1 = true
        hasOperand2 = false
        return output(value)
    }

    private fun inputToOperand(): Double {
        isDecimalPointInputed = false
        val operand = input.toDouble()
        input = EMPTY_INPUT
        return operand
    }

    private fun applyOperator(): Double =
        when (operator) {
            Operator.ADDITION -> operand1 + operand2
            Operator.SUBTRACTION -> operand1 - operand2
            Operator.MULTIPLICATION -> operand1 * operand2
            Operator.DIVISION -> {
                if (operand2.isZero()) {
                    isDivisionByZero = true
                    Double.NaN
                } else {
                    operand1 / operand2
                }
            }
            Operator.NONE -> operand1
        }

    fun inputClear(): Output {
        value = 0.0
        operand1 = 0.0
        hasOperand1 = false
        hasOperand2 = false
        operator = Operator.NONE
        isDivisionByZero = false
        return inputClearEntry()
    }

    fun inputClearEntry(): Output {
        input = EMPTY_INPUT
        isDecimalPointInputed = false
        return output(ZERO_OUTPUT)
    }

    fun delete(): Output =
        when {
            input.length == 1 -> {
                input = EMPTY_INPUT
                output(ZERO_OUTPUT)
            }
            input.isNotEmpty() -> {
                input = input.dropLast(1)
                output(input.toDouble())
            }
            else -> output
        }

    fun inputToggleSign(): Output =
        when {
            hasOperand1 -> {
                operand1 = -operand1
                output(operand1)
            }
            input.isNotEmpty() -> {
                input = if (input.startsWith(MINUS_SIGN)) {
                    input.drop(1)
                } else {
                    MINUS_SIGN + input
                }
                output(input.toDouble())
            }
            else -> output(ZERO_OUTPUT)
        }

    fun inputNumber(number: Double): Output {
        operand1 = number
        input = EMPTY_INPUT
        isDecimalPointInputed = false
        return output(number)
    }

    fun hasError(): Boolean = isDivisionByZero

    fun getOperatorLabel(): String = operator.label

    enum class Operator(val label: String) {
        NONE(""),
        ADDITION("+"),
        SUBTRACTION("-"),
        MULTIPLICATION("*"),
        DIVISION("/"),
    }

    companion object {
        const val DECIMAL_POINT = "."
        const val EMPTY_INPUT = ""
        const val MAX_DIGITS_LENGTH = 16
        const val MINUS_SIGN = "-"
        const val ZERO_OUTPUT: Output = 0.0
    }
}