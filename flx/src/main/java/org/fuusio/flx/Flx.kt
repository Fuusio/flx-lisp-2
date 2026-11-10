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
package org.fuusio.flx

import org.fuusio.flx.core.*
import org.fuusio.flx.core.function.FunctionSpec
import org.fuusio.flx.core.function.FunctionSymbol
import org.fuusio.flx.core.logger.Logger
import org.fuusio.flx.core.macro.MacroSpec
import org.fuusio.flx.core.macro.MacroSymbol
import org.fuusio.flx.core.repl.FlxRepl
import org.fuusio.flx.core.type.UID
import org.fuusio.flx.core.vm.Ctx

object Flx {

    var logger = Logger()

    private var rootContext: Ctx? = null

    var ctx: Ctx
        get() = rootContext!!
        set(value) {
            val shouldInternSymbols = rootContext == null
            rootContext = value
            if (shouldInternSymbols) internSymbols()
        }

    // Keywords and user symbols are interned weakly: one instance per name while something uses it,
    // forgotten once nothing does. Both are made from source text and data (JSON keys become
    // keywords), so a long-running app parsing many scripts would otherwise keep every name it has
    // ever seen. Symbols compare by name, so a name interned again later is equal to the old one.
    private val keywords = WeakInterner<Keyword>()
    private val constantSymbols = hashMapOf<String, ConstantSymbol>()
    private val functionSymbols = hashMapOf<String, FunctionSymbol>()
    private val macroSymbols = hashMapOf<String, MacroSymbol>()
    private val symbols = WeakInterner<Symbol>()
    private val identifiedSymbols = hashMapOf<UID, Symbol>()

    // Name indexes for the parser. Several built-ins share a name ('+' is both ADDITION and
    // STRING__PLUS); the parser gets one of them, chosen by [isPreferredOver], and the call falls back
    // to the others in [overloads] when the argument types do not match (see Evaluator).
    private val functionSymbolsByName = hashMapOf<String, FunctionSymbol>()
    private val macroSymbolsByName = hashMapOf<String, MacroSymbol>()
    private val overloads = hashMapOf<String, MutableList<FunctionFormSpec>>()

    private fun internSymbols() {
        internConstantSymbols()
        FunctionSpec.internSymbols()
        MacroSpec.internSymbols()
    }

    fun reset() {
        ctx.reset()
        keywords.clear()
        symbols.clear()
        synchronized(identifiedSymbols) { identifiedSymbols.clear() }
    }

    fun finalize() {
        rootContext = null
    }

    fun eval(string: String) = FlxRepl().eval(string)

    fun getKeyword(string: String): Keyword =
        keywords.getOrPut(string) {
            // The keyword holds its key, so the interned entry lasts exactly as long as the keyword.
            Keyword.create(string).also { it.internKey = string }
        }

    fun getSymbol(spec: FunctionFormSpec): Symbol
            = when {
                spec.isMacroSpec() -> macroSymbols[spec.getId()] ?: throw IllegalStateException("Failed to find symbol: ${spec.symbol}")
                else -> functionSymbols[spec.getId()]
                    ?: throw IllegalStateException("Failed to find symbol: ${spec.symbol}")
            }

    fun getSymbol(macroSpec: MacroSpec) = macroSymbols[macroSpec.getId()]!!

    fun getSymbol(string: String): Symbol =
        functionSymbols[string] ?: (macroSymbols[string] ?: (constantSymbols[string] ?: (symbols.get(string) ?: internSymbol(string))))

    /**
     * The symbol the parser produces for [string]: a built-in function, then a macro, then a
     * constant, then a user symbol — one hash lookup each, instead of scanning every table.
     */
    fun getParsedSymbol(string: String): Symbol =
        functionSymbolsByName[string]
            ?: macroSymbolsByName[string]
            ?: constantSymbols[string]
            ?: symbols.get(string)
            ?: internSymbol(string)

