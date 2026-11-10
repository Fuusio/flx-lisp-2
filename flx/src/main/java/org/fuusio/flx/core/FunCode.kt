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

import java.lang.StringBuilder

open class FunCode(private val forms: Array<Any>) : Code(forms) {

    override fun toString(): String {
        val string = StringBuilder()
        forms.forEach { form ->
            if (string.isNotEmpty()) string.append(' ')
            string.append(form.toLiteral())
        }
        return "[funCode: $string]"
    }
}