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
package org.fuusio.flx.core.repl

import org.fuusio.flx.Flx
import org.fuusio.flx.core.*
import org.fuusio.flx.core.error.FlxException
import org.fuusio.flx.core.error.FlxRuntimeException
import org.fuusio.flx.core.error.ParseException
import org.fuusio.flx.core.error.StackDepthExceededException
import org.fuusio.flx.core.parser.FormParser
import org.fuusio.flx.core.parser.ParserError
import org.fuusio.flx.core.parser.ParserObserver
import org.fuusio.flx.core.parser.ParsingContext
import org.fuusio.flx.core.vm.Ctx

class FlxRepl(val ctx: Ctx = Flx.ctx) {

    fun parse(string: String, observer: ParserObserver): List<Any> =
        FormParser(observer).parse(ParsingContext(), string)

    fun eval(string: String, parseObserver: ParserObserver = createObserver(string)): Any {
        val forms = parse(string, parseObserver)
        var value: Any = Null

        forms.forEach { form -> value = evalTopLevel(form) }
        return value
    }

    /** A top-level form's value. A `(return …)` outside any function ends the form it is in, with its value. */
    private fun evalTopLevel(form: Any): Any =
        try {
            form.eval(ctx)
        } catch (signal: ReturnSignal) {
            signal.value
        }

    fun eval(forms: List<Any>): Any {
        var value: Any = Null

        try {
            forms.forEach { form -> value = evalTopLevel(form) }
        } catch (exception: FlxException) {
            // exception.printStackTrace()
            ctx.onException(exception)
        } catch (exception: Exception) {
            //exception.printStackTrace()
            ctx.onException(FlxRuntimeException(ctx, exception))
        } catch (_: StackOverflowError) {
            // Nesting too deep outside any function (user functions report it themselves).
            ctx.onException(StackDepthExceededException(ctx, NAME_ANONYMOUS))
        }
        return value
    }

    /**
     * Reports a syntax error as a [ParseException], as ParsedForm does. The error used to be ignored
     * here, and since the parser drops its results on error, eval("(+ 1") quietly returned Null.
     */
    private fun createObserver(literal: String) = object : ParserObserver {
        override fun onNext(parsingResult: Any) {}

        override fun onError(error: ParserError) {
            throw ParseException(ctx, error, literal)
        }
    }
}