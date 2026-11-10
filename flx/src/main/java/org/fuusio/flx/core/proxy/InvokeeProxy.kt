package org.fuusio.flx.core.proxy

import org.fuusio.flx.core.Null
import org.fuusio.flx.core.spec.*
import org.fuusio.flx.core.vm.Ctx
import java.util.*
import kotlin.reflect.KClass

abstract class InvokeeProxy<T: Any>(val type: KClass<*>) {

    @Suppress("UNCHECKED_CAST")
    open fun get(ctx: Ctx, propertySpec: PropertySpec<T>): Any =
        propertySpec.get(ctx.invokee as T) ?: Null

    open fun get(ctx: Ctx, propertySpec: CompanionObjectPropertySpec): Any =
        propertySpec.get() ?: Null

    @Suppress("UNCHECKED_CAST")
    open fun set(ctx: Ctx, propertySpec: PropertySpec<T>, value: Any) {
        propertySpec.set(ctx.invokee as T, if (value is Null) null else value)
    }

    open fun set(ctx: Ctx, propertySpec: CompanionObjectPropertySpec, value: Any) {
        propertySpec.set(if (value is Null) null else value)
    }

    @Suppress("UNCHECKED_CAST")
    fun invoke(ctx: Ctx, funSpec: FunSpec<T>): Any =
        funSpec.invoke(ctx.invokee as T, ctx.args) ?: Null


    fun invoke(ctx: Ctx, funSpec: CompanionObjectFunSpec): Any {
        return funSpec.invoke(ctx.args) ?: Null
    }

    protected fun error(): Null { // TODO
        return Null
    }

    open fun getConstructorSpec(specName: String): ConstructorSpec<out Any>? = null

    open fun getFunSpec(specName: String): FunSpec<out Any>? = null

    open fun getCompanionObjectFunSpec(specName: String): CompanionObjectFunSpec? = null

    open fun getPropertySpec(specName: String): PropertySpec<out Any>? = null

    open fun getCompanionObjectPropertySpec(specName: String): CompanionObjectPropertySpec? = null
    companion object {

        fun any(name: String, cardinality: Cardinality = Cardinality.REQUIRED) =
            ParamSpec(name, Any::class, cardinality)

        fun boolean(name: String, cardinality: Cardinality = Cardinality.REQUIRED) =
            ParamSpec(name, Boolean::class, cardinality)

        fun byte(name: String, cardinality: Cardinality = Cardinality.REQUIRED) =
            ParamSpec(name, Byte::class, cardinality)

        fun char(name: String, cardinality: Cardinality = Cardinality.REQUIRED) =
            ParamSpec(name, Char::class, cardinality)

        fun date(name: String, cardinality: Cardinality = Cardinality.REQUIRED) =
            ParamSpec(name, Date::class, cardinality)

        fun double(name: String, cardinality: Cardinality = Cardinality.REQUIRED) =
            ParamSpec(name, Double::class, cardinality)

        fun float(name: String, cardinality: Cardinality = Cardinality.REQUIRED) =
            ParamSpec(name, Float::class, cardinality)

        fun int(name: String, cardinality: Cardinality = Cardinality.REQUIRED) =
            ParamSpec(name, Int::class, cardinality)

        fun long(name: String, cardinality: Cardinality = Cardinality.REQUIRED) =
            ParamSpec(name, Long::class, cardinality)

        fun number(name: String, cardinality: Cardinality = Cardinality.REQUIRED) =
            ParamSpec(name, Number::class, cardinality)

        fun short(name: String, cardinality: Cardinality = Cardinality.REQUIRED) =
            ParamSpec(name, Short::class, cardinality)

        fun string(name: String, cardinality: Cardinality = Cardinality.REQUIRED) =
            ParamSpec(name, String::class, cardinality)

        fun toBoolean(value: Any): Boolean = value as Boolean

        fun toByte(value: Any): Byte = (value as Number).toByte()

        fun toDouble(value: Any): Double = (value as Number).toDouble()

        fun toFloat(value: Any): Float = (value as Number).toFloat()

        fun toInt(value: Any): Int = (value as Number).toInt()

        fun toLong(value: Any): Long = (value as Number).toLong()

        fun toShort(value: Any): Short = (value as Number).toShort()
    }
}