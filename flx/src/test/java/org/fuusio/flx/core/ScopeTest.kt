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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested

/**
 * Functions are closures over the scope they are defined in (lexical scoping), not over the scope
 * they happen to be called from.
 */
class ScopeTest : FlxUnitTest() {

    @Nested
    inner class Test {

        @org.junit.jupiter.api.Test
        fun `fn sees the let binding around its definition`() {
            assertEquals(15L, eval("(val add (let [n 10] (fn [x] (+ x n)))) (add 5)"))
        }

        @org.junit.jupiter.api.Test
        fun `fn updates a captured variable`() {
            assertEquals(2L, eval("(val counter (let [c 0] (fn [] (++ c)))) (counter) (counter)"))
        }

        @org.junit.jupiter.api.Test
        fun `function body does not see the caller's local bindings`() {
            assertEquals(Null, eval("(fun f [] y) (let [y 1] (f))"))
        }

        @org.junit.jupiter.api.Test
        fun `parameter shadows a global of the same name`() {
            assertEquals(3L, eval("(val x 100) (fun f [x] (+ x 1)) (f 2)"))
        }

        @org.junit.jupiter.api.Test
        fun `parameter can be reassigned`() {
            assertEquals(2L, eval("(fun f [x] (do (= x (+ x 1)) x)) (f 1)"))
        }

        @org.junit.jupiter.api.Test
        fun `parameter can be increased`() {
            assertEquals(3L, eval("(fun g [n] (do (++ n) (++ n))) (g 1)"))
        }

        @org.junit.jupiter.api.Test
        fun `reassigning a parameter does not change the caller's variable`() {
            assertEquals(1L, eval("(var a 1L) (fun h [x] (= x 99)) (h a) a"))
        }

        @org.junit.jupiter.api.Test
        fun `recursive function finds itself`() {
            assertEquals(120L, eval("(fun fact [n] (if-else (< n 2) 1 (* n (fact (- n 1))))) (fact 5)"))
        }
    }
}
