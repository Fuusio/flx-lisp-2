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

sealed class Assignment {

    abstract fun get(): Any
}

/**
 * A view of one parameter slot of a call frame, made only when something asks for the parameter's
 * assignment in order to change it (`=`, `++`). Parameters themselves are stored as plain values.
 */
class SlotAssignment(private val values: Array<Any>, private val index: Int) : Assignment() {

    override fun get(): Any = values[index]

    fun set(value: Any): Any {
        values[index] = value
        return value
    }
}

data class SymbolAssignment(var value: Any) : Assignment() {

    override fun get(): Any = value

    fun set(value: Any): Any {
        this.value = value
        return value
    }
}

data class VarAssignment(var value: Any) : Assignment() {

    override fun get(): Any = value

    fun set(value: Any): Any {
        this.value = value
        return value
    }
}

data class ValAssignment(val value: Any) : Assignment() {

    override fun get(): Any = value
}