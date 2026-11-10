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
@file:Suppress("UNUSED_PARAMETER")

package org.fuusio.flx.core.function

import kotlinx.serialization.json.*
import org.fuusio.flx.Flx
import org.fuusio.flx.core.*
import org.fuusio.flx.core.`object`.ObjectInstance
import org.fuusio.flx.core.component.Calculator
import org.fuusio.flx.core.error.*
import org.fuusio.flx.core.extension.hasDecimals
import org.fuusio.flx.core.macro.MacroSymbol
import org.fuusio.flx.core.type.Array
import org.fuusio.flx.core.type.List
import org.fuusio.flx.core.type.Map
import org.fuusio.flx.core.type.Set
import org.fuusio.flx.core.parser.*
import org.fuusio.flx.core.rx.Observable
import org.fuusio.flx.core.rx.PeriodicTimerObservable
import org.fuusio.flx.core.rx.Subscription
import org.fuusio.flx.core.rx.TimerObservable
import org.fuusio.flx.core.type.*
import org.fuusio.flx.core.type.Collection
import org.fuusio.flx.core.type.impl.ImmutableArray
import org.fuusio.flx.core.type.impl.ImmutableList
import org.fuusio.flx.core.type.impl.ImmutableMap
import org.fuusio.flx.core.type.impl.ImmutableSet
import org.fuusio.flx.core.type.impl.asKey
import org.fuusio.flx.core.type.impl.hashCapacityFor
import org.fuusio.flx.core.vm.Ctx
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoField
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.random.Random
import kotlin.reflect.KClass

/**
 * `is` with an object name. Both `is` specs share the symbol, so either may be the one called:
 * this one answers as [fnIsInstance] does, for objects and for other values alike.
 */
