package org.fuusio.flx.core

import org.fuusio.flx.FlxUnitTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SequenceFunctionsTest : FlxUnitTest() {

    @Test
    fun `reduce and fold aggregate sequences with and without initial value`() {
        assertEquals(10L, eval("(reduce + [1 2 3 4])"))
        assertEquals(110L, eval("(reduce + 100 [1 2 3 4])"))
        assertEquals(10L, eval("(.reduce [1 2 3 4] +)"))
        assertEquals(110L, eval("(.reduce [1 2 3 4] 100 +)"))
        assertEquals(24L, eval("(fold * 1 [2 3 4])"))
        assertEquals(24L, eval("(.fold [2 3 4] 1 (fn [acc x] (* acc x)))"))
        assertEquals(Null, eval("(reduce + [])"))
        assertEquals(0L, eval("(reduce + 0L [])"))
    }

    @Test
    fun `find and indexOf search sequences and strings`() {
        assertEquals(4, eval("(.find [1 3 4 6] .isEven)"))
        assertEquals(Null, eval("(.find [1 3 5] .isEven)"))
        assertEquals("b", eval("(get (find [{:id \"a\" :ok false} {:id \"b\" :ok true}] :ok) :id)"))

        assertEquals(2, eval("(.indexOf [\"a\" \"b\" \"c\" \"b\"] \"c\")"))
        assertEquals(1, eval("(indexOf [\"a\" \"b\" \"c\" \"b\"] \"b\")"))
        assertEquals(3, eval("(.indexOf [\"a\" \"b\" \"c\" \"b\"] \"b\" 2)"))
        assertEquals(-1, eval("(.indexOf [1 2 3] 9)"))
        assertEquals(1, eval("(.indexOf [10 20 30] 20L)"))

        assertEquals(2, eval("(.indexOf \"hello\" \"ll\")"))
        assertEquals(3, eval("(.indexOf \"hello\" \"l\" 3L)"))
        assertEquals(1, eval("(.indexOf \"hello\" 'e')"))
    }

    @Test
    fun `any all count and filter accept functions BuiltIn symbols and keywords`() {
        assertEquals(true, eval("(.any [1 3 4] .isEven)"))
        assertEquals(false, eval("(any [1 3 5] .isEven)"))
        assertEquals(true, eval("(all [2 4 6] .isEven)"))
        assertEquals(false, eval("(.all [2 3 6] .isEven)"))

        assertEquals(3L, eval("(.count [1 2 3 4 5 6] .isEven)"))
        assertEquals(6L, eval("(.count [1 2 3 4 5 6])"))
        assertEquals(5L, eval("(count \"hello\")"))

        val todosCode = """
            (val todos [{"text" "A" "done" false} {"text" "B" "done" true} {"text" "C" "done" false}])
            (fun isOpen [todo] (not (get todo "done")))
            [(.count todos isOpen) (.size (.filter todos isOpen))]
        """
        assertEquals(array(2L, 2L), eval(todosCode))

        assertEquals(list(2, 4), eval("(.filter [1 2 3 4] .isEven)"))
        assertEquals(list(2, 4), eval("(filter [1 2 3 4] .isEven)"))
        assertEquals(list("A", "B"), eval("(map :name [{:name \"A\"} {:name \"B\"}])"))
    }

    @Test
    fun `sort and sortBy order sequences naturally or by key selector`() {
        assertEquals(array(1, 2, 3, 4), eval("(.sort [3 1 4 2])"))
        assertEquals(list("a", "b", "c"), eval("(sort (quote (\"c\" \"a\" \"b\")))"))

        val byKeyword = eval("""
            (val items [{:name "Charlie" :score 20} {:name "Alice" :score 30} {:name "Bob" :score 10}])
            (map :name (sortBy :score items))
        """)
        assertEquals(list("Bob", "Charlie", "Alice"), byKeyword)

        val byFn = eval("""
            (val words ["pear" "fig" "apple"])
            (.sortBy words .size)
        """)
        assertEquals(array("fig", "pear", "apple"), byFn)
    }

    @Test
    fun `reverse take drop and dropLast work on sequences and strings`() {
        assertEquals(array(3, 2, 1), eval("(.reverse [1 2 3])"))
        assertEquals(list(3, 2, 1), eval("(reverse (quote (1 2 3)))"))
        assertEquals("cba", eval("(.reverse \"abc\")"))

        assertEquals(array(1, 2), eval("(.take [1 2 3 4] 2)"))
        assertEquals(array(1, 2), eval("(take 2 [1 2 3 4])"))
        assertEquals(list(1, 2), eval("(.take (quote (1 2 3 4)) 2)"))
        assertEquals("he", eval("(.take \"hello\" 2)"))

        assertEquals(array(3, 4), eval("(.drop [1 2 3 4] 2)"))
        assertEquals(array(3, 4), eval("(drop 2 [1 2 3 4])"))
        assertEquals(list(3, 4), eval("(.drop (quote (1 2 3 4)) 2)"))
        assertEquals("llo", eval("(.drop \"hello\" 2)"))

        assertEquals(array(1, 2), eval("(.dropLast [1 2 3 4] 2)"))
        assertEquals(list(1, 2), eval("(dropLast (quote (1 2 3 4)) 2)"))
        assertEquals("hel", eval("(.dropLast \"hello\" 2)"))
    }

    @Test
    fun `distinct and groupBy deduplicate and partition sequences`() {
        assertEquals(array(1, 2, 3), eval("(.distinct [1 2 1 3 2 1])"))
        assertEquals(list("a", "b"), eval("(distinct (quote (\"a\" \"b\" \"a\")))"))

        val grouped = eval("""
            (val items [{"tag" "work" "v" 1} {"tag" "home" "v" 2} {"tag" "work" "v" 3}])
            (val g (groupBy "tag" items))
            [(.size (get g "work")) (.size (get g "home"))]
        """)
        assertEquals(array(2L, 1L), grouped)
    }

    @Test
    fun `range generates numeric arrays for positive and negative steps`() {
        assertEquals(array(0, 1, 2, 3), eval("(range 4)"))
        assertEquals(array(2, 3, 4), eval("(range 2 5)"))
        assertEquals(array(0, 2, 4, 6), eval("(range 0 8 2)"))
        assertEquals(array(5, 4, 3, 2), eval("(range 5 1 -1)"))
        assertEquals(array(), eval("(range 0)"))
        assertEquals(array(), eval("(range 5 2)"))
        assertEquals(array(0L, 1L, 2L), eval("(range 3L)"))
    }

    @Test
    fun `sum average and join aggregate and format sequences`() {
        assertEquals(10L, eval("(.sum [1 2 3 4])"))
        assertEquals(10.5, eval("(sum [1 2.5 3 4])") as Double, 1e-9)
        assertEquals(0L, eval("(.sum [])"))

        assertEquals(2.5, eval("(.average [1 2 3 4])") as Double, 1e-9)
        assertEquals(0.0, eval("(average [])") as Double, 1e-9)

        assertEquals("a, b, c", eval("(.join [\"a\" \"b\" \"c\"] \", \")"))
        assertEquals("1-2-3", eval("(join [1 2 3] \"-\")"))
        assertEquals("abc", eval("(.join [\"a\" \"b\" \"c\"])"))
    }
}
