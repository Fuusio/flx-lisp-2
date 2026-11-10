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

import org.fuusio.flx.core.Callable
import org.fuusio.flx.core.EvaluableObject
import org.fuusio.flx.core.error.ObjectException
import org.fuusio.flx.core.vm.Ctx

/** Calls the method [methodName] of the instance its first argument is: `(.name instance args…)`. */
data class MethodCall(val methodName: String) : EvaluableObject(), Callable {

    override fun call(ctx: Ctx, args: Array<Any>): Any {
        val instance = invokee(ctx, ".$methodName", args)
        val method = instance.declaration.getMethod(methodName)
            ?: throw ObjectException(ctx, "Object '${instance.declaration.name}' has no method '$methodName'")
        return method.call(ctx, args)
    }

    override fun toString() = ".$methodName"
}

/** The instance [args] start with, or an error naming [what] was called. */
internal fun invokee(ctx: Ctx, what: String, args: Array<Any>): ObjectInstance =
    args.firstOrNull() as? ObjectInstance ?: throw ObjectException(ctx, "'$what' must be called with an object as its first argument")
