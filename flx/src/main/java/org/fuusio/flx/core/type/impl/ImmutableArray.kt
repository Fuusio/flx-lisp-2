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

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import org.fuusio.flx.core.*
import org.fuusio.flx.core.error.InvalidJsonArrayElementException
import org.fuusio.flx.core.toLiteral
import org.fuusio.flx.core.type.*
import org.fuusio.flx.core.type.Array
import org.fuusio.flx.core.type.Collection
import org.fuusio.flx.core.type.List
import org.fuusio.flx.core.type.Map
import org.fuusio.flx.core.type.Set
import org.fuusio.flx.core.vm.Ctx
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

    /**
     * The contents. They are never changed in place: add, addAll, remove and clear replace them with new contents
     * (kotlinx persistent collections, which share structure with the old ones), so the wrapper keeps
     * its in-place behavior while the contents themselves can be shared freely — by evaluated
     * literals, `rest`, `conj`, and conversions between collection types — with nothing copied.
     * A list or set given to the constructor is taken over as it is and must not be changed afterward.
     */
class ImmutableArray(elements: kotlin.collections.List<Any> = persistentListOf()) : EvaluableObject(), Array {

    internal var elements: kotlin.collections.List<Any> = elements
        private set

    private val constantCache = ConstantCache()

    private fun persistentElements(): PersistentList<Any> =
        (elements as? PersistentList<Any>) ?: elements.toPersistentList().also { elements = it }

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

    override fun conj(tail: Any): Array = ImmutableArray(persistentElements().adding(tail))

    override fun cons(head: Any): List = ImmutableList(persistentElements().addingAt(0, head))

    override fun setAt(index: Int, value: Any): Array = ImmutableArray(persistentElements().replacingAt(index, value))

    override fun removeAt(index: Int): Array = ImmutableArray(persistentElements().removingAt(index))

    override fun insertAt(index: Int, value: Any): Array = ImmutableArray(persistentElements().addingAt(index, value))

    override fun subarray(start: Int, end: Int): Array = ImmutableArray(elements.subList(start, end))

    override fun first() = if (elements.isNotEmpty()) elements.first() else Null

    override fun last() = if (elements.isNotEmpty()) elements.last() else Null

    override fun rest(): Array = if (elements.size < 2) ImmutableArray() else ImmutableArray(elements.subList(1, size()))

    override fun get(index: Int) = if (elements.isNotEmpty()) elements[index] else Null

    override fun isEmpty() = elements.isEmpty()

    override fun isNotEmpty() = elements.isNotEmpty()

    override fun size() = elements.size

    override fun isIndexable() = true

    override fun isArray() = true

    override fun isSequence() = true

    override fun isSizable() = true

    // Always a new array object, so that .add on the result never changes the literal; a literal of
    // plain values (see isSelfEvaluatingValue) shares its contents with it, since neither changes them.
    override fun eval(ctx: Ctx): Array {
        val contents = elements
        if (isConstantLiteral(contents)) return ImmutableArray(contents)
        val size = contents.size
        val values = ArrayList<Any>(size)
        for (i in 0 until size) values.add(contents[i].eval(ctx))
        return ImmutableArray(values)
    }

    private fun isConstantLiteral(contents: kotlin.collections.List<Any>): Boolean =
        constantCache.get(contents) ?: constantCache.put(contents, contents.all { it.isSelfEvaluatingValue() })

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return when (other) {
            is ImmutableArray -> {
                sameValues(this.elements, other.elements)
            }
            else -> false
        }
    }

    override fun hashCode() = valuesHash(elements)

    override fun toString() = toLiteral()

    @Suppress("unused")
    fun toKotlinArray(): kotlin.Array<Any> = Array(elements.size) { index -> elements[index] }

    override fun toLiteral(): String {
        val string = StringBuilder()
        elements.forEach { element ->
            if (string.isNotEmpty()) string.append(' ')
            string.append(element.toLiteral())
        }
        return "[$string]"
    }

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

    override fun toList() = ImmutableList(elements)

    override fun toSet(): Set {
        val set = mutableSetOf<Any>()
        elements.forEach { element -> set.add(element) }
        return ImmutableSet.create(set)
    }

    override fun isArrayOf(type: FlxType): Boolean =
        isArrayOf(type.getTypeClass())

    private fun isArrayOf(elementType: KClass<*>): Boolean {
        if (elements.isEmpty()) return false
        val javaClass = elementType.java
        elements.forEach { element -> if (!javaClass.isAssignableFrom(element::class.java)) return false }
        return true
    }

    override fun isBooleanArray(): Boolean = isArrayOf(Boolean::class)

    override fun isByteArray(): Boolean = isArrayOf(Byte::class)

    override fun isCharArray(): Boolean = isArrayOf(Char::class)

    override fun isDoubleArray(): Boolean = isArrayOf(Double::class)

    override fun isFloatArray(): Boolean = isArrayOf(Float::class)

    override fun isIntArray(): Boolean = isArrayOf(Int::class)

    override fun isLongArray(): Boolean = isArrayOf(Long::class)

    override fun isStringArray(): Boolean = isArrayOf(String::class)

    override fun toBooleanArray() = BooleanArray(elements.size) { index -> elements[index] as Boolean}

    override fun toByteArray() = ByteArray(elements.size) { index -> elements[index] as Byte}

    override fun toCharArray() = CharArray(elements.size) { index -> elements[index] as Char}

    override fun toDoubleArray() = DoubleArray(elements.size) { index -> elements[index] as Double}

    override fun toFloatArray() = FloatArray(elements.size) { index -> elements[index] as Float}

    override fun toIntArray() = IntArray(elements.size) { index -> elements[index] as Int}

    override fun toLongArray() = LongArray(elements.size) { index -> elements[index] as Long}

    override fun toStringArray() = Array(elements.size) { index -> elements[index].toString()}

    companion object {

        fun create(list: ArrayList<Any>) = ImmutableArray(list)

        fun create(list: kotlin.collections.List<Any>) = ImmutableArray(list)

        fun create(string: String): ImmutableArray {
            val elements = mutableListOf<Any>()
            string.forEach { char -> elements.add(char) }
            return ImmutableArray(elements)
        }

        fun create(charArray: CharArray): ImmutableArray {
            val elements = mutableListOf<Any>()
            charArray.forEach { char -> elements.add(char) }
            return ImmutableArray(elements)
        }
    }
}