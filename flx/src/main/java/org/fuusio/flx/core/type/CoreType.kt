/*
 * Copyright (C) 2001 - 2021 Marko Salmela
 *
 * http://fuusio.org
 *
 * All rights reserved.
 */
package org.fuusio.flx.core.type

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.fuusio.flx.core.*
import org.fuusio.flx.core.component.Calculator
import org.fuusio.flx.core.function.FunctionFormSymbol
import org.fuusio.flx.core.function.FunctionObject
import org.fuusio.flx.core.function.FunctionSymbol
import org.fuusio.flx.core.http.HttpCallback
import org.fuusio.flx.core.macro.MacroSymbol
import org.fuusio.flx.core.rx.Observable
import org.fuusio.flx.core.rx.Subscription
import java.util.*
import kotlin.reflect.KClass

@Suppress("unused")
enum class CoreType(private val argType: KClass<*>) : FlxType {

    ANY(Any::class),
    ARRAY(Array::class),
    BOOLEAN(Boolean::class),
    BYTE(Byte::class),
    CALCULATOR(Calculator::class),
    CALLBACK(Callback::class),
    CHAR(Char::class),
    CLASS(Class::class),
    CODE(Code::class),
    COLLECTION(Collection::class),
    COLOR(Color::class),
    DATE(Date::class),
    DOUBLE(Double::class),
    FLOAT(Float::class),
    FUNCTION(FunctionObject::class),
    FUNCTION_SYMBOL(FunctionSymbol::class),
    FUNCTION_FORM_SYMBOL(FunctionFormSymbol::class),
    HTTP_CALLBACK(HttpCallback::class),
    INFO(Info::class),
    INT(Int::class),
    INDEXABLE(Indexable::class),
    JSON_ARRAY(JsonArray::class),
    JSON_ELEMENT(JsonElement::class),
    JSON_OBJECT(JsonObject::class),
    JSON_PRIMITIVE(JsonPrimitive::class),
    KEYWORD(Keyword::class),
    LIST(List::class),
    LONG(Long::class),
    MACRO_SYMBOL(MacroSymbol::class),
    MAP(Map::class),
    NULL(Null::class),
    NUMBER(Number::class),
    OBSERVABLE(Observable::class),
    OPTIONS(Options::class),
    PAIR(Pair::class),
    PARSED_FORM(ParsedForm::class),
    REGEX(Regex::class),
    SEQUENCE(Sequence::class),
    SET(Set::class),
    SHORT(Short::class),
    SIZEABLE(Sizable::class),
    STRING(String::class),
    SUBSCRIPTION(Subscription::class),
    SYMBOL(Symbol::class),
    SYSTEM_SYMBOL(SystemSymbol::class),
    UNIT(Unit::class),

    VARIABLE(Variable::class);

    /**
     * The Java class behind [argType], boxed for primitives (Int → java.lang.Integer). Checked with
     * Class.isInstance, which the JIT handles directly, instead of KClass.isInstance, which goes
     * through Kotlin's class-reference lookups on every call — and this runs for every argument of
     * every built-in call.
     */
    private val javaType: Class<*> = argType.javaObjectType

    override fun getTypeClass() = argType

    // A whole number is an Int when written as a literal and a Long when computed, so an arg
    // declared INT or LONG takes either: (.substring "hello" 1 3) used to be refused, its 1 being an Int.
    override fun isInstance(any: Any): Boolean =
        this === ANY || javaType.isInstance(any) ||
            ((this === LONG || this === INT) && (any is Int || any is Long || any is Short || any is Byte)) ||
            (this === FUNCTION && (any is Callable || any is FunctionSymbol || any is MacroSymbol))

    override fun getTypeName(): String =
        argType.simpleName!!

    companion object {

        fun resolveType(typeName: String): FlxType? {
            entries.forEach { value ->
                if (value.getTypeName() == typeName) return value
            }
            return null
        }
    }
}