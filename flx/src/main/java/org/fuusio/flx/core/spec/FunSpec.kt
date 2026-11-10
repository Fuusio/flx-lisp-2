package org.fuusio.flx.core.spec

import kotlin.reflect.KClass

interface FunSpec<T: Any> {
    val funName: String
    val returnType: KClass<*>
    val params: Array<out ParamSpec>
    val isNullable: Boolean

    fun invoke(invokee: T, args: Array<out Any>): Any?
}