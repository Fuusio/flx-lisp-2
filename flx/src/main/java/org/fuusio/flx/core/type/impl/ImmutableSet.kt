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

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import org.fuusio.flx.core.vm.Ctx
import org.fuusio.flx.core.EvaluableObject
import org.fuusio.flx.core.Keyword
import org.fuusio.flx.core.error.InvalidJsonArrayElementException
import org.fuusio.flx.core.eval
import org.fuusio.flx.core.toLiteral
import org.fuusio.flx.core.type.Array
import org.fuusio.flx.core.type.Collection
import org.fuusio.flx.core.type.Color
import org.fuusio.flx.core.type.List
import org.fuusio.flx.core.type.Map
import org.fuusio.flx.core.type.Set
import java.lang.StringBuilder
import java.util.*

    /**
     * The contents. They are never changed in place: add, addAll, remove and clear replace them
     * with new contents (kotlinx persistent collections, which share structure with the old ones),
     * so the wrapper keeps its in-place behavior while the contents themselves can be shared freely
     * — by evaluated literals, `rest`, `conj`, and conversions between collection types — with
     * nothing copied. A list or set given to the constructor is taken over as it is and must not be
     * changed afterward.
     */
class ImmutableSet private constructor(
    elements: kotlin.collections.Set<Any>,
    @Suppress("UNUSED_PARAMETER") normalized: Unit,
) : EvaluableObject(), Set {

    /** A set of [elements]. Whole numbers are kept as Longs (see [asKey]), so that 1 is in it once, however it was made. */
    constructor(elements: kotlin.collections.Set<Any> = persistentSetOf()) : this(normalizedElements(elements), Unit)

    // Persistent sets keep insertion order, as mutableSetOf() did.
    internal var elements: kotlin.collections.Set<Any> = elements
        private set

    private val constantCache = ConstantCache()

    private fun persistentElements(): PersistentSet<Any> =
        (elements as? PersistentSet<Any>) ?: elements.toPersistentSet().also { elements = it }

    fun add(element: Any): Boolean {
        val value = element.asKey()
        if (value in elements) return false
        elements = persistentElements().adding(value)
        return true
    }

    fun addAll(collection: Collection) {
        val added = collection.contents() ?: return
        elements = persistentElements().mutate { set -> added.forEach { set.add(it.asKey()) } }
    }

    fun remove(element: Any): Boolean {
        val value = element.asKey()
        if (value !in elements) return false
        elements = persistentElements().removing(value)
        return true
    }

    override fun clear() {
        elements = persistentSetOf()
    }

    override fun conj(tail: Any): Set =
        ImmutableSet(persistentElements().adding(tail.asKey()), Unit)

    override fun cons(head: Any): List {
        val list = ArrayList<Any>(elements.size + 1)
        list.add(head)
        list.addAll(elements)
        return ImmutableList(list)
    }

    override fun toList(): List = ImmutableList(elements.toList())

    override fun isEmpty() = elements.isEmpty()

    override fun isNotEmpty() = elements.isNotEmpty()

    override fun size() = elements.size

    override fun isSet() = true

    override fun isSizable() = true

    override fun eval(ctx: Ctx): Set {
        if (elements.isEmpty()) {
            return ImmutableSet()
        }
        // A new set object each time, as for arrays, sharing the contents of a plain-value literal.
        val contents = elements
        if (isConstantLiteral(contents)) return ImmutableSet(contents, Unit)
        val values = LinkedHashSet<Any>(hashCapacityFor(contents.size))
        contents.forEach { value -> values.add(value.eval(ctx).asKey()) }
        return ImmutableSet(values, Unit)
    }

    private fun isConstantLiteral(contents: kotlin.collections.Set<Any>): Boolean =
        constantCache.get(contents) ?: constantCache.put(contents, contents.all { it.isSelfEvaluatingValue() })

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return when (other) {
            is ImmutableSet -> {
                this.elements == other.elements
            }
            else -> false
        }
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }

    override fun toArray(): Array = ImmutableArray(elements.toList())

    override fun toJson(ctx: Ctx): JsonArray = buildJsonArray {
        elements.forEach { element ->
            val jsonElement: JsonElement = when (element) {
                is String -> JsonPrimitive(element)
                is Boolean -> JsonPrimitive(element)
                is Number -> JsonPrimitive(element)
                is Keyword -> JsonPrimitive(element.toLiteral())
                is Char -> JsonPrimitive(element.toString())
                is Date -> JsonPrimitive(element.toLiteral())
                is Color -> JsonPrimitive(element.toLiteral())
                is List -> JsonPrimitive(element.toLiteral())
                is Array -> element.toJson(ctx)
                is Map -> element.toJson(ctx)
                is Set -> element.toJson(ctx)
                else -> throw InvalidJsonArrayElementException(ctx, element)
            }
            add(jsonElement)
        }
    }

    override fun toString() = toLiteral()

    override fun toLiteral(): String {
        val string = StringBuilder()
        elements.forEach { element ->
            if (string.isNotEmpty()) string.append(' ')
            string.append(element.toLiteral())
        }
        return "#{$string}"
    }

    @Suppress("unused")
    fun toKotlinSet(): kotlin.collections.Set<Any> = elements

    companion object {

        fun create(charArray: CharArray): ImmutableSet {
            val set = mutableSetOf<Any>()
            charArray.forEach { char -> set.add(char) }
            return ImmutableSet(set)
        }

        fun create(elements: kotlin.collections.Set<Any>) = ImmutableSet(elements)
    }
}