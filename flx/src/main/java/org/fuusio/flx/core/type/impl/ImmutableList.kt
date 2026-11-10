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
package org.fuusio.flx.core.type.impl

import org.fuusio.flx.core.ReturnSignal
import org.fuusio.flx.core.debug.Debugging
import org.fuusio.flx.core.error.FormTrace
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.fuusio.flx.core.*
import org.fuusio.flx.core.function.FunctionObject
import org.fuusio.flx.core.macro.MacroSpec
import org.fuusio.flx.core.macro.MacroSymbol
import org.fuusio.flx.core.type.List
import org.fuusio.flx.core.toLiteral
import org.fuusio.flx.core.type.Collection
import org.fuusio.flx.core.type.Set
import org.fuusio.flx.core.vm.Ctx
import java.lang.StringBuilder

    /**
     * The contents. They are never changed in place: add, addAll, remove and clear replace them with new contents
     * (kotlinx persistent collections, which share structure with the old ones), so the wrapper keeps
     * its in-place behavior while the contents themselves can be shared freely — by evaluated
     * literals, `rest`, `conj`, and conversions between collection types — with nothing copied.
     * A list or set given to the constructor is taken over as it is and must not be changed afterward.
     */
class ImmutableList(elements: kotlin.collections.List<Any> = persistentListOf()) : EvaluableObject(), List {

    internal var elements: kotlin.collections.List<Any> = elements
        private set

    /**
     * How this list is called when evaluated as a form, cached from its head (see [CallPlan]), and
     * the contents it was made for: a change to the list replaces [elements], and the plan with it.
     */
    private var callPlan: CallPlan? = null
    private var callPlanElements: kotlin.collections.List<Any>? = null

    private fun persistentElements(): PersistentList<Any> =
        (elements as? PersistentList<Any>) ?: elements.toPersistentList().also { persistent ->
            if (callPlanElements === elements) callPlanElements = persistent
            elements = persistent
        }

    fun add(element: Any): Boolean {
        elements = persistentElements().adding(element)
        return true
    }

    fun addAll(collection: Collection) {
        val added = collection.contents() ?: return
        elements = persistentElements().addingAll(added)
    }

    fun remove(element: Any): Boolean {
        val index = elements.indexOf(element)
        if (index < 0) return false
        elements = persistentElements().removingAt(index)
        return true
    }

    override fun clear() {
        elements = persistentListOf()
    }

    // conj shares the existing contents (amortized constant time once they are persistent); cons
    // still copies, as inserting at the front of a vector does.
    override fun conj(tail: Any): List = ImmutableList(persistentElements().adding(tail))

    override fun cons(head: Any): List = ImmutableList(persistentElements().addingAt(0, head))

    override fun setAt(index: Int, value: Any): List = ImmutableList(persistentElements().replacingAt(index, value))

    override fun removeAt(index: Int): List = ImmutableList(persistentElements().removingAt(index))

    override fun insertAt(index: Int, value: Any): List = ImmutableList(persistentElements().addingAt(index, value))

    override fun filter(ctx: Ctx, predicate: FunctionObject): List {
        val filteredElements = mutableListOf<Any>()
        elements.forEach { element -> if (predicate.callWithOwnedArgs(ctx, arrayOf(element)).toBoolean()) filteredElements.add(element) }
        return create(filteredElements)
    }

    override fun first() = if (elements.isNotEmpty()) elements.first() else Null

    override fun last() = if (elements.isNotEmpty()) elements.last() else Null

    override fun rest(): List = if (elements.size < 2) ImmutableList() else ImmutableList(elements.subList(1, size()))

    override fun get(index: Int) = if (elements.isNotEmpty()) elements[index] else Null

    override fun toList(): List = this

    override fun isEmpty() = elements.isEmpty()

    override fun isNotEmpty() = elements.isNotEmpty()

    override fun size() = elements.size

    override fun isIndexable() = true

    override fun isList() = true

    override fun isSequence() = true

    override fun isSizable() = true

    override fun eval(ctx: Ctx): Any {
        var plan = callPlan
        if (plan == null || callPlanElements !== elements) {
            // Plan first, then the contents it belongs to: a racing reader that sees the new
            // contents also sees the plan made for them.
            plan = CallPlan.of(this)
            callPlan = plan
            callPlanElements = elements
        }
        // One volatile read when not debugging.
        Debugging.debugger?.beforeEval(ctx, this)
        return try {
            plan.eval(ctx, this)
        } catch (signal: ReturnSignal) {
            throw signal // a return on its way out of its function, not a failure
        } catch (e: RuntimeException) {
            // The innermost failing form, for the editor's error marker (see FormTrace).
            FormTrace.record(e, this)
            throw e
        }
    }

    override fun toString() = toLiteral()

    override fun toLiteral(): String {
        if (size() > 0) {
            val head = elements[0]
            if (head is MacroSymbol && head.macroSpec == MacroSpec.QUOTE) return "'${rest()}"
        }
        val string = StringBuilder()
        elements.forEach { element ->
            if (string.isNotEmpty()) string.append(' ')
            string.append(element.toLiteral())
        }
        return "($string)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return when (other) {
            is ImmutableList -> {
               sameValues(this.elements, other.elements)
            }
            else -> false
        }
    }

    override fun hashCode() = valuesHash(elements)

    override fun toArray(): org.fuusio.flx.core.type.Array {
        return ImmutableArray.create(elements)
    }

    override fun toSet(): Set {
        val set = mutableSetOf<Any>()
        elements.forEach { element -> set.add(element) }
        return ImmutableSet.create(set)
    }

    @Suppress("unused")
    fun toKotlinList(): kotlin.collections.List<Any> = elements

    companion object {

        fun create(list: ArrayList<Any>) = ImmutableList(list)

        fun create(list: kotlin.collections.List<Any>) = ImmutableList(list)

        fun create(string: String): ImmutableList {
            val elements = mutableListOf<Any>()
            string.forEach { char -> elements.add(char) }
            return ImmutableList(elements)
        }

        fun create(charArray: CharArray): ImmutableList {
            val elements = mutableListOf<Any>()
            charArray.forEach { char -> elements.add(char) }
            return ImmutableList(elements)
        }

        fun create(vararg elements: Any) = ImmutableList(elements.toList())
    }
}