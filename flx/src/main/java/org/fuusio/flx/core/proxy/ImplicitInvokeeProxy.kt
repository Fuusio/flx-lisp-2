package org.fuusio.flx.core.proxy

import kotlin.reflect.KClass

abstract class ImplicitInvokeeProxy<T: Any>(type: KClass<T>) : InvokeeProxy<T>(type)