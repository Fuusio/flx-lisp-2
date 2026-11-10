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

@Suppress("unused")
enum class TokenChar(
    val char: Char,
    val isSymbolBeginChar: Boolean = true,
    val isSymbolChar: Boolean = true,
    /**
     * Whether the character ends the token before it — a delimiter. False unless stated: the default
     * used to be true, which made every character (and, through [get], every unlisted one — letters
     * and digits included) end a number, so `12abc` read as `12` `abc` and `(1+2)` as `(1 +2)`.
     */
    val isTokenEndChar: Boolean = false
) {
    AMBERSAND('&', isSymbolBeginChar = false, isSymbolChar = false),
    ASTERISK('*'),
    AT_SIGN('@', isSymbolBeginChar = false, isSymbolChar = false),
    BACKSLASH('\\', isSymbolBeginChar = false, isSymbolChar = false),
    CARET('^', isSymbolBeginChar = false, isSymbolChar = false),
    COLON(':', isSymbolBeginChar = false, isSymbolChar = true),
    COMMA(',', isSymbolBeginChar = false, isSymbolChar = false),
    DOLLAR_SIGN('$', isSymbolBeginChar = false, isSymbolChar = false),
    DOUBLE_QUOTE('"', isSymbolBeginChar = false, isSymbolChar = false, isTokenEndChar = true),
    EQUALS_TO('='),
    EXCLAMATION_MARK('!'),
    GREATER_THAN('>'),
    HYPHEN('-'),
    LEFT_BRACES('{', isSymbolBeginChar = false, isSymbolChar = false, isTokenEndChar = true),
    LEFT_BRACKETS('[', isSymbolBeginChar = false, isSymbolChar = false, isTokenEndChar = true),
    LEFT_PARENTHESIS('(', isSymbolBeginChar = false, isSymbolChar = false, isTokenEndChar = true),
    LESSER_THAN('<'),
    NEW_LINE('\n', isSymbolBeginChar = false, isSymbolChar = false, isTokenEndChar = true),
    NUL('\u0000', isSymbolBeginChar = false, isSymbolChar = false, isTokenEndChar = true),
    NUMBER_SIGN('#', isSymbolBeginChar = false, isSymbolChar = false),
    PERIOD('.'),
    RIGHT_BRACES('}', isSymbolBeginChar = false, isSymbolChar = false, isTokenEndChar = true),
    RIGHT_BRACKETS(']', isSymbolBeginChar = false, isSymbolChar = false, isTokenEndChar = true),
    RIGHT_PARENTHESIS(')', isSymbolBeginChar = false, isSymbolChar = false, isTokenEndChar = true),
    PERCENT_SIGN('%', isSymbolChar = false),
    PLUS('+'),
    QUESTION_MARK('?'),
    SEMI_COLON(';', isSymbolBeginChar = false, isSymbolChar = false, isTokenEndChar = true),
    SLASH('/', isSymbolChar = false),
    SPACE(' ', isSymbolBeginChar = false, isSymbolChar = false, isTokenEndChar = true),
    QUOTE('\'', isSymbolBeginChar = false, isSymbolChar = false),
    TILDE('~', isSymbolBeginChar = false, isSymbolChar = false),
    UNDERSCORE('_');

    fun matches(char: Char) = this.char == char

    companion object {

        private val mapping = hashMapOf<Char, TokenChar>()

        init {
            entries.forEach { value -> mapping[value.char] = value }
        }

        fun get(char: Char) = mapping[char] ?: NUL

        /** The listed character, or null for any other (letters, digits, ...), which is never a delimiter. */
        private fun listed(char: Char): TokenChar? = mapping[char]

        /** Whitespace or a listed delimiter: ( ) [ ] { } " ; or the end of the input. */
        fun isDelimiter(char: Char) = char.isWhitespace() || listed(char)?.isTokenEndChar == true

        fun isCharEndChar(char: Char) = char == QUOTE.char

        fun isNumberBeginChar(char: Char) = char.isDigit() || HYPHEN.matches(char)

        fun isNumberEndChar(char: Char) = isDelimiter(char)

        fun isIntegerChar(char: Char) = char.isDigit()

        fun isKeywordBeginChar(char: Char) = COLON.matches(char)

        fun isKeywordChar(char: Char) =
            char != NUL.char && (char.isJavaIdentifierPart() || listed(char)?.isSymbolChar == true)

        fun isKeywordEndChar(char: Char) = isDelimiter(char)

        fun isRegexEndChar(char: Char) = NUL.matches(char)

        fun isSymbolBeginChar(char: Char) =
            char != NUL.char && (char.isJavaIdentifierStart() || listed(char)?.isSymbolBeginChar == true)

        fun isSymbolChar(char: Char) =
            char != NUL.char && (char.isJavaIdentifierPart() || listed(char)?.isSymbolChar == true)

        fun isSymbolEndChar(char: Char) = isDelimiter(char)
    }
}