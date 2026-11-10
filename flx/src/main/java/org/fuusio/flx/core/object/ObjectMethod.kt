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
package org.fuusio.flx.core.`object`

import org.fuusio.flx.core.NAME_ANONYMOUS
import org.fuusio.flx.core.ReturnSignal
import org.fuusio.flx.core.Symbol
import org.fuusio.flx.core.error.ObjectException
import org.fuusio.flx.core.error.StackDepthExceededException
import org.fuusio.flx.core.eval
import org.fuusio.flx.core.function.AbstractFunctionObject
import org.fuusio.flx.core.macro.RecurValues
import org.fuusio.flx.core.toLiteral
import org.fuusio.flx.core.vm.Ctx

/**
 * A method of an FLX object. It is called with the instance first, then its [parameters]; the body
 * runs in a frame whose parent is the instance's scope, so it sees the instance's properties.
 */
class ObjectMethod(
    override val functionName: Symbol?,
    private val parameters: Array<Symbol>,
    private val functionBody: Any,
) : AbstractFunctionObject(functionName) {

    val name: String get() = functionName?.name ?: NAME_ANONYMOUS

    override fun call(ctx: Ctx, args: Array<Any>): Any {
        val instance = args.firstOrNull() as? ObjectInstance
            ?: throw ObjectException(ctx, "Method '$name' must be called with an object as its first argument")
        var values: Array<Any> = if (args.size <= 1) Ctx.emptyArgs else args.copyOfRange(1, args.size)
        var frame = instance.scope.createFrame(ctx)
        while (true) {
            if (parameters.size != values.size) {
                throw ObjectException(ctx, "Method '$name' takes ${parameters.size} arguments, not ${values.size}")
            }
            frame.bindSlots(parameters, values)
            val result = try {
                functionBody.eval(frame)
            } catch (signal: ReturnSignal) {
                return signal.value
            } catch (_: StackOverflowError) {
                throw StackDepthExceededException(ctx, name)
            }
            if (result !is RecurValues) return result
            values = result.values
            if (!frame.canReuseForRecur()) {
                frame = instance.scope.createFrame(ctx)
            }
        }
    }

    override fun toString(): String =
        "ObjectMethod[ name = \"$name\", parameters = [${parameters.joinToString(" ")}], body = ${functionBody.toLiteral()} ]"
}
