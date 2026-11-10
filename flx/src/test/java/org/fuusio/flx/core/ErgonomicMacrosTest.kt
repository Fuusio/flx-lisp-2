package org.fuusio.flx.core

import org.fuusio.flx.Flx
import org.fuusio.flx.FlxUnitTest
import org.fuusio.flx.core.error.ImmutableSymbolException
import org.fuusio.flx.core.type.Map
import org.fuusio.flx.core.type.Sequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ErgonomicMacrosTest : FlxUnitTest() {

    private fun Sequence.toKotlinList(): kotlin.collections.List<Any> =
        (0 until size()).map { this[it] }

    @Test
    fun `thread-first and thread-last macros work with methods functions macros and keywords`() {
        assertEquals(
            "hello-world",
            eval("""(-> "  Hello World  " (.trim) (.toLowerCase) (.replace " " "-"))""")
        )
        assertEquals(
            "hello-world",
            eval("""(-> "  Hello World  " .trim .toLowerCase (.replace " " "-"))""")
        )
        assertEquals(
            14L,
            eval("""(-> 3 (+ 4) (* 2))""")
        )
        assertEquals(
            listOf("Buy milk", "Call Alice"),
            (eval(
                """
                (val items [
                  {"title" "Buy milk" "open" true}
                  {"title" "Pay bills" "open" false}
                  {"title" "Call Alice" "open" true}
                ])
                (fun isOpen [item] (get item "open"))
                (fun getTitle [item] (get item "title"))
                (->> items (filter isOpen) (map getTitle))
                """.trimIndent()
            ) as Sequence).toKotlinList()
        )
        assertEquals(
            "Ada",
            eval("""(-> {:user {:name "Ada"}} :user :name)""")
        )
    }

    @Test
    fun `null-coalescing and conditional binding macros work`() {
        assertEquals("fallback", eval("""(?: nil "fallback" "ignored")"""))
        assertEquals("first", eval("""(coalesce "first" "second")"""))
        assertEquals(false, eval("""(?: nil false "fallback")"""))
        assertEquals(Null, eval("""(?: nil nil)"""))

        // Short-circuiting check: later forms are not evaluated once a non-nil value is found
        assertEquals(
            1L,
            eval("""(var calls 0) (?: (do (= calls (+ calls 1)) "ok") (do (= calls (+ calls 10)) "no")) calls""")
        )

        assertEquals(
            "Found: 42",
            eval("""(if-let [x (get {:a 42} :a)] (str "Found: " x) "Missing")""")
        )
        assertEquals(
            "Missing",
            eval("""(if-let [x (get {:a 42} :b)] (str "Found: " x) "Missing")""")
        )
        assertEquals(
            "Falsy",
            eval("""(if-let [x false] "Truthy" "Falsy")""")
        )
        assertEquals(
            Null,
            eval("""(if-let [x nil] "Truthy")""")
        )

        assertEquals(
            15L,
            eval(
                """
                (var sideEffect 0)
                (when-let [n 5]
                  (= sideEffect (+ sideEffect n))
                  (* sideEffect 3))
                """.trimIndent()
            )
        )
        assertEquals(
            0,
            eval(
                """
                (var sideEffect 0)
                (when-let [n nil]
                  (= sideEffect 99))
                sideEffect
                """.trimIndent()
            )
        )
    }

    @Test
    fun `in-place var mutation macros work`() {
        assertEquals(
            false,
            eval("""(var flag true) (toggle! flag)""")
        )
        assertEquals(
            true,
            eval("""(var flag true) (toggle! flag) (toggle! flag) flag""")
        )

        assertEquals(
            15L,
            eval("""(var sum 10) (+= sum 5) sum""")
        )
        assertEquals(
            6.5,
            eval("""(var sum 10) (-= sum 3.5) sum""")
        )
        assertEquals(
            "hello world",
            eval("""(var s "hello") (+= s " world") s""")
        )

        val todos = eval(
            """
            (var todos ["a" "b"])
            (update! todos conj "c")
            todos
            """.trimIndent()
        ) as Sequence
        assertEquals(listOf("a", "b", "c"), todos.toKotlinList())

        val user = eval(
            """
            (var user {:name "Ada" :score 10})
            (update! user assoc :score 20)
            user
            """.trimIndent()
        ) as Map
        assertEquals(20, user[Flx.getKeyword("score")])

        assertThrows(ImmutableSymbolException::class.java) {
            eval("""(val constant 10) (+= constant 1)""")
        }
    }

    @Test
    fun `try and try-or macros handle runtime errors safely`() {
        assertEquals(
            0,
            eval("""(try-or (/ 10 0) 0)""")
        )
        assertEquals(
            5.0,
            eval("""(try-or (/ 10 2) 0)""")
        )
        assertEquals(
            Null,
            eval("""(try-or (.toInt "not-a-number"))""")
        )
        assertEquals(
            -1,
            eval("""(try-or (.toInt "not-a-number") -1)""")
        )

        val caught = eval(
            """
            (try
              (val x 10)
              (/ x 0)
              (catch err (str "Caught: " err)))
            """.trimIndent()
        ) as String
        assertTrue(caught.startsWith("Caught: "), caught)

        assertEquals(
            30L,
            eval(
                """
                (try
                  (val a 10)
                  (val b 20)
                  (+ a b)
                  (catch err -1))
                """.trimIndent()
            )
        )
    }

    @Test
    fun `implicit do works in fun fn while for forEach and repeat`() {
        // fun with multiple body forms and tail recur
        assertEquals(
            120L,
            eval(
                """
                (fun factorial [n acc]
                  (val done (<= n 1))
                  (if-else done acc (recur (- n 1) (* acc n))))
                (factorial 5 1)
                """.trimIndent()
            )
        )

        // fn with multiple body forms
        assertEquals(
            21L,
            eval(
                """
                (val compute (fn [x]
                  (val doubled (* x 2))
                  (+ doubled 1)))
                (compute 10)
                """.trimIndent()
            )
        )

        // while with multiple body forms
        assertEquals(
            10L,
            eval(
                """
                (var i 0)
                (var total 0)
                (while (< i 4)
                  (+= i 1)
                  (+= total i))
                total
                """.trimIndent()
            )
        )

        // for with multiple body forms
        assertEquals(
            listOf(2L, 4L, 6L),
            (eval(
                """
                (var out [])
                (for i 1 3 1
                  (val doubled (* i 2))
                  (update! out conj doubled))
                out
                """.trimIndent()
            ) as Sequence).toKotlinList()
        )

        // forEach with multiple body forms
        assertEquals(
            listOf("A!", "B!"),
            (eval(
                """
                (var out [])
                (forEach item ["a" "b"]
                  (val upper (.toUpperCase item))
                  (update! out conj (str upper "!")))
                out
                """.trimIndent()
            ) as Sequence).toKotlinList()
        )

        // repeat with multiple body forms
        assertEquals(
            6L,
            eval(
                """
                (var a 0)
                (var b 0)
                (repeat 3
                  (+= a 1)
                  (+= b a))
                b
                """.trimIndent()
            )
        )
    }
}
