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
@file:Suppress("UNUSED_PARAMETER")

package org.fuusio.flx.core.macro

import org.fuusio.flx.Flx
import org.fuusio.flx.core.*
import org.fuusio.flx.core.`object`.ObjectMethod
import org.fuusio.flx.core.error.ArgError
import org.fuusio.flx.core.error.FlxException
import org.fuusio.flx.core.error.InvalidArgException
import org.fuusio.flx.core.error.InvalidNumberOfArgsException
import org.fuusio.flx.core.error.NotCallableException
import org.fuusio.flx.core.error.UninitializedSymbolAccessException
import org.fuusio.flx.core.error.ValRedefinitionException
import org.fuusio.flx.core.error.VarRedefinitionException
import org.fuusio.flx.core.function.DefinedFunction
import org.fuusio.flx.core.function.FunctionObject
import org.fuusio.flx.core.function.FunctionSpec
import org.fuusio.flx.core.function.FunctionSymbol
import org.fuusio.flx.core.function.invokeUnary
import org.fuusio.flx.core.http.HttpCallback
import org.fuusio.flx.core.type.Array
import org.fuusio.flx.core.type.CoreType
import org.fuusio.flx.core.type.List
import org.fuusio.flx.core.type.Sequence
import org.fuusio.flx.core.type.impl.ImmutableList
import org.fuusio.flx.core.util.isEven
import org.fuusio.flx.core.util.isOdd
import org.fuusio.flx.core.vm.Ctx

fun fnControlFlowReturn(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    // Leaves the function (see ReturnSignal); a bare (return) gives nil.
    throw ReturnSignal(if (args.isNotEmpty()) args[0].eval(ctx) else Null)
}

fun fnAssign(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val symbol = args[0] as Symbol
    val value = args[1].eval(ctx)
    when (val assignment = ctx.getAssignment(symbol)) {
        is ValAssignment -> throw ValRedefinitionException(ctx, symbol)
        is VarAssignment -> assignment.set(value)
        is SymbolAssignment -> assignment.set(value)
        is SlotAssignment -> assignment.set(value)
        else -> ctx.addAssignment(symbol, SymbolAssignment(value))
    }
    return value
}

fun fnWhenWithArgument(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val switchValue = args[0].eval(ctx)
    val argsCount = args.size
    val isEvenArgs = argsCount.isEven()
    val lastIndex = argsCount - if (isEvenArgs) 0 else 1
    for (i in 1 until lastIndex step 2) {
        val caseValue = args[i].eval(ctx)
        if (equals(caseValue, switchValue) || caseValue is Else) return args[i + 1].eval(ctx)
    }
    return Null
}

private fun equals(value1: Any, value2: Any): Boolean =
    normalize(value1) == normalize(value2)

private fun normalize(value: Any): Any =
    when (value) {
        is Int -> value.toLong()
        is Short -> value.toLong()
        is Byte -> value.toLong()
        is Float -> value.toDouble()
        else -> value
    }

fun fnDecrease(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    val symbol = args[0] as Symbol
    val value: Number = when (val originalValue = symbol.get(ctx)) {
        is Float -> originalValue.toDouble() - 1
        is Double -> originalValue - 1
        is Number -> originalValue.toLong() - 1
        else -> throw InvalidArgException(
            ctx,
            Flx.getSymbol(MacroSpec.DECREASE),
            1,
            NAME_VARIABLE,
            "Symbol is required to have a number value"
        )
    }
    symbol.set(ctx, value)
    return value
}

fun fnFn(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): DefinedFunction =
    DefinedFunction(null, prepareParameters(ctx, MacroSpec.FUN, 0, args[0]), packFunctionBody(args, 1), ctx)

