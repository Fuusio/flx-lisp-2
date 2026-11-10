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
package org.fuusio.flx.core

import org.fuusio.flx.FlxUnitTest
import org.fuusio.flx.core.type.impl.ImmutableArray
import org.fuusio.flx.core.type.impl.ImmutableList
import org.fuusio.flx.core.type.impl.ImmutableMap
import org.fuusio.flx.core.type.impl.ImmutableSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Collections share their contents (conj, rest, conversions, evaluated literals), while .add,
 * .remove and .clear still change the collection they are called on — and only that one.
 */
class CollectionSharingTest : FlxUnitTest() {

    @Nested
    inner class InPlace {

        @Test
        fun `add, remove and clear change the collection itself`() {
            val list = ImmutableList.create(1L, 2L)
            assertTrue(list.add(3L))
            assertEquals(ImmutableList.create(1L, 2L, 3L), list)
            assertTrue(list.remove(1L))
            assertFalse(list.remove(99L))
            assertEquals(ImmutableList.create(2L, 3L), list)
            list.clear()
            assertTrue(list.isEmpty())
        }

        @Test
        fun `set add reports whether the element was new`() {
            val set = set(1L) as ImmutableSet
            assertTrue(set.add(2L))
            assertFalse(set.add(2L))
            assertEquals(set(1L, 2L), set)
        }

        @Test
        fun `addAll takes a map's values`() {
            val array = array(1L) as ImmutableArray
            array.addAll(map("k", 2L))
            assertEquals(array(1L, 2L), array)
        }
    }

    @Nested
    inner class Sharing {

        @Test
        fun `conj leaves the original alone, and the original's changes leave the result alone`() {
            val list = ImmutableList.create(1L, 2L)
            val longer = list.conj(3L) as ImmutableList
            list.add(9L)
            assertEquals(ImmutableList.create(1L, 2L, 3L), longer)
            longer.add(4L)
            assertEquals(ImmutableList.create(1L, 2L, 9L), list)
        }

        @Test
        fun `conj in a loop builds the whole list`() {
            var list: org.fuusio.flx.core.type.List = ImmutableList()
            for (i in 1..1000) list = list.conj(i.toLong())
            assertEquals(1000, list.size())
            assertEquals(1L, list.first())
            assertEquals(1000L, list.last())
        }

        @Test
        fun `cons puts the element first`() {
            assertEquals(ImmutableList.create(0L, 1L, 2L), ImmutableList.create(1L, 2L).cons(0L))
        }

        @Test
        fun `rest is unaffected by later changes to the list`() {
            val list = ImmutableList.create(1L, 2L, 3L)
            val rest = list.rest()
            list.add(4L)
            list.remove(2L)
            assertEquals(ImmutableList.create(2L, 3L), rest)
        }

        @Test
        fun `array converted to a list is independent of it`() {
            val array = array(1L, 2L) as ImmutableArray
            val list = array.toList() as ImmutableList
            array.add(3L)
            list.add(4L)
            assertEquals(array(1L, 2L, 3L), array)
            assertEquals(ImmutableList.create(1L, 2L, 4L), list)
        }

        @Test
        fun `set conj leaves the original alone`() {
            val set = set(1L) as ImmutableSet
            val bigger = set.conj(2L)
            assertEquals(set(1L), set)
            assertEquals(set(1L, 2L), bigger)
        }

        @Test
        fun `cleared map no longer holds its pairs, and a copy made earlier still does`() {
            val ctx = ctx()
            val map = map("k", 1L) as ImmutableMap
            val evaluated = map.eval(ctx)
            map.clear()
            assertTrue(map.isEmpty())
            assertEquals(map("k", 1L), evaluated)
        }

        @Test
        fun `script modifying an array literal in a loop starts fresh each time`() {
            val result = eval("""
                (fun f [] (do (val a [1L 2L]) (.add a 3L) a))
                (f)
                (f)
            """)
            assertEquals(array(1L, 2L, 3L), result)
        }
    }
}
