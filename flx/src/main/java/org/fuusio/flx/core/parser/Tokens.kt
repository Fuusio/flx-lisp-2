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
import org.fuusio.flx.core.Else
import org.fuusio.flx.core.Null
import org.fuusio.flx.core.parser.CharStream.Companion.BACKSLASH
import org.fuusio.flx.core.parser.CharStream.Companion.DOUBLE_QUOTE
import org.fuusio.flx.core.parser.CharStream.Companion.HYPHEN
import org.fuusio.flx.core.parser.CharStream.Companion.LEFT_BRACES
import org.fuusio.flx.core.parser.CharStream.Companion.LEFT_BRACKETS
import org.fuusio.flx.core.parser.CharStream.Companion.LEFT_PARENTHESIS
import org.fuusio.flx.core.parser.CharStream.Companion.LOWER_R
import org.fuusio.flx.core.parser.CharStream.Companion.LOWER_X
import org.fuusio.flx.core.parser.CharStream.Companion.NEW_LINE
import org.fuusio.flx.core.parser.CharStream.Companion.NUL
import org.fuusio.flx.core.parser.CharStream.Companion.NUMBER_SIGN
import org.fuusio.flx.core.parser.CharStream.Companion.QUOTE
import org.fuusio.flx.core.parser.CharStream.Companion.RIGHT_BRACES
import org.fuusio.flx.core.parser.CharStream.Companion.RIGHT_BRACKETS
import org.fuusio.flx.core.parser.CharStream.Companion.RIGHT_PARENTHESIS
import org.fuusio.flx.core.parser.CharStream.Companion.SEMI_COLON
import org.fuusio.flx.core.parser.CharStream.Companion.SPACE
import org.fuusio.flx.core.parser.CharStream.Companion.UPPER_R
import org.fuusio.flx.core.parser.CharStream.Companion.UPPER_X
import java.util.*

sealed class Token {
    open fun content(): String = ""
    open fun toObject(): Any = Null
}

class ParsingCompletedToken : Token() {

    override fun content() = "END"

    companion object {
        val TOKEN = ParsingCompletedToken()

        fun begins(char: Char) = char == NUL
    }
}

class ArrayBeginToken : Token() {

    override fun content() = LEFT_BRACKETS.toString()

    override fun toObject() = Null

    companion object {
        val TOKEN = ArrayBeginToken()

        fun begins(char: Char) = char == LEFT_BRACKETS
    }
}

class ArrayEndToken : Token() {

    override fun content() = RIGHT_BRACKETS.toString()

    companion object {
        val TOKEN = ArrayEndToken()

        fun begins(char: Char) = char == RIGHT_BRACKETS
    }
}

class ListBeginToken : Token() {

    override fun content() = LEFT_PARENTHESIS.toString()

    companion object {
        val TOKEN = ListBeginToken()

        fun begins(char: Char) = char == LEFT_PARENTHESIS
    }
}

class ListEndToken : Token() {

    override fun content() = RIGHT_PARENTHESIS.toString()

    companion object {
        val TOKEN = ListEndToken()

        fun begins(char: Char) = char == RIGHT_PARENTHESIS
    }
}

class MapBeginToken : Token() {

    override fun content() = LEFT_BRACES.toString()

    companion object {
        val TOKEN = MapBeginToken()

        fun begins(char: Char) = char == LEFT_BRACES
    }
}

class MapOrSetEndToken : Token() {

    override fun content() = RIGHT_BRACES.toString()

    companion object {
        val TOKEN = MapOrSetEndToken()

        fun begins(char: Char) = char == RIGHT_BRACES
    }
}

//class QuoteToken : Token() {
//
//    override fun content() = QUOTE.toString()
//
//    companion object {
//        val TOKEN = QuoteToken()
//
//        fun begins(char: Char) = char == QUOTE
//    }
//}

class SetBeginToken : Token() {

    override fun content() = "#{"

    companion object {
        val TOKEN = SetBeginToken()
    }
}

