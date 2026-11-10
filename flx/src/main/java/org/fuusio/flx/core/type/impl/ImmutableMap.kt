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

import android.os.Bundle
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.fuusio.flx.core.*
import org.fuusio.flx.core.error.InvalidBundleValueException
import org.fuusio.flx.core.error.InvalidJsonKeyException
import org.fuusio.flx.core.error.InvalidJsonValueException
import org.fuusio.flx.core.type.*
import org.fuusio.flx.core.type.Array
import org.fuusio.flx.core.type.List
import org.fuusio.flx.core.type.Map
import org.fuusio.flx.core.type.Set
import org.fuusio.flx.core.vm.Ctx
import java.io.Serializable
import java.lang.StringBuilder
import java.util.*

/**
 * A map. Like the other collections its contents are never changed in place — clear, its only
 * modifying operation, replaces them — so they can be shared. A map given to the constructor is
 * taken over as it is and must not be changed afterward.
 */
class ImmutableMap private constructor(
    pairs: kotlin.collections.Map<Any, Any>,
    @Suppress("UNUSED_PARAMETER") normalized: Unit,
) : EvaluableObject(), Map {

    /**
     * A map of [pairs]. Whole-number keys are kept as Longs (see [asKey]), so that 1 is one key
     * whether it was written as a literal or computed; the operations below look keys up the same way.
     */
    constructor(pairs: kotlin.collections.Map<Any, Any> = persistentMapOf()) : this(normalizedKeys(pairs), Unit)

    internal var pairs: kotlin.collections.Map<Any, Any> = pairs
        private set

    private val constantCache = ConstantCache()

    private fun persistentPairs(): PersistentMap<Any, Any> =
        (pairs as? PersistentMap<Any, Any>) ?: pairs.toPersistentMap().also { pairs = it }

    override fun get(key: Any): Any = get(key, Null)

    override fun get(key: Any, fallback: Any): Any =
        if (pairs.isEmpty()) fallback else pairs[key.asKey()] ?: fallback

    override fun containsKey(key: Any): Boolean = pairs.containsKey(key.asKey())

    override fun put(key: Any, value: Any): Map = ImmutableMap(persistentPairs().putting(key.asKey(), value), Unit)

    fun assoc(args: kotlin.Array<Any>, startIndex: Int = 1): Map {
        val remaining = args.size - startIndex
        if (remaining <= 0) return this
        if (remaining == 2) return put(args[startIndex], args[startIndex + 1])
        return ImmutableMap(persistentPairs().mutate { map ->
            var i = startIndex
            while (i + 1 < args.size) {
                map[args[i].asKey()] = args[i + 1]
                i += 2
            }
        }, Unit)
    }

    override fun removeKey(key: Any): Map =
        if (!pairs.containsKey(key.asKey())) this else ImmutableMap(persistentPairs().removing(key.asKey()), Unit)

    fun dissoc(args: kotlin.Array<Any>, startIndex: Int = 1): Map {
        val remaining = args.size - startIndex
        if (remaining <= 0) return this
        if (remaining == 1) return removeKey(args[startIndex])
        var base = persistentPairs()
        for (i in startIndex until args.size) {
            val key = args[i].asKey()
            if (base.containsKey(key)) base = base.removing(key)
        }
        return if (base === pairs) this else ImmutableMap(base, Unit)
    }

    fun merge(other: Map): Map {
        if (other.isEmpty()) return this
        if (isEmpty() && other is ImmutableMap) return ImmutableMap(other.persistentPairs(), Unit)
        val otherPairs = if (other is ImmutableMap) other.pairs else normalizedKeys(other.keys().associateWith { other[it] })
        return ImmutableMap(persistentPairs().puttingAll(otherPairs), Unit)
    }

    override fun toList(): List {
        val elements = mutableListOf<Any>()
        pairs.keys.forEach { key ->
            elements.add(key)
            elements.add(pairs[key] ?: Null)
        }
        return ImmutableList.create(elements)
    }

    override fun clear() {
        pairs = persistentMapOf()
    }

    override fun isEmpty() = pairs.isEmpty()

    override fun isNotEmpty() = pairs.isNotEmpty()

    override fun size() = pairs.size

    override fun isMap() = true

    override fun eval(ctx: Ctx): ImmutableMap =
        if (isEmpty())
            ImmutableMap()
        else if (isConstantLiteral(pairs)) {
            // A new map each time, made in one bulk copy. It is still a copy, not shared contents,
            // because evaluated maps have always been HashMaps: sharing the literal's
            // insertion-ordered map would change the order in which evaluated maps print and iterate.
            ImmutableMap(HashMap(pairs), Unit)
        } else {
            // Entries rather than keys and a second lookup per key; sized up front.
            val values = HashMap<Any, Any>(hashCapacityFor(pairs.size))
            for ((key, value) in pairs) values[key.eval(ctx).asKey()] = value.eval(ctx)
            ImmutableMap(values, Unit)
        }

    private fun isConstantLiteral(contents: kotlin.collections.Map<Any, Any>): Boolean =
        constantCache.get(contents) ?: constantCache.put(
            contents,
            contents.all { (key, value) -> key.isSelfEvaluatingValue() && value.isSelfEvaluatingValue() },
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return when (other) {
            is ImmutableMap -> {
                // Keys are whole numbers one way already (asKey); values may be either.
                this.pairs.size == other.pairs.size &&
                    this.pairs.all { (key, value) -> other.pairs.containsKey(key) && sameValue(value, other.pairs[key]) }
            }
            else -> false
        }
    }

    override fun hashCode(): Int {
        var hash = 0
        for ((key, value) in pairs) hash += key.hashCode() xor value.asKey().hashCode()
        return hash
    }

    override fun toArray(): Array {
        val elements = mutableListOf<Any>()
        pairs.keys.forEach { key ->
            elements.add(key)
            elements.add(pairs.getValue(key))
        }
        return ImmutableArray.create(elements)
    }

    override fun toBundle(ctx: Ctx): Bundle {
        val bundle = Bundle()

        pairs.keys.forEach { key ->
            val bundleKey = key.toString()
            when (val value = pairs.getValue(key)) {
                is Array -> {
                    when {
                        value.isBooleanArray() -> bundle.putBooleanArray(bundleKey, value.toBooleanArray())
                        value.isByteArray() -> bundle.putByteArray(bundleKey, value.toByteArray())
                        value.isCharArray() -> bundle.putCharArray(bundleKey, value.toCharArray())
                        value.isDoubleArray() -> bundle.putDoubleArray(bundleKey, value.toDoubleArray())
                        value.isFloatArray() -> bundle.putFloatArray(bundleKey, value.toFloatArray())
                        value.isIntArray() -> bundle.putIntArray(bundleKey, value.toIntArray())
                        value.isLongArray() -> bundle.putLongArray(bundleKey, value.toLongArray())
                        else -> bundle.putStringArray(bundleKey, value.toStringArray())
                    }
                }
                is Boolean -> bundle.putBoolean(bundleKey, value)
                is Byte -> bundle.putByte(bundleKey, value)
                is Char -> bundle.putChar(bundleKey, value)
                is Double -> bundle.putDouble(bundleKey, value)
                is Float -> bundle.putFloat(bundleKey, value)
                is Int -> bundle.putInt(bundleKey, value)
                is Long -> bundle.putLong(bundleKey, value)
                is String -> bundle.putString(bundleKey, value)
                is Serializable -> bundle.putSerializable(bundleKey, value)

                else -> throw InvalidBundleValueException(ctx, value)
            }
        }
        return bundle
    }

    override fun keys(): kotlin.collections.Set<Any> = pairs.keys

    override fun values(): kotlin.collections.Collection<Any> = pairs.values

    override fun toJson(ctx: Ctx): JsonObject = buildJsonObject {
        pairs.keys.forEach { key ->
            val propertyKey = when (key) {
                is String -> key
                is Keyword -> key.toLiteral()
                is Char -> key.toLiteral()
                else -> throw InvalidJsonKeyException(ctx, key)
            }

            val propertyValue: JsonElement = when (val value = pairs[key]) {
                is String -> JsonPrimitive(value)
                is Boolean -> JsonPrimitive(value)
                is Number -> JsonPrimitive(value)
                is Keyword -> JsonPrimitive(value.toLiteral())
                is Char -> JsonPrimitive(value.toLiteral())
                is Date -> JsonPrimitive(value.toLiteral())
                is Color -> JsonPrimitive(value.toLiteral())
                is List -> JsonPrimitive(value.toLiteral())
                is Array -> value.toJson(ctx)
                is Map -> value.toJson(ctx)
                is Set -> value.toJson(ctx)
                else -> throw InvalidJsonValueException(ctx, key)
            }
            put(propertyKey, propertyValue)
        }
    }

    override fun toSet(): Set {
        val set = mutableSetOf<Any>()
        pairs.keys.forEach { key ->
            set.add(key)
            set.add(pairs.getValue(key))
        }
        return ImmutableSet.create(set)
    }

    override fun toString() = toLiteral()

    @Suppress("unused")
    override fun toLiteral(): String {
        val string = StringBuilder()
        pairs.forEach { pair ->
            if (string.isNotEmpty()) string.append(' ')
            string.append(pair.key.toLiteral())
            string.append(' ')
            string.append(pair.value.toLiteral())
        }
        return "{$string}"
    }

    @Suppress("unused")
    fun toKotlinMap(): kotlin.collections.Map<Any, Any> = pairs

    companion object {
        fun create(pairs: kotlin.collections.Map<Any, Any>) = ImmutableMap(pairs)
    }
}