fun fnObjectIs(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    fnIsInstance(ctx, args, signatureIndex)

fun fnNumberHasDecimals(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    when (val number = args[0] as Number) {
        is Double -> number.hasDecimals()
        is Float -> number.hasDecimals()
        else -> false
    }

fun fnNumberIsWholeNumber(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    when (val number = args[0] as Number) {
        is Double -> !number.hasDecimals()
        is Float -> !number.hasDecimals()
        else -> true
    }

fun fnCalculatorNew(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Calculator =
    Calculator()

fun fnCalculatorDigit(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double {
    val calculator = args[0] as Calculator
    val number = (args[1] as Number).toInt()
    val digit = when {
        number < 0 -> 0
        number > 9 -> 9
        else -> number
    }
    return calculator.inputDigit(digit)
}

fun fnCalculatorDecimalPoint(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    (args[0] as Calculator).inputDecimalPoint()

fun fnCalculatorAddition(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    (args[0] as Calculator).inputAddition()

fun fnCalculatorSubtraction(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    (args[0] as Calculator).inputSubtraction()

fun fnCalculatorMultiplication(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    (args[0] as Calculator).inputMultiplication()

fun fnCalculatorDivision(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    (args[0] as Calculator).inputDivision()

fun fnCalculatorPercentage(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    (args[0] as Calculator).inputPercentage()

fun fnCalculatorToggleSign(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    (args[0] as Calculator).inputToggleSign()

fun fnCalculatorClear(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    (args[0] as Calculator).inputClear()

fun fnCalculatorClearEntry(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    (args[0] as Calculator).inputClearEntry()

fun fnCalculatorEquals(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    (args[0] as Calculator).inputEquals()

fun fnCalculatorHasError(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    (args[0] as Calculator).hasError()

fun fnCalculatorOperand1(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val calculator = (args[0] as Calculator)
    return if (calculator.hasOperand1) calculator.operand1 else Null
}

fun fnCalculatorOperand2(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val calculator = (args[0] as Calculator)
    return if (calculator.hasOperand2) calculator.operand2 else Null
}

fun fnCalculatorOperator(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val label = (args[0] as Calculator).getOperatorLabel()
    return label.ifEmpty { Null }
}

fun fnLogD(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Null {
    Flx.logger.debug(args[0].toString())
    return Null
}

fun fnLogI(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Null {
    Flx.logger.info(args[0].toString())
    return Null
}

fun fnLogW(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Null {
    Flx.logger.warning(args[0].toString())
    return Null
}

fun fnLogE(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Null {
    Flx.logger.error(args[0].toString())
    return Null
}

fun fnNoop(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int) {}

fun fnAbs(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number =
    when (val number = args[0] as Number) {
        is Float -> number.toDouble().absoluteValue
        is Double -> number.absoluteValue
        else -> number.toLong().absoluteValue
    }

fun fnAcos(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    kotlin.math.acos((args[0] as Number).toDouble())

fun fnAddition(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    // Whole numbers are added as Long, exactly; the sum switches to Double at the first Double or
    // Float argument, or if it would overflow a Long. It used to be Double throughout, which gave
    // wrong results for sums beyond 2^53 and boxed every step.
    var sum = 0L
    var i = 0
    while (i < args.size) {
        val arg = args[i]
        if (!arg.isIntegralNumber()) break
        val n = (arg as Number).toLong()
        val result = sum + n
        if (((sum xor result) and (n xor result)) < 0L) break // overflow
        sum = result
        i++
    }
    if (i == args.size) return sum

    var value = sum.toDouble()
    for (j in i until args.size) value += (args[j] as Number).toDouble()
    return value
}

fun fnAnd(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    for (arg in args) if (arg.isFalse()) return false
    return true
}

fun fnAsin(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    kotlin.math.asin((args[0] as Number).toDouble())

fun fnAtan(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    kotlin.math.atan((args[0] as Number).toDouble())

fun fnAtan2(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    kotlin.math.atan2((args[0] as Number).toDouble(), (args[1] as Number).toDouble())

fun fnHypot(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double {
    val x = (args[0] as Number).toDouble()
    val y = (args[1] as Number).toDouble()
    return if (args.size == 2) {
        kotlin.math.hypot(x, y)
    } else {
        val z = (args[2] as Number).toDouble()
        kotlin.math.hypot(kotlin.math.hypot(x, y), z)
    }
}

fun fnNumberClamp(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    val value = args[0] as Number
    val min = args[1] as Number
    val max = args[2] as Number
    return if (value.isIntegralNumber() && min.isIntegralNumber() && max.isIntegralNumber()) {
        value.toLong().coerceIn(min.toLong(), max.toLong())
    } else {
        value.toDouble().coerceIn(min.toDouble(), max.toDouble())
    }
}

fun fnStringPlus(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String =
    (args[0] as String) + args[1].toString()

fun fnStringIsBlank(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    when (val value = args[0]) {
        is String -> value.isBlank()
        is Null -> true
        else -> throw IllegalStateException()
    }

fun fnStringButlast(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String {
    val count = (args[0] as Number).toInt()
    return when (val string = args[1]) {
        is String -> {
            val length = string.length
            if (count > length) "" else string.substring(0, length - count)
        }
        is Null -> ""
        else -> throw IllegalStateException()
    }
}

fun fnNumberBitAnd(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    var isLongUsed = false
    val operand1 = when (val number = args[0] as Number) {
        is Byte -> number.toInt()
        is Double -> {
            isLongUsed = true
            number.toLong()
        }
        is Float -> number.toInt()
        is Int -> number
        is Long -> {
            isLongUsed = true
            number
        }
        is Short -> number.toInt()
        else -> throw IllegalStateException()
    }
    val operand2 = when (val number = args[1] as Number) {
        is Byte -> number.toInt()
        is Double -> {
            isLongUsed = true
            number.toLong()
        }
        is Float -> number.toInt()
        is Int -> number
        is Long -> {
            isLongUsed = true
            number
        }
        is Short -> number.toInt()
        else -> throw IllegalStateException()
    }
    return if (isLongUsed) operand1.toLong() and operand2.toLong()
        else operand1.toInt() and operand2.toInt()
}

fun fnNumberInv(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number =
    when (val number = args[0] as Number) {
        is Byte -> number.toInt().inv()
        is Double -> number.toLong().inv()
        is Float -> number.toInt().inv()
        is Int -> number.inv()
        is Long -> number.inv()
        is Short -> number.toInt().inv()
        else -> throw IllegalStateException()
    }

fun fnNumberBitOr(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    var isLongUsed = false
    val operand1 = when (val number = args[0] as Number) {
        is Byte -> number.toInt()
        is Double -> {
            isLongUsed = true
            number.toLong()
        }
        is Float -> number.toInt()
        is Int -> number
        is Long -> {
            isLongUsed = true
            number
        }
        is Short -> number.toInt()
        else -> throw IllegalStateException()
    }
    val operand2 = when (val number = args[1] as Number) {
        is Byte -> number.toInt()
        is Double -> {
            isLongUsed = true
            number.toLong()
        }
        is Float -> number.toInt()
        is Int -> number
        is Long -> {
            isLongUsed = true
            number
        }
        is Short -> number.toInt()
        else -> throw IllegalStateException()
    }
    return if (isLongUsed) operand1.toLong() or operand2.toLong()
    else operand1.toInt() or operand2.toInt()
}

fun fnNumberShl(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long =
    toLong(args[0]).shl((args[1] as Number).toInt())

fun fnNumberShr(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long =
    toLong(args[0]).shr((args[1] as Number).toInt())

fun fnNumberUshr(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long =
    toLong(args[0]).ushr((args[1] as Number).toInt())

fun fnNumberBitXor(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    var isLongUsed = false
    val operand1 = when (val number = args[0] as Number) {
        is Byte -> number.toInt()
        is Double -> {
            isLongUsed = true
            number.toLong()
        }
        is Float -> number.toInt()
        is Int -> number
        is Long -> {
            isLongUsed = true
            number
        }
        is Short -> number.toInt()
        else -> throw IllegalStateException()
    }
    val operand2 = when (val number = args[1] as Number) {
        is Byte -> number.toInt()
        is Double -> {
            isLongUsed = true
            number.toLong()
        }
        is Float -> number.toInt()
        is Int -> number
        is Long -> {
            isLongUsed = true
            number
        }
        is Short -> number.toInt()
        else -> throw IllegalStateException()
    }
    return if (isLongUsed) operand1.toLong() xor operand2.toLong()
    else operand1.toInt() xor operand2.toInt()
}

private fun toLong(any: Any): Long = (any as Number).toLong()

fun fnCeil(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    kotlin.math.ceil((args[0] as Number).toDouble())

fun fnStringCapitalize(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String =
    (args[0] as String).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

fun fnStringChop(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String {
    val string = args[0] as String
    return if (string.isNotEmpty()) string.substring(0, string.length - 1) else string
}

fun fnColor(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Color {
    val alpha = if (args.size == 4) normalizeColorComponent((args[3] as Number).toInt()) else 255
    val red = normalizeColorComponent((args[0] as Number).toInt())
    val green = normalizeColorComponent((args[1] as Number).toInt())
    val blue = normalizeColorComponent((args[2] as Number).toInt())
    return Color(android.graphics.Color.argb(alpha, red, green, blue))
}

private fun normalizeColorComponent(value: Int): Int =
    when {
        value < 0 -> 0
        value > 255 -> 255
        else -> value
    }

fun fnCompare(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long {
    val string1 = args[0] as String
    val string2 = args[1] as String
    return string1.compareTo(string2).toLong()
}

fun fnArrayAddAll(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Array {
    val array = args[0] as ImmutableArray
    array.addAll(args[1] as Collection)
    return array
}

fun fnListAddAll(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): List {
    val list = args[0] as ImmutableList
    list.addAll(args[1] as Collection)
    return list
}

fun fnSetAddAll(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Set {
    val set = args[0] as ImmutableSet
    set.addAll(args[1] as Collection)
    return set
}

fun fnArrayAdd(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    (args[0] as ImmutableArray).add(args[1])

fun fnListAdd(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    (args[0] as ImmutableList).add(args[1])

fun fnSetAdd(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    (args[0] as ImmutableSet).add(args[1])

fun fnCollectionRemove(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    when (val collection = args[0]) {
        is ImmutableArray -> collection.remove(args[1])
        is ImmutableList -> collection.remove(args[1])
        is ImmutableSet -> collection.remove(args[1])
        else -> false
    }

fun fnArrayRemove(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    (args[0] as ImmutableArray).remove(args[1])

fun fnListRemove(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    (args[0] as ImmutableList).remove(args[1])

fun fnSetRemove(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    (args[0] as ImmutableSet).remove(args[1])


fun fnCollectionClear(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int) {
    (args[0] as Collection).clear()
}

fun fnCollectionIsEmpty(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    val collection = args[0] as Collection
    return collection.isEmpty()
}

fun fnCollectionIsNotEmpty(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    val collection = args[0] as Collection
    return collection.isNotEmpty()
}

fun fnConj(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any =
    when (val collection = args[0]) {
        is Null -> collection.conj(args[1])
        is Array -> collection.conj(args[1])
        is List -> collection.conj(args[1])
        is Set -> collection.conj(args[1])
        else -> throw IllegalStateException()
    }

fun fnCons(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): List =
    when (val sequence = args[1]) {
        is Null -> sequence.cons(args[0])
        is Sequence -> sequence.cons(args[0])
        is String -> sequence.cons(args[0])
        is Set -> sequence.cons(args[0])
        else -> throw IllegalStateException()
    }

fun fnCos(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    kotlin.math.cos((args[0] as Number).toDouble())

fun fnDate(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Date = Date()

private fun zonedDateTime(date: Date): ZonedDateTime =
    Instant.ofEpochMilli(date.time).atZone(ZoneId.systemDefault())

fun fnDateGetYear(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long =
    zonedDateTime(args[0] as Date).year.toLong()

fun fnDateGetDay(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long =
    zonedDateTime(args[0] as Date).dayOfMonth.toLong()

fun fnDateGetMonth(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long =
    zonedDateTime(args[0] as Date).monthValue.toLong()

fun fnDateGetHour(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long =
    zonedDateTime(args[0] as Date).hour.toLong()

fun fnDateGetMinute(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long =
    zonedDateTime(args[0] as Date).minute.toLong()

fun fnDateGetSecond(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long =
    zonedDateTime(args[0] as Date).second.toLong()

fun fnDateGetMillisecond(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long =
    zonedDateTime(args[0] as Date).get(ChronoField.MILLI_OF_SECOND).toLong()

fun fnNumberDec(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    return when (val number = args[0]) {
        is Byte -> number - 1
        is Double -> number - 1.0
        is Float -> number - 1.0f
        is Int -> number - 1
        is Long -> number - 1L
        is Short -> number - 1
        else -> throw IllegalStateException()
    }
}


fun fnDivision(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    var value = 0.0
    val argsCount = args.size

    if (argsCount > 0) {
        value = (args[0] as Number).toDouble()

        // It is the divisors that must not be zero. The check used to test the dividend instead,
        // so (/ 0 5) failed and (/ 5 0) returned Infinity.
        for (i in 1 until argsCount) {
            val divisor = (args[i] as Number).toDouble()
            if (divisor >= -EPSILON && divisor <= EPSILON) throw ZeroDivisionException(ctx)
            value /= divisor
        }
    }
    return value
}

fun fnStringDrop(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val firstIsNumber = args[0] is Number && args[1] !is Number
    val target = if (firstIsNumber) args[1] else args[0]
    val count = ((if (firstIsNumber) args[0] else args[1]) as Number).toInt().coerceAtLeast(0)
    return when (target) {
        is String -> target.drop(count)
        is Null -> ""
        is ImmutableArray -> {
            if (count == 0) target
            else if (count >= target.size()) ImmutableArray()
            else ImmutableArray(target.elements.subList(count, target.size()))
        }
        is ImmutableList -> {
            if (count == 0) target
            else if (count >= target.size()) ImmutableList()
            else ImmutableList(target.elements.subList(count, target.size()))
        }
        is Sequence -> {
            val size = target.size()
            if (count >= size) ImmutableList()
            else {
                val list = ArrayList<Any>(size - count)
                for (i in count until size) list.add(target[i])
                if (target is Array) ImmutableArray.create(list) else ImmutableList.create(list)
            }
        }
        else -> throw IllegalStateException()
    }
}

fun fnStringDropLast(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val firstIsNumber = args[0] is Number && args[1] !is Number
    val target = if (firstIsNumber) args[1] else args[0]
    val count = ((if (firstIsNumber) args[0] else args[1]) as Number).toInt().coerceAtLeast(0)
    return when (target) {
        is String -> target.dropLast(count)
        is Null -> ""
        is ImmutableArray -> {
            if (count == 0) target
            else if (count >= target.size()) ImmutableArray()
            else ImmutableArray(target.elements.subList(0, target.size() - count))
        }
        is ImmutableList -> {
            if (count == 0) target
            else if (count >= target.size()) ImmutableList()
            else ImmutableList(target.elements.subList(0, target.size() - count))
        }
        is Sequence -> {
            val size = target.size()
            if (count >= size) ImmutableList()
            else {
                val end = size - count
                val list = ArrayList<Any>(end)
                for (i in 0 until end) list.add(target[i])
                if (target is Array) ImmutableArray.create(list) else ImmutableList.create(list)
            }
        }
        else -> throw IllegalStateException()
    }
}

fun fnEqual(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    val first = args[0]
    for (i in 1 until args.size) if (!flxEquals(first, args[i])) return false
    return true
}

fun fnStringEndsWith(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    (args[0] as String).endsWith(args[1] as String)

fun fnEpochTime(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long =
    System.currentTimeMillis()

fun fnEval(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int) = args[0].eval(ctx)

fun fnEvalString(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int) = fnParse(
    ctx,
    args,
    signatureIndex
).eval(ctx)

fun fnExp(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    kotlin.math.exp((args[0] as Number).toDouble())

fun fnCollectionFilter(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): List {
    val firstIsCollection = args[0] is Sequence || args[0] is Set || (args[0] is Map && args[1] !is Sequence && args[1] !is Set)
    val collection = if (firstIsCollection) args[0] else args[1]
    val predicate = if (firstIsCollection) args[1] else args[0]
    val filtered = mutableListOf<Any>()
    when (collection) {
        is ImmutableArray -> {
            collection.elements.forEach { element ->
                if (invokeUnary(ctx, predicate, element).toBoolean()) filtered.add(element)
            }
        }
        is ImmutableList -> {
            collection.elements.forEach { element ->
                if (invokeUnary(ctx, predicate, element).toBoolean()) filtered.add(element)
            }
        }
        is ImmutableSet -> {
            collection.elements.forEach { element ->
                if (invokeUnary(ctx, predicate, element).toBoolean()) filtered.add(element)
            }
        }
        is Sequence -> {
            val size = collection.size()
            for (i in 0 until size) {
                val element = collection[i]
                if (invokeUnary(ctx, predicate, element).toBoolean()) filtered.add(element)
            }
        }
        is Map -> {
            val list = collection.toList()
            for (i in 0 until list.size()) {
                val element = list[i]
                if (invokeUnary(ctx, predicate, element).toBoolean()) filtered.add(element)
            }
        }
        is Set -> {
            val list = collection.toList()
            for (i in 0 until list.size()) {
                val element = list[i]
                if (invokeUnary(ctx, predicate, element).toBoolean()) filtered.add(element)
            }
        }
        else -> throw IllegalStateException()
    }
    return ImmutableList.create(filtered)
}

fun fnSequenceFirst(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any =
    when (val arg = args[0]) {
        is String -> if (arg.isEmpty()) Null else arg[0] // nil, as of an empty array
        is Sequence -> arg.first()
        else -> throw IllegalStateException()
    }

fun fnFloor(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    kotlin.math.floor((args[0] as Number).toDouble())

fun fnFormat(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String {
    val argsCount = args.size
    var string = when (val form = args[0]) {
        is String -> form
        else -> form.toLiteral()
    }

    if (args.size > 1) {
        val formatArgs = (1 until argsCount).map { args[it] }.toTypedArray()
        try {
            string = string.format(*formatArgs)
        } catch (e: Exception) {
            throw StringFormatException(ctx, e.message!!)
        }
    }
    return string
}

fun fnGet(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any =
    when (signatureIndex) {
        0 -> fnJsonGet(ctx, args, signatureIndex)
        1 -> fnJsonGet(ctx, args, signatureIndex)
        2 -> fnMapGet(ctx, args, signatureIndex)
        3 -> fnMapGet(ctx, args, signatureIndex)
        else -> throw IllegalStateException()
    }

fun fnMapGet(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val map = args[0] as Map
    return map.get(args[1], if (args.size == 3) args[2] else Null)
}

fun fnMapAssoc(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Map =
    when (val map = args[0]) {
        is ImmutableMap -> map.assoc(args)
        is Map -> {
            var result: Map = map
            var i = 1
            while (i + 1 < args.size) {
                result = result.put(args[i], args[i + 1])
                i += 2
            }
            result
        }
        else -> throw IllegalStateException()
    }

fun fnMapDissoc(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Map =
    when (val map = args[0]) {
        is ImmutableMap -> map.dissoc(args)
        is Map -> {
            var result: Map = map
            for (i in 1 until args.size) result = result.removeKey(args[i])
            result
        }
        else -> throw IllegalStateException()
    }

fun fnMapMerge(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Map {
    if (args.isEmpty()) return ImmutableMap()
    var result = args[0] as Map
    for (i in 1 until args.size) {
        val next = args[i] as Map
        result = when (val current = result) {
            is ImmutableMap -> current.merge(next)
            else -> {
                var updated = current
                next.keys().forEach { key -> updated = updated.put(key, next[key]) }
                updated
            }
        }
    }
    return result
}

fun fnMapKeys(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Array =
    when (val target = args[0]) {
        is Map -> ImmutableArray.create(target.keys().toList())
        is JsonObject -> ImmutableArray.create(target.keys.map { convertJsonKeyToForm(it) })
        else -> throw IllegalStateException()
    }

fun fnMapValues(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Array =
    when (val target = args[0]) {
        is Map -> ImmutableArray.create(target.values().toList())
        is JsonObject -> ImmutableArray.create(target.values.map { jsonElementToForm(ctx, it) })
        else -> throw IllegalStateException()
    }

fun fnMapContainsKey(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    val key = args[1]
    return when (val target = args[0]) {
        is Map -> target.containsKey(key)
        is JsonObject -> {
            val field = key as? String ?: key.toLiteral()
            target.containsKey(field)
        }
        else -> throw IllegalStateException()
    }
}

private fun parsePathSteps(pathArg: Any): kotlin.collections.List<Any> =
    when (pathArg) {
        is String -> pathArg.split('/').filter { it.isNotEmpty() }
        is ImmutableArray -> pathArg.elements
        is ImmutableList -> pathArg.elements
        is Sequence -> ArrayList<Any>(pathArg.size()).apply {
            for (i in 0 until pathArg.size()) add(pathArg[i])
        }
        else -> listOf(pathArg)
    }

private fun resolveMapKey(map: Map, step: Any): Any {
    if (map.containsKey(step)) return step
    if (step is Keyword && map.containsKey(step.symbol.name)) return step.symbol.name
    if (step is String) {
        map.keys().firstOrNull { key ->
            (key as? String ?: key.toLiteral()) == step || (key is Keyword && key.symbol.name == step)
        }?.let { return it }
        if (step.length > 1 && step.startsWith(':')) return Flx.getKeyword(step.substring(1))
    }
    return step
}

private fun newMapKey(step: Any): Any =
    if (step is String && step.length > 1 && step.startsWith(':')) Flx.getKeyword(step.substring(1)) else step

private fun updateAtPath(
    ctx: Ctx,
    current: Any,
    steps: kotlin.collections.List<Any>,
    depth: Int,
    updater: (Any) -> Any,
): Any {
    if (depth >= steps.size) return updater(current)
    val normalized = when (current) {
        is JsonObject -> jsonObjectToMap(ctx, current)
        is JsonArray -> jsonArrayToArray(ctx, current)
        else -> current
    }
    val step = steps[depth]
    return when (normalized) {
        is Map -> {
            val key = resolveMapKey(normalized, step)
            val child = if (normalized.containsKey(key)) normalized[key] else Null
            normalized.put(key, updateAtPath(ctx, child, steps, depth + 1, updater))
        }
        is Sequence -> {
            if (normalized === Null) {
                val key = newMapKey(step)
                ImmutableMap().put(key, updateAtPath(ctx, Null, steps, depth + 1, updater))
            } else {
                val index = when (step) {
                    is Number -> step.toInt()
                    is String -> step.toIntOrNull() ?: throw IndexOutOfBoundsException("Invalid sequence index: $step")
                    else -> throw IndexOutOfBoundsException("Invalid sequence index: $step")
                }
                if (index < 0 || index >= normalized.size()) {
                    throw IndexOutOfBoundsException("Index $index out of bounds for length ${normalized.size()}")
                }
                val child = normalized[index]
                normalized.setAt(index, updateAtPath(ctx, child, steps, depth + 1, updater))
            }
        }
        else -> {
            val key = newMapKey(step)
            ImmutableMap().put(key, updateAtPath(ctx, Null, steps, depth + 1, updater))
        }
    }
}

fun fnAssocIn(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val steps = parsePathSteps(args[1])
    if (steps.isEmpty()) return args[0]
    val newValue = args[2]
    return updateAtPath(ctx, args[0], steps, 0) { newValue }
}

fun fnUpdateIn(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val steps = parsePathSteps(args[1])
    val fn = args[2]
    if (steps.isEmpty()) return invokeUnary(ctx, fn, args[0])
    return updateAtPath(ctx, args[0], steps, 0) { oldValue ->
        invokeUnary(ctx, fn, oldValue)
    }
}

fun fnJsonGet(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    var jsonObject = args[0] as JsonObject
    val field = when (val key = args[1]) {
        is String -> key
        else -> key.toLiteral()
    }
    if (field.contains('/')) {
        val fields = field.split("/")
        fields.forEach { key ->
            when (val value = jsonObject[key]) {
                is JsonObject -> jsonObject = value
                null -> return if (args.size == 3) args[2] else Null
                else -> return value
            }
        }
        return jsonObject
    } else {
        return jsonObject[field] ?: if (args.size == 3) args[2] else Null
    }
}

fun fnGreaterThan(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    var value = args[0] as Number

    for (i in 1 until args.size) {
        val nextValue = args[i] as Number
        if (value.isLessThanOrEqualTo(nextValue)) return false
        value = nextValue
    }
    return true
}

fun fnGreaterThanOrEqual(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    var value = args[0] as Number

    for (i in 1 until args.size) {
        val nextValue = args[i] as Number
        if (value.isLessThan(nextValue)) return false
        value = nextValue
    }
    return true
}

fun fnIdentical(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    args[0] === args[1]

fun fnNumberInc(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    return when (val number = args[0]) {
        is Byte -> number + 1
        is Double -> number + 1.0
        is Float -> number + 1.0f
        is Int -> number + 1
        is Long -> number + 1L
        is Short -> number + 1
        else -> throw IllegalStateException()
    }
}

fun fnStringIndexOf(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Int {
    val startIndex = if (args.size >= 3) (args[2] as Number).toInt().coerceAtLeast(0) else 0
    return when (val target = args[0]) {
        is String -> {
            val needle = when (val item = args[1]) {
                is String -> item
                is Char -> item.toString()
                else -> item.toLiteral()
            }
            target.indexOf(needle, startIndex)
        }
        is ImmutableArray -> {
            val needle = args[1]
            val elements = target.elements
            for (i in startIndex until elements.size) {
                if (flxEquals(elements[i], needle)) return i
            }
            -1
        }
        is ImmutableList -> {
            val needle = args[1]
            val elements = target.elements
            for (i in startIndex until elements.size) {
                if (flxEquals(elements[i], needle)) return i
            }
            -1
        }
        is Sequence -> {
            val needle = args[1]
            val size = target.size()
            for (i in startIndex until size) {
                if (flxEquals(target[i], needle)) return i
            }
            -1
        }
        else -> throw IllegalStateException()
    }
}

fun fnAnyIsArray(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    args[0].isArray()

fun fnAnyIsBoolean(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    args[0].isBoolean()

fun fnAnyIsByte(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean = args[0].isByte()

fun fnAnyIsDoubleQ(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    args[0].isDouble()

fun fnSizableIsEmpty(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    when (val arg = args[0]) {
        is String -> arg.isEmpty()
        is Sizable -> arg.isEmpty()
        else -> throw IllegalStateException()
    }

fun fnSizableIsNotEmpty(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    when (val arg = args[0]) {
        is String -> arg.isNotEmpty()
        is Sizable -> arg.isNotEmpty()
        else -> throw IllegalStateException()
    }

fun fnNumberIsEven(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    (args[0] as Number).toLong() % 2 == 0L

fun fnAnyIsFalse(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    args[0].isFalse()

fun fnAnyIsJsonArray(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    args[0] is JsonArray

fun fnAnyIsJsonElement(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    args[0] is JsonElement

fun fnAnyIsJsonObject(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    args[0] is JsonObject

fun fnAnyIsJsonPrimitive(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    args[0] is JsonPrimitive

fun fnAnyIsMap(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean = args[0].isMap()

fun fnAnyIsList(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean = args[0].isList()

fun fnAnyIsLong(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean = args[0].isLong()

fun fnAnyIsNegative(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    (args[0] as Number).toDouble() < 0

fun fnAnyIsNull(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean = args[0].isNull()

fun fnAnyIsNotNull(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    !args[0].isNull()

fun fnNumberIsOdd(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    (args[0] as Number).toLong() % 2 != 0L

fun fnNumberIsPositive(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    (args[0] as Number).toDouble() > 0

fun fnAnyIsSet(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean = args[0].isSet()

fun fnAnyIsString(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    args[0].isString()

fun fnAnyIsSymbol(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    args[0].isSymbol()

fun fnAnyIsTrue(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean = args[0].isTrue()

private const val EPSILON = 0.000000000001

fun fnNumberIsZero(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    when (val number = (args[0] as Number)) {
        is Double -> number >= -EPSILON && number <= EPSILON
        is Float -> number >= -EPSILON && number <= EPSILON
        // Any whole number: a Long zero is not == to the Int 0.
        else -> number.toLong() == 0L
    }

fun fnSequenceLast(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any =
    when (val arg = args[0]) {
        is String -> if (arg.isEmpty()) Null else arg[arg.length - 1] // nil, as of an empty array
        is Sequence -> arg.last()
        else -> throw IllegalStateException()
    }

fun fnAnyGetClassName(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String =
    args[0]::class.java.name

/**
 * Whether [args]' first value is of the type the second names: a class value, an FLX object
 * declaration's name, or the qualified or simple name of the value's class or a supertype. Names are
 * only compared: no class is loaded by name, so FLX code cannot reach platform classes this way.
 */
fun fnIsInstance(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    val instance = args[0]
    return when (val type = args[1]) {
        is KClass<*> -> type.java.isInstance(instance)
        is Class<*> -> type.isInstance(instance)
        is String -> when (instance) {
            is ObjectInstance -> instance.declaration.name == type
            else -> instance.javaClass.isNamed(type)
        }
        else -> type::class.java.isInstance(instance)
    }
}

private fun Class<*>.isNamed(name: String): Boolean =
    this.name == name || simpleName == name ||
        superclass?.isNamed(name) == true || interfaces.any { it.isNamed(name) }

fun fnLesserThan(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    var value = args[0] as Number

    for (i in 1 until args.size) {
        val nextValue = args[i] as Number
        if (value.isGreaterThanOrEqualTo(nextValue)) return false
        value = nextValue
    }
    return true
}

fun fnLesserThanOrEqual(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    var value = args[0] as Number

    for (i in 1 until args.size) {
        val nextValue = args[i] as Number
        if (value.isGreaterThan(nextValue)) return false
        value = nextValue
    }
    return true
}

fun fnLog(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    kotlin.math.log((args[0] as Number).toDouble(), (args[1] as Number).toDouble())

fun fnStringToLowerCase(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String =
    (args[0] as String).lowercase(Locale.ROOT)

private const val REGEX_CACHE_CAPACITY = 64

private val regexCache = object : LinkedHashMap<String, Regex>(16, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Regex>?): Boolean =
        size > REGEX_CACHE_CAPACITY
}

private fun cachedRegex(pattern: String): Regex =
    synchronized(regexCache) {
        regexCache.getOrPut(pattern) { pattern.toRegex() }
    }

fun fnStringMatches(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    val regex = when (val arg = args[1]) {
        is String -> cachedRegex(arg)
        is Regex -> arg
        else -> throw IllegalStateException()
    }
    return regex.matches(args[0] as String)
}

fun fnMax(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    // The result is a Long when the largest argument is a whole number, a Double otherwise. The
    // running maximum used to start from Double.MIN_VALUE — the smallest *positive* double — so the
    // maximum of negative numbers came out as 0.
    if (args.isEmpty()) return 0L
    var max = args[0] as Number
    for (i in 1 until args.size) {
        val arg = args[i] as Number
        if (arg.isGreaterThan(max)) max = arg
    }
    return if (max.isIntegralNumber()) max.toLong() else max.toDouble()
}

fun fnMin(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    // The result is a Long when the smallest argument is a whole number, a Double otherwise.
    if (args.isEmpty()) return Long.MAX_VALUE
    var min = args[0] as Number
    for (i in 1 until args.size) {
        val arg = args[i] as Number
        if (arg.isLessThan(min)) min = arg
    }
    return if (min.isIntegralNumber()) min.toLong() else min.toDouble()
}

fun fnMultiplication(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    // As fnAddition: exact Long arithmetic for whole numbers, Double from the first Double or Float
    // argument or on overflow.
    var product = 1L
    var i = 0
    while (i < args.size) {
        val arg = args[i]
        if (!arg.isIntegralNumber()) break
        val n = (arg as Number).toLong()
        val result = product * n
        val overflows = product != 0L &&
            (result / product != n || (product == -1L && n == Long.MIN_VALUE))
        if (overflows) break
        product = result
        i++
    }
    if (i == args.size) return product

    var value = product.toDouble()
    for (j in i until args.size) value *= (args[j] as Number).toDouble()
    return value
}

fun fnNot(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean = !args[0].toBoolean()

fun fnNotEqual(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    val first = args[0]
    for (i in 1 until args.size) if (!flxEquals(first, args[i])) return true
    return false
}

fun fnStringCharAt(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Char {
    val string = args[0] as String
    val index = (args[1] as Number).toInt()
    return string[index]
}

fun fnSequenceGet(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val indexable = args[0] as Indexable
    val index = (args[1] as Number).toInt()
    return indexable[index]
}

fun fnSequenceSetAt(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Sequence {
    val sequence = args[0] as Sequence
    val index = (args[1] as Number).toInt()
    return sequence.setAt(index, args[2])
}

fun fnSequenceRemoveAt(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Sequence {
    val sequence = args[0] as Sequence
    val index = (args[1] as Number).toInt()
    return sequence.removeAt(index)
}

fun fnSequenceInsertAt(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Sequence {
    val sequence = args[0] as Sequence
    val index = (args[1] as Number).toInt()
    return sequence.insertAt(index, args[2])
}

fun fnOr(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    for (arg in args) if (arg.isTrue()) return true
    return false
}

fun fnStringParseBoolean(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any =
    when (args[0].toString().lowercase(Locale.ROOT)) {
        "true" -> true
        "false" -> false
        else -> Null
    }

fun fnStringParseByte(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any =
    args[0].toString().toByteOrNull() ?: Null

fun fnStringParseDouble(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any =
    args[0].toString().toDoubleOrNull() ?: Null

fun fnStringParseFloat(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any =
    args[0].toString().toFloatOrNull() ?: Null

fun fnStringParseInt(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any =
    args[0].toString().toIntOrNull() ?: Null

fun fnStringParseLong(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any =
    args[0].toString().toLongOrNull() ?: Null

fun fnStringParseShort(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any =
    args[0].toString().toShortOrNull() ?: Null

fun fnNumberPow(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    (args[0] as Number).toDouble().pow((args[1] as Number).toDouble())

fun fnPrint(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    ctx.getFlxVm().print(fnFormat(ctx, args, signatureIndex))
    return Unit
}

fun fnPrintln(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    ctx.getFlxVm().println(fnFormat(ctx, args, signatureIndex))
    return Unit
}

fun fnRand(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    if (args.isEmpty()) Random.Default.nextDouble()
    else Random.Default.nextDouble((args[0] as Number).toDouble())

fun fnRandInt(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long =
    if (args.size == 1) {
        Random.Default.nextLong((args[0] as Number).toLong())
    } else {
        Random.Default.nextLong((args[0] as Number).toLong(), (args[1] as Number).toLong())
    }

fun fnRandChoice(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any =
    when (val collection = args[0]) {
        is Sequence -> if (collection.isEmpty()) Null else collection[Random.Default.nextInt(collection.size())]
        is ImmutableSet -> if (collection.isEmpty()) Null else collection.elements.elementAt(Random.Default.nextInt(collection.size()))
        is Set -> if (collection.isEmpty()) Null else collection.toArray().let { it[Random.Default.nextInt(it.size())] }
        else -> throw IllegalStateException()
    }

fun fnShuffle(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Array =
    when (val collection = args[0]) {
        is ImmutableArray -> {
            if (collection.size() <= 1) ImmutableArray(collection.elements)
            else ImmutableArray.create(collection.elements.shuffled(Random.Default))
        }
        is ImmutableList -> {
            if (collection.size() <= 1) ImmutableArray(collection.elements)
            else ImmutableArray.create(collection.elements.shuffled(Random.Default))
        }
        is ImmutableSet -> {
            if (collection.size() <= 1) collection.toArray()
            else ImmutableArray.create(collection.elements.shuffled(Random.Default))
        }
        is Sequence -> {
            val size = collection.size()
            val list = ArrayList<Any>(size)
            for (i in 0 until size) list.add(collection[i])
            list.shuffle(Random.Default)
            ImmutableArray.create(list)
        }
        is Set -> {
            val array = collection.toArray() as ImmutableArray
            ImmutableArray.create(array.elements.shuffled(Random.Default))
        }
        else -> throw IllegalStateException()
    }

fun fnParse(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    // Text that is not FLX is an error that says so; it used to be dropped, and then there was no
    // form to return (an index error). Text with no form in it — empty, or only a comment — is nil.
    val parserObserver = object : ParserObserver {
        override fun onNext(parsingResult: Any) {}
        override fun onError(error: ParserError) {
            throw ParseException(ctx, error, args[0] as String)
        }
    }
    val results = FormParser(parserObserver).parse(ParsingContext(), args[0] as String)
    return results.lastOrNull() ?: Null
}

fun fnStringIsEmpty(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    (args[0] as String).isEmpty()

fun fnStringIsNotEmpty(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    (args[0] as String).isNotEmpty()

fun fnStringLength(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Int =
    (args[0] as String).length

fun fnStringParseEval(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val parserObserver = object : ParserObserver {
        override fun onNext(parsingResult: Any) {}
        override fun onError(error: ParserError) {
            throw ParseException(ctx, error, args[0].toString())
        }
    }
    var result: Any = Null
    FormParser(parserObserver).parse(ParsingContext(), args[0].toString()).forEach { form ->
        result = form.eval(ctx)
    }
    return result
}

fun fnCompile(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): ParsedForm =
    ParsedForm(args[0].toString())

fun fnRem(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    val divider = args[1] as Number
    return when (val number = args[0] as Number) {
        is Byte -> rem(number, divider)
        is Double -> rem(number, divider)
        is Float -> rem(number, divider)
        is Int -> rem(number, divider)
        is Long -> rem(number, divider)
        is Short -> rem(number, divider)
        else -> throw IllegalStateException()
    }
}

fun rem(byte: Byte, divider: Number): Number =
    when (divider) {
        is Byte -> byte.rem(divider)
        is Double -> byte.rem(divider)
        is Float -> byte.rem(divider)
        is Int -> byte.rem(divider)
        is Long -> byte.rem(divider)
        is Short -> byte.rem(divider)
        else -> throw IllegalStateException()
    }

fun rem(double: Double, divider: Number): Number =
    when (divider) {
        is Byte -> double.rem(divider)
        is Double -> double.rem(divider)
        is Float -> double.rem(divider)
        is Int -> double.rem(divider)
        is Long -> double.rem(divider)
        is Short -> double.rem(divider)
        else -> throw IllegalStateException()
    }

fun rem(float: Float, divider: Number): Number =
    when (divider) {
        is Byte -> float.rem(divider)
        is Double -> float.rem(divider)
        is Float -> float.rem(divider)
        is Int -> float.rem(divider)
        is Long -> float.rem(divider)
        is Short -> float.rem(divider)
        else -> throw IllegalStateException()
    }

fun rem(int: Int, divider: Number): Number =
    when (divider) {
        is Byte -> int.rem(divider)
        is Double -> int.rem(divider)
        is Float -> int.rem(divider)
        is Int -> int.rem(divider)
        is Long -> int.rem(divider)
        is Short -> int.rem(divider)
        else -> throw IllegalStateException()
    }

fun rem(long: Long, divider: Number): Number =
    when (divider) {
        is Byte -> long.rem(divider)
        is Double -> long.rem(divider)
        is Float -> long.rem(divider)
        is Int -> long.rem(divider)
        is Long -> long.rem(divider)
        is Short -> long.rem(divider)
        else -> throw IllegalStateException()
    }

fun rem(short: Short, divider: Number): Number =
    when (divider) {
        is Byte -> short.rem(divider)
        is Double -> short.rem(divider)
        is Float -> short.rem(divider)
        is Int -> short.rem(divider)
        is Long -> short.rem(divider)
        is Short -> short.rem(divider)
        else -> throw IllegalStateException()
    }

fun fnStringRepeat(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String {
    val string = args[0].toString()
    val times = (args[1] as Number).toLong()
    if (times <= 0L || string.isEmpty()) return ""
    if (times > MAX_GENERATED_CHARS / string.length) throw ResultTooLargeException(ctx, ".repeat", "$MAX_GENERATED_CHARS characters")
    val builder = StringBuilder(string.length * times.toInt())
    repeat(times.toInt()) { builder.append(string) }
    return builder.toString()
}

fun fnStringReplaceChar(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String {
    val oldChar = args[1] as Char
    val newChar = args[2] as Char
    return (args[0] as String).replace(oldChar, newChar)
}

fun fnStringReplaceFirstRegex(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String {
    val regex = when (val arg = args[1]) {
        is String -> cachedRegex(arg)
        is Regex -> arg
        else -> throw IllegalStateException()
    }
    return (args[0] as String).replaceFirst(regex, args[2] as String)
}

fun fnStringReplaceFirstString(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String =
    (args[0] as String).replaceFirst(args[1] as String, args[2] as String)


fun fnStringReplaceRegex(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String {
    val regex = when (val arg = args[1]) {
        is String -> cachedRegex(arg)
        is Regex -> arg
        else -> throw IllegalStateException()
    }
    return (args[0] as String).replace(regex, args[2] as String)
}

fun fnStringReplace(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String =
    (args[0] as String).replace(args[1] as String, args[2] as String)

fun fnSequenceRest(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Sequence =
    when (val arg = args[0]) {
        is Sequence -> arg.rest()
        is String -> {
            when (arg.length) {
                0 -> ImmutableArray()
                else -> ImmutableArray.create(arg.substring(1))
            }
        }
        else -> throw IllegalStateException()
    }

fun fnStringReverse(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any =
    when (val target = args[0]) {
        is String -> target.reversed()
        is Null -> ""
        is ImmutableArray -> {
            if (target.size() <= 1) target
            else ImmutableArray.create(target.elements.asReversed().toList())
        }
        is ImmutableList -> {
            if (target.size() <= 1) target
            else ImmutableList.create(target.elements.asReversed().toList())
        }
        is Sequence -> {
            val size = target.size()
            val list = ArrayList<Any>(size)
            for (i in size - 1 downTo 0) list.add(target[i])
            if (target is Array) ImmutableArray.create(list) else ImmutableList.create(list)
        }
        else -> throw IllegalStateException()
    }

fun fnRound(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    kotlin.math.round((args[0] as Number).toDouble())

fun fnNumberSign(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long {
    val number = (args[0] as Number).toDouble()
    return when {
        number < 0.0 -> -1L
        number > 0.0 -> 1L
        else -> 0L
    }
}

fun fnSqrt(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    kotlin.math.sqrt((args[0] as Number).toDouble())

fun fnSin(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    kotlin.math.sin((args[0] as Number).toDouble())

fun fnSizeableSize(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long =
    when (val arg = args[0]) {
        is String -> arg.length.toLong()
        is Sizable -> arg.size().toLong()
        else -> throw IllegalStateException()
    }

fun fnStringSplit(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Array {
    val regex = when (val arg = args[1]) {
        is String -> cachedRegex(arg)
        is Regex -> arg
        else -> throw IllegalStateException()
    }
    val limit = if (args.size == 3) (args[2] as Number).toInt() else Int.MAX_VALUE
    val parts = (args[0] as String).split(regex, limit)
    val elements = ArrayList<Any>(parts.size)
    elements.addAll(parts)
    return ImmutableArray.create(elements)
}

fun fnStringSplitLines(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Array {
    val elements = mutableListOf<Any>()
    val string = (args[0] as String).replace("\r\n", "\n")
    elements.addAll(string.split('\n'))
    return ImmutableArray.create(elements)
}

fun fnStringStartsWith(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    (args[0] as String).startsWith(args[1] as String)

fun fnStr(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String {
    val builder = StringBuilder()
    args.forEach { element ->
        builder.append(
            when (element) {
                is Null -> ""
                is String -> element
                is Char -> element.toString()
                else -> element.toLiteral()
            }
        )
    }
    return builder.toString()
}

fun fnArraySubArray(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Array {
    val array = args[0] as Array
    val start = (args[1] as Number).toInt()

    return if (args.size == 2) {
        array.subarray(start, array.size())
    } else {
        val end = (args[2] as Number).toInt()
        array.subarray(start, end)
    }
}

fun fnObservableSubscribe(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Subscription {
    val observable = args[0] as Observable
    val observer = args[1] as FunctionObject
    return observable.subscribe(observer)
}

fun fnSubscriptionUnsubscribe(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Observable {
    val subscription = args[0] as Subscription
    val observable = subscription.observable
    observable.unsubscribe(subscription)
    return observable
}

fun fnPeriodicTimerObservable(
    ctx: Ctx,
    args: kotlin.Array<Any>,
    signatureIndex: Int
): PeriodicTimerObservable {
    val initialDelay = (args[0] as Number).toLong()
    val period = (args[1] as Number).toLong()
    return PeriodicTimerObservable(ctx, initialDelay, period)
}

fun fnTimerObservable(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): TimerObservable {
    val period = (args[0] as Number).toLong()
    return TimerObservable(ctx, period)
}

fun fnSubscriptionStart(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Subscription {
    val subscription = args[0] as Subscription
    if (!subscription.observable.start()) {
        val observable = subscription.observable
        throw FailedToStartObservableException(ctx, observable, observable.getErrorDescription())
    }
    return subscription
}

fun fnSubscriptionPause(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Subscription {
    val subscription = args[0] as Subscription
    subscription.observable.pause()
    return subscription
}

fun fnSubscriptionResume(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Subscription {
    val subscription = args[0] as Subscription
    subscription.observable.resume()
    return subscription
}

fun fnSubscriptionStop(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Subscription {
    val subscription = args[0] as Subscription
    subscription.observable.stop()
    return subscription
}


fun fnStringSubstring(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String {
    val string = args[0] as String
    val start = (args[1] as Number).toInt()

    return if (args.size == 2) {
        string.substring(start)
    } else {
        val end = (args[2] as Number).toInt()
        string.substring(start, end)
    }
}

fun fnStringContains(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    (args[0] as String).contains(args[1] as String)


fun fnPercentage(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    val percentage = (args[0] as Number).toDouble()
    val total = (args[1] as Number).toDouble()
    return percentage / 100.0 * total
}

fun fnSubtraction(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    // As fnAddition: exact Long arithmetic for whole numbers, Double from the first Double or Float
    // argument or on overflow.
    val argsCount = args.size
    if (argsCount == 0) return 0L

    val first = args[0] as Number
    if (argsCount == 1) {
        return if (first.isIntegralNumber() && first.toLong() != Long.MIN_VALUE) -first.toLong()
        else -first.toDouble()
    }

    if (first.isIntegralNumber()) {
        var difference = first.toLong()
        var i = 1
        while (i < argsCount) {
            val arg = args[i]
            if (!arg.isIntegralNumber()) break
            val n = (arg as Number).toLong()
            val result = difference - n
            if (((difference xor n) and (difference xor result)) < 0L) break // overflow
            difference = result
            i++
        }
        if (i == argsCount) return difference

        var value = difference.toDouble()
        for (j in i until argsCount) value -= (args[j] as Number).toDouble()
        return value
    }

    var value = first.toDouble()
    for (j in 1 until argsCount) value -= (args[j] as Number).toDouble()
    return value
}

fun fnStringTail(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String {
    val count = (args[1] as Number).toInt()
    return when (val string = args[0]) {
        is String -> {
            val length = string.length
            if (count > length) "" else string.substring(length - count, length)
        }
        is Null -> ""
        else -> throw IllegalStateException()
    }
}

fun fnStringTake(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val firstIsNumber = args[0] is Number && args[1] !is Number
    val target = if (firstIsNumber) args[1] else args[0]
    val count = ((if (firstIsNumber) args[0] else args[1]) as Number).toInt().coerceAtLeast(0)
    return when (target) {
        is String -> {
            val length = target.length
            if (count > length) target else target.substring(0, count)
        }
        is Null -> ""
        is ImmutableArray -> {
            if (count == 0) ImmutableArray()
            else if (count >= target.size()) target
            else ImmutableArray(target.elements.subList(0, count))
        }
        is ImmutableList -> {
            if (count == 0) ImmutableList()
            else if (count >= target.size()) target
            else ImmutableList(target.elements.subList(0, count))
        }
        is Sequence -> {
            val size = target.size()
            val limit = count.coerceAtMost(size)
            val list = ArrayList<Any>(limit)
            for (i in 0 until limit) list.add(target[i])
            if (target is Array) ImmutableArray.create(list) else ImmutableList.create(list)
        }
        else -> throw IllegalStateException()
    }
}

internal fun invokeUnary(ctx: Ctx, callableArg: Any, item: Any): Any =
    when (callableArg) {
        is Callable -> callableArg.callWithOwnedArgs(ctx, arrayOf(item))
        is FunctionSymbol -> callableArg.functionSpec.callWithOwnedArgs(ctx, arrayOf(item))
        is MacroSymbol -> callableArg.macroSpec.callWithOwnedArgs(ctx, arrayOf(item))
        is Keyword -> extractByKey(ctx, item, callableArg)
        is String -> extractByKey(ctx, item, callableArg)
        else -> throw IllegalStateException()
    }

private fun invokeBinary(ctx: Ctx, callableArg: Any, first: Any, second: Any): Any =
    when (callableArg) {
        is Callable -> callableArg.callWithOwnedArgs(ctx, arrayOf(first, second))
        is FunctionSymbol -> callableArg.functionSpec.callWithOwnedArgs(ctx, arrayOf(first, second))
        is MacroSymbol -> callableArg.macroSpec.callWithOwnedArgs(ctx, arrayOf(first, second))
        else -> throw IllegalStateException()
    }

private fun extractByKey(ctx: Ctx, item: Any, key: Any): Any =
    when (item) {
        is Map -> {
            val resolvedKey = resolveMapKey(item, key)
            item[resolvedKey]
        }
        is JsonObject -> {
            val field = when (key) {
                is String -> key
                is Keyword -> key.symbol.name
                else -> key.toLiteral()
            }
            val element = item[field] ?: item[key.toLiteral()]
            if (element != null) jsonElementToForm(ctx, element) else Null
        }
        else -> Null
    }

private fun sequenceList(collection: Any): kotlin.collections.List<Any> =
    when (collection) {
        is ImmutableArray -> collection.elements
        is ImmutableList -> collection.elements
        is ImmutableSet -> collection.elements.toList()
        is Sequence -> {
            val size = collection.size()
            val list = ArrayList<Any>(size)
            for (i in 0 until size) list.add(collection[i])
            list
        }
        is Set -> {
            val array = collection.toArray()
            val size = array.size()
            val list = ArrayList<Any>(size)
            for (i in 0 until size) list.add(array[i])
            list
        }
        is String -> collection.map { it }
        else -> throw IllegalStateException()
    }

fun fnReduce(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    return if (args.size == 2) {
        val firstIsSeq = args[0] is Sequence || args[0] is Set || args[0] is String
        val fn = if (firstIsSeq) args[1] else args[0]
        val elements = sequenceList(if (firstIsSeq) args[0] else args[1])
        if (elements.isEmpty()) return Null
        var acc = elements[0]
        for (i in 1 until elements.size) {
            acc = invokeBinary(ctx, fn, acc, elements[i])
        }
        acc
    } else {
        // 3 args: either Lisp-style (reduce fn init seq) or Kotlin-style (.reduce seq init fn)
        val firstIsSeq = (args[0] is Sequence || args[0] is Set || args[0] is String) &&
            (args[2] is Callable || args[2] is FunctionSymbol || args[2] is MacroSymbol)
        val fn = if (firstIsSeq) args[2] else args[0]
        var acc = args[1]
        val elements = sequenceList(if (firstIsSeq) args[0] else args[2])
        for (i in elements.indices) {
            acc = invokeBinary(ctx, fn, acc, elements[i])
        }
        acc
    }
}

fun fnSequenceFind(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val firstIsSeq = args[0] is Sequence || args[0] is Set || args[0] is String
    val elements = sequenceList(if (firstIsSeq) args[0] else args[1])
    val predicate = if (firstIsSeq) args[1] else args[0]
    for (i in elements.indices) {
        val item = elements[i]
        if (invokeUnary(ctx, predicate, item).toBoolean()) return item
    }
    return Null
}

fun fnSequenceAny(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    val firstIsSeq = args[0] is Sequence || args[0] is Set || args[0] is String
    val elements = sequenceList(if (firstIsSeq) args[0] else args[1])
    val predicate = if (firstIsSeq) args[1] else args[0]
    for (i in elements.indices) {
        if (invokeUnary(ctx, predicate, elements[i]).toBoolean()) return true
    }
    return false
}

fun fnSequenceAll(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    val firstIsSeq = args[0] is Sequence || args[0] is Set || args[0] is String
    val elements = sequenceList(if (firstIsSeq) args[0] else args[1])
    val predicate = if (firstIsSeq) args[1] else args[0]
    for (i in elements.indices) {
        if (!invokeUnary(ctx, predicate, elements[i]).toBoolean()) return false
    }
    return true
}

fun fnSequenceCount(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long {
    if (args.size == 1) return fnSizeableSize(ctx, args, signatureIndex)
    val firstIsSeq = args[0] is Sequence || args[0] is Set || args[0] is String
    val elements = sequenceList(if (firstIsSeq) args[0] else args[1])
    val predicate = if (firstIsSeq) args[1] else args[0]
    var count = 0L
    for (i in elements.indices) {
        if (invokeUnary(ctx, predicate, elements[i]).toBoolean()) count++
    }
    return count
}

private fun compareFlxValues(a: Any, b: Any): Int {
    if (a === b) return 0
    if (a is Null) return -1
    if (b is Null) return 1
    if (a is Number && b is Number) {
        return when {
            a.isLessThan(b) -> -1
            a.isGreaterThan(b) -> 1
            else -> 0
        }
    }
    if (a is String && b is String) return a.compareTo(b)
    if (a is Char && b is Char) return a.compareTo(b)
    if (a is Boolean && b is Boolean) return a.compareTo(b)
    if (a is Keyword && b is Keyword) return a.symbol.name.compareTo(b.symbol.name)
    if (a is Symbol && b is Symbol) return a.name.compareTo(b.name)
    return a.toString().compareTo(b.toString())
}

fun fnSequenceSort(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Sequence {
    val target = args[0]
    val elements = sequenceList(target)
    if (elements.size <= 1) {
        return if (target is List && target !is Null) ImmutableList.create(elements) else ImmutableArray.create(elements)
    }
    val sorted = elements.sortedWith(::compareFlxValues)
    return if (target is List && target !is Null) ImmutableList.create(sorted) else ImmutableArray.create(sorted)
}

fun fnSequenceSortBy(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Sequence {
    val firstIsSeq = (args[0] is Sequence || args[0] is Set) && args[1] !is Sequence && args[1] !is Set
    val target = if (firstIsSeq) args[0] else args[1]
    val keySelector = if (firstIsSeq) args[1] else args[0]
    val elements = sequenceList(target)
    if (elements.size <= 1) {
        return if (target is List && target !is Null) ImmutableList.create(elements) else ImmutableArray.create(elements)
    }
    val decorated = ArrayList<Pair<Any, Any>>(elements.size)
    for (i in elements.indices) {
        val elem = elements[i]
        decorated.add(elem to invokeUnary(ctx, keySelector, elem))
    }
    decorated.sortWith { a, b -> compareFlxValues(a.second, b.second) }
    val sorted = ArrayList<Any>(decorated.size)
    for (i in decorated.indices) sorted.add(decorated[i].first)
    return if (target is List && target !is Null) ImmutableList.create(sorted) else ImmutableArray.create(sorted)
}

fun fnSequenceDistinct(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Sequence {
    val target = args[0]
    val elements = sequenceList(target)
    // 1 is 1 whether an Int (a literal) or a Long (computed); the first of them is kept.
    val distinctList = elements.distinctBy { it.asKey() }
    return if (target is List && target !is Null) ImmutableList.create(distinctList) else ImmutableArray.create(distinctList)
}

fun fnSequenceGroupBy(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Map {
    val firstIsSeq = (args[0] is Sequence || args[0] is Set) && args[1] !is Sequence && args[1] !is Set
    val target = if (firstIsSeq) args[0] else args[1]
    val keySelector = if (firstIsSeq) args[1] else args[0]
    val elements = sequenceList(target)
    val groups = LinkedHashMap<Any, MutableList<Any>>()
    for (i in elements.indices) {
        val elem = elements[i]
        val key = invokeUnary(ctx, keySelector, elem)
        groups.getOrPut(key) { ArrayList() }.add(elem)
    }
    val mapEntries = LinkedHashMap<Any, Any>(hashCapacityFor(groups.size))
    for ((key, list) in groups) {
        mapEntries[key] = ImmutableArray.create(list)
    }
    return ImmutableMap.create(mapEntries)
}

/** The most elements a built-in makes in one call (`range`), and the longest string (`.repeat`). */
internal const val MAX_GENERATED_ELEMENTS = 10_000_000L
internal const val MAX_GENERATED_CHARS = 50_000_000L

fun fnRange(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Array {
    val start: Long
    val end: Long
    val step: Long
    when (args.size) {
        1 -> {
            start = 0L
            end = (args[0] as Number).toLong()
            step = 1L
        }
        2 -> {
            start = (args[0] as Number).toLong()
            end = (args[1] as Number).toLong()
            step = 1L
        }
        else -> {
            start = (args[0] as Number).toLong()
            end = (args[1] as Number).toLong()
            step = (args[2] as Number).toLong()
        }
    }
    if (step == 0L) return ImmutableArray()
    if (step > 0L && start >= end) return ImmutableArray()
    if (step < 0L && start <= end) return ImmutableArray()

    val longCount = if (step > 0L) {
        (end - start + step - 1L) / step
    } else {
        (start - end - step - 1L) / (-step)
    }
    // Refused before it is built: a range of a billion numbers ended the program out of memory.
    // (A span too wide for a Long wraps negative, and is as much too large.)
    if (longCount < 0L || longCount > MAX_GENERATED_ELEMENTS) throw ResultTooLargeException(ctx, "range", "$MAX_GENERATED_ELEMENTS numbers")
    val count = longCount.toInt()
    val useLong = args.any { it is Long } ||
        start < Int.MIN_VALUE || start > Int.MAX_VALUE ||
        end < Int.MIN_VALUE || end > Int.MAX_VALUE ||
        step < Int.MIN_VALUE || step > Int.MAX_VALUE
    val list = ArrayList<Any>(count)
    var current = start
    if (step > 0L) {
        while (current < end) {
            list.add(if (useLong) current else current.toInt())
            current += step
        }
    } else {
        while (current > end) {
            list.add(if (useLong) current else current.toInt())
            current += step
        }
    }
    return ImmutableArray.create(list)
}

fun fnSequenceSum(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    val elements = sequenceList(args[0])
    if (elements.isEmpty()) return 0L
    var sum = 0L
    var i = 0
    while (i < elements.size) {
        val elem = elements[i]
        if (!elem.isIntegralNumber()) break
        val n = (elem as Number).toLong()
        val result = sum + n
        if (((sum xor result) and (n xor result)) < 0L) break
        sum = result
        i++
    }
    if (i == elements.size) return sum
    var doubleSum = sum.toDouble()
    for (j in i until elements.size) {
        doubleSum += (elements[j] as Number).toDouble()
    }
    return doubleSum
}

fun fnSequenceAverage(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double {
    val elements = sequenceList(args[0])
    if (elements.isEmpty()) return 0.0
    var sum = 0.0
    for (i in elements.indices) {
        sum += (elements[i] as Number).toDouble()
    }
    return sum / elements.size.toDouble()
}

fun fnSequenceJoin(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String {
    val elements = sequenceList(args[0])
    if (elements.isEmpty()) return ""
    val separator = if (args.size >= 2) {
        when (val sep = args[1]) {
            is String -> sep
            is Char -> sep.toString()
            is Null -> ""
            else -> sep.toLiteral()
        }
    } else ""
    val builder = StringBuilder()
    for (i in elements.indices) {
        if (i > 0 && separator.isNotEmpty()) builder.append(separator)
        when (val elem = elements[i]) {
            is Null -> {}
            is String -> builder.append(elem)
            is Char -> builder.append(elem)
            else -> builder.append(elem.toLiteral())
        }
    }
    return builder.toString()
}

fun fnTan(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    kotlin.math.tan((args[0] as Number).toDouble())

fun fnAnyToBoolean(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean =
    args[0].toBoolean()

fun fnAnyToByte(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Byte =
    when (val form = args[0]) {
        is String -> form.toByte()
        is Number -> form.toByte()
        is JsonPrimitive -> form.content.toByte()
        else -> throw IllegalStateException()
    }

fun fnAnyToChar(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Char =
    when (val form = args[0]) {
        is String -> if (form.isEmpty()) '\u0000' else form[0]
        is Number -> form.toInt().toChar()
        is JsonPrimitive -> form.content.firstOrNull() ?: '\u0000'
        else -> throw IllegalStateException()
    }

fun fnAnyToDouble(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Double =
    when (val form = args[0]) {
        is String -> form.toDouble()
        is Number -> form.toDouble()
        is JsonPrimitive -> form.content.toDouble()
        else -> throw IllegalStateException()
    }

fun fnAnyToFloat(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Float =
    when (val form = args[0]) {
        is String -> form.toFloat()
        is Number -> form.toFloat()
        is JsonPrimitive -> form.content.toFloat()
        else -> throw IllegalStateException()
    }

fun fnAnyToInt(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Int =
    when (val form = args[0]) {
        is String -> form.toInt()
        is Number -> form.toInt()
        is JsonPrimitive -> form.content.toInt()
        else -> throw IllegalStateException()
    }

fun fnAnyToJson(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): JsonObject =
    when (val form = args[0]) {
        is String -> Json.parseToJsonElement(form).jsonObject
        is Map -> form.toJson(ctx)
        is JsonObject -> form
        else -> throw IllegalStateException()
    }

fun fnAnyToLong(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Long =
    when (val form = args[0]) {
        is String -> form.toLong()
        is Number -> form.toLong()
        is JsonPrimitive -> form.content.toLong()
        else -> throw IllegalStateException()
    }

fun fnAnyToShort(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Short =
    when (val form = args[0]) {
        is String -> form.toShort()
        is Number -> form.toShort()
        is JsonPrimitive -> form.content.toShort()
        else -> throw IllegalStateException()
    }

fun fnAnyToNumber(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number =
    when (val form = args[0]) {
        is String -> form.toDoubleOrNull() ?: form.toLong()
        is Number -> form.toLong()
        is JsonPrimitive -> form.content.toLongOrNull() ?: form.content.toDoubleOrNull() ?: form.content.toLong()
        else -> throw IllegalStateException()
    }

fun fnAnyToMap(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Map =
    jsonObjectToMap(ctx, args[0] as JsonObject)

fun jsonObjectToMap(ctx: Ctx, jsonObject: JsonObject): Map {
    val pairs = LinkedHashMap<Any, Any>(hashCapacityFor(jsonObject.size))

    jsonObject.forEach { (entryKey, jsonElement) ->
        val key = convertJsonKeyToForm(entryKey)
        pairs[key] = jsonElementToForm(ctx, jsonElement)
    }

    return ImmutableMap.create(pairs)
}

private fun jsonElementToForm(ctx: Ctx, jsonElement: JsonElement): Any =
    when (jsonElement) {
        is JsonPrimitive -> convertJsonPrimitiveToForm(jsonElement)
        is JsonArray -> jsonArrayToArray(ctx, jsonElement)
        is JsonObject -> jsonObjectToMap(ctx, jsonElement)
    }

private fun convertJsonKeyToForm(key: String): Any =
    when {
        key.length > 1 && key.startsWith(':') -> Flx.getKeyword(key.substring(1))
        key.length >= 3 && key.startsWith('\'') && key.endsWith('\'') -> CharToken.parseChar(key) ?: key
        else -> key
    }

fun jsonArrayToArray(ctx: Ctx, jsonArray: JsonArray): Array {
    val elements = ArrayList<Any>(jsonArray.size)

    jsonArray.forEach { jsonElement ->
        val element = when (jsonElement) {
            is JsonPrimitive -> convertJsonPrimitiveToForm(jsonElement)
            is JsonArray -> jsonArrayToArray(ctx, jsonElement)
            is JsonObject -> jsonObjectToMap(ctx, jsonElement)
        }
        elements.add(element)
    }
    return ImmutableArray.create(elements)
}

private fun convertJsonPrimitiveToForm(primitive: JsonPrimitive): Any {
    if (primitive is JsonNull) return Null
    if (!primitive.isString) {
        val bool = primitive.booleanOrNull
        if (bool != null) return bool
        val longVal = primitive.content.toLongOrNull()
        if (longVal != null) return longVal
        val doubleVal = primitive.content.toDoubleOrNull()
        if (doubleVal != null) return doubleVal
    }
    val string = primitive.content
    return when {
        string.startsWith(":") -> Flx.getKeyword(string.substring(1))
        string.startsWith("'") && string.endsWith("'") -> CharToken.parseChar(string) ?: string
        else -> string
    }
}

fun fnAnyToArray(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Array =
    when (val form = args[0]) {
        is String -> form.toArray()
        is List -> form.toArray()
        is Array -> form
        is Map -> form.toArray()
        is Set -> form.toArray()
        else -> ImmutableArray.create(mutableListOf(form))
    }

fun fnAnyToList(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): List =
    when (val form = args[0]) {
        is String -> form.toList()
        is List -> form
        is Array -> form.toList()
        is Map -> form.toList()
        is Set -> form.toList()
        else -> ImmutableList.create(listOf(form))
    }

fun fnAnyToSet(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Set =
    when (val form = args[0]) {
        is String -> form.toSet()
        is List -> form.toSet()
        is Array -> form.toSet()
        is Map -> form.toSet()
        is Set -> form
        else -> ImmutableSet.create(setOf(form))
    }

fun fnAnyToString(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String =
    args[0].toString()

fun fnStringTrim(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String =
    (args[0] as String).trim()

fun fnStringTrimStart(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String =
    (args[0] as String).trimStart()

fun fnStringTrimEnd(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String =
    (args[0] as String).trimEnd()

fun fnStringToUpperCase(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): String =
    (args[0] as String).uppercase(Locale.ROOT)

fun fnXor(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    var count = 0
    for (arg in args) if (arg.isTrue()) count++
    return count == 1
}

// ---------------------------------------------------------------------------------------------------------------------
// Number comparison and arithmetic helpers

/** Long, Int, Short or Byte: a value that Long arithmetic represents exactly. */
internal fun Any.isIntegralNumber(): Boolean = this is Long || this is Int || this is Short || this is Byte

internal fun flxEquals(a: Any, b: Any): Boolean =
    if (a.isIntegralNumber() && b.isIntegralNumber()) (a as Number).toLong() == (b as Number).toLong()
    else a == b

// Whole numbers are compared as Long, which is exact over the whole range; anything else as Double,
// as before. Comparing through Double made distinct Longs above 2^53 compare equal.
private fun Number.isLessThan(other: Number): Boolean =
    if (isIntegralNumber() && other.isIntegralNumber()) toLong() < other.toLong() else toDouble() < other.toDouble()

private fun Number.isLessThanOrEqualTo(other: Number): Boolean =
    if (isIntegralNumber() && other.isIntegralNumber()) toLong() <= other.toLong() else toDouble() <= other.toDouble()

private fun Number.isGreaterThan(other: Number): Boolean =
    if (isIntegralNumber() && other.isIntegralNumber()) toLong() > other.toLong() else toDouble() > other.toDouble()

private fun Number.isGreaterThanOrEqualTo(other: Number): Boolean =
    if (isIntegralNumber() && other.isIntegralNumber()) toLong() >= other.toLong() else toDouble() >= other.toDouble()