class CharToken(val value: Char) : Token() {

    override fun content() = value.toString()

    override fun toObject(): Any = value

    companion object {
        fun begins(char: Char) = char == QUOTE

        fun parse(stream: CharStream): Token {
            while(stream.hasNext()) {
                when (stream.next()) {
                    // The escaped character is part of the literal, even a quote: '\''.
                    BACKSLASH -> if (stream.hasNext()) stream.next()
                    QUOTE -> return createToken(stream)
                    else -> {}
                }
            }
            return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_CHAR, stream.position)
        }

        private fun createToken(stream: CharStream): Token {
            val tokenString = stream.getTokenString(true)
            val char = parseChar(tokenString)
            return if (char != null) CharToken(char) else ErrorToken(ParserErrorCode.INVALID_CHAR, tokenString)
        }

        /**
         * The character of a literal written with its quotes: 'a', an escape — '\n' '\t' '\r' '\b'
         * '\f' '\0' '\\' '\'' '\"' — or '\uXXXX'. Null if it is none of these. A two-character body
         * used to yield its second character, so '\n' read as the letter n.
         */
        fun parseChar(tokenString: String): Char? {
            val body = tokenString.substring(1, tokenString.length - 1)
            return when {
                body.length == 1 && body[0] != BACKSLASH -> body[0]
                body.length == 2 && body[0] == BACKSLASH -> escapedChar(body[1])
                body.length == 6 && body.startsWith("\\u") -> body.substring(2).toIntOrNull(16)?.toChar()
                else -> null
            }
        }
    }
}

class BaseIntegerToken(val value: Number) : Token() {

    override fun content() = value.toString()

    override fun toObject(): Any = value

    companion object {

        fun parse(stream: CharStream): Token {
            while(stream.hasNext()) {
                val char = stream.next()
                when {
                    char.isLetterOrDigit() -> {} // digits of the base, and a B or L suffix
                    TokenChar.isNumberEndChar(char) -> return createToken(stream)
                    // Returned: this error used to be created and dropped, and parsing went on.
                    else -> return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_BASE_INTEGER, stream.position)
                }
            }
            return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_BASE_INTEGER, stream.position)
        }

        private fun createToken(stream: CharStream): Token {
            val tokenString = stream.getTokenString()
            val normalizedTokenString = tokenString.lowercase(Locale.US)
            val index = normalizedTokenString.indexOf('r')

            return try {
                val base = tokenString.substring(0, index).toInt()
                val valueString = tokenString.substring(index + 1)
                when (valueString.last()) {
                    'B' -> BaseIntegerToken(valueString.substring(0, valueString.length - 1).toByte(base))
                    'L' -> BaseIntegerToken(valueString.substring(0, valueString.length - 1).toLong(base))
                    else -> BaseIntegerToken(valueString.toIntOrNull(base) ?: valueString.toLong(base))
                }
            } catch (_: Exception) {
                ErrorToken(ParserErrorCode.INVALID_BASE_INTEGER, tokenString)
            }
        }
    }
}

class IntegerToken(val value: Number) : Token() {

    override fun content() = value.toString()

    override fun toObject(): Any = value

