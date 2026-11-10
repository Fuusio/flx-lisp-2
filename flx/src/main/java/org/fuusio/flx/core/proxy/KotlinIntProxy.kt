package org.fuusio.flx.core.proxy

import org.fuusio.flx.core.spec.FunSpec
import org.fuusio.flx.core.spec.ParamSpec
import org.fuusio.flx.core.vm.Ctx
import kotlin.reflect.KClass

object KotlinIntProxy : ClassProxy<Int>(Int::class) {

    enum class Fun(
        override val funName: String,
        override val returnType: KClass<*>,
        override val isNullable: Boolean,
        override vararg val params: ParamSpec,
    ) : FunSpec<Int> {
        AND("and", Int::class, false, p("other", Int::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.and(toInt(args[0]))
            }
        },
        COMPARE_TO("compareTo", Int::class, false, p("other", Byte::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.compareTo(toByte(args[0]))
            }
        },
        COMPARE_TO_2("compareTo", Int::class, false, p("other", Double::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.compareTo(toDouble(args[0]))
            }
        },
        COMPARE_TO_3("compareTo", Int::class, false, p("other", Float::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.compareTo(toFloat(args[0]))
            }
        },
        COMPARE_TO_4("compareTo", Int::class, false, p("other", Int::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.compareTo(toInt(args[0]))
            }
        },
        COMPARE_TO_5("compareTo", Int::class, false, p("other", Long::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.compareTo(toLong(args[0]))
            }
        },
        COMPARE_TO_6("compareTo", Int::class, false, p("other", Short::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.compareTo(toShort(args[0]))
            }
        },
        DEC("dec", Int::class, false) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.dec()
            }
        },
        DIV("div", Int::class, false, p("other", Byte::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.div(toByte(args[0]))
            }
        },
        DIV_2("div", Double::class, false, p("other", Double::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.div(toDouble(args[0]))
            }
        },
        DIV_3("div", Float::class, false, p("other", Float::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.div(toFloat(args[0]))
            }
        },
        DIV_4("div", Int::class, false, p("other", Int::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.div(toInt(args[0]))
            }
        },
        DIV_5("div", Long::class, false, p("other", Long::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.div(toLong(args[0]))
            }
        },
        DIV_6("div", Int::class, false, p("other", Short::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.div(toShort(args[0]))
            }
        },
        INC("inc", Int::class, false) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.inc()
            }
        },
        INV("inv", Int::class, false) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.inv()
            }
        },
        MINUS("minus", Int::class, false, p("other", Byte::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.minus(toByte(args[0]))
            }
        },
        MINUS_2("minus", Double::class, false, p("other", Double::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.minus(toDouble(args[0]))
            }
        },
        MINUS_3("minus", Float::class, false, p("other", Float::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.minus(toFloat(args[0]))
            }
        },
        MINUS_4("minus", Int::class, false, p("other", Int::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.minus(toInt(args[0]))
            }
        },
        MINUS_5("minus", Long::class, false, p("other", Long::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.minus(toLong(args[0]))
            }
        },
        MINUS_6("minus", Int::class, false, p("other", Short::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.minus(toShort(args[0]))
            }
        },
        OR("or", Int::class, false, p("other", Int::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.or(toInt(args[0]))
            }
        },
        PLUS("plus", Int::class, false, p("other", Byte::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.plus(toByte(args[0]))
            }
        },
        PLUS_2("plus", Double::class, false, p("other", Double::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.plus(toDouble(args[0]))
            }
        },
        PLUS_3("plus", Float::class, false, p("other", Float::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.plus(toFloat(args[0]))
            }
        },
        PLUS_4("plus", Int::class, false, p("other", Int::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.plus(toInt(args[0]))
            }
        },
        PLUS_5("plus", Long::class, false, p("other", Long::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.plus(toLong(args[0]))
            }
        },
        PLUS_6("plus", Int::class, false, p("other", Short::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.plus(toShort(args[0]))
            }
        },
        RANGE_TO("rangeTo", IntRange::class, false, p("other", Byte::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.rangeTo(toByte(args[0]))
            }
        },
        RANGE_TO_2("rangeTo", IntRange::class, false, p("other", Int::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.rangeTo(toInt(args[0]))
            }
        },
        RANGE_TO_3("rangeTo", LongRange::class, false, p("other", Long::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.rangeTo(toLong(args[0]))
            }
        },
        RANGE_TO_4("rangeTo", IntRange::class, false, p("other", Short::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.rangeTo(toShort(args[0]))
            }
        },
        REM("rem", Int::class, false, p("other", Byte::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.rem(toByte(args[0]))
            }
        },
        REM_2("rem", Double::class, false, p("other", Double::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.rem(toDouble(args[0]))
            }
        },
        REM_3("rem", Float::class, false, p("other", Float::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.rem(toFloat(args[0]))
            }
        },
        REM_4("rem", Int::class, false, p("other", Int::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.rem(toInt(args[0]))
            }
        },
        REM_5("rem", Long::class, false, p("other", Long::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.rem(toLong(args[0]))
            }
        },
        REM_6("rem", Int::class, false, p("other", Short::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.rem(toShort(args[0]))
            }
        },
        SHL("shl", Int::class, false, p("bitCount", Int::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.shl(toInt(args[0]))
            }
        },
        SHR("shr", Int::class, false, p("bitCount", Int::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.shr(toInt(args[0]))
            }
        },
        TIMES("times", Int::class, false, p("other", Byte::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.times(toByte(args[0]))
            }
        },
        TIMES_2("times", Double::class, false, p("other", Double::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.times(toDouble(args[0]))
            }
        },
        TIMES_3("times", Float::class, false, p("other", Float::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.times(toFloat(args[0]))
            }
        },
        TIMES_4("times", Int::class, false, p("other", Int::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.times(toInt(args[0]))
            }
        },
        TIMES_5("times", Long::class, false, p("other", Long::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.times(toLong(args[0]))
            }
        },
        TIMES_6("times", Int::class, false, p("other", Short::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.times(toShort(args[0]))
            }
        },
        TO_BYTE("toByte", Byte::class, false) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.toByte()
            }
        },
        TO_CHAR("toChar", Char::class, false) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.toChar()
            }
        },
        TO_DOUBLE("toDouble", Double::class, false) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.toDouble()
            }
        },
        TO_FLOAT("toFloat", Float::class, false) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.toFloat()
            }
        },
        TO_INT("toInt", Int::class, false) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.toInt()
            }
        },
        TO_LONG("toLong", Long::class, false) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.toLong()
            }
        },
        TO_SHORT("toShort", Short::class, false) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.toShort()
            }
        },
        UNARY_MINUS("unaryMinus", Int::class, false) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.unaryMinus()
            }
        },
        UNARY_PLUS("unaryPlus", Int::class, false) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.unaryPlus()
            }
        },
        USHR("ushr", Int::class, false, p("bitCount", Int::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.ushr(toInt(args[0]))
            }
        },
        XOR("xor", Int::class, false, p("other", Int::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.xor(toInt(args[0]))
            }
        },
        EQUALS("equals", Boolean::class, false, p("other", Any::class)) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.equals(args[0])
            }
        },
        HASH_CODE("hashCode", Int::class, false) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.hashCode()
            }
        },
        TO_STRING("toString", String::class, false) {
            override fun invoke(invokee: Int, args: Array<out Any>): Any {
                return invokee.toString()
            }
        },
    }

    override fun getFunSpec(specName: String): FunSpec<Int> {
        return Fun.valueOf(specName)
    }
}