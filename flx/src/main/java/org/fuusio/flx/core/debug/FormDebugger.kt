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
package org.fuusio.flx.core.debug

import org.fuusio.flx.core.vm.Ctx

/**
 * Hears each call form just before it is evaluated, with the scope it is evaluated in: what a
 * debugger needs to stop at a breakpoint or a step, and to show the variables there. It may
 * block — the program waits — and may throw to end the run.
 */
fun interface FormDebugger {
    fun beforeEval(ctx: Ctx, form: Any)
}

/**
 * The debugger of the program being run, if it is being debugged. One at a time, as FLX sessions
 * run one at a time; the host sets it for a session's evaluation and clears it afterwards.
 */
object Debugging {
    @Volatile
    @JvmField
    var debugger: FormDebugger? = null
}
