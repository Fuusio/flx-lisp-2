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
package org.fuusio.flx.core.parser

import org.fuusio.flx.Flx
import org.fuusio.flx.FlxUnitTest
import org.fuusio.flx.core.Symbol
import org.fuusio.flx.core.error.ParseException
import org.fuusio.flx.core.type.impl.ImmutableList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/** Malformed input is reported, token boundaries are enforced, and literals support escapes and larger numbers. */
class ParserFixesTest : FlxUnitTest() {

    private class Outcome(val results: kotlin.collections.List<Any>, val errors: kotlin.collections.List<ParserError>)

    private fun parseAll(expression: String): Outcome {
        initVM()
        val errors = mutableListOf<ParserError>()
        val observer = object : ParserObserver {
            override fun onNext(parsingResult: Any) {}
            override fun onError(error: ParserError) {
                errors.add(error)
            }
        }
        val results = FormParser(observer).parse(ParsingContext(), expression)
        return Outcome(results, errors)
    }

    private fun parseOne(expression: String): Any {
        val outcome = parseAll(expression)
        assertTrue(outcome.errors.isEmpty(), "Unexpected errors: ${outcome.errors.map { it.description }}")
        assertEquals(1, outcome.results.size)
        return outcome.results[0]
    }

    private fun assertFails(expression: String, code: ParserErrorCode? = null) {
        val outcome = parseAll(expression)
        assertTrue(outcome.errors.isNotEmpty(), "Expected a parse error for: $expression")
        assertTrue(outcome.results.isEmpty())
        if (code != null) assertEquals(code, outcome.errors[0].code)
    }

    @Nested
    inner class Brackets {

        @Test
        fun `stray closing parenthesis`() = assertFails("1 2 )", ParserErrorCode.UNEXPECTED_CLOSING)

        @Test
        fun `stray closing brace`() = assertFails("}", ParserErrorCode.UNEXPECTED_CLOSING)

        @Test
        fun `mismatched closing bracket`() = assertFails("(1 2] 3)", ParserErrorCode.UNEXPECTED_CLOSING)

        @Test
        fun `nested structures still parse`() {
            val list = parseOne("(1 [2 3] {:a 4} #{5})") as ImmutableList
            assertEquals(4, list.size())
        }
    }

    @Nested
    inner class Boundaries {

        @Test
        fun `digits followed by letters`() = assertFails("12abc")

        @Test
        fun `operator glued to a number`() = assertFails("(1+2)")

        @Test
        fun `suffix followed by more characters`() = assertFails("12Lx")

        @Test
        fun `delimiters still end tokens`() {
            val list = parseOne("(foo\"s\"[1](2);;c\n)") as ImmutableList
            assertEquals(4, list.size())
        }
    }

    @Nested
    inner class Hyphen {

        @Test
        fun `arrow is one symbol`() = assertEquals("->", (parseOne("->") as Symbol).name)

        @Test
        fun `symbol starting with a hyphen`() = assertEquals("-foo", (parseOne("-foo") as Symbol).name)

        @Test
        fun `double hyphen`() = assertEquals("--", (parseOne("--") as Symbol).name)

        @Test
        fun `lone hyphen`() = assertEquals("-", (parseOne("-") as Symbol).name)

        @Test
        fun `negative numbers`() {
            assertEquals(-5, parseOne("-5"))
            assertEquals(-1.5, parseOne("-1.5"))
        }
    }

    @Nested
    inner class Chars {

        @Test
        fun `escaped characters`() {
            assertEquals('\n', parseOne("'\\n'"))
            assertEquals('\t', parseOne("'\\t'"))
            assertEquals('\'', parseOne("'\\''"))
            assertEquals('\\', parseOne("'\\\\'"))
            assertEquals('\u03A9', parseOne("'\\u03A9'"))
        }

        @Test
        fun `two characters are not a char`() = assertFails("'ab'", ParserErrorCode.INVALID_CHAR)
    }

    @Nested
    inner class Strings {

        @Test
        fun `escaped quote`() = assertEquals("a\"b", parseOne("\"a\\\"b\""))

        @Test
        fun `escaped newline and tab`() = assertEquals("x\ny\tz", parseOne("\"x\\ny\\tz\""))

        @Test
        fun `escaped backslash`() = assertEquals("\\", parseOne("\"\\\\\""))

        @Test
        fun `unicode escape`() = assertEquals("\u03A9", parseOne("\"\\u03A9\""))

        @Test
        fun `unknown escape is kept as written`() = assertEquals("\\d+", parseOne("\"\\d+\""))

        @Test
        fun `unterminated string`() = assertFails("\"abc")
    }

    @Nested
    inner class Numbers {

        @Test
        fun `exponents`() {
            assertEquals(1.0e10, parseOne("1e10"))
            assertEquals(1.5e-3, parseOne("1.5e-3"))
            assertEquals(2.0e3, parseOne("2E+3"))
        }

        @Test
        fun `malformed doubles`() {
            assertFails("1.5x")
            assertFails("1e")
            assertFails("1.2.3")
        }

        @Test
        fun `whole number too large for an Int is a Long`() {
            assertEquals(3_000_000_000L, parseOne("3000000000"))
            assertEquals(42, parseOne("42"))
        }

        @Test
        fun `large and negative hexadecimals`() {
            assertEquals(0xFFFFFFFFL, parseOne("0xFFFFFFFF"))
            assertEquals(-255, parseOne("-0xFF"))
        }

        @Test
        fun `malformed base integer is reported`() = assertFails("2r1.1")
    }

    @Nested
    inner class Keywords {

        @Test
        fun `keyword drops only its leading colon`() = assertEquals(Flx.getKeyword("abc"), parseOne(":abc"))
    }

    @Nested
    inner class Reporting {

        @Test
        fun `token errors keep their own message`() {
            val outcome = parseAll("'ab'")
            assertTrue(outcome.errors[0].description.startsWith("Invalid char"))
        }

        @Test
        fun `repl reports parse errors`() {
            assertThrows(ParseException::class.java) { eval("(+ 1") }
        }
    }
}
