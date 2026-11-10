/*
 * Copyright (C) 2016 - 2026 Marko Salmela
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
package org.fuusio.flx.core.type.impl

import org.fuusio.flx.Flx
import org.fuusio.flx.core.Callable
import org.fuusio.flx.core.Evaluable
import org.fuusio.flx.core.Evaluator
import org.fuusio.flx.core.Null
import org.fuusio.flx.core.Symbol
import org.fuusio.flx.core.error.InvalidArgTypeException
import org.fuusio.flx.core.error.JavaErrors
import org.fuusio.flx.core.error.NotCallableException
import org.fuusio.flx.core.error.UninitializedSymbolAccessException
import org.fuusio.flx.core.isFalse
import org.fuusio.flx.core.isTrue
import org.fuusio.flx.core.FunctionFormSpec
import org.fuusio.flx.core.function.DefinedFunction
import org.fuusio.flx.core.function.FunctionSpec
import org.fuusio.flx.core.function.FunctionSymbol
import org.fuusio.flx.core.macro.LetBindings
import org.fuusio.flx.core.macro.MacroSpec
import org.fuusio.flx.core.macro.MacroSymbol
import org.fuusio.flx.core.macro.defineFunction
import org.fuusio.flx.core.macro.evalLet
import org.fuusio.flx.core.macro.evalLoop
import org.fuusio.flx.core.macro.packFunctionBody
import org.fuusio.flx.core.macro.prepareLetBindings
import org.fuusio.flx.core.macro.prepareLoopBindings
import org.fuusio.flx.core.macro.prepareParameters
import org.fuusio.flx.core.type.List
import org.fuusio.flx.core.type.SignatureSpec
import org.fuusio.flx.core.vm.Ctx

/**
 * How a list form is called, worked out once from its head and then reused on every evaluation.
 *
 * Evaluating `(f a b)` used to run a type check over the head each time; a form in a loop body or a
 * recursive function repeated that on every pass. The head of a parsed form does not change, so
 * [ImmutableList] asks [of] once and caches the plan, dropping it if the list is modified.
 *
 * Macros still receive the raw forms, and everything that has to be looked up at run time — the
 * value of a symbol in head position — still is: only the dispatch decision is cached.
 */
internal sealed class CallPlan {

    abstract fun eval(ctx: Ctx, form: ImmutableList): Any

    /** `()` and a form whose head is not callable (a literal list such as `(1 2 3)`) evaluate to themselves. */
    object SelfEvaluating : CallPlan() {
        override fun eval(ctx: Ctx, form: ImmutableList): Any = form
    }

    /**
     * A built-in function: its arguments are evaluated straight into the array it receives, and
     * only the signatures whose arity matches [argForms] are checked. When the first matching
     * signature is guaranteed to accept the arguments' types, it is called directly with no
     * per-call signature check.
     */
    class Builtin(private val spec: FunctionFormSpec, private val argForms: Array<Any>) : CallPlan() {

        private class Candidate(val spec: FunctionFormSpec, val signature: SignatureSpec)

        private class Plan(
            val unconditional: Candidate?,
            val candidates: Array<Candidate>,
        )

        private var plan: Plan? = null

        override fun eval(ctx: Ctx, form: ImmutableList): Any {
            val resolved = plan ?: resolve().also { plan = it }
            val args = evalArgs(ctx, argForms)
            resolved.unconditional?.let { target -> return call(ctx, target, args) }
            for (candidate in resolved.candidates) {
                if (candidate.signature.checkArgTypes(args)) return call(ctx, candidate, args)
            }
            return Evaluator.callSpec(ctx, spec, args)
        }

        /** The call itself; a Java error out of the built-in becomes an FLX one (see [JavaErrors]). */
        private fun call(ctx: Ctx, target: Candidate, args: Array<Any>): Any =
            try {
                target.spec.function(ctx, args, target.signature.index)
            } catch (error: Throwable) {
                throw JavaErrors.ofBuiltin(ctx, target.spec, args, error) ?: error
            }

        private fun resolve(): Plan {
            val matching = mutableListOf<Candidate>()
            for (sig in spec.signatureSpecs) {
                if (sig.checkArgsCount(argForms)) matching.add(Candidate(spec, sig))
            }
            for (overload in Flx.getOverloads(spec)) {
                if (overload === spec) continue
                for (sig in overload.signatureSpecs) {
                    if (sig.checkArgsCount(argForms)) matching.add(Candidate(overload, sig))
                }
            }
            val unconditional = matching.firstOrNull()?.takeIf { it.signature.alwaysMatchesTypes(argForms) }
            return Plan(unconditional, matching.toTypedArray())
        }
    }

