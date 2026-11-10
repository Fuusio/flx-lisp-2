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

import org.fuusio.flx.Flx
import org.fuusio.flx.core.function.FunctionSpec
import org.fuusio.flx.core.parser.FormParser
import org.fuusio.flx.core.parser.ParserError
import org.fuusio.flx.core.parser.ParserObserver
import org.fuusio.flx.core.parser.ParsingContext
import org.fuusio.flx.core.type.impl.ImmutableList

/** A string template that cannot be read, e.g. an unclosed `${`. */
class StringTemplateException(message: String) : IllegalArgumentException(message)

/**
 * String interpolation: `"Count: ${count}"` is `(str "Count: " count)`. What is between `${`
 * and `}` is FLX source, one form: a name, a call, anything. `\${` is a literal `${`. A string
 * without `${` is just the string.
 */
object StringTemplate {
    private const val BEGIN = "\${"

    fun isTemplate(string: String): Boolean = string.contains(BEGIN)

    /** The text and form parts of [string]; a [StringTemplateException] when it cannot be read. */
    fun parts(string: String): List<Any> {
        val parts = mutableListOf<Any>()
        val text = StringBuilder()
        var i = 0
        while (i < string.length) {
            if (string.startsWith("\\" + BEGIN, i)) {
                text.append(BEGIN)
                i += BEGIN.length + 1
                continue
            }
            if (!string.startsWith(BEGIN, i)) {
                text.append(string[i++])
                continue
            }
            val end = closingBrace(string, i + BEGIN.length)
                ?: throw StringTemplateException("A \${ in \"${string.take(40)}\" has no closing }")
            val source = string.substring(i + BEGIN.length, end).trim()
            if (source.isEmpty()) throw StringTemplateException("An empty \${} in \"${string.take(40)}\"")
            val forms = parse(source)
            if (forms.size != 1) throw StringTemplateException("\${$source} must hold one form, not ${forms.size}")
            if (text.isNotEmpty()) parts.add(text.toString()).also { text.clear() }
            parts.add(forms[0])
            i = end + 1
        }
        if (text.isNotEmpty() || parts.isEmpty()) parts.add(text.toString())
        return parts
    }

    /** [string] as a form: the string itself, or a `str` call of its parts. */
    fun compile(string: String): Any {
        if (!string.contains("\$")) return string
        val parts = parts(string)
        if (parts.size == 1 && parts[0] is String) return parts[0]
        return ImmutableList(listOf<Any>(Flx.getSymbol(FunctionSpec.STR)) + parts)
    }

    /** The index of the } that closes the template starting at [from]: braces nest, and strings are skipped. */
    private fun closingBrace(string: String, from: Int): Int? {
        var depth = 0
        var i = from
        var inString = false
        while (i < string.length) {
            val char = string[i]
            when {
                inString && char == '\\' -> i++
                char == '"' -> inString = !inString
                inString -> {}
                char == '{' -> depth++
                char == '}' -> if (depth == 0) return i else depth--
            }
            i++
        }
        return null
    }

    private fun parse(source: String): List<Any> {
        val errors = mutableListOf<ParserError>()
        val observer = object : ParserObserver {
            override fun onNext(parsingResult: Any) = Unit
            override fun onError(error: ParserError) { errors.add(error) }
        }
        val context = ParsingContext()
        val forms = FormParser(observer).parse(context, source)
        val error = errors.firstOrNull() ?: context.errors.firstOrNull()
        if (error != null) throw StringTemplateException("\${$source} cannot be read: ${error.description}")
        return forms
    }
}
