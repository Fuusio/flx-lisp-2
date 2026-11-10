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
package org.fuusio.flx.core.http

import android.os.Handler
import android.os.Looper
import org.fuusio.flx.core.Code
import org.fuusio.flx.core.Symbol
import org.fuusio.flx.core.function.AbstractFunctionObject
import org.fuusio.flx.core.vm.Ctx

/** An HTTP response callback; a closure over [definingCtx] when given. */
open class HttpCallback(
    private val code: Code,
    private val definingCtx: Ctx? = null,
) : AbstractFunctionObject(null) {

    init {
        definingCtx?.markClosureEscaped()
    }

    private val parameter = Symbol("response")

    override fun call(ctx: Ctx, args: Array<Any>): Any {
        val callbackCtx = definingCtx?.createFrame(ctx) ?: ctx.create()
        callbackCtx.set(parameter, args[0], true)
        return code.eval(callbackCtx)
    }

    override fun toString(): String =
        "HttpCallback[ body = ${code.toLiteral()} ]"

    companion object {
        fun invoke(ctx: Ctx, callback: HttpCallback, vararg args: Any) {
            Handler(Looper.getMainLooper()).post {
                callback.call(ctx, arrayOf(*args))
            }
        }
    }
}