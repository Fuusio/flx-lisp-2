package org.fuusio.flx.core.extension

fun Double.isZero() = this > -Double.MIN_VALUE && this < Double.MIN_VALUE

fun Double.hasDecimals() = this.rem(1) != 0.0