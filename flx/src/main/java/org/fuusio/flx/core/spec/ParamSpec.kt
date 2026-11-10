package org.fuusio.flx.core.spec

import kotlin.reflect.KClass

data class ParamSpec(
    val name: String,
    val type: KClass<*>,
    val cardinality: Cardinality = Cardinality.REQUIRED,
    val isNullable: Boolean = false,
)