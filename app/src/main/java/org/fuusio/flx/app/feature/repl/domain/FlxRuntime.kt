package org.fuusio.flx.app.feature.repl.domain

import org.fuusio.flx.Flx
import org.fuusio.flx.Program
import org.fuusio.flx.core.vm.Ctx

/** A stateful local FLX session. Implementations execute away from the UI thread. */
interface FlxRuntime {

    /** Parses and evaluates FLX source (the REPL). */
    suspend fun evaluate(source: String): Evaluation

    suspend fun reset()
}

/**
 * The outcome of one evaluation: what was printed ([output]), and either the [result] literal of
 * the last form or an [error].
 */
data class Evaluation(
    val output: String,
    val result: String? = null,
    val error: String? = null,
)

/** The process-global FLX registry: built-in symbols exist only once [Flx.ctx] has been set. */
object FlxEnvironment {
    private val lock = Any()

    fun ensureInitialized() {
        synchronized(lock) {
            // Do not replace an existing root context.
            if (!Flx.isInitialized()) Flx.ctx = Ctx().apply { program = Program() }
        }
    }
}