fun fnMethod(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Symbol {
    val name = args[0] as Symbol
    val paramsArray = args[1] as Array
    val params = Array(paramsArray.size()) { i -> getSymbol(
        ctx,
        MacroSpec.METHOD,
        1,
        NAME_ARGS,
        paramsArray[i]
    )}
    val body = packFunctionBody(args, 2)
    val method = ObjectMethod(name, params, body)
    ctx.set(name, method)
    return name
}

fun fnFun(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Symbol =
    defineFunction(ctx, args[0] as Symbol, prepareParameters(ctx, MacroSpec.FUN, 1, args[1]), packFunctionBody(args, 2))

fun fnCallback(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Callback {
    val code = when (val arg = args[0]) {
        is Code -> arg
        else -> arg.eval(ctx) as Code
    }
    val params = Array(args.size - 1) { i -> getSymbol(
        ctx,
        MacroSpec.CALLBACK,
        i + 1,
        NAME_ARGS,
        args[i + 1]
    )}
    return Callback(params, code, ctx)
}

fun fnHttpCallback(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): HttpCallback {
    val code = when (val arg = args[0]) {
        is Code -> arg
        else -> arg.eval(ctx) as Code
    }
    return HttpCallback(code, ctx)
}

fun fnProc(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Callback {
    val paramsArray = args[0] as Array
    val params = Array(paramsArray.size()) { i -> getSymbol(
        ctx,
        MacroSpec.PROC,
        0,
        NAME_ARGS,
        paramsArray[i]
    )}
    val code = args[1] as Code
    return Callback(params, code, ctx)
}

fun fnCode(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Code =
    Code(args.copyOf())

fun fnDo(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val macroCtx = ctx.create()
    var result: Any = Null
    args.forEach { arg ->
        result = arg.eval(macroCtx)
    }
    return result
}

fun fnRepeat(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val times = when (val number = args[0].eval(ctx)) {
        is Number -> number.toLong()
        else -> throw InvalidArgException(
            ctx, MacroSpec.REPEAT, ArgError(
                0,
                "n",
                CoreType.NUMBER,
                number::class
            ), "First argument has to evaluate to a number"
        )
    }
    var result: Any = Null
    for (i in 0 until times) {
        result = evalLoopBodyForms(ctx, args, 1)
        if (!ctx.canContinue()) break
    }
    return result
}

fun fnControlFlowFor(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val macroCtx = ctx.create()
    val symbol = args[0] as Symbol
    val start = (args[1].eval(macroCtx) as Number).toInt()
    val end = (args[2].eval(macroCtx) as Number).toInt()
    val step = (args[3].eval(macroCtx) as Number).toInt()
    var result: Any = Null

    // The loop variable belongs to the loop: bound once in the loop's own scope and updated in place.
    // It used to be set with symbol.set, which walked out through the enclosing scopes on every
    // iteration and overwrote a variable of the same name there, if there was one.
    val index = SymbolAssignment(start)
    macroCtx.addAssignment(symbol, index)

    for (i in start..end step step) {
        index.set(i)
        result = evalLoopBodyForms(macroCtx, args, 4)
        if (!macroCtx.canContinue()) break
    }
    return result
}

fun fnForEach(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val macroCtx = ctx.create()
    val symbol = args[0] as Symbol
    var result: Any = Null

    // As in fnControlFlowFor: the element variable is local to the loop and updated in place.
    val element = SymbolAssignment(Null)
    macroCtx.addAssignment(symbol, element)

    when (val sequence = args[1].eval(macroCtx)) {
        is Sequence -> {
            val count = sequence.size()

            for (i in 0 until count) {
                element.set(sequence[i])
                result = evalLoopBodyForms(macroCtx, args, 2)
                if (!macroCtx.canContinue()) break
            }
        }
        is String -> {
            sequence.forEach { char ->
                element.set(char)
                result = evalLoopBodyForms(macroCtx, args, 2)
                if (!macroCtx.canContinue()) return result
            }
        }
        else -> throw InvalidArgException(
            ctx, MacroSpec.FOR_EACH, ArgError(
                1,
                "sequence",
                CoreType.SEQUENCE,
                sequence::class
            ), "Second argument has to evaluate to a sequence"
        )
    }
    return result
}

@Suppress("UNUSED_PARAMETER")
fun fnHelp(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Info {
    val systemSymbol = args[0] as SystemSymbol
    val name = systemSymbol.name
    val spec = systemSymbol.getSpec()
    val builder = StringBuilder()

    spec.signatureSpecs.forEach { signatureSpec ->
        if (builder.isEmpty()) builder.append(' ')

        signatureSpec.argSpecs.forEach { argSpec ->
            builder.append(' ')
            builder.append(argSpec.getArgName())
            builder.append(": ")
            builder.append(argSpec.type.getTypeName())
        }
    }

    val argNames = builder.toString()
    return Info("($name$argNames) \u21e8 ${spec.outputType.getTypeName()}")
}

fun fnIncrease(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    val symbol = args[0] as Symbol
    val value: Number = when (val originalValue = symbol.get(ctx)) {
        is Float -> originalValue.toDouble() + 1
        is Double -> originalValue + 1
        is Number -> originalValue.toLong() + 1
        else -> throw InvalidArgException(
            ctx,
            Flx.getSymbol(MacroSpec.INCREASE),
            1,
            NAME_VARIABLE,
            "Symbol is required to have a number value"
        )
    }
    symbol.set(ctx, value)
    return value
}

fun fnControlFlowIfElse(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any
        = if (args[0].eval(ctx).toBoolean()) args[1].eval(ctx) else args[2].eval(ctx)

fun fnControlFlowIf(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any
    = if (args[0].eval(ctx).toBoolean()) args[1].eval(ctx) else Null // nil, as from (when false …)

fun fnLet(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any =
    evalLet(ctx, prepareLetBindings(ctx, args[0]), args)

fun fnMap(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): List {
    val head = args[0].eval(ctx)
    val function: Callable? = when (head) {
        is Callable -> head
        is FunctionSymbol -> head.functionSpec
        is MacroSymbol -> head.macroSpec
        is Keyword -> if (args.size == 2) null else {
            val argError = ArgError(0, NAME_FUNCTION, CoreType.FUNCTION, head::class)
            throw InvalidArgException(
                ctx,
                MacroSpec.MAP,
                argError,
                "Expression ${args[0].toLiteral()} does not reference to a function"
            )
        }
        else -> {
            val argError = ArgError(0, NAME_FUNCTION, CoreType.FUNCTION, head::class)
            throw InvalidArgException(
                ctx,
                MacroSpec.MAP,
                argError,
                "Expression ${args[0].toLiteral()} does not reference to a function"
            )
        }
    }

    val sequencesCount = args.size - 1
    if (sequencesCount == 1) {
        return when (val sequence = args[1].eval(ctx)) {
            is Sequence -> {
                val size = sequence.size()
                val items = ArrayList<Any>(size)
                for (i in 0 until size) {
                    val elem = sequence[i]
                    items.add(
                        if (function != null) function.callWithOwnedArgs(ctx, arrayOf(elem))
                        else invokeUnary(ctx, head, elem)
                    )
                }
                ImmutableList.create(items)
            }
            is String -> {
                val size = sequence.length
                val items = ArrayList<Any>(size)
                for (i in 0 until size) {
                    val elem = sequence[i]
                    items.add(
                        if (function != null) function.callWithOwnedArgs(ctx, arrayOf(elem))
                        else invokeUnary(ctx, head, elem)
                    )
                }
                ImmutableList.create(items)
            }
            else -> {
                val argError = ArgError(0, NAME_SEQUENCE, CoreType.SEQUENCE, sequence::class)
                throw InvalidArgException(
                    ctx,
                    MacroSpec.MAP,
                    argError,
                    "Invalid sequence argument. Expression ${args[1].toLiteral()} is not a sequence nor string"
                )
            }
        }
    }

    val sequences = Array(sequencesCount) { i ->
        when (val sequence = args[i + 1].eval(ctx)) {
            is Sequence, is String -> sequence
            else -> {
                val argError = ArgError(0, NAME_SEQUENCE, CoreType.SEQUENCE, sequence::class)
                throw InvalidArgException(
                    ctx,
                    MacroSpec.MAP,
                    argError,
                    "Invalid sequence argument. Expression ${args[1].toLiteral()} is not a sequence nor string"
                )
            }
        }
    }

    val minSize = sequences.minOf { seq ->
        when (seq) {
            is Sequence -> seq.size()
            is String -> seq.length
            else -> 0
        }
    }
    val items = ArrayList<Any>(minSize)
    for (itemIndex in 0 until minSize) {
        val callArgs = Array(sequencesCount) { i ->
            when (val seq = sequences[i]) {
                is Sequence -> seq[itemIndex]
                is String -> seq[itemIndex]
                else -> Null
            }
        }
        items.add(function!!.callWithOwnedArgs(ctx, callArgs))
    }
    return ImmutableList.create(items)
}

/**
 * `(loop [name value ...] form*)`: like `let`, but a last form evaluating to `(recur ...)` rebinds
 * the names and runs the forms again — iteration in constant stack space.
 */
fun fnLoop(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any =
    evalLoop(ctx, prepareLoopBindings(ctx, args[0]), args)

/** `(recur form*)`: the evaluated forms, for the enclosing `loop` or function to run again with. */
fun fnRecur(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): RecurValues =
    if (args.isEmpty()) RecurValues.EMPTY else RecurValues(Array(args.size) { i -> args[i].eval(ctx) })

@Suppress("UNUSED_PARAMETER")
fun fnQuote(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any = args[0]

fun fnVal(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val symbol = args[0] as Symbol

    if (ctx.hasAssignment(symbol)) throw ValRedefinitionException(ctx, symbol)

    val value = args[1].eval(ctx)
    ctx.addAssignment(symbol, ValAssignment(value))
    return value
}

fun fnVar(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val symbol = args[0] as Symbol

    if (ctx.hasAssignment(symbol)) throw VarRedefinitionException(ctx, symbol)

    var value: Any = Null
    for (i in 1 until args.size) {
        value = args[1].eval(ctx)
    }
    ctx.addAssignment(symbol, VarAssignment(value))
    return value
}

fun fnWhen(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val argsCount = args.size
    val isEvenArgs = argsCount.isEven()
    val lastIndex = argsCount - if (isEvenArgs) 0 else 1
    for (i in 0 until lastIndex step 2) {
        if (args[i].eval(ctx).toBoolean()) return args[i + 1].eval(ctx)
    }
    return Null
}

fun fnWhile(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val macroCtx = ctx.create()
    val expression = args[0]
    var result: Any = Null
    while (expression.eval(macroCtx).toBoolean() && ctx.canContinue()) {
        result = evalLoopBodyForms(macroCtx, args, 1)
    }
    return result
}

private class QuotedValue(val value: Any) : EvaluableObject() {
    override fun eval(ctx: Ctx): Any = value
}

fun fnThreadFirst(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    var acc: Any = args[0].eval(ctx)
    for (i in 1 until args.size) {
        acc = evalThreadedStep(ctx, acc, args[i], threadFirst = true)
    }
    return acc
}

fun fnThreadLast(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    var acc: Any = args[0].eval(ctx)
    for (i in 1 until args.size) {
        acc = evalThreadedStep(ctx, acc, args[i], threadFirst = false)
    }
    return acc
}

private fun evalThreadedStep(ctx: Ctx, acc: Any, stepForm: Any, threadFirst: Boolean): Any {
    if (stepForm is List) {
        if (stepForm.isEmpty()) return acc
        val head = stepForm[0]
        val restCount = stepForm.size() - 1
        if (head is MacroSymbol || head is MacroSpec) {
            val macroSpec = if (head is MacroSymbol) head.macroSpec else head as MacroSpec
            val macroArgs = Array(restCount + 1) { index ->
                if (threadFirst) {
                    if (index == 0) QuotedValue(acc) else stepForm[index]
                } else {
                    if (index < restCount) stepForm[index + 1] else QuotedValue(acc)
                }
            }
            return Evaluator.evalMacro(ctx, macroSpec, macroArgs)
        }
        val callArgs = Array(restCount + 1) { index ->
            if (threadFirst) {
                if (index == 0) acc else stepForm[index].eval(ctx)
            } else {
                if (index < restCount) stepForm[index + 1].eval(ctx) else acc
            }
        }
        return invokeHeadWithArgs(ctx, head, callArgs)
    }
    return when (stepForm) {
        is MacroSymbol -> Evaluator.evalMacro(ctx, stepForm.macroSpec, arrayOf(QuotedValue(acc)))
        is MacroSpec -> Evaluator.evalMacro(ctx, stepForm, arrayOf(QuotedValue(acc)))
        else -> invokeHeadWithArgs(ctx, stepForm, arrayOf(acc))
    }
}

private fun invokeHeadWithArgs(ctx: Ctx, head: Any, callArgs: kotlin.Array<Any>): Any =
    when (head) {
        is FunctionSymbol -> Evaluator.callSpec(ctx, head.functionSpec, callArgs)
        is FunctionSpec -> Evaluator.callSpec(ctx, head, callArgs)
        is Keyword -> invokeUnary(ctx, head, callArgs[0])
        is Callable -> head.callWithOwnedArgs(ctx, callArgs)
        is Symbol -> when (val value = head.get(ctx)) {
            is FunctionSpec -> Evaluator.callSpec(ctx, value, callArgs)
            is Keyword -> invokeUnary(ctx, value, callArgs[0])
            is Callable -> value.callWithOwnedArgs(ctx, callArgs)
            is Null -> throw UninitializedSymbolAccessException(ctx, head)
            else -> throw NotCallableException(ctx, value)
        }
        is List -> when (val value = head.eval(ctx)) {
            is FunctionSpec -> Evaluator.callSpec(ctx, value, callArgs)
            is Keyword -> invokeUnary(ctx, value, callArgs[0])
            is Callable -> value.callWithOwnedArgs(ctx, callArgs)
            else -> throw NotCallableException(ctx, value)
        }
        else -> throw NotCallableException(ctx, head)
    }

fun fnCoalesce(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    for (arg in args) {
        val value = arg.eval(ctx)
        if (value !is Null) return value
    }
    return Null
}

fun fnIfLet(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val bindings = prepareConditionalBindings(ctx, MacroSpec.IF_LET, args[0])
    val names = bindings.names
    val valueForms = bindings.valueForms
    val branchCtx = ctx.create()
    for (i in names.indices) {
        val evalCtx = if (i == 0) ctx else branchCtx
        val value = valueForms[i].eval(evalCtx)
        if (!value.toBoolean()) {
            return if (args.size >= 3) args[2].eval(ctx) else Null
        }
        if (names.size == 1) {
            branchCtx.bindSlots(names, arrayOf(value))
        } else {
            branchCtx.addAssignment(names[i], SymbolAssignment(value))
        }
    }
    return args[1].eval(branchCtx)
}

fun fnWhenLet(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val bindings = prepareConditionalBindings(ctx, MacroSpec.WHEN_LET, args[0])
    val names = bindings.names
    val valueForms = bindings.valueForms
    val branchCtx = ctx.create()
    for (i in names.indices) {
        val evalCtx = if (i == 0) ctx else branchCtx
        val value = valueForms[i].eval(evalCtx)
        if (!value.toBoolean()) return Null
        if (names.size == 1) {
            branchCtx.bindSlots(names, arrayOf(value))
        } else {
            branchCtx.addAssignment(names[i], SymbolAssignment(value))
        }
    }
    var result: Any = Null
    for (i in 1 until args.size) {
        result = args[i].eval(branchCtx)
    }
    return result
}

private fun prepareConditionalBindings(ctx: Ctx, spec: MacroSpec, bindingsForm: Any): LetBindings {
    val bindings = bindingsForm as? Array ?: throw InvalidArgException(
        ctx, spec, ArgError(0, NAME_BINDINGS, CoreType.ARRAY, bindingsForm::class),
        "Expression ${bindingsForm.toLiteral()} does not evaluate to an Array"
    )
    val bindingsCount = bindings.size()
    if (bindingsCount == 0 || bindingsCount.isOdd()) {
        throw InvalidArgException(ctx, spec, ArgError(0, NAME_BINDINGS, CoreType.ANY, Any::class), "Invalid bindings definition")
    }
    val count = bindingsCount / 2
    val names = Array(count) { i ->
        bindings[i * 2] as? Symbol ?: throw InvalidArgException(
            ctx, spec, ArgError(0, NAME_BINDINGS, CoreType.SYMBOL, bindings[i * 2]::class),
            "Invalid symbol bindings definition. Expression ${bindings[i * 2].toLiteral()} is not a Symbol"
        )
    }
    val valueForms = Array(count) { i -> bindings[i * 2 + 1] }
    return LetBindings(names, valueForms, hasRepeatedNames = false)
}

fun fnToggle(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Boolean {
    val symbol = args[0] as Symbol
    val value = !symbol.get(ctx).toBoolean()
    symbol.set(ctx, value)
    return value
}

fun fnAddAssign(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val symbol = args[0] as Symbol
    val originalValue = symbol.get(ctx)
    val delta = args[1].eval(ctx)
    val newValue: Any = when (originalValue) {
        is Number -> {
            if (delta !is Number) {
                throw InvalidArgException(
                    ctx,
                    Flx.getSymbol(MacroSpec.ADD_ASSIGN),
                    2,
                    NAME_VALUE,
                    "Value is required to be a number"
                )
            }
            if (originalValue is Double || originalValue is Float || delta is Double || delta is Float) {
                originalValue.toDouble() + delta.toDouble()
            } else {
                originalValue.toLong() + delta.toLong()
            }
        }
        is String -> originalValue + delta.toString()
        else -> throw InvalidArgException(
            ctx,
            Flx.getSymbol(MacroSpec.ADD_ASSIGN),
            1,
            NAME_VARIABLE,
            "Symbol is required to have a number or string value"
        )
    }
    symbol.set(ctx, newValue)
    return newValue
}

fun fnSubAssign(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Number {
    val symbol = args[0] as Symbol
    val originalValue = symbol.get(ctx)
    val delta = args[1].eval(ctx)
    if (originalValue !is Number) {
        throw InvalidArgException(
            ctx,
            Flx.getSymbol(MacroSpec.SUB_ASSIGN),
            1,
            NAME_VARIABLE,
            "Symbol is required to have a number value"
        )
    }
    if (delta !is Number) {
        throw InvalidArgException(
            ctx,
            Flx.getSymbol(MacroSpec.SUB_ASSIGN),
            2,
            NAME_VALUE,
            "Value is required to be a number"
        )
    }
    val newValue: Number = if (originalValue is Double || originalValue is Float || delta is Double || delta is Float) {
        originalValue.toDouble() - delta.toDouble()
    } else {
        originalValue.toLong() - delta.toLong()
    }
    symbol.set(ctx, newValue)
    return newValue
}

fun fnUpdate(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val symbol = args[0] as Symbol
    val currentValue = symbol.get(ctx)
    val fnForm = args[1]
    val extraCount = args.size - 2
    val newValue = if (fnForm is MacroSymbol || fnForm is MacroSpec) {
        val macroSpec = if (fnForm is MacroSymbol) fnForm.macroSpec else fnForm as MacroSpec
        val macroArgs = Array(extraCount + 1) { index ->
            if (index == 0) QuotedValue(currentValue) else args[index + 1]
        }
        Evaluator.evalMacro(ctx, macroSpec, macroArgs)
    } else {
        val callArgs = Array(extraCount + 1) { index ->
            if (index == 0) currentValue else args[index + 1].eval(ctx)
        }
        invokeHeadWithArgs(ctx, fnForm, callArgs)
    }
    symbol.set(ctx, newValue)
    return newValue
}

fun fnTryOr(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any =
    try {
        args[0].eval(ctx)
    } catch (signal: ReturnSignal) {
        throw signal // a return, not an error
    } catch (_: FlxException) {
        if (args.size > 1) args[1].eval(ctx) else Null
    } catch (_: RuntimeException) {
        if (args.size > 1) args[1].eval(ctx) else Null
    }

fun fnTry(ctx: Ctx, args: kotlin.Array<Any>, signatureIndex: Int): Any {
    val last = args.lastOrNull()
    val catchClause = (last as? List)?.takeIf {
        it.isNotEmpty() && (it[0] as? Symbol)?.name == "catch"
    }
    val bodyEnd = if (catchClause != null) args.size - 1 else args.size
    return try {
        var result: Any = Null
        for (i in 0 until bodyEnd) {
            result = args[i].eval(ctx)
            if (!ctx.canContinue()) break
        }
        result
    } catch (signal: ReturnSignal) {
        throw signal // a return, not an error
    } catch (e: RuntimeException) {
        if (catchClause == null) return Null
        val catchCtx = ctx.create()
        val handlerStart: Int
        if (catchClause.size() >= 3 && catchClause[1] is Symbol) {
            val errSym = catchClause[1] as Symbol
            val errMsg = e.message ?: e::class.simpleName ?: "Error"
            catchCtx.bindSlots(arrayOf(errSym), arrayOf(errMsg))
            handlerStart = 2
        } else {
            handlerStart = 1
        }
        var result: Any = Null
        for (i in handlerStart until catchClause.size()) {
            result = catchClause[i].eval(catchCtx)
            if (!catchCtx.canContinue()) break
        }
        result
    }
}

// ---------------------------------------------------------------------------------------------------------------------
// Preparation shared by the macros and by their call-site plans (CallPlan), which do it once per site
// instead of on every evaluation.

/** The names and value forms of a `let`'s bindings, checked and split once. */
internal class LetBindings(
    internal val names: kotlin.Array<Symbol>,
    internal val valueForms: kotlin.Array<Any>,
    /** A name bound twice, e.g. `[x 1 x 2]`: bound one after the other so the last one wins, as before. */
    internal val hasRepeatedNames: Boolean,
)

internal fun prepareLetBindings(ctx: Ctx, bindingsForm: Any): LetBindings {
    val bindings = when (bindingsForm) {
        is Array -> bindingsForm
        else -> {
            val argError = ArgError(0, NAME_BINDINGS, CoreType.ARRAY, bindingsForm::class)
            throw InvalidArgException(
                ctx,
                MacroSpec.LET,
                argError,
                "Expression ${bindingsForm.toLiteral()} does not evaluate to an Array"
            )
        }
    }
    val bindingsCount = bindings.size()

    if (bindingsCount == 0 || bindingsCount.isOdd()) {
        val argError = ArgError(0, NAME_BINDINGS, CoreType.ANY, Any::class)
        throw InvalidArgException(ctx, MacroSpec.LET, argError, "Invalid bindings definition")
    }
    val count = bindingsCount / 2
    val names = Array(count) { i ->
        when (val symbol = bindings[i * 2]) {
            is Symbol -> symbol
            else -> {
                val argError = ArgError(0, NAME_BINDINGS, CoreType.SYMBOL, symbol::class)
                throw InvalidArgException(
                    ctx,
                    MacroSpec.LET,
                    argError,
                    "Invalid symbol bindings definition. Expression ${symbol.toLiteral()} is not a Symbol"
                )
            }
        }
    }
    val valueForms = Array(count) { i -> bindings[i * 2 + 1] }
    val hasRepeatedNames = names.map { it.name }.toSet().size != count
    return LetBindings(names, valueForms, hasRepeatedNames)
}

/**
 * `let`: evaluates the binding values in the enclosing scope, binds them in a new scope and
 * evaluates the forms (args[1] onwards) there.
 *
 * The names are *new* bindings that hide outer variables of the same name. They used to be set with
 * [Ctx.set], which walked outward and overwrote an outer variable instead — `(var x 1) (let [x 2] x)`
 * left x at 2, and an outer val made the let fail. They are now held as the scope's parameter slots,
 * like a function's: no map, no assignment object per binding.
 */
internal fun evalLet(ctx: Ctx, bindings: LetBindings, args: kotlin.Array<Any>): Any {
    val names = bindings.names
    val valueForms = bindings.valueForms
    val values = Array(names.size) { i -> valueForms[i].eval(ctx) }
    val letCtx = ctx.create()

    if (bindings.hasRepeatedNames) {
        for (i in names.indices) letCtx.addAssignment(names[i], SymbolAssignment(values[i]))
    } else {
        letCtx.bindSlots(names, values)
    }

    var value: Any = Null
    for (i in 1 until args.size) {
        value = args[i].eval(letCtx)
    }
    return value
}

internal fun prepareLoopBindings(ctx: Ctx, bindingsForm: Any): LetBindings {
    val bindings = bindingsForm as? Array ?: throw InvalidArgException(
        ctx, MacroSpec.LOOP, ArgError(0, NAME_BINDINGS, CoreType.ARRAY, bindingsForm::class),
        "Expression ${bindingsForm.toLiteral()} is not an Array of bindings",
    )
    val bindingsCount = bindings.size()
    if (bindingsCount.isOdd()) {
        throw InvalidArgException(
            ctx, MacroSpec.LOOP, ArgError(0, NAME_BINDINGS, CoreType.ANY, Any::class), "Invalid bindings definition",
        )
    }
    val count = bindingsCount / 2
    val names = Array(count) { i ->
        bindings[i * 2] as? Symbol ?: throw InvalidArgException(
            ctx, MacroSpec.LOOP, ArgError(0, NAME_BINDINGS, CoreType.SYMBOL, bindings[i * 2]::class),
            "Expression ${bindings[i * 2].toLiteral()} is not a Symbol",
        )
    }
    val valueForms = Array(count) { i -> bindings[i * 2 + 1] }
    return LetBindings(names, valueForms, hasRepeatedNames = false)
}

internal fun evalLoop(ctx: Ctx, bindings: LetBindings, args: kotlin.Array<Any>): Any {
    val names = bindings.names
    val valueForms = bindings.valueForms
    var values = if (names.isEmpty()) Ctx.emptyArgs else Array(names.size) { i -> valueForms[i].eval(ctx) }
    var loopCtx = ctx.create()

    while (true) {
        loopCtx.bindSlots(names, values)
        var result: Any = Null
        for (i in 1 until args.size) {
            result = args[i].eval(loopCtx)
        }
        if (result !is RecurValues) return result
        if (result.values.size != names.size) {
            throw InvalidNumberOfArgsException(ctx, MacroSpec.LOOP.symbol, result.values.size, names.size)
        }
        if (ctx.vm?.canContinue() == false) return Null
        values = result.values
        if (!loopCtx.canReuseForRecur()) {
            loopCtx = ctx.create()
        }
    }
}

/** A parameter vector, `[x y]`, as the symbols a function binds; [argIndex] is its position, for errors. */
internal fun prepareParameters(ctx: Ctx, spec: MacroSpec, argIndex: Int, paramsForm: Any): kotlin.Array<Symbol> {
    val paramsArray = paramsForm as Array
    return Array(paramsArray.size()) { i -> getSymbol(ctx, spec, argIndex, NAME_ARGS, paramsArray[i]) }
}

/** A `fun` body, with `(compile "...")` turned into a form parsed on first use. */
internal fun prepareFunctionBody(bodyForm: Any): Any =
    when (bodyForm) {
        is List -> {
            val head = bodyForm.first()
            if (head is FunctionSymbol && head.functionSpec == FunctionSpec.COMPILE) {
                ParsedForm(bodyForm[1].toString())
            } else {
                bodyForm
            }
        }
        else -> bodyForm
    }

/** Evaluates multiple body forms in sequence inside an existing function/method frame. */
class BodyForms(private val forms: kotlin.Array<Any>) : EvaluableObject() {
    override fun eval(ctx: Ctx): Any {
        var result: Any = Null
        for (form in forms) {
            result = form.eval(ctx)
            if (!ctx.canContinue()) break
        }
        return result
    }

    override fun toString(): String = forms.joinToString(" ") { it.toLiteral() }
}

/** Packs one or more body forms starting at [startIndex] into a single evaluable body. */
fun packFunctionBody(args: kotlin.Array<Any>, startIndex: Int): Any {
    val count = args.size - startIndex
    return when {
        count <= 0 -> Null
        count == 1 -> prepareFunctionBody(args[startIndex])
        else -> BodyForms(Array(count) { i -> prepareFunctionBody(args[startIndex + i]) })
    }
}

/** Evaluates one or more body forms of a loop (`while`, `for`, `forEach`, `repeat`), several in a scope of their own. */
internal fun evalLoopBodyForms(ctx: Ctx, args: kotlin.Array<Any>, startIndex: Int): Any {
    if (args.size == startIndex + 1) return args[startIndex].eval(ctx)
    val bodyCtx = ctx.create()
    var result: Any = Null
    for (i in startIndex until args.size) {
        result = args[i].eval(bodyCtx)
        if (!bodyCtx.canContinue()) break
    }
    return result
}

/** `fun`: a named function, a closure over [ctx], bound to [name] there. */
internal fun defineFunction(ctx: Ctx, name: Symbol, parameters: kotlin.Array<Symbol>, body: Any): Symbol {
    ctx.set(name, DefinedFunction(name, parameters, body, ctx))
    return name
}

@Suppress("SameParameterValue")
fun getSymbol(ctx: Ctx, spec: MacroSpec, argIndex: Int, argName: String, arg: Any): Symbol {
    return when (arg) {
        is Symbol -> arg
        else -> when (spec) {
            MacroSpec.CALLBACK -> {
                val description = "Arg at index $argIndex should be a Symbol"
                throw InvalidArgException(
                    ctx,
                    spec,
                    ArgError(argIndex, argName, CoreType.SYMBOL, arg::class),
                    description
                )
            }
            MacroSpec.FUN -> {
                val description = "Args array should contains only Symbols"
                throw InvalidArgException(
                    ctx,
                    spec,
                    ArgError(argIndex, argName, CoreType.SYMBOL, arg::class),
                    description
                )
            }
            MacroSpec.PROC -> {
                val description = "Args array should contains only Symbols"
                throw InvalidArgException(
                    ctx,
                    spec,
                    ArgError(argIndex, argName, CoreType.SYMBOL, arg::class),
                    description
                )
            }
            else -> throw IllegalStateException()
        }
    }
}
