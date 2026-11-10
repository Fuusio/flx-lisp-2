package org.fuusio.flx.core.spec

import kotlin.reflect.KClass

interface PropertySpec<T: Any> {
    val propertyName: String
    val propertyType: KClass<*>
    val isImmutable: Boolean
    val isNullable: Boolean

    fun get(invokee: T): Any?
    fun set(invokee: T, value: Any?)
}