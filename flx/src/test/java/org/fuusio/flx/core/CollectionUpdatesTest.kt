package org.fuusio.flx.core

import org.fuusio.flx.Flx
import org.fuusio.flx.FlxUnitTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CollectionUpdatesTest : FlxUnitTest() {

    private fun kw(name: String) = Flx.getKeyword(name)

    @Nested
    inner class MapUpdates {

        @Test
        fun `assoc and put return a new map and leave the original unchanged`() {
            assertEquals(
                map("a", 1L, "b", 20L, "c", 30L),
                eval("""
                    (val m {"a" 1L "b" 2L})
                    (val m2 (.put m "b" 20L))
                    (val m3 (assoc m2 "c" 30L))
                    m3
                """),
            )
            assertEquals(
                map("a", 1L, "b", 2L),
                eval("""
                    (val m {"a" 1L "b" 2L})
                    (.put m "b" 99L)
                    m
                """),
            )
        }

        @Test
        fun `assoc accepts multiple key value pairs`() {
            assertEquals(
                map(kw("a"), 10L, kw("b"), 20L, kw("c"), 30L),
                eval("(assoc {:a 1L} :a 10L :b 20L :c 30L)"),
            )
        }

        @Test
        fun `dissoc and removeKey remove one or more keys without mutating the original`() {
            assertEquals(
                map("a", 1L),
                eval("""
                    (val m {"a" 1L "b" 2L "c" 3L})
                    (dissoc m "b" "c" "missing")
                """),
            )
            assertEquals(
                map("a", 1L, "c", 3L),
                eval("(.removeKey {\"a\" 1L \"b\" 2L \"c\" 3L} \"b\")"),
            )
        }

        @Test
        fun `merge combines maps from left to right`() {
            assertEquals(
                map(kw("theme"), "dark", kw("fontSize"), 16L, kw("compact"), true),
                eval("(merge {:theme \"light\" :fontSize 14L} {:theme \"dark\"} {:fontSize 16L :compact true})"),
            )
        }

        @Test
        fun `keys and vals work on maps and JsonObjects`() {
            assertEquals(array("a"), eval("(keys {\"a\" 10L})"))
            assertEquals(array("a"), eval("(.keys {\"a\" 10L})"))
            assertEquals(array(10L), eval("(vals {\"a\" 10L})"))
            assertEquals(array(10L), eval("(.values {\"a\" 10L})"))

            assertEquals(array("x"), eval("(keys (.toJson \"{\\\"x\\\": 42}\"))"))
            assertEquals(array(42L), eval("(.values (.toJson \"{\\\"x\\\": 42}\"))"))
        }

        @Test
        fun `containsKey detects keys even when their value is false or nil`() {
            assertTrue(eval("(containsKey {\"done\" false \"note\" nil} \"done\")") as Boolean)
            assertTrue(eval("(.containsKey {\"done\" false \"note\" nil} \"note\")") as Boolean)
            assertFalse(eval("(.containsKey {\"done\" false} \"other\")") as Boolean)

            assertTrue(eval("(.containsKey (.toJson \"{\\\"done\\\": false}\") \"done\")") as Boolean)
            assertFalse(eval("(containsKey (.toJson \"{\\\"done\\\": false}\") \"missing\")") as Boolean)
        }
    }

    @Nested
    inner class SequenceUpdates {

        @Test
        fun `assocAt and setAt replace an element in an array or list`() {
            assertEquals(
                array(10L, 99L, 30L),
                eval("""
                    (val a [10L 20L 30L])
                    (val b (.setAt a 1 99L))
                    b
                """),
            )
            assertEquals(
                array(10L, 20L, 30L),
                eval("""
                    (val a [10L 20L 30L])
                    (assocAt a 1 99L)
                    a
                """),
            )
            assertEquals(
                list(10L, 99L, 30L),
                eval("(assocAt (10L 20L 30L) 1 99L)"),
            )
        }

        @Test
        fun `removeAt removes the element at index in an array or list`() {
            assertEquals(array("a", "c"), eval("(.removeAt [\"a\" \"b\" \"c\"] 1)"))
            assertEquals(array("b", "c"), eval("(removeAt [\"a\" \"b\" \"c\"] 0)"))
            assertEquals(list(1L, 3L), eval("(.removeAt (1L 2L 3L) 1)"))
        }

        @Test
        fun `insertAt inserts an element at index in an array or list`() {
            assertEquals(array(1L, 2L, 3L), eval("(.insertAt [1L 3L] 1 2L)"))
            assertEquals(array(0L, 1L, 2L), eval("(insertAt [1L 2L] 0 0L)"))
            assertEquals(array(1L, 2L, 3L), eval("(insertAt [1L 2L] 2 3L)"))
            assertEquals(list(1L, 2L, 3L), eval("(.insertAt (1L 3L) 1 2L)"))
        }
    }

    @Nested
    inner class NestedUpdates {

        @Test
        fun `assocIn updates nested maps and arrays by slash path or key sequence`() {
            assertEquals(
                map("todos", array(map("text", "Milk", "done", false), map("text", "Bread", "done", true))),
                eval("""
                    (val data {"todos" [{"text" "Milk" "done" false} {"text" "Bread" "done" false}]})
                    (assocIn data "todos/1/done" true)
                """),
            )
            assertEquals(
                map(kw("user"), map(kw("scores"), array(10L, 25L))),
                eval("(assocIn {:user {:scores [10L 20L]}} [:user :scores 1] 25L)"),
            )
        }

        @Test
        fun `assocIn creates missing intermediate maps`() {
            assertEquals(
                map("a", map("b", map("c", 42L))),
                eval("(assocIn {} \"a/b/c\" 42L)"),
            )
        }

        @Test
        fun `updateIn transforms nested values with a function`() {
            assertEquals(
                map("a", array(map("count", 11L))),
                eval("""
                    (val data {"a" [{"count" 10L}]})
                    (updateIn data "a/0/count" .inc)
                """),
            )
            assertEquals(
                map(kw("items"), array(10L, 25L, 30L)),
                eval("(updateIn {:items [10L 20L 30L]} [:items 1] (fn [x] (+ x 5L)))"),
            )
        }
    }
}
