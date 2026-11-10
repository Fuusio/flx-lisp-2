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
@file:Suppress("unused", "UnusedReceiverParameter")

package org.fuusio.flx.core

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import org.fuusio.flx.core.type.*
import org.fuusio.flx.core.type.Array
import org.fuusio.flx.core.type.List
import org.fuusio.flx.core.type.Map
import org.fuusio.flx.core.type.Set
import org.fuusio.flx.core.type.impl.ImmutableArray
import org.fuusio.flx.core.type.impl.ImmutableList
import org.fuusio.flx.core.type.impl.ImmutableSet
import org.fuusio.flx.core.vm.Ctx
import java.util.*

// ---------------------------------------------------------------------------------------------------------------------
// Any

// Every FLX type is Evaluable and evaluates itself through its own override, so one type check and
// a virtual call do what a chain of eight checks did; anything else (numbers, strings, booleans)
// evaluates to itself.
fun Any.eval(ctx: Ctx): Any = if (this is Evaluable) this.eval(ctx) else this

fun Any.isArray(): Boolean =
    when (this) {
        is Array -> true
        else -> false
    }

fun Any.isBoolean(): Boolean =
    when (this) {
        is Boolean -> true
        else -> false
    }

fun Any.isByte(): Boolean =
    when (this) {
        is Byte -> true
        else -> false
    }

fun Any.isChar(): Boolean =
    when (this) {
        is Char -> true
        else -> false
    }

fun Any.isDouble(): Boolean =
    when (this) {
        is Double -> true
        else -> false
    }

fun Any.isFalse() =
    when (this) {
        is Boolean -> !this
        is Null -> true
        else -> false
    }

fun Any.isFloat(): Boolean =
    when (this) {
        is Float -> true
        else -> false
    }

fun Any.isIndexable(): Boolean =
    when (this) {
        is Indexable, is String -> true
        else -> false
    }

fun Any.isInt(): Boolean =
    when (this) {
        is Int -> true
        else -> false
    }

fun Any.isList(): Boolean =
    when (this) {
        is List -> true
        else -> false
    }

fun Any.isLong(): Boolean =
    when (this) {
        is Long -> true
        else -> false
    }

fun Any.isMap(): Boolean =
    when (this) {
        is Map -> true
        else -> false
    }

fun Any.isNull() =
    when (this) {
        is Null -> true
        else -> false
    }

fun Any.isSequence(): Boolean =
    when (this) {
        is Sequence, is String -> true
        else -> false
    }

fun Any.isShort(): Boolean =
    when (this) {
        is Short -> true
        else -> false
    }

fun Any.isSet(): Boolean =
    when (this) {
        is Set -> true
        else -> false
    }

fun Any.isSizable(): Boolean =
    when (this) {
        is Sizable, is String -> true
        else -> false
    }

fun Any.isString(): Boolean =
    when (this) {
        is String -> true
        else -> false
    }

fun Any.isSymbol(): Boolean =
    when (this) {
        is Symbol -> true
        else -> false
    }

fun Any.isTrue()
    = when (this) {
        is Boolean -> this
        is Null -> false
        else -> true
    }

fun Any.toBoolean()
    = when (this) {
        is Boolean -> this
        is Null -> false
        is JsonPrimitive -> booleanOrNull ?: false
        else -> true
}
fun Any.toLiteral(): String
        = when(this) {
    is Char -> "'$this'"
    is Color -> this.toLiteral()
    is String -> "\"$this\""
    is Regex -> "#\"${this.pattern}\""
    is Keyword -> this.toLiteral()
    is JsonObject -> this.toString()
    else -> this.toString()
}

// ---------------------------------------------------------------------------------------------------------------------
// Boolean

fun Boolean.isBoolean() = true

fun Boolean.isFalse() = !this

fun Boolean.isNull() = false

fun Boolean.isTrue() = this

fun Boolean.toBoolean() = this

// ---------------------------------------------------------------------------------------------------------------------
// ArrayList

fun ArrayList<Any>.eval(ctx: Ctx) =
    Evaluator.eval(ctx, ImmutableList.create(this))

// ---------------------------------------------------------------------------------------------------------------------
// Byte

fun Byte.isByte() = true

// ---------------------------------------------------------------------------------------------------------------------
// Char

fun Char.isChar() = true

fun Char.toLiteral() = "'$this'"

// ---------------------------------------------------------------------------------------------------------------------
// Date

fun Date.isDate() = true

fun Date.toLiteral() = this.toString()

// ---------------------------------------------------------------------------------------------------------------------
// Double

fun Double.isDouble() = true

// ---------------------------------------------------------------------------------------------------------------------
// Float

fun Float.isFloat() = true

// ---------------------------------------------------------------------------------------------------------------------
// Int

fun Int.isInt() = true

// ---------------------------------------------------------------------------------------------------------------------
// List

fun kotlin.collections.List<Any>.eval(ctx: Ctx) =
    Evaluator.eval(ctx, ImmutableList.create(this))

// ---------------------------------------------------------------------------------------------------------------------
// Long

fun Long.isLong() = true

// ---------------------------------------------------------------------------------------------------------------------
// Regex

fun Regex.toLiteral() = "#\"$this\""

// ---------------------------------------------------------------------------------------------------------------------
// Short

fun Short.isShort() = true

// ---------------------------------------------------------------------------------------------------------------------
// String

fun String.cons(head: Any): List {
    val list = mutableListOf<Any>()
    list.add(head)
    toCharArray().forEach { char -> list.add(char) }
    return ImmutableList.create(list)
}

fun String.first() = if (isNotEmpty()) this[0] else throw IllegalStateException() // TODO

fun String.isIndexable() = true

fun String.isSequence() = true

fun String.isSizable() = true

fun String.isString() = true

fun String.last() = if (isNotEmpty()) this[length - 1] else throw IllegalStateException() // TODO

fun String.rest() = if (isNotEmpty()) this.subSequence(1, length) else ""

fun String.size() = length

fun String.toArray(): Array = ImmutableArray.create(this.toCharArray())

fun String.toList(): List = ImmutableList.create(this.toCharArray())

fun String.toSet(): Set = ImmutableSet.create(this.toCharArray())