    companion object {
        fun begins(char: Char) = char.isDigit() || char == HYPHEN

        fun parse(stream: CharStream): Token {
            // A '-' begins a number only when a digit or a '.' follows it; otherwise it begins a
            // symbol: -, --, ->, -foo. (The check used to compare the next character with itself,
            // so only -- worked, and -> read as the two symbols - and >.)
            if (stream.tokenBeginChar() == HYPHEN) {
                val next = stream.current()
                if (!next.isDigit() && next != '.') return SymbolToken.parse(stream)
            }

            while(stream.hasNext()) {
                val char = stream.next()
                when {
                    char.isDigit() -> {}
                    char == '.' -> return DoubleToken.parse(stream)
                    char == 'e' || char == 'E' -> return DoubleToken.parseExponent(stream)
                    char == LOWER_R || char == UPPER_R -> return BaseIntegerToken.parse(stream)
                    char == LOWER_X || char == UPPER_X -> return HexadecimalToken.parse(stream)
                    char == 'B' || char == 'L' -> {
                        // A suffix ends the number: 12Lx is an error, not 12L followed by x.
                        if (!TokenChar.isNumberEndChar(stream.current())) {
                            return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_LONG, stream.position)
                        }
                        return createToken(stream, true)
                    }
                    TokenChar.isNumberEndChar(char) -> return createToken(stream)
                    else -> return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_LONG, stream.position)
                }
            }
            return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_LONG, stream.position)
        }

        private fun createToken(stream: CharStream, positionInclusive: Boolean = false): Token {
            val tokenString = stream.getTokenString(positionInclusive)

            return try {
                when (tokenString.last()) {
                    'B' -> IntegerToken(tokenString.substring(0, tokenString.length - 1).toByte())
                    'L' -> IntegerToken(tokenString.substring(0, tokenString.length - 1).toLong())
                    // An Int when it fits, a Long when it does not: 3000000000 used to be an error.
                    else -> IntegerToken(tokenString.toIntOrNull() ?: tokenString.toLong())
                }
            } catch (_: Exception) {
                ErrorToken(ParserErrorCode.INVALID_LONG, tokenString)
            }
        }
    }
}

class HexadecimalToken(val value: Number) : Token() {

    override fun content() = value.toString()

    override fun toObject(): Any = value

    companion object {

        fun parse(stream: CharStream): Token {
            while(stream.hasNext()) {
                val char = stream.next()
                when {
                    char.isLetterOrDigit() -> {} // hex digits, and an L suffix
                    TokenChar.isNumberEndChar(char) -> return createToken(stream)
                    else -> return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_HEXADECIMAL, stream.position)
                }
            }
            return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_HEXADECIMAL, stream.position)
        }

        private fun createToken(stream: CharStream): Token {
            val tokenString = stream.getTokenString()
            // "0x" or "-0x" before the digits; the sign used to be left in, so -0xFF failed.
            val isNegative = tokenString.startsWith(HYPHEN)
            val digitsStart = if (isNegative) 3 else 2

            return try {
                val value: Number = when (tokenString.last()) {
                    'L' -> tokenString.substring(digitsStart, tokenString.length - 1).toLong(16)
                    else -> {
                        val digits = tokenString.substring(digitsStart)
                        digits.toIntOrNull(16) ?: digits.toLong(16)
                    }
                }
                HexadecimalToken(if (isNegative) negate(value) else value)
            } catch (_: Exception) {
                ErrorToken(ParserErrorCode.INVALID_HEXADECIMAL, tokenString)
            }
        }

        private fun negate(value: Number): Number = if (value is Int) -value else -value.toLong()
    }
}

class DoubleToken(val value: Double) : Token() {

    override fun content() = value.toString()

    override fun toObject(): Any = value

    companion object {

        /** The rest of a double after its '.': digits, then optionally an exponent (1.5e-3). */
        fun parse(stream: CharStream): Token = scan(stream, afterExponentMark = false)

        /** The rest of a double written without a '.', after its 'e': 1e10, 2E-3. */
        fun parseExponent(stream: CharStream): Token = scan(stream, afterExponentMark = true)

        private fun scan(stream: CharStream, afterExponentMark: Boolean): Token {
            var hasExponent = afterExponentMark
            var signAllowed = afterExponentMark

            while(stream.hasNext()) {
                val char = stream.next()
                when {
                    char.isDigit() -> signAllowed = false
                    !hasExponent && (char == 'e' || char == 'E') -> {
                        hasExponent = true
                        signAllowed = true
                    }
                    signAllowed && (char == '+' || char == HYPHEN) -> signAllowed = false
                    TokenChar.isNumberEndChar(char) -> return createToken(stream)
                    // Returned: this error used to be created and dropped, so anything up to the
                    // next delimiter was taken in and left for toDouble to judge.
                    else -> return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_DOUBLE, stream.position)
                }
            }
            return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_DOUBLE, stream.position)
        }

        private fun createToken(stream: CharStream): Token {
            val tokenString = stream.getTokenString()
            return try {
                DoubleToken(tokenString.toDouble())
            } catch (_: Exception) {
                ErrorToken(ParserErrorCode.INVALID_DOUBLE, tokenString)
            }
        }
    }
}