    /**
     * `(and a b …)` and `(or a b …)`: the arguments are evaluated one by one, and only until the
     * answer is known — `(and (isNotEmpty xs) (> (first xs) 0))` does not look into an empty `xs`,
     * and `(or done (fetch))` does not fetch when done. The answer is true or false, as from the
     * functions themselves, which is what `and` and `or` still are as values (`(reduce and flags)`).
     */
    class ShortCircuit(private val isAnd: Boolean, private val argForms: Array<Any>) : CallPlan() {
        override fun eval(ctx: Ctx, form: ImmutableList): Any {
            for (argForm in argForms) {
                val value = if (argForm is Evaluable) argForm.eval(ctx) else argForm
                if (isAnd) {
                    if (value.isFalse()) return false
                } else if (value.isTrue()) return true
            }
            return isAnd
        }
    }

    /**
     * A macro, which receives its argument forms unevaluated. They are the same on every call, so the
     * array is built once; macros and signature checks only read it, re-entrant calls included.
     *
     * For the same reason the signature check always has the same outcome here: which macro and
     * signature fit is worked out on the first call — in the same order as [Evaluator.callSpec] —
     * and later calls go straight to it. (Built-in *function* calls are checked every time; their
     * argument values differ from call to call.)
     */
    class Macro(private val spec: FunctionFormSpec, private val argForms: Array<Any>) : CallPlan() {

        private class Resolved(val spec: FunctionFormSpec, val signature: SignatureSpec)

        // One reference, written once it is complete, so a racing reader never pairs one macro
        // with another's signature.
        private var resolved: Resolved? = null

        override fun eval(ctx: Ctx, form: ImmutableList): Any {
            val target = resolved ?: resolve(ctx).also { resolved = it }
            return target.spec.function(ctx, argForms, target.signature.index)
        }

        private fun resolve(ctx: Ctx): Resolved {
            spec.checkArgs(argForms)?.let { return Resolved(spec, it) }
            for (overload in Flx.getOverloads(spec)) {
                if (overload === spec) continue
                overload.checkArgs(argForms)?.let { return Resolved(overload, it) }
            }
            throw InvalidArgTypeException(ctx, spec, spec.getArgError(ctx, argForms))
        }
    }

    // `let`, `fn` and `fun` with their static parts — binding names, parameter vectors, a `fun`
    // body — checked and prepared on the first evaluation, as the generic Macro plan does for the
    // signature, instead of on every one. A failed preparation is not kept: it fails again next time.

    /** `(let [bindings] form*)`, its bindings split into names and value forms once. */
    class Let(private val argForms: Array<Any>) : CallPlan() {

        private var bindings: LetBindings? = null

        override fun eval(ctx: Ctx, form: ImmutableList): Any {
            val prepared = bindings ?: prepare(ctx).also { bindings = it }
            return evalLet(ctx, prepared, argForms)
        }

        private fun prepare(ctx: Ctx): LetBindings {
            checkSignature(ctx, MacroSpec.LET, argForms)
            return prepareLetBindings(ctx, argForms[0])
        }
    }

    /** `(loop [bindings] form*)`, its bindings split into names and value forms once. */
    class Loop(private val argForms: Array<Any>) : CallPlan() {

        private var bindings: LetBindings? = null

        override fun eval(ctx: Ctx, form: ImmutableList): Any {
            val prepared = bindings ?: prepare(ctx).also { bindings = it }
            return evalLoop(ctx, prepared, argForms)
        }

        private fun prepare(ctx: Ctx): LetBindings {
            checkSignature(ctx, MacroSpec.LOOP, argForms)
            return prepareLoopBindings(ctx, argForms[0])
        }
    }

    /** `(fn params body*)`: each evaluation makes a new closure, sharing one parameter array and packed body. */
    class Fn(private val argForms: Array<Any>) : CallPlan() {

        private class Prepared(val parameters: Array<Symbol>, val body: Any)

        private var prepared: Prepared? = null

        override fun eval(ctx: Ctx, form: ImmutableList): Any {
            val definition = prepared ?: prepare(ctx).also { prepared = it }
            return DefinedFunction(null, definition.parameters, definition.body, ctx)
        }

