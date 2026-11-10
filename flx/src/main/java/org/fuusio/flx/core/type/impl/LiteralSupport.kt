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

import org.fuusio.flx.core.Evaluable
import org.fuusio.flx.core.Keyword
import org.fuusio.flx.core.Null

/**
 * A value that evaluates to itself and is shared as-is: a number, string, character, boolean (none
 * of them Evaluable), a keyword or null.
 *
 * A collection literal made only of these — `[1 2 3]`, `{:a 1 :b "x"}` — evaluates to a copy of
 * itself, so it can be copied in bulk instead of element by element. Nested collections do not
 * count: they are mutable, and each evaluation must give a fresh copy of them too.
 */
internal fun Any.isSelfEvaluatingValue(): Boolean = this !is Evaluable || this is Keyword || this === Null

/**
 * The contents of a collection as a Kotlin collection, for addAll: an array's, list's or set's
 * elements, or a map's values — or null for any other collection, which addAll has always ignored.
 */
internal fun org.fuusio.flx.core.type.Collection.contents(): kotlin.collections.Collection<Any>? =
    when (this) {
        is ImmutableArray -> elements
        is ImmutableList -> elements
        is ImmutableMap -> pairs.values
        is ImmutableSet -> elements
        else -> null
    }

/**
 * A value as a map key or set element. A whole number is always a Long there: a literal `1` is an
 * Int and `(+ 0 1)` a Long, and as keys they were two different ones — `(get {1 "one"} (+ 0 1))`
 * found nothing, and `#{1 (+ 0 1)}` held 1 twice.
 */
internal fun Any.asKey(): Any = when (this) {
    is Int -> toLong()
    is Short -> toLong()
    is Byte -> toLong()
    else -> this
}

/** Whether [a] and [b] are the same value: equal, or the same whole number as an Int and a Long. */
internal fun sameValue(a: Any?, b: Any?): Boolean = a == b || (a is Number && b is Number && a.asKey() == b.asKey())

/** Whether [a] and [b] hold the same values ([sameValue]) in the same order: `[1]` equals `[(+ 0 1)]`. */
internal fun sameValues(a: kotlin.collections.List<Any>, b: kotlin.collections.List<Any>): Boolean {
    if (a === b) return true
    if (a.size != b.size) return false
    val left = a.iterator()
    val right = b.iterator()
    while (left.hasNext()) if (!sameValue(left.next(), right.next())) return false
    return true
}

/** A hash that agrees with [sameValues]: a whole number counts the same as an Int and as a Long. */
internal fun valuesHash(values: Iterable<Any>): Int {
    var hash = 1
    for (value in values) hash = 31 * hash + value.asKey().hashCode()
    return hash
}

private fun Any.isUnnormalizedKey(): Boolean = this is Int || this is Short || this is Byte

/** [pairs] with its keys as [asKey] makes them: [pairs] itself when none needs changing, else a copy in the same order. */
internal fun normalizedKeys(pairs: kotlin.collections.Map<Any, Any>): kotlin.collections.Map<Any, Any> {
    if (pairs.keys.none { it.isUnnormalizedKey() }) return pairs
    val normalized = LinkedHashMap<Any, Any>(hashCapacityFor(pairs.size))
    for ((key, value) in pairs) normalized[key.asKey()] = value
    return normalized
}

/** [elements] as [asKey] makes them: [elements] itself when none needs changing, else a copy in the same order. */
internal fun normalizedElements(elements: kotlin.collections.Set<Any>): kotlin.collections.Set<Any> {
    if (elements.none { it.isUnnormalizedKey() }) return elements
    return elements.mapTo(LinkedHashSet(hashCapacityFor(elements.size))) { it.asKey() }
}

/** Capacity for a hash-based collection that will hold [size] entries without resizing. */
internal fun hashCapacityFor(size: Int): Int = if (size < 3) size + 1 else (size / 0.75f + 1f).toInt()

/**
 * Remembers whether a collection literal is constant (see [isSelfEvaluatingValue]).
 *
 * A collection's contents are never changed in place — a change replaces them with a new object —
 * so the answer holds for as long as the contents are the same object, and one identity check
 * tells whether it can be reused.
 */
internal class ConstantCache {

    // One reference per answer, each written in a single step, so a concurrent reader can never
    // pair one object's contents with another's answer.
    private var constantContents: Any? = null
    private var nonConstantContents: Any? = null

    /** The remembered answer for [contents], or null when it must be worked out. */
    fun get(contents: Any): Boolean? = when {
        constantContents === contents -> true
        nonConstantContents === contents -> false
        else -> null
    }

    fun put(contents: Any, isConstant: Boolean): Boolean {
        if (isConstant) constantContents = contents else nonConstantContents = contents
        return isConstant
    }
}
