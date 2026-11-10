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

import org.fuusio.flx.core.type.impl.ImmutableArray
import org.fuusio.flx.core.type.impl.ImmutableList
import org.fuusio.flx.core.type.impl.ImmutableMap
import org.fuusio.flx.core.type.impl.ImmutableSet

class FormParser(private val observer: ParserObserver) {

    fun parse(context: ParsingContext, expression: String): List<Any> {
        val parsingResults = mutableListOf<Any>()
        context.tokenStream = TokenStream(expression)

        while (context.canContinue()) {
            parse(context, parsingResults)?.let { result ->
                parsingResults.add(result)
                observer.onNext(result)
            }
        }
        if (context.hasErrors()) parsingResults.clear()
        return parsingResults
    }

    private fun parse(
        context: ParsingContext,
        elements: MutableList<Any>,
        beginToken: Token? = null,
        endToken: Token? = null
    ): Any? {

        while (context.canContinue()) {
            val token = context.nextToken()
            val result: Any? = when (token) {

                is SymbolToken -> token.toObject()
                // "Count: ${count}" is (str "Count: " count); other strings are themselves.
                is StringToken -> org.fuusio.flx.core.StringTemplate.compile(token.string)
                is IntegerToken -> token.toObject()
                is DoubleToken -> token.toObject()
                is CharToken -> token.toObject()
                is BaseIntegerToken -> token.toObject()
                is HexadecimalToken -> token.toObject()
                is KeywordToken -> token.toObject()
                is RegexToken -> token.toObject()

                is ListBeginToken -> parseStructure(context, token, ListEndToken.TOKEN)
                is ArrayBeginToken -> parseStructure(context, token, ArrayEndToken.TOKEN)
                is MapBeginToken -> parseStructure(context, token, MapOrSetEndToken.TOKEN)
                is SetBeginToken -> parseStructure(context, token, MapOrSetEndToken.TOKEN)

                is ListEndToken, is ArrayEndToken, is MapOrSetEndToken -> {
                    // Only the closer of the structure being parsed may end it. Any other — a stray
                    // ')' at the top level, the ']' in (1 2] — used to be accepted and built its
                    // collection from the enclosing element list, which then had that collection
                    // added to it: a structure containing itself. A stray '}' crashed on beginToken!!.
                    if (token !== endToken) {
                        return onError(
                            context,
                            ParserErrorCode.UNEXPECTED_CLOSING,
                            token.content(),
                            context.tokenStream.getTokenBeginPosition(),
                        )
                    }
                    context.decParsingLevel()
                    return when (token) {
                        is ListEndToken -> ImmutableList(elements)
                        is ArrayEndToken -> ImmutableArray(elements)
                        else -> createMapOrSet(context, beginToken!!, elements)
                    }
                }

                is CommentToken -> null
                is WhiteSpaceToken -> null

                // The token's own error — "Invalid char: 'ab'", "Failed to parse string at 12" —
                // rather than the generic "Failed to parse a form" it used to be replaced with.
                is ErrorToken -> reportError(context, token.error)

                is ParsingCompletedToken -> {
                    if (endToken != null) {
                        val code = when(beginToken) {
                            ArrayBeginToken.TOKEN -> ParserErrorCode.FAILED_TO_PARSE_ARRAY
                            ListBeginToken.TOKEN -> ParserErrorCode.FAILED_TO_PARSE_LIST
                            MapBeginToken.TOKEN -> ParserErrorCode.FAILED_TO_PARSE_MAP
                            SetBeginToken.TOKEN -> ParserErrorCode.FAILED_TO_PARSE_SET
                            else -> ParserErrorCode.UNKNOWN_ERROR
                        }
                        onError(context, code, context.tokenStream.getPosition())
                    }
                    context.parsingCompleted = true
                    null
                }
            }

            when {
                context.isTopParsingLevel() -> return result
                else -> result?.let { elements.add(it) }
            }
        }
        return null
    }

    private fun parseStructure(context: ParsingContext, beginToken: Token, endToken: Token): Any? {
        context.incParsingLevel()
        return parse(context, mutableListOf(), beginToken, endToken)
    }

    private fun createMapOrSet(context: ParsingContext, beginToken: Token, elements: MutableList<Any>): Any? =
        when(beginToken) {
            is MapBeginToken -> createMap(context, elements)
            is SetBeginToken -> createSet(context, elements)
            else -> throw IllegalStateException()
        }

    private fun createMap(context: ParsingContext, elements: MutableList<Any>): Any? {
        return if (context.token == MapOrSetEndToken.TOKEN) {
            val size = elements.size

            if (size % 2 == 0) {
                val map = mutableMapOf<Any, Any>()
                for (i in 0 until size step 2) map[elements[i]] = elements[i + 1]
                ImmutableMap(map)
            } else
                onError(context, ParserErrorCode.FAILED_TO_PARSE_MAP, context.tokenStream.getPosition())
        } else
            onError(context, ParserErrorCode.FAILED_TO_PARSE_MAP, context.tokenStream.getPosition())
    }

    private fun createSet(context: ParsingContext, elements: MutableList<Any>): Any? {
        return if (context.token == MapOrSetEndToken.TOKEN)
            ImmutableSet(elements.toMutableSet())
        else
            onError(context, ParserErrorCode.FAILED_TO_PARSE_SET, context.tokenStream.getPosition())
    }

//    private fun createQuote(element: Any): org.fuusio.flx.core.type.List {
//        val list = mutableListOf<Any>()
//        list.add(Flx.getSymbol(MacroSpec.QUOTE))
//        list.add(element)
//        return ImmutableList(list)
//    }

    private fun reportError(context: ParsingContext, error: ParserError): Any? {
        context.errors.add(error)
        observer.onError(error)
        return null
    }

    private fun onError(
        context: ParsingContext,
        errorCode: ParserErrorCode,
        vararg args: Any
    ): Any? {
        val error = ParserError.create(errorCode, *args)
        context.errors.add(error)
        observer.onError(error)
        return null
    }
}

 

