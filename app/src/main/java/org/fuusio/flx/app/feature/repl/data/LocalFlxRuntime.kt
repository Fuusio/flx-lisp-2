package org.fuusio.flx.app.feature.repl.data

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.fuusio.flx.Flx
import org.fuusio.flx.Program
import org.fuusio.flx.app.feature.repl.domain.Evaluation
import org.fuusio.flx.app.feature.repl.domain.FlxEnvironment
import org.fuusio.flx.app.feature.repl.domain.FlxRuntime
import org.fuusio.flx.core.error.FlxException
import org.fuusio.flx.core.error.FormTrace
import org.fuusio.flx.core.parser.ParserError
import org.fuusio.flx.core.parser.ParserObserver
import org.fuusio.flx.core.toLiteral
import org.fuusio.flx.core.vm.Ctx
import org.fuusio.flx.core.vm.FlxVM

/**
 * One REPL session, serialized because the interpreter and its symbol tables are mutable.
 *
 * Definitions made in the session (`val`, `var`, `fun`, …) live in its own [Ctx] beneath the global FLX
 * context, so [reset] forgets them without touching the built-in symbols.
 */
class LocalFlxRuntime : FlxRuntime {

    @Volatile private var vm: FlxVM? = null

    companion object {
        private val lock = Any()
    }

    private class SessionCtx : Ctx(parent = Flx.ctx) {
        override fun onException(exception: FlxException): Nothing = throw exception
    }

    private fun createVm(): FlxVM {
        // Built-in symbols live in Flx's process-global registry.
        FlxEnvironment.ensureInitialized()
        val context = SessionCtx()
        return FlxVM(context).also {
            context.vm = it
            context.program = Program()
            it.start()
        }
    }

    override suspend fun evaluate(source: String): Evaluation = withContext(Dispatchers.IO + NonCancellable) {
        // The VM's large-stack worker cannot be interrupted safely. Keep the lock and output stream
        // alive until it actually exits, even if the screen is closed during evaluation.
        synchronized(lock) {
            val machine = vm ?: createVm().also { vm = it }
            val bytes = ByteArrayOutputStream()
            val stream = PrintStream(bytes, true, Charsets.UTF_8.name())
            val previous = machine.out
            machine.out = stream
            fun output(): String {
                stream.flush()
                return bytes.toString(Charsets.UTF_8.name())
            }
            try {
                val errors = mutableListOf<ParserError>()
                val observer = object : ParserObserver {
                    override fun onNext(parsingResult: Any) = Unit
                    override fun onError(error: ParserError) {
                        errors.add(error)
                    }
                }
                val result = FlxVM.runWithStack(FlxVM.LARGE_STACK_SIZE) {
                    val forms = machine.repl.parse(source, observer)
                    if (errors.isEmpty()) machine.repl.eval(forms).toLiteral() else null
                }
                val error = errors.takeIf { it.isNotEmpty() }?.joinToString("\n") { it.description }
                Evaluation(output(), result = result, error = error)
            } catch (error: Exception) {
                Evaluation(output(), error = error.message ?: error.javaClass.simpleName)
            } catch (_: StackOverflowError) {
                Evaluation(output(), error = "Maximum stack depth exceeded")
            } finally {
                FormTrace.clear()
                Flx.logger.clearLog()
                machine.out = previous
                stream.close()
            }
        }
    }

    override suspend fun reset() = withContext(Dispatchers.IO) {
        synchronized(lock) {
            vm?.stop()
            vm?.dispose()
            vm = null
        }
    }
}
