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
import org.fuusio.flx.core.Null
import org.fuusio.flx.core.Symbol
import org.fuusio.flx.core.type.UID
import org.fuusio.flx.core.vm.Ctx

/**
 * An FLX object type: its properties and methods, built by the FLX Visual Editor from an object
 * model. A program binds it with `(val Name.new <declaration>)`; evaluating the declaration yields
 * the [ObjectConstructor] that `(Name.new)` calls, closed over the scope it was bound in.
 *
 * An object is FLX data — scopes and bindings — so no class is loaded and nothing is reflected.
 */
class ObjectDeclaration(
    val name: String,
    val uid: UID,
    val properties: List<PropertyDeclaration>,
    val methods: List<ObjectMethod>,
    /** The constructor's parameters: `(Name.new a b)` binds them as vals of the instance. */
    val parameters: List<Symbol> = emptyList(),
) : EvaluableObject() {

    private val propertiesByName: Map<String, PropertyDeclaration> = properties.associateBy { it.name.name }
    private val methodsByName: Map<String, ObjectMethod> = methods.associateBy { it.name }

    fun getProperty(name: String): PropertyDeclaration? = propertiesByName[name]

    fun getMethod(name: String): ObjectMethod? = methodsByName[name]

    override fun eval(ctx: Ctx): Any = ObjectConstructor(this, ctx)

    override fun toString() = "object $name"

    companion object {
        /** In a method or a property's initial value, `this` is the instance. */
        const val THIS = "this"
    }
}

/** A `val` or `var` of an object; [initialValue] is a form, evaluated for each new instance. */
class PropertyDeclaration(val name: Symbol, val isMutable: Boolean, val initialValue: Any = Null)
