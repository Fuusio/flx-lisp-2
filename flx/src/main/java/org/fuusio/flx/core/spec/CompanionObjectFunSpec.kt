package org.fuusio.flx.core.spec

import kotlin.reflect.KClass

interface CompanionObjectFunSpec {
    val funName: String
    val returnType: KClass<*>
    val isNullable: Boolean
    val params: Array<out ParamSpec>

    fun invoke(args: Array<out Any>): Any?
}