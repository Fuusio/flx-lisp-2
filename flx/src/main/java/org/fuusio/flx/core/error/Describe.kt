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
package org.fuusio.flx.core.error

import org.fuusio.flx.core.Callable
import org.fuusio.flx.core.Keyword
import org.fuusio.flx.core.Null
import org.fuusio.flx.core.toLiteral
import org.fuusio.flx.core.type.CoreType
import org.fuusio.flx.core.type.FlxType

/** One of [types]: what an arg of overloaded built-ins may be, for error messages. */
class AnyOfType(val types: List<FlxType>) : FlxType {
    override fun getTypeClass(): kotlin.reflect.KClass<*> = Any::class
    override fun getTypeName(): String = types.joinToString(" | ") { it.getTypeName() }
    override fun isInstance(any: Any): Boolean = types.any { it.isInstance(any) }
}

/** Values and types in words, for error messages: "a number (5)", "a widget". */
object Describe {
    private const val MAX_LITERAL = 40

    fun value(value: Any): String = when (value) {
        Null -> "nil"
        is Boolean -> "a boolean ($value)"
        is Number -> "a number (${value.toLiteral()})"
        is Char -> "a character ('$value')"
        is String -> "a string (${short(value.toLiteral())})"
        is Keyword -> "a keyword (${value.toLiteral()})"
        is org.fuusio.flx.core.type.Map -> "a map"
        is org.fuusio.flx.core.type.Set -> "a set"
        is org.fuusio.flx.core.type.Array -> "an array"
        is org.fuusio.flx.core.type.List -> "a list"
        is Callable -> "a function"
        else -> withArticle(value::class.simpleName ?: "value") + " (" + short(value.toLiteral()) + ")"
    }

    fun type(type: FlxType): String = when (type) {
        CoreType.NUMBER, CoreType.INT, CoreType.LONG, CoreType.DOUBLE, CoreType.FLOAT, CoreType.SHORT, CoreType.BYTE -> "a number"
        CoreType.STRING -> "a string"
        CoreType.BOOLEAN -> "a boolean"
        CoreType.CHAR -> "a character"
        CoreType.FUNCTION, CoreType.CALLBACK -> "a function"
        CoreType.ANY -> "a value"
        is AnyOfType -> type.types.map { Describe.type(it) }.distinct().let { names ->
            if (names.size == 1) names[0] else names.dropLast(1).joinToString(", ") + " or " + names.last()
        }
        else -> withArticle(type.getTypeName())
    }

    private fun withArticle(name: String): String {
        val word = name.replaceFirstChar { it.lowercase() }
        return (if (word.firstOrNull()?.let { it.lowercaseChar() in "aeiou" } == true) "an " else "a ") + word
    }

    private fun short(literal: String) = if (literal.length <= MAX_LITERAL) literal else literal.take(MAX_LITERAL - 1) + "…"
}
