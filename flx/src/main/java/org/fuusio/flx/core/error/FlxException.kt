/*
 * Copyright (C) 2016 - 2021 Marko Salmela
 *
 * http://fuusio.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fuusio.flx.core.error

import org.fuusio.flx.core.vm.Ctx

abstract class FlxException(val ctx: Ctx) : IllegalStateException()

/**
 * The innermost form whose evaluation an exception came out of, so that the editor can show a
 * runtime error on the exact form rather than on its top-level line. Recorded once per exception,
 * by the first (innermost) form it passes; FLX sessions run one at a time.
 */
object FormTrace {
    @Volatile private var throwable: Throwable? = null
    @Volatile private var form: Any? = null

    fun record(error: Throwable, failed: Any) {
        if (throwable === error) return
        throwable = error
        form = failed
    }

    /** The form [error] came out of, or null when it did not come out of a form. */
    fun formOf(error: Throwable): Any? = if (throwable === error) form else null

    fun clear() {
        throwable = null
        form = null
    }
}