    /**
     * The built-ins that share [spec]'s name, [spec] itself included, in the order they were
     * interned. Callers skip [spec] themselves.
     */
    fun getOverloads(spec: FunctionFormSpec): List<FunctionFormSpec> =
        overloads[spec.symbol] ?: emptyList()

    fun getSymbol(string: String, uid: UID): Symbol =
        when (uid) {
            0 -> getSymbol(string)
            else -> synchronized(identifiedSymbols) {
                identifiedSymbols.getOrPut(uid) { Symbol(string) }
            }
        }

    /**
     * A new plain symbol. A name starting with '.' used to make a symbol that called a Java method
     * of that name by reflection; FLX has no reflection, so such a name is an ordinary symbol, and
     * calling it fails unless the program defines it.
     */
    private fun internSymbol(string: String): Symbol {
        val symbol = Symbol(string)
        internSymbol(symbol)
        return symbol
    }

    fun internSymbol(symbol: Symbol): Symbol {
        when (symbol) {
            is FunctionSymbol -> {
                val spec = symbol.functionSpec
                functionSymbols[spec.getId()] = symbol
                val current = functionSymbolsByName[symbol.name]
                if (current == null || current.functionSpec === spec || spec.isPreferredOver(current.functionSpec)) {
                    functionSymbolsByName[symbol.name] = symbol
                }
                addOverload(spec)
            }
            is MacroSymbol -> {
                val spec = symbol.macroSpec
                macroSymbols[spec.getId()] = symbol
                val current = macroSymbolsByName[symbol.name]
                if (current == null || current.macroSpec === spec || spec.isPreferredOver(current.macroSpec)) {
                    macroSymbolsByName[symbol.name] = symbol
                }
                addOverload(spec)
            }
            is ConstantSymbol -> constantSymbols[symbol.name] = symbol
            else -> symbols.put(symbol.name, symbol)
        }
        return symbol
    }

    fun isInitialized() = rootContext != null

    /** Makes [spec] an overload of the built-ins of its name, without it becoming what the name means. */
    internal fun internOverload(spec: FunctionFormSpec) = addOverload(spec)

    private fun addOverload(spec: FunctionFormSpec) {
        val specs = overloads.getOrPut(spec.symbol) { mutableListOf() }
        // Symbols are interned again whenever a new root context is set; keep each spec once.
        if (specs.none { it === spec }) specs.add(spec)
    }

    /**
     * Which of two built-ins with the same name a bare symbol means: the Lisp-style function
     * (`(+ 1 2)`) over the Kotlin-style method-like one (`(+ "a" 1)` as String.plus), and otherwise
     * the first one found. The other stays reachable through overload fallback.
     */
    private fun FunctionFormSpec.isPreferredOver(other: FunctionFormSpec): Boolean =
        style == FunctionStyle.LISP_FUNCTION && other.style != FunctionStyle.LISP_FUNCTION

    private fun internConstantSymbols() {
        internConstantSymbol(NAME_E, Math.E)
        internConstantSymbol(NAME_PI, Math.PI)
    }

    private fun internConstantSymbol(name: String, value: Any) {
        internSymbol(ConstantSymbol(name, value))
    }
}

/**
 * Interns values by name, holding them weakly: a value stays interned while anything else refers to
 * it, and its entry disappears after it has been collected. The key must be a String the value
 * itself holds (a symbol's name), so the entry lives exactly as long as the value. Thread-safe.
 */
internal class WeakInterner<T : Any> {

    private val entries = java.util.WeakHashMap<String, java.lang.ref.WeakReference<T>>()

    @Synchronized
    fun get(key: String): T? = entries[key]?.get()

    @Synchronized
    fun put(key: String, value: T) {
        entries[key] = java.lang.ref.WeakReference(value)
    }

    @Synchronized
    fun getOrPut(key: String, create: () -> T): T {
        entries[key]?.get()?.let { return it }
        val value = create()
        entries[key] = java.lang.ref.WeakReference(value)
        return value
    }

    @Synchronized
    fun clear() {
        entries.clear()
    }
}
