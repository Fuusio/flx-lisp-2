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

import org.fuusio.flx.core.Symbol
import org.fuusio.flx.core.Callable
import org.fuusio.flx.core.EvaluableObject
import org.fuusio.flx.core.ValAssignment
import org.fuusio.flx.core.VarAssignment
import org.fuusio.flx.core.error.ObjectException
import org.fuusio.flx.core.eval
import org.fuusio.flx.core.vm.Ctx

/**
 * `Name.new`: makes an instance of [declaration]. The instance's scope is a child of
 * [definingCtx], where the object was declared, so property values and methods see the program's
 * definitions. The constructor's args are bound to the declaration's parameters as vals, and
 * `this` to the instance, before the properties are initialised, in order — so an initial value
 * may use the parameters, `this` and earlier properties, and a method may call the instance's
 * other methods: `(.area this)`.
 */
class ObjectConstructor(val declaration: ObjectDeclaration, private val definingCtx: Ctx) : EvaluableObject(), Callable {

    init {
        definingCtx.markClosureEscaped()
    }

    override fun call(ctx: Ctx, args: Array<Any>): ObjectInstance {
        val parameters = declaration.parameters
        if (args.size != parameters.size) {
            throw ObjectException(
                ctx,
                if (parameters.isEmpty()) "'${declaration.name}.new' takes no args, not ${args.size}"
                else "'${declaration.name}.new' takes ${parameters.size} ${if (parameters.size == 1) "arg" else "args"} (${parameters.joinToString(" ") { it.name }}), not ${args.size}",
            )
        }
        val instance = ObjectInstance(declaration, definingCtx.createFrame(ctx))
        parameters.forEachIndexed { index, parameter -> instance.scope.addAssignment(parameter, ValAssignment(args[index])) }
        instance.scope.addAssignment(THIS_SYMBOL, ValAssignment(instance))
        declaration.properties.forEach { property ->
            val value = property.initialValue.eval(instance.scope)
            instance.scope.addAssignment(property.name, if (property.isMutable) VarAssignment(value) else ValAssignment(value))
        }
        return instance
    }

    override fun toString() = "${declaration.name}.new"

    private companion object {
        val THIS_SYMBOL = Symbol(ObjectDeclaration.THIS)
    }
}
