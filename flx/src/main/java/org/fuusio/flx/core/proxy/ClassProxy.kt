package org.fuusio.flx.core.proxy

import org.fuusio.flx.core.Keywords
import org.fuusio.flx.core.spec.Cardinality
import org.fuusio.flx.core.spec.ConstructorSpec
import org.fuusio.flx.core.spec.ParamSpec
import org.fuusio.flx.core.vm.Ctx
import kotlin.reflect.KClass

abstract class ClassProxy<T: Any>(type: KClass<T>) : InvokeeProxy<T>(type) {

    @Suppress("UNCHECKED_CAST")
    open fun construct(ctx: Ctx, constructorSpec: ConstructorSpec<T>): T = Unit as T

    protected fun p(name: String, type: KClass<*>, cardinality: Cardinality = Cardinality.REQUIRED): ParamSpec =
        ParamSpec(name, type, cardinality)

    companion object{
        val NO_KEYWORDS: Keywords = arrayOf()

        fun keywords(vararg keyword: String): Keywords = arrayOf(*keyword)
    }
}