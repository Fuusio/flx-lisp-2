/*
 * Copyright (C) 2016 - 2026 Marko Salmela
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

/**
 * Evaluation nested deeper than the thread's stack allows — typically unbounded or very deep
 * recursion. Raised in place of the StackOverflowError, which would otherwise end the thread.
 * `loop`/`recur`, or running the VM with a larger stack (FlxVM.evalWithLargeStack), avoids it.
 */
class StackDepthExceededException(ctx: Ctx, private val functionName: String) : FlxException(ctx) {

    override val message: String
        get() = "Evaluation nested too deeply in '$functionName'. Use loop/recur for long iterations, or run with a larger stack."

    companion object {
        /** Does nothing; calling it initializes the class (see DefinedFunction's companion). */
        fun preload() = Unit
    }
}
