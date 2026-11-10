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
package org.fuusio.flx.core.error

import org.fuusio.flx.core.vm.Ctx
import org.fuusio.flx.Flx
import org.fuusio.flx.core.FunctionFormSpec
import org.fuusio.flx.core.Symbol
import org.fuusio.flx.core.function.FunctionSpec
import org.fuusio.flx.core.macro.MacroSpec
import org.fuusio.flx.core.type.FlxType
import kotlin.reflect.KClass

class InvalidArgTypeException(
    ctx: Ctx,
    private val functionName: Symbol,
    private val argIndex: Int,
    private val argName: String,
    private val wrongArgType: KClass<*>,
    private val expectedType: FlxType? = null,
    private val actualValue: Any? = null,
) : FlxException(ctx) {

    constructor(ctx: Ctx, functionSpec: FunctionSpec, argError: ArgError, wrongArgType: KClass<*>)
            : this(ctx, Flx.getSymbol(functionSpec), argError.argIndex, argError.argName, wrongArgType, argError.expectedType, argError.actualValue)

    constructor(ctx: Ctx, functionSpec: FunctionFormSpec, argError: ArgError)
            : this(ctx, Flx.getSymbol(functionSpec), argError.argIndex, argError.argName, argError.actualType, argError.expectedType, argError.actualValue)

    constructor(ctx: Ctx, macroSpec: MacroSpec, argError: ArgError)
            : this(ctx, Flx.getSymbol(macroSpec), argError.argIndex, argError.argName, argError.actualType, argError.expectedType, argError.actualValue)

    /** As the user reads it: "'.setText' needs a widget as 'widget', got a number (5)". */
    override val message: String
        get() {
            val expected = expectedType?.let(Describe::type)
            val got = if (actualValue != null) Describe.value(actualValue) else "a value of type '${wrongArgType.simpleName}'"
            return if (expected != null) "'$functionName' needs $expected as '$argName', got $got"
            else "'$functionName' cannot take $got as '$argName' (arg ${argIndex + 1})"
        }
}