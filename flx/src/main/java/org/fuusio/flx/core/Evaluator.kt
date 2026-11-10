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

import org.fuusio.flx.core.error.AnyOfType
import org.fuusio.flx.Flx
import org.fuusio.flx.core.error.*
import org.fuusio.flx.core.function.FunctionSpec
import org.fuusio.flx.core.function.FunctionSymbol
import org.fuusio.flx.core.macro.MacroSpec
import org.fuusio.flx.core.macro.MacroSymbol
import org.fuusio.flx.core.type.List
import org.fuusio.flx.core.vm.Ctx

object Evaluator {

    fun eval(ctx: Ctx, list: List): Any {
        return when {
            list.isNotEmpty() -> {
                val argsCount = list.size() - 1

                when (val head = list[0]) {
                    is FunctionSpec -> callSpec(
                        ctx,
                        head,
                        Array(argsCount) { i -> list[i + 1].eval(ctx) })
                    is MacroSpec -> evalMacro(
                        ctx,
                        head,
                        Array(argsCount) { i -> list[i + 1] })
                    is Callable -> head.callWithOwnedArgs(ctx,  Array(argsCount) { i -> list[i + 1].eval(ctx) })
                    is FunctionSymbol -> callSpec(
                        ctx,
                        head.functionSpec,
                        Array(argsCount) { i -> list[i + 1].eval(ctx) })
                    is MacroSymbol -> evalMacro(
                        ctx,
                        head.macroSpec,
                        Array(argsCount) { i -> list[i + 1] })
                    is Symbol -> {
                        when (val value = head.get(ctx)) {
                            is Callable -> value.callWithOwnedArgs(ctx, Array(argsCount) { i -> list[i + 1].eval(ctx) })
                            is Null -> throw UninitializedSymbolAccessException(ctx, head)
                            else -> throw NotCallableException(ctx, value)
                        }
                    }
                    else -> this
                }
            }
            list.isEmpty() -> Null
            else -> throw IllegalStateException()
        }
    }

    fun eval(ctx: Ctx, head: Any, args: Array<Any>): Any =
        when (head) {
            is FunctionSpec -> evalFunction(ctx, head, args)
            is MacroSpec -> evalMacro(ctx, head, args)
            // A function value's arguments are evaluated like any call's. They used to be passed as
            // the unevaluated forms, so ((fn [y] (* y 2)) i) bound y to the symbol i, not its value.
            is Callable -> {
                for (i in args.indices) args[i] = args[i].eval(ctx)
                head.callWithOwnedArgs(ctx, args)
            }
            else ->  throw NotCallableException(ctx, head)
        }

    /**
     * Evaluates the forms in [args] — in place, so [args] must be an array made for this call — and
     * calls [spec]. Where the forms are at hand, evaluating them straight into [callSpec]'s array
     * does the same.
     */
    fun evalFunction(ctx: Ctx, spec: FunctionFormSpec, args: Array<Any>): Any {
        for (i in args.indices) args[i] = args[i].eval(ctx)
        return callSpec(ctx, spec, args)
    }

    fun evalMacro(ctx: Ctx, spec: FunctionFormSpec, args: Array<Any>): Any = callSpec(ctx, spec, args)

    /**
     * Calls [spec] with [args], or — when their types match none of its signatures — another
     * built-in with the same name whose signature they do match. Both '+' functions, for example:
     * the parser resolves `+` to ADDITION, and `(+ "a" 1)` still reaches STRING__PLUS. The error, if
     * nothing matches, is reported against [spec], as before.
     */
    /** A built-in called with its values; a Java error out of it becomes an FLX one (see [JavaErrors]). */
    private fun callBuiltin(ctx: Ctx, spec: FunctionFormSpec, signatureIndex: Int, args: Array<Any>): Any =
        try {
            spec.function(ctx, args, signatureIndex)
        } catch (error: Throwable) {
            // A macro's args are forms, and what it runs is the program: its errors are not its own.
            throw (if (spec.isMacroSpec()) null else JavaErrors.ofBuiltin(ctx, spec, args, error)) ?: error
        }

    fun callSpec(ctx: Ctx, spec: FunctionFormSpec, args: Array<Any>): Any {
        spec.checkArgs(args)?.let { signature -> return callBuiltin(ctx, spec, signature.index, args) }
        for (overload in Flx.getOverloads(spec)) {
            if (overload === spec) continue
            overload.checkArgs(args)?.let { signature -> return callBuiltin(ctx, overload, signature.index, args) }
        }
        val error = spec.getArgError(ctx, args)
        // With overloads, the message names every kind of value the arg could have been.
        val expected = (listOf(spec) + Flx.getOverloads(spec)).distinct()
            .mapNotNull { candidate -> candidate.signatureSpecs.firstOrNull { it.checkArgsCount(args) }?.argSpecs?.getOrNull(error.argIndex)?.type }
            .distinct()
        throw InvalidArgTypeException(
            ctx, Flx.getSymbol(spec), error.argIndex, error.argName, error.actualType,
            if (expected.size > 1) AnyOfType(expected) else error.expectedType, error.actualValue,
        )
    }
}