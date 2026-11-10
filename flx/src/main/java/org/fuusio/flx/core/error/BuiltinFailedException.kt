/*
 * Copyright (C) 2016 - 2026 Marko Salmela
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

import org.fuusio.flx.core.FunctionFormSpec
import org.fuusio.flx.core.vm.Ctx
import java.util.regex.PatternSyntaxException

/**
 * A built-in function that failed on the values it was given, said in FLX's terms:
 * `'nth' failed: index 7 is out of range for a size of 3`, where the program used to get the
 * Java error underneath (`IndexOutOfBoundsException : Index 7 out of bounds for length 3`, or
 * nothing but a class name). It is an [FlxException] like any other: `try`/`catch` gets its message.
 */
class BuiltinFailedException(
    ctx: Ctx,
    val functionName: String,
    val reason: String,
    cause: Throwable,
) : FlxException(ctx) {

    init {
        initCause(cause)
    }

    override val message: String
        get() = "'$functionName' failed: $reason"
}

/** Java errors in FLX's terms. */
object JavaErrors {

    /**
     * The FLX error for [error] thrown by the built-in [spec] called with [args], or null when
     * [error] is not a Java error to translate: an FLX error already, a `return` on its way out, a
     * cancellation, or anything this does not know — those are passed on as they are.
     */
    fun ofBuiltin(ctx: Ctx, spec: FunctionFormSpec, args: Array<Any>, error: Throwable): FlxException? {
        if (error is FlxException) return null
        val reason = reason(error) { cannotBeUsedWith(args) } ?: return null
        return BuiltinFailedException(ctx, spec.symbol, reason, error)
    }

    /**
     * What went wrong, for the kinds of Java error a built-in runs into on bad values; null for any
     * other. [wrongValues] words the cases where the error itself says nothing a user can read.
     */
    fun reason(error: Throwable, wrongValues: () -> String): String? = when (error) {
        is FlxException -> null
        is kotlinx.serialization.SerializationException -> "the text is not valid JSON"
        is PatternSyntaxException -> "\"${error.pattern}\" is not a valid regular expression (${error.description})"
        is NumberFormatException -> numberFormat(error.message)
        is IndexOutOfBoundsException -> indexOutOfBounds(error.message)
        is NoSuchElementException -> error.message?.takeIf { it.isNotBlank() }?.replaceFirstChar { it.lowercase() }?.trimEnd('.') ?: "there is no such element"
        is ClassCastException, is NullPointerException -> wrongValues()
        is ArithmeticException -> when {
            error.message?.contains("by zero") == true -> "it divides by zero"
            else -> error.message?.takeIf { it.isNotBlank() }?.replaceFirstChar { it.lowercase() } ?: "the result cannot be calculated"
        }
        is NegativeArraySizeException -> "a size cannot be negative"
        is OutOfMemoryError -> "the result is too large to fit in memory"
        else -> when (error.javaClass) {
            // Exactly these: their subclasses include FLX's own errors and coroutine cancellation.
            IllegalStateException::class.java, IllegalArgumentException::class.java, UnsupportedOperationException::class.java ->
                // A message that names Java classes is not one for the program's author.
                error.message?.takeIf { it.isNotBlank() && "class " !in it && "kotlin" !in it && "java." !in it }
                    ?.replaceFirstChar { it.lowercase() }?.trimEnd('.') ?: wrongValues()
            else -> null
        }
    }

    private fun cannotBeUsedWith(args: Array<Any>): String =
        if (args.isEmpty()) "it cannot be used here"
        else "it cannot work with what it was given: " + args.joinToString(", ") { runCatching { Describe.value(it) }.getOrDefault("a value") }

    private val quotedInput = Regex("""For input string: "(.*)".*""", RegexOption.DOT_MATCHES_ALL)

    private fun numberFormat(message: String?): String {
        val input = message?.let { quotedInput.matchEntire(it) }?.groupValues?.get(1)
        return when {
            input != null -> "\"$input\" is not a number"
            message == "empty String" -> "\"\" is not a number"
            message.isNullOrBlank() -> "the text is not a number"
            else -> message.replaceFirstChar { it.lowercase() }
        }
    }

    private val indexAndSize = listOf(
        Regex("""Index (-?\d+) out of bounds for length (\d+)"""),
        Regex("""[Ii]ndex:? (-?\d+),? [Ss]ize:? (\d+)"""),
        Regex("""[Ii]ndex:? (-?\d+),? length:? (\d+)"""),
        Regex(""".*index (-?\d+) out of bounds for length (\d+)"""),
    )
    private val rangeAndLength = listOf(
        Regex("""begin (-?\d+), end (-?\d+), length (\d+)"""),
        Regex("""Range \[(-?\d+), (-?\d+)\) out of bounds for length (\d+)"""),
    )

    private fun indexOutOfBounds(message: String?): String {
        if (message.isNullOrBlank()) return "an index is out of range"
        for (pattern in indexAndSize) {
            pattern.find(message)?.let { match -> return "index ${match.groupValues[1]} is out of range for a size of ${match.groupValues[2]}" }
        }
        for (pattern in rangeAndLength) {
            pattern.find(message)?.let { match ->
                return "the range ${match.groupValues[1]} to ${match.groupValues[2]} is out of range for a length of ${match.groupValues[3]}"
            }
        }
        return "an index is out of range (${message.trimEnd('.')})"
    }
}
