package org.fuusio.flx.core.spec

import kotlin.reflect.KClass

interface CompanionObjectPropertySpec {
    val propertyName: String
    val propertyType: KClass<*>
    val isImmutable: Boolean

    fun get(): Any?
    fun set(value: Any?)
}