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
package org.fuusio.flx.core.type

import androidx.annotation.StringRes
import org.fuusio.flx.core.error.ArgError
import org.fuusio.flx.core.type.impl.isSelfEvaluatingValue
import kotlin.Array

class SignatureSpec(
    @StringRes val description: Int,
    val argSpecs: Array<ArgSpec>,
    var index: Int = 0,
)  {

    val firstRepeatableArgIndex: Int
    val repeatableArgsCount: Int

    // Facts about the signature that checkArgs used to recompute on every call.
    private val types: Array<FlxType> = Array(argSpecs.size) { i -> argSpecs[i].type }
    private val isRepeatable: Boolean = argSpecs.isNotEmpty() && argSpecs.last().isRepeatable()
    private val isOptionalOrVararg: Boolean =
        argSpecs.isNotEmpty() && (argSpecs.last().isOptional() || argSpecs.last().isVarArg())

    /** Every argument accepts anything, so only the count needs checking. */
    internal val acceptsAnyTypes: Boolean = argSpecs.all { it.type === CoreType.ANY }

    init {
        var counter = 0
        var firstIndex = -1
        repeat(argSpecs.size) { index ->
            if (argSpecs[index].isRepeatable()) {
                if (firstIndex < 0) firstIndex = index
                counter++
            }
        }
        firstRepeatableArgIndex = firstIndex
        repeatableArgsCount = counter
    }

    fun getArgName(index: Int): String {
        val argSpec = if (index < argSpecs.size) argSpecs[index] else argSpecs.last()
        return argSpec.getArgName()
    }

    fun getArgSpec(index: Int): ArgSpec {
        val argSpecsCount = argSpecs.size
        return if (hasRepeatableArgs()) {
            if (index < firstRepeatableArgIndex) {
                argSpecs[index]
            } else {
                val offset = (index - firstRepeatableArgIndex) % repeatableArgsCount
                argSpecs[firstRepeatableArgIndex + offset]
            }
        } else {
            if (index < argSpecsCount) {
                argSpecs[index]
            } else {
                argSpecs.last()
            }
        }
    }

    fun getArgsCount(): Int  = argSpecs.size

    fun getRequiredArgsCount(): Int = argSpecs.size - if (isLastArgOptional()) 1 else 0

    private fun isLastArgOptional(): Boolean {
        val count = argSpecs.size
        return count > 0 && (argSpecs[count - 1].isOptional() || argSpecs[count - 1].isVarArg())
    }


    fun getArgType(index: Int): FlxType = if (index < argSpecs.size) argSpecs[index].type else argSpecs.last().type

    fun checkArgs(args: Array<Any>): Boolean = checkArgsCount(args) && checkArgTypes(args)

    /** Checks only argument types, when [checkArgsCount] is already known to be true. */
    internal fun checkArgTypes(args: Array<Any>): Boolean {
        if (acceptsAnyTypes) return true
        for (i in args.indices) {
            val type = typeAt(i)
            if (type !== CoreType.ANY && !type.isInstance(args[i])) return false
        }
        return true
    }

    /** Whether [argForms] are guaranteed to satisfy this signature's types on every evaluation. */
    internal fun alwaysMatchesTypes(argForms: Array<Any>): Boolean {
        if (acceptsAnyTypes) return true
        for (i in argForms.indices) {
            val type = typeAt(i)
            if (type === CoreType.ANY) continue
            val form = argForms[i]
            if (!form.isSelfEvaluatingValue() || !type.isInstance(form)) return false
        }
        return true
    }

    /** [getArgSpec]'s type, without going through the ArgSpec. */
    private fun typeAt(index: Int): FlxType =
        when {
            isRepeatable && index >= firstRepeatableArgIndex ->
                types[firstRepeatableArgIndex + (index - firstRepeatableArgIndex) % repeatableArgsCount]
            index < types.size -> types[index]
            else -> types[types.size - 1]
        }

    fun checkArgsCount(argValues: Array<Any>) =
            if (isRepeatable) {
                argValues.size >= argSpecs.size &&
                        (argValues.size - firstRepeatableArgIndex) % repeatableArgsCount == 0
            } else {
                if (isOptionalOrVararg) {
                    argValues.size >= argSpecs.size - 1
                } else {
                    argValues.size == argSpecs.size
                }
            }

    fun getArgError(args: Array<Any>): ArgError? {
        for (i in args.indices) {
            // Past the declared args, a vararg's or repeatable's spec applies (it used to index past the specs).
            val spec = getArgSpec(i)
            if (!spec.type.isInstance(args[i]))
                return ArgError(i, spec.getArgName(), spec.type, args[i]::class, args[i])
        }
        return null
    }

    fun hasVararg() = argSpecs.isNotEmpty() && argSpecs.last().isVarArg()

    fun hasOptionalArg() = argSpecs.isNotEmpty() && argSpecs.last().isOptional()

    private fun hasOptionalOrVararg() =
        argSpecs.isNotEmpty() && (argSpecs.last().isOptional() || argSpecs.last().isVarArg())

    fun hasRepeatableArgs() = argSpecs.isNotEmpty() && (argSpecs.last().isRepeatable())
}