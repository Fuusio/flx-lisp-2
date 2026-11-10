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
package org.fuusio.flx.core.macro

import org.fuusio.flx.core.vm.Ctx

/**
 * What `recur` evaluates to: the new values for the enclosing `loop`'s bindings or function's
 * parameters. `loop` and user functions check a body's result for it and, instead of returning it,
 * run the body again with these values — a loop in constant stack space, where self-recursion would
 * add JVM frames on every step.
 *
 * `recur` belongs in tail position (the value a `loop` or function body returns, through `if-else`,
 * `when`, `do` and `let`). Used anywhere else it is just a value, and a built-in given it as an
 * argument reports a wrong argument type.
 */
class RecurValues(val values: Array<Any>) {
    override fun toString(): String = "(recur ${values.joinToString(" ")})"

    companion object {
        internal val EMPTY = RecurValues(Ctx.emptyArgs)
    }
}
