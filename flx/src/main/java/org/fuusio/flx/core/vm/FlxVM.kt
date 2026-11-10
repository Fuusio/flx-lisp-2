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
@file:Suppress("unused")

package org.fuusio.flx.core.vm

import org.fuusio.flx.Flx
import org.fuusio.flx.Program
import org.fuusio.flx.core.*
import org.fuusio.flx.core.repl.FlxRepl
import java.util.concurrent.CountDownLatch
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

open class FlxVM(val ctx: Ctx) {
    val repl = FlxRepl(ctx)
    var out = System.out!!

    @Volatile
    var executionState = ExecutionState.IDLE

    private val observers = mutableSetOf<VmObserver>()

    fun addObserver(observer: VmObserver) {
        synchronized(observers) {
            observers.add(observer)
        }
    }

    fun removeObserver(observer: VmObserver) {
        synchronized(observers) {
            observers.remove(observer)
        }
    }

    open fun dispose() {}

    fun eval(expression: String): Any = repl.eval(expression)

    fun eval(form: Any): Any = form.eval(ctx)

    /**
     * [eval] on a new thread with a [stackSize]-byte stack, waiting for the result; an exception is
     * rethrown here. A tree-walking interpreter spends several JVM frames per FLX call, and Android
     * gives background threads about 1 MB, so deep recursion that fails there can succeed here.
     * It blocks the calling thread, so call it off the main thread for anything long-running.
     */
    fun evalWithLargeStack(expression: String, stackSize: Long = LARGE_STACK_SIZE): Any =
        runWithStack(stackSize) { eval(expression) }

    /** Guards [executionState] while paused: [canContinue] waits on it until resumed or stopped. */
    private val pauseLock = Object()

    /**
     * Whether evaluation may go on, checked by loops and between top-level forms. While paused, it
     * waits here — on the evaluating thread, never the main one — until resumed or stopped.
     */
    fun canContinue(): Boolean {
        synchronized(pauseLock) {
            while (executionState == ExecutionState.PAUSED) pauseLock.wait()
        }
        return executionState.isStarted()
    }

    fun print(string: String) {
        out.print(string)
    }

    fun println(string: String) {
        out.println(string)
    }

    fun start(): ExecutionState {
        if (executionState == ExecutionState.IDLE) {
            executionState = ExecutionState.STARTED
            observers.forEach { observer -> observer.onStarted(this) }
        }
        return executionState
    }

    fun pause(): ExecutionState {
        if (executionState == ExecutionState.STARTED) {
            executionState = ExecutionState.PAUSED

            synchronized(observers) {
                observers.forEach { observer -> observer.onPaused(this) }
            }
        }
        return executionState
    }

    fun resume(): ExecutionState {
        if (executionState == ExecutionState.PAUSED) {
            synchronized(pauseLock) {
                executionState = ExecutionState.STARTED
                pauseLock.notifyAll()
            }
            observers.forEach { observer -> observer.onResumed(this) }
        }
        return executionState
    }

    fun stop(): ExecutionState {
        if (executionState == ExecutionState.STARTED ||
            executionState == ExecutionState.PAUSED) {
            synchronized(pauseLock) {
                executionState = ExecutionState.STOPPED
                pauseLock.notifyAll()
            }
            observers.forEach { observer -> observer.onStopped(this) }
        }
        return executionState
    }

    fun canPause() = executionState == ExecutionState.STARTED

    fun canResume() = executionState == ExecutionState.PAUSED

    fun canStart() = executionState == ExecutionState.IDLE

    fun canStop() = executionState == ExecutionState.STARTED || executionState == ExecutionState.PAUSED

    fun isStopped() = executionState == ExecutionState.STOPPED

    fun isPaused() = executionState == ExecutionState.PAUSED

    fun reset() {
        executionState = ExecutionState.IDLE
        ctx.reset()
        Flx.reset()
    }

    companion object {

        /** Stack size for [evalWithLargeStack]: room for recursion some tens of thousands of calls deep. */
        const val LARGE_STACK_SIZE: Long = 64L * 1024 * 1024

        private class LargeStackThread(runnable: Runnable) :
            Thread(null, runnable, "flx-vm", LARGE_STACK_SIZE) {
            init {
                isDaemon = true
            }
        }

        private val largeStackExecutor = ThreadPoolExecutor(
            1,
            Int.MAX_VALUE,
            60L,
            TimeUnit.SECONDS,
            SynchronousQueue(),
        ) { runnable -> LargeStackThread(runnable) }

        /**
         * Runs [block] on a thread with a [stackSize]-byte stack and returns its result or rethrows
         * its failure. Calls using [LARGE_STACK_SIZE] reuse a pooled daemon worker instead of
         * allocating a new thread and 64 MB stack on every evaluation.
         */
        fun <T> runWithStack(stackSize: Long, block: () -> T): T {
            if (stackSize == LARGE_STACK_SIZE) {
                if (Thread.currentThread() is LargeStackThread) return block()
                var outcome: Result<T>? = null
                val done = CountDownLatch(1)
                largeStackExecutor.execute {
                    try {
                        outcome = runCatching(block)
                    } finally {
                        done.countDown()
                    }
                }
                done.await()
                return outcome!!.getOrThrow()
            }
            var outcome: Result<T>? = null
            val thread = Thread(null, { outcome = runCatching(block) }, "flx-vm", stackSize)
            thread.start()
            thread.join() // join makes the thread's write of outcome visible here
            return outcome!!.getOrThrow()
        }

        fun create(ctx: Ctx, program: Program = Program()): FlxVM {
            val vm = FlxVM(ctx)
            vm.ctx.program = program
            return vm
        }
    }
}