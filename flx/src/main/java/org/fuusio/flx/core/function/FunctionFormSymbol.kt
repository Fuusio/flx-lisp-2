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
package org.fuusio.flx.core.function

import org.fuusio.flx.core.Null
import org.fuusio.flx.core.Symbol
import org.fuusio.flx.core.SystemSymbol
import org.fuusio.flx.core.vm.Ctx

/**
 * The symbol of a built-in function or macro.
 *
 * Used as a value — `(map inc xs)` — it evaluates to its built-in, unless a variable of the same
 * name is in scope. Built-ins used to be bound in the shared root context for this, some 330
 * bindings made at start-up; now the symbol answers for itself when no binding is found.
 */
abstract class FunctionFormSymbol(name: String) : Symbol(name), SystemSymbol {

    /**
     * Set once any scope binds this name — `(fun sum …)`, a parameter called `min`. Calls of the
     * built-in then check whether the name is bound where they run, and call the binding instead;
     * until then they skip that check.
     */
    @JvmField
    @Volatile
    var isShadowed = false

    override fun get(ctx: Ctx): Any {
        val value = ctx.get(this)
        // Null is also what a variable explicitly set to null holds; only an unbound name falls back.
        return if (value === Null && !ctx.isBound(this)) getSpec() else value
    }

    override fun eval(ctx: Ctx): Any = get(ctx)
}