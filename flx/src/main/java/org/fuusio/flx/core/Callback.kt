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

import android.os.Handler
import android.os.Looper
import org.fuusio.flx.core.error.InvalidNumberOfArgsException
import org.fuusio.flx.core.function.AbstractFunctionObject
import org.fuusio.flx.core.vm.Ctx
import java.lang.StringBuilder

/** A callback; like [org.fuusio.flx.core.function.DefinedFunction], a closure over [definingCtx] when given. */
open class Callback(
    private val parameters: Array<Symbol>,
    private val code: Code,
    private val definingCtx: Ctx? = null,
) : AbstractFunctionObject(null) {

    init {
        definingCtx?.markClosureEscaped()
    }

    // Same storage rule as DefinedFunction: copy a possibly shared array, adopt an owned one.
    // Observable, for one, passes the same event array to every observer.
    override fun call(ctx: Ctx, args: Array<Any>): Any = callInFrame(ctx, args.copyOf())

    override fun callWithOwnedArgs(ctx: Ctx, args: Array<Any>): Any = callInFrame(ctx, args)

    private fun callInFrame(ctx: Ctx, args: Array<Any>): Any {
        val callbackCtx = definingCtx?.createFrame(ctx) ?: ctx.create()

        if (parameters.size != args.size) {
            ctx.onException(InvalidNumberOfArgsException(ctx, this, args.size, parameters.size))
            return Null
        }
        callbackCtx.bindSlots(parameters, args)
        return code.eval(callbackCtx)
    }

    override fun toString(): String =
        "Callback[ name = \"$functionName\", parameters = [${getParameterList()}], body = ${code.toLiteral()} ]"

    private fun getParameterList(): String {
        val builder = StringBuilder()
        parameters.forEach { symbol ->
            if (builder.isNotEmpty()) builder.append(' ')
            builder.append(symbol.toString())
        }
        return builder.toString()
    }

    companion object {
        fun invoke(ctx: Ctx, callback: Callback, vararg args: Any) {
            Handler(Looper.getMainLooper()).post {
                callback.callWithOwnedArgs(ctx, arrayOf(*args))
            }
        }
    }
}