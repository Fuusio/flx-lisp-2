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
package org.fuusio.flx.core.vm

import org.fuusio.flx.Program
import org.fuusio.flx.core.*
import org.fuusio.flx.core.error.FlxException
import org.fuusio.flx.core.error.ImmutableSymbolException
import org.fuusio.flx.core.type.Color

open class Ctx(
    open var vm: FlxVM? = null,
    open val parent: Ctx? = null
) {
    /**
     * The rarely used per-scope state, kept out of the scope object itself: most scopes are call
     * frames and `let`/`do`/`loop` scopes that never set any of it, and every field saved there is
     * saved on each of them. Created on the first write.
     */
    private class Extras {
        var args: Array<Any> = emptyArgs
        var invokee: Any = Unit
        var program: Program? = null
    }

    private var extras: Extras? = null

    private fun extras(): Extras = extras ?: Extras().also { extras = it }

    var args: Array<Any>
        get() = extras?.args ?: emptyArgs
        set(value) {
            extras().args = value
        }

    var invokee: Any
        get() = extras?.invokee ?: Unit
        set(value) {
            extras().invokee = value
        }

    /**
     * The program this scope belongs to: set on the program's own context and found from any scope
     * inside it by walking outward. Scopes used to copy it into a field of their own on creation;
     * it is only ever read from the VM's context, so the copies were never used.
     */
    var program: Program
        get() {
            var scope: Ctx? = this
            while (scope != null) {
                scope.extras?.program?.let { return it }
                scope = scope.parent
            }
            throw UninitializedPropertyAccessException("No program has been set for this context")
        }
        set(value) {
            extras().program = value
        }

    private var isReturned = false
    private var returnValue: Any = Unit

    /**
     * This scope's own bindings, keyed by symbol name. Created on the first binding: most scopes
     * (`do`, `while`, `if` bodies, zero-parameter calls) never bind anything, and a small initial
     * capacity suits the rest — a call frame typically holds only its parameters.
     */
    private var assignments: HashMap<String, Assignment>? = null

    /**
     * A call frame's parameters: names and values side by side, bound by [bindSlots]. They take the
     * place of one map entry and one [SymbolAssignment] per parameter per call, and a function has
     * few enough parameters that comparing names beats hashing.
     */
    private var slotNames: Array<Symbol>? = null
    private var slotValues: Array<Any>? = null

    /**
     * True once a closure has captured this scope or a child of it. When false and no local
     * [assignments] have been added, a `loop` or tail-recursive function can reuse this scope
     * across `recur` passes instead of allocating a fresh [Ctx] per pass.
     */
    private var hasEscapedClosure = false

    internal fun markClosureEscaped() {
        var scope: Ctx? = this
        while (scope != null && !scope.hasEscapedClosure) {
            scope.hasEscapedClosure = true
            scope = scope.parent
        }
    }

    internal fun canReuseForRecur(): Boolean =
        !hasEscapedClosure && assignments == null && !isReturned

    /**
     * True for a subclass that resolves some symbols itself by overriding [get] (FLX objects
     * used to; none does now). The iterative lookup in [get] hands over to such a scope instead of reading
     * its bindings directly, so the override still takes effect when it sits higher in the chain.
     */
    protected open val overridesLookup: Boolean
        get() = false

    /**
     * This scope's own bindings, names and values, for a debugger's variables: its parameters
     * and what was bound in it (vals, vars, functions). Not those of the scopes around it.
     */
    fun ownBindings(): List<Pair<String, Any>> = buildList {
        slotNames?.let { names -> val values = slotValues; names.forEachIndexed { i, name -> values?.getOrNull(i)?.let { add(name.name to it) } } }
        assignments?.forEach { (name, assignment) -> add(name to assignment.get()) }
    }

    open fun create(): Ctx = Ctx(vm = vm, parent = this)

    open fun create(vm: FlxVM): Ctx = Ctx(vm = vm, parent = this)

    /**
     * A call frame for a function or callback defined in this scope and now called from [caller].
     *
     * The frame's parent is this — the *defining* scope — so the body sees the variables that were
     * visible where it was written (lexical scoping, i.e. closures), and a lookup walks only as
     * many scopes as the source is nested, however deep the call stack is. The VM that decides
     * whether execution may continue comes from the caller.
     */
    fun createFrame(caller: Ctx): Ctx = Ctx(vm = caller.vm ?: vm, parent = this)

    /**
     * Binds a call frame's parameters. [values] becomes the frame's storage and is written to when a
     * parameter is reassigned, so it must belong to this call (see [org.fuusio.flx.core.Callable.callWithOwnedArgs]).
     */
    fun bindSlots(names: Array<Symbol>, values: Array<Any>) {
        if (names.isEmpty()) return
        slotNames = names
        slotValues = values
    }

    private fun slotIndex(symbol: Symbol): Int {
        val names = slotNames ?: return -1
        val name = symbol.name
        for (i in names.indices) {
            val s = names[i]
            if (s === symbol || s.name == name) return i
        }
        return -1
    }

    open fun isReturned(): Boolean = isReturned

    open fun setReturned(isReturned: Boolean) {
        this.isReturned = isReturned
    }

    open fun getReturnValue(): Any = returnValue

    open fun setReturnValue(returnValue: Any) {
        this.returnValue = returnValue
    }

    open fun resetReturnState() {
        this.isReturned = false
        this.returnValue = Unit
    }

    // Both lookups walk the scope chain in a loop rather than recursing through each parent, which
    // keeps a deep chain off the JVM stack and costs one hash lookup per scope that has bindings.
    open fun get(symbol: Symbol): Any {
        val name = symbol.name
        var scope: Ctx? = this
        while (scope != null) {
            if (scope !== this && scope.overridesLookup) return scope.get(symbol)
            val slot = scope.slotIndex(symbol)
            if (slot >= 0) return scope.slotValues!![slot]
            val assignment = scope.assignments?.get(name)
            if (assignment != null) return assignment.get()
            scope = scope.parent
        }
        return Null
    }

    internal fun getAssignment(symbol: Symbol): Assignment? {
        val name = symbol.name
        var scope: Ctx? = this
        while (scope != null) {
            val slot = scope.slotIndex(symbol)
            if (slot >= 0) return SlotAssignment(scope.slotValues!!, slot)
            val assignment = scope.assignments?.get(name)
            if (assignment != null) return assignment
            scope = scope.parent
        }
        return null
    }

    open fun set(symbol: Symbol, value: Any, isArgValue: Boolean = false) {
        if (isArgValue) {
            addAssignment(symbol, SymbolAssignment(value))
        } else {
            when (val assignment = getAssignment(symbol)) {
                is VarAssignment -> assignment.set(value)
                is SymbolAssignment -> assignment.set(value)
                is SlotAssignment -> assignment.set(value)
                is ValAssignment -> throw ImmutableSymbolException(this, symbol)
                else -> addAssignment(symbol, SymbolAssignment(value))
            }
        }
    }

    /** Whether [symbol] has a binding in this scope or an enclosing one, whatever its value. */
    fun isBound(symbol: Symbol): Boolean {
        val name = symbol.name
        var scope: Ctx? = this
        while (scope != null) {
            if (scope.slotIndex(symbol) >= 0 || scope.assignments?.containsKey(name) == true) return true
            scope = scope.parent
        }
        return false
    }

    fun hasAssignment(symbol: Symbol): Boolean =
        slotIndex(symbol) >= 0 || assignments?.containsKey(symbol.name) == true

    fun getFlxVm(): FlxVM = vm ?: parent!!.getFlxVm()

    fun canContinue(): Boolean = vm!!.canContinue()

    fun reset() {
        assignments?.clear()
        parent?.reset()
    }

    open fun onException(exception: FlxException) {
        parent!!.onException(exception)
    }

    fun addAssignment(symbol: Symbol, assignment: Assignment) {
        // A parameter keeps living in its slot; a map entry beside it would never be read.
        val slot = slotIndex(symbol)
        if (slot >= 0) {
            slotValues!![slot] = assignment.get()
            return
        }
        val bindings = assignments ?: HashMap<String, Assignment>(INITIAL_BINDINGS_CAPACITY).also { assignments = it }
        bindings[symbol.name] = assignment
    }

    companion object {
        val emptyArgs = emptyArray<Any>()

        /** Room for a few bindings before the first resize; most scopes hold one to three. */
        private const val INITIAL_BINDINGS_CAPACITY = 4
    }
}
