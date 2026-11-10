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

import org.fuusio.flx.core.vm.Ctx

/**
 * A name with a value of its own, `e` and `pi`: the value unless the program has bound the name.
 * The name is not reserved — `(catch e …)`, a parameter `e` or `(val e 5)` are that binding, where
 * `e` used to be Euler's number whatever the program said.
 */
class ConstantSymbol(name: String, private val value: Any) : Symbol(name) {

    override fun get(ctx: Ctx): Any = if (ctx.isBound(this)) ctx.get(this) else value

    override fun eval(ctx: Ctx): Any = if (ctx.isBound(this)) ctx.get(this) else value
}