class KeywordToken(val string: String) : Token() {

    override fun content() = string

    // Only the leading colon is dropped: replace(":", "") used to remove them all, so :a:b was :ab.
    override fun toObject(): Any = Flx.getKeyword(string.substring(1))

    companion object {

        fun begins(char: Char) = TokenChar.isKeywordBeginChar(char)

        fun parse(stream: CharStream): Token {
            while(stream.hasNext()) {
                val char = stream.next()

                when {
                    TokenChar.isKeywordChar(char) -> {}
                    TokenChar.isKeywordEndChar(char) -> return KeywordToken(stream.getTokenString())
                    else -> return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_KEYWORD, stream.position)
                }
            }
            return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_KEYWORD, stream.position)
        }
    }
}

class RegexToken(val string: String) : Token() {

    override fun content() = string

    override fun toObject(): Regex = string.toRegex()

    companion object {

        fun begins(char: Char) = char == NUMBER_SIGN

        fun parse(stream: CharStream): Token {
            while(stream.hasNext()) {
                return when (stream.next()) {
                    LEFT_BRACES -> SetBeginToken.TOKEN
                    DOUBLE_QUOTE -> parseRegex(stream)
                    NUMBER_SIGN -> SymbolToken.parse(stream)
                    else -> ErrorToken(ParserErrorCode.FAILED_TO_PARSE_REGEX_OR_SET, stream.position)
                }
            }
            return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_REGEX, stream.position)
        }

        private fun parseRegex(stream: CharStream): Token {
            while(stream.hasNext()) {
                val char = stream.next()
                when {
                    char == DOUBLE_QUOTE -> {
                        val tokenString = stream.getTokenString(true)
                        return RegexToken(tokenString.substring(2, tokenString.length - 1))
                    }

                    TokenChar.isRegexEndChar(char) -> return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_REGEX, stream.position)
                    else -> {}
                }
            }
            return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_REGEX, stream.position)
        }
    }
}

class StringToken(val string: String) : Token() {

    override fun content() = "\"$string\""

    override fun toObject(): Any = string

    companion object {
        fun begins(char: Char) = char == DOUBLE_QUOTE

        fun parse(stream: CharStream): Token {
            var hasEscapes = false
            while(stream.hasNext()) {
                when(stream.next()) {
                    // The escaped character is part of the string, even a quote: "a\"b".
                    BACKSLASH -> {
                        hasEscapes = true
                        if (stream.hasNext()) stream.next()
                    }
                    DOUBLE_QUOTE -> {
                        val tokenString = stream.getTokenString(true)
                        val body = tokenString.substring(1, tokenString.length - 1)
                        return StringToken(if (hasEscapes) unescape(body) else body)
                    }
                    else -> {}
                }
            }
            return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_STRING, stream.position)
        }

        /**
         * [body] with its escapes replaced: \n \t \r \b \f \0 \\ \' \" and \uXXXX. Any other
         * backslash is kept as written, so strings such as "\d+" that relied on backslashes being
         * literal still read the same.
         */
        private fun unescape(body: String): String {
            val builder = StringBuilder(body.length)
            var i = 0
            while (i < body.length) {
                val char = body[i]
                if (char != BACKSLASH || i + 1 >= body.length) {
                    builder.append(char)
                    i++
                    continue
                }
                val next = body[i + 1]
                val unicode = if (next == 'u' && i + 6 <= body.length) body.substring(i + 2, i + 6).toIntOrNull(16) else null
                when {
                    unicode != null -> {
                        builder.append(unicode.toChar())
                        i += 6
                    }
                    else -> {
                        val escaped = escapedChar(next)
                        if (escaped != null) builder.append(escaped) else builder.append(char).append(next)
                        i += 2
                    }
                }
            }
            return builder.toString()
        }
    }
}

class SymbolToken(val string: String) : Token() {

    override fun content() = string

    override fun toObject(): Any =
        when (string) {
            SYMBOL_FALSE -> false
            SYMBOL_NIL -> Null
            SYMBOL_NULL -> Null
            SYMBOL_TRUE -> true
            SYMBOL_ELSE -> Else
            else -> Flx.getParsedSymbol(string)
        }

    companion object {

        const val SYMBOL_E = "e"
        const val SYMBOL_FALSE = "false"
        const val SYMBOL_NIL = "nil"
        const val SYMBOL_NULL = "null"
        const val SYMBOL_PI = "pi"
        const val SYMBOL_TRUE = "true"
        const val SYMBOL_ELSE = "_*_else"

        fun begins(char: Char) = TokenChar.isSymbolBeginChar(char)

        fun parse(stream: CharStream): Token {
            while(stream.hasNext()) {
                val char = stream.next()
                when {
                    TokenChar.isSymbolChar(char) -> {}
                    TokenChar.isSymbolEndChar(char) -> return SymbolToken(stream.getTokenString())
                    else -> return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_SYMBOL, stream.position)
                }
            }
            return ErrorToken(ParserErrorCode.FAILED_TO_PARSE_SYMBOL, stream.position)
        }
    }
}

class ErrorToken(code: ParserErrorCode, vararg args: Any) : Token() {

    val error = ParserError.create(code, *args)

    override fun content() = error.description
}

class CommentToken(val string: String) : Token() {

    override fun content() = string

    companion object {
        fun begins(char: Char) = char == SEMI_COLON

        fun parse(stream: CharStream): Token {
            while(stream.hasNext()) {
                when(stream.next()) {
                    NEW_LINE , NUL -> return createCommentToken(stream)
                    else -> {}
                }
            }
            return createCommentToken(stream)
        }

        private fun createCommentToken(stream: CharStream): Token {
            val tokenString = stream.getTokenString(true)
            return if (tokenString.startsWith(";;")) {
                val lastChar = tokenString.last()
                if (lastChar == NEW_LINE || lastChar == NUL) CommentToken(tokenString.substring(0, tokenString.length - 1)) else CommentToken(tokenString)
            } else ErrorToken(ParserErrorCode.FAILED_TO_PARSE_COMMENT, stream.position)
        }
    }
}

class WhiteSpaceToken(val string: String) : Token() {

    override fun content(): String {
        var content = ""

        string.forEach { char ->
            content += if (char == SPACE) '_' else '$'
        }
        return content
    }

    companion object {
        fun begins(char: Char) = char.isWhitespace()

        /**
         * The parser discards whitespace, so one shared token stands for every run of it. Each run
         * used to cost a substring and a token object — for every gap between two tokens.
         */
        val TOKEN = WhiteSpaceToken("")

        fun parse(stream: CharStream): Token {
            while(stream.hasNext()) {
                val char = stream.next()
                when {
                    char.isWhitespace() || char == NUL -> {}
                    else -> {
                        stream.unread() // the first non-whitespace character begins the next token
                        return TOKEN
                    }
                }
            }
            throw IllegalStateException()
        }
    }
}

/** The character a backslash escape stands for — \n \t \r \b \f \0 \\ \' \" — or null for any other. */
internal fun escapedChar(char: Char): Char? =
    when (char) {
        'n' -> '\n'
        't' -> '\t'
        'r' -> '\r'
        'b' -> '\b'
        'f' -> '\u000C'
        '0' -> '\u0000'
        '\\' -> '\\'
        '\'' -> '\''
        '"' -> '"'
        else -> null
    }