        private fun prepare(ctx: Ctx): Prepared {
            checkSignature(ctx, MacroSpec.FN, argForms)
            return Prepared(
                prepareParameters(ctx, MacroSpec.FUN, 0, argForms[0]),
                packFunctionBody(argForms, 1),
            )
        }
    }

    /** `(fun name params body*)`, its parameters and body prepared once. */
    class Fun(private val argForms: Array<Any>) : CallPlan() {

        private class Prepared(val name: Symbol, val parameters: Array<Symbol>, val body: Any)

        private var prepared: Prepared? = null

        override fun eval(ctx: Ctx, form: ImmutableList): Any {
            val definition = prepared ?: prepare(ctx).also { prepared = it }
            return defineFunction(ctx, definition.name, definition.parameters, definition.body)
        }

        private fun prepare(ctx: Ctx): Prepared {
            checkSignature(ctx, MacroSpec.FUN, argForms)
            return Prepared(
                argForms[0] as Symbol,
                prepareParameters(ctx, MacroSpec.FUN, 1, argForms[1]),
                packFunctionBody(argForms, 2),
            )
        }
    }

    /** A callable value written directly in head position, such as a built-in's spec object. */
    class Direct(private val callable: Callable, private val argForms: Array<Any>) : CallPlan() {
        override fun eval(ctx: Ctx, form: ImmutableList): Any = callable.callWithOwnedArgs(ctx, evalArgs(ctx, argForms))
    }

    /** A symbol naming a user function or callback: its value is looked up on every call. */
    class Named(private val head: Symbol, private val argForms: Array<Any>) : CallPlan() {
        override fun eval(ctx: Ctx, form: ImmutableList): Any =
            when (val value = head.get(ctx)) {
                is Callable -> value.callWithOwnedArgs(ctx, evalArgs(ctx, argForms))
                is Null -> throw UninitializedSymbolAccessException(ctx, head)
                else -> throw NotCallableException(ctx, value)
            }
    }

    /** A form in head position, such as `((fn [x| x) 1)`: evaluated first, then called. */
    class Computed(private val head: List, private val argForms: Array<Any>) : CallPlan() {
        // A copy: Evaluator.eval may evaluate the forms in place in the array it is given.
        override fun eval(ctx: Ctx, form: ImmutableList): Any = Evaluator.eval(ctx, head.eval(ctx), argForms.copyOf())
    }

    companion object {

        /**
         * The arguments' values, in an array made for this call. The form array is read directly —
         * no List access per argument — and a value that is not Evaluable (a number, string,
         * boolean) is taken as it is, without the generic eval call.
         */
        private fun evalArgs(ctx: Ctx, argForms: Array<Any>): Array<Any> =
            if (argForms.isEmpty()) Ctx.emptyArgs
            else Array(argForms.size) { i ->
                val form = argForms[i]
                if (form is Evaluable) form.eval(ctx) else form
            }

        private fun checkSignature(ctx: Ctx, spec: MacroSpec, argForms: Array<Any>) {
            if (spec.checkArgs(argForms) == null) {
                throw InvalidArgTypeException(ctx, spec, spec.getArgError(ctx, argForms))
            }
        }

        /** The plan for [form]; the cases and their order are those of the former per-call `when`. */
        fun of(form: ImmutableList): CallPlan {
            val elements = form.elements
            if (elements.isEmpty()) return SelfEvaluating
            val head = elements[0]
            val argForms = if (elements.size == 1) Ctx.emptyArgs else Array(elements.size - 1) { i -> elements[i + 1] }
            return when (head) {
                is FunctionSymbol -> when (head.functionSpec) {
                    FunctionSpec.AND -> ShortCircuit(isAnd = true, argForms)
                    FunctionSpec.OR -> ShortCircuit(isAnd = false, argForms)
                    else -> Builtin(head.functionSpec, argForms)
                }
                is MacroSymbol -> when (head.macroSpec) {
                    MacroSpec.LET -> Let(argForms)
                    MacroSpec.LOOP -> Loop(argForms)
                    MacroSpec.FN -> Fn(argForms)
                    MacroSpec.FUN -> Fun(argForms)
                    else -> Macro(head.macroSpec, argForms)
                }
                is Callable -> Direct(head, argForms)
                is Symbol -> Named(head, argForms)
                is List -> Computed(head, argForms)
                else -> SelfEvaluating
            }
        }
    }
}
