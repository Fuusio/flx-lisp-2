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
package org.fuusio.flx.core.function

import org.fuusio.flx.core.*
import org.fuusio.flx.core.error.InvalidNumberOfArgsException
import org.fuusio.flx.core.error.StackDepthExceededException
import org.fuusio.flx.core.macro.RecurValues
import org.fuusio.flx.core.vm.Ctx
import java.lang.StringBuilder

/**
 * A function written in FLX.
 *
 * [definingCtx] is the scope the function was defined in. Calls run in a frame whose parent is that
 * scope, so the body is a closure over it: it sees the variables around its definition, and not
 * whatever happens to be bound at the call site. When it is null (a function built directly in
 * Kotlin), the frame falls back to the caller's scope, as before.
 */
class DefinedFunction(
    override val functionName: Symbol? = null,
    private val parameters: Array<Symbol>,
    private val functionBody: Any,
    private val definingCtx: Ctx? = null,
) : AbstractFunctionObject(functionName) {

    init {
        definingCtx?.markClosureEscaped()
    }

    // The argument values become the frame's parameter storage, so an array that may be shared
    // is copied first; the evaluator hands over a fresh one and skips the copy.
    override fun call(ctx: Ctx, args: Array<Any>): Any = callInFrame(ctx, args.copyOf())

    override fun callWithOwnedArgs(ctx: Ctx, args: Array<Any>): Any = callInFrame(ctx, args)

    private fun callInFrame(ctx: Ctx, args: Array<Any>): Any {
        var values = args
        var functionCtx = definingCtx?.createFrame(ctx) ?: ctx.create()
        while (true) {
            if (parameters.size != values.size) {
                throw InvalidNumberOfArgsException(functionCtx, this, values.size, parameters.size)
            }
            functionCtx.bindSlots(parameters, values)

            val result = try {
                functionBody.eval(functionCtx)
            } catch (signal: ReturnSignal) {
                return signal.value
            } catch (_: StackOverflowError) {
                // Reported as an FLX error the program can handle, instead of the Error that would
                // end the thread. Thrown from the deepest call whose catch has enough stack left.
                throw StackDepthExceededException(ctx, functionName?.name ?: NAME_ANONYMOUS)
            }
            // A tail (recur ...) runs the body again with new arguments; a fresh frame is made only
            // when a closure or local binding from this pass needs to keep its own scope.
            if (result !is RecurValues) return result
            values = result.values
            if (!functionCtx.canReuseForRecur()) {
                functionCtx = definingCtx?.createFrame(ctx) ?: ctx.create()
            }
        }
    }

    override fun toString(): String =
        when {
            functionName != null -> "DefinedFunction[ name = \"$functionName\", parameters = [${getParameterList()}], body = ${functionBody.toLiteral()} ]"
            else -> "DefinedFunction[ parameters = [${getParameterList()}], body = ${functionBody.toLiteral()} ]"
        }

    private fun getParameterList(): String {
        val builder = StringBuilder()
        parameters.forEach { symbol ->
            if (builder.isNotEmpty()) builder.append(' ')
            builder.append(symbol.toString())
        }
        return builder.toString()
    }

    companion object {
        init {
            // Initialize StackDepthExceededException now, while the stack is shallow. Its first use is
            // otherwise at the deepest point of a runaway recursion, where initializing the class can
            // itself overflow; the JVM then marks it unusable for good, and every later use — in this
            // run or the next test — fails with NoClassDefFoundError instead.
            StackDepthExceededException.preload()
        }
    }
}
