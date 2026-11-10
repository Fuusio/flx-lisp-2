package org.fuusio.flx.core.spec

interface ConstructorSpec<T: Any> {
    val params: Array<out ParamSpec>

    fun invoke(args: Array<out Any>): T
}