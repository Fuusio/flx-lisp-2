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
package org.fuusio.flx.core.`object`

import org.fuusio.flx.core.EvaluableObject
import org.fuusio.flx.core.ValAssignment
import org.fuusio.flx.core.VarAssignment
import org.fuusio.flx.core.error.ObjectException
import org.fuusio.flx.core.toLiteral
import org.fuusio.flx.core.vm.Ctx

/**
 * An instance of an FLX object. Its properties are the `val` and `var` bindings of its own
 * [scope], so a method — which runs in a frame under that scope — reads them as plain symbols, and
 * `=` changes a `var` and refuses a `val` exactly as anywhere else in FLX.
 */
class ObjectInstance(val declaration: ObjectDeclaration, internal val scope: Ctx) : EvaluableObject() {

    fun get(ctx: Ctx, name: String): Any {
        val property = declaration.getProperty(name)
            ?: throw ObjectException(ctx, "Object '${declaration.name}' has no property '$name'")
        return scope.getAssignment(property.name)!!.get()
    }

    fun set(ctx: Ctx, name: String, value: Any): Any {
        val property = declaration.getProperty(name)
            ?: throw ObjectException(ctx, "Object '${declaration.name}' has no property '$name'")
        when (val assignment = scope.getAssignment(property.name)) {
            is VarAssignment -> assignment.set(value)
            is ValAssignment -> throw ObjectException(ctx, "Property '$name' of object '${declaration.name}' is a val and cannot be changed")
            else -> throw ObjectException(ctx, "Object '${declaration.name}' has no property '$name'")
        }
        return value
    }

    override fun toString(): String =
        declaration.properties.joinToString(", ", "${declaration.name}{", "}") { property ->
            "${property.name.name} = ${scope.getAssignment(property.name)?.get()?.toLiteral()}"
        }
}
