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

package org.fuusio.flx.core.macro

import org.fuusio.flx.Flx
import org.fuusio.flx.R
import org.fuusio.flx.core.*
import org.fuusio.flx.core.function.Function
import org.fuusio.flx.core.error.ArgError
import org.fuusio.flx.core.error.InvalidNumberOfArgsException
import org.fuusio.flx.core.type.ArgModifier
import org.fuusio.flx.core.type.SignatureSpec
import org.fuusio.flx.core.type.CoreType
import org.fuusio.flx.core.type.CoreType.*
import org.fuusio.flx.core.vm.Ctx

enum class MacroSpec(
    override val symbol: String,
    override val function: Function,
    override val outputType: CoreType,
    override val signatureSpecs: Array<SignatureSpec>,
    override val style: FunctionStyle = FunctionStyle.LISP_FUNCTION,
    override val searchKeywords: String = "",
) : FunctionFormSpec
{
    CONTROL_FLOW_RETURN("return", ::fnControlFlowReturn, UNIT,
        signatures {
            signature {
                description(R.string.desc_control_flow_return)
            }
        },
    ),

    CONTROL_FLOW_RETURN_VALUE("return", ::fnControlFlowReturn, UNIT,
        signatures {
            signature {
                description(R.string.desc_control_flow_return_value)
                arg(ANY, NAME_VALUE)
            }
        },
    ),

    HELP("?", ::fnHelp, INFO,
        signatures {
            signature {
                description(R.string.desc_help)
                arg(FUNCTION_FORM_SYMBOL, NAME_SYMBOL)
            }
        }
    ),

    ASSIGN("=", ::fnAssign, ANY,
        signatures {
            signature {
                description(R.string.desc_assign)
                arg(SYMBOL, NAME_VARIABLE)
                arg(ANY, NAME_VALUE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "assign",
    ),

    CALLBACK("callback", ::fnCallback, CoreType.CALLBACK,
        signatures {
            signature {
                description(R.string.desc_callback)
                arg(ANY, NAME_CODE)
                arg(SYMBOL, NAME_ARGS, ArgModifier.VARARG)
            }
        }
    ),

    HTTP_CALLBACK("httpCallback", ::fnHttpCallback, CoreType.HTTP_CALLBACK,
        signatures {
            signature {
                description(R.string.desc_http_callback)
                arg(ANY, NAME_CODE)
            }
        }
    ),

    DECREASE("--", ::fnDecrease, NUMBER,
        signatures {
            signature {
                description(R.string.desc_decrease)
                arg(SYMBOL, NAME_VARIABLE)
            }
        }
    ),

    SWITCH("switch", ::fnWhenWithArgument, ANY,
        signatures {
            signature {
                description(R.string.desc_when_with_argument)
                arg(ANY, NAME_ARGUMENT)
                arg(ANY, NAME_CASE, ArgModifier.REPEATABLE)
                arg(ANY, NAME_FORM, ArgModifier.REPEATABLE)
            }
        },
        searchKeywords = "when"
    ),

    FN("fn", ::fnFn, FUNCTION,
        signatures {
            signature {
                description(R.string.desc_fn)
                arg(ARRAY, NAME_ARGS)
                arg(ANY, NAME_BODY)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        }
    ),

    METHOD("method", ::fnMethod, SYMBOL,
        signatures {
            signature {
                description(R.string.desc_method)
                arg(SYMBOL, NAME_NAME)
                arg(ARRAY, NAME_ARGS)
                arg(ANY, NAME_BODY)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        }
    ),

    FUN("fun", ::fnFun, SYMBOL,
        signatures {
            signature {
                description(R.string.desc_fun)
                arg(SYMBOL, NAME_NAME)
                arg(ARRAY, NAME_ARGS)
                arg(ANY, NAME_BODY)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        }
    ),

    PROC("proc", ::fnProc, CoreType.CALLBACK,
        signatures {
            signature {
                description(R.string.desc_proc)
                arg(ARRAY, NAME_ARGS)
                arg(ANY, NAME_CODE)
            }
        }
    ),

    CODE("code", ::fnCode, CoreType.CODE,
        signatures {
            signature {
                description(R.string.desc_code)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        }
    ),

    DO("do", ::fnDo, ANY,
        signatures {
            signature {
                description(R.string.desc_do)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        }
    ),

    REPEAT("repeat", ::fnRepeat, ANY,
        signatures {
            signature {
                description(R.string.desc_repeat)
                arg(ANY, NAME_N)
                arg(ANY, NAME_FORM)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        }
    ),

    FOR_EACH("forEach", ::fnForEach, ANY,
        signatures {
            signature {
                description(R.string.desc_for_each)
                arg(SYMBOL, NAME_SYMBOL)
                arg(ANY, NAME_SEQUENCE)
                arg(ANY, NAME_FORM)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        }
    ),

    ALIAS_HELP("help", ::fnHelp, INFO,
        signatures {
            signature {
                description(R.string.desc_help)
                arg(FUNCTION_FORM_SYMBOL, NAME_SYMBOL)
            }
        }
    ),

    IF("if", ::fnControlFlowIfElse, ANY,
        signatures {
            signature {
                description(R.string.desc_control_flow_if_else)
                arg(ANY, NAME_CONDITION)
                arg(ANY, NAME_TRUE_BRANCH)
                arg(ANY, NAME_FALSE_BRANCH)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION
    ),

    CONTROL_FLOW_IF_ELSE("if-else", ::fnControlFlowIfElse, ANY,
        signatures {
            signature {
                description(R.string.desc_control_flow_if_else)
                arg(ANY, NAME_CONDITION)
                arg(ANY, NAME_TRUE_BRANCH)
                arg(ANY, NAME_FALSE_BRANCH)
            }
        }
    ),

    CONTROL_FLOW_IF("if", ::fnControlFlowIf, ANY,
        signatures {
            signature {
                description(R.string.desc_control_flow_if)
                arg(ANY, NAME_CONDITION)
                arg(ANY, NAME_TRUE_BRANCH)
            }
        }
    ),

    FOR("for", ::fnControlFlowFor, ANY,
        signatures {
            signature {
                description(R.string.desc_for)
                arg(ANY, NAME_INDEX_VAR)
                arg(ANY, NAME_START)
                arg(ANY, NAME_END)
                arg(ANY, NAME_STEP)
                arg(ANY, NAME_FORM)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        }
    ),

    INCREASE("++", ::fnIncrease, NUMBER,
        signatures {
            signature {
                description(R.string.desc_increase)
                arg(SYMBOL, NAME_VARIABLE)
            }
        }
    ),

    LET("let", ::fnLet, ANY,
        signatures {
            signature {
                description(R.string.desc_let)
                arg(ANY, NAME_BINDINGS)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        }
    ),

    MAP("map", ::fnMap, LIST,
        signatures {
            signature {
                description(R.string.desc_map)
                arg(ANY, NAME_FUNCTION)
                arg(ANY, NAME_SEQUENCE)
                arg(ANY, NAME_SEQUENCES, ArgModifier.VARARG)
            }
        }
    ),

    QUOTE("quote", ::fnQuote, ANY,
        signatures {
            signature {
                description(R.string.desc_quote)
                arg(ANY, NAME_FORM)
            }
        }
    ),

    LAZY("lazy", ::fnQuote, ANY,
        signatures {
            signature {
                description(R.string.desc_lazy)
                arg(ANY, NAME_FORM)
            }
        }
    ),

    VAL("val", ::fnVal, ANY,
        signatures {
            signature {
                description(R.string.desc_val)
                arg(SYMBOL, NAME_SYMBOL)
                arg(ANY, NAME_ASSIGN_VALUE)
            }
        }
    ),

    VAR("var", ::fnVar, ANY,
        signatures {
            signature {
                description(R.string.desc_var)
                arg(SYMBOL, NAME_SYMBOL)
                arg(ANY, NAME_ASSIGN_VALUE_OPTIONAL, ArgModifier.OPTIONAL)
            }
        }
    ),

    WHEN("when", ::fnWhen, ANY,
        signatures {
            signature {
                description(R.string.desc_when)
                arg(ANY, NAME_CONDITION, ArgModifier.REPEATABLE)
                arg(ANY, NAME_FORM, ArgModifier.REPEATABLE)
            }
        }
    ),

    LOOP("loop", ::fnLoop, ANY,
        signatures {
            signature {
                description(R.string.desc_loop)
                arg(ANY, NAME_BINDINGS)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        }
    ),

    RECUR("recur", ::fnRecur, ANY,
        signatures {
            signature {
                description(R.string.desc_recur)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        }
    ),

    WHILE("while", ::fnWhile, ANY,
        signatures {
            signature {
                description(R.string.desc_while)
                arg(ANY, NAME_CONDITION)
                arg(ANY, NAME_DO_FORM)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        }
    ),

    THREAD_FIRST("->", ::fnThreadFirst, ANY,
        signatures {
            signature {
                description(R.string.desc_thread_first)
                arg(ANY, NAME_VALUE)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        },
        searchKeywords = "thread first pipe pipeline"
    ),

    THREAD_LAST("->>", ::fnThreadLast, ANY,
        signatures {
            signature {
                description(R.string.desc_thread_last)
                arg(ANY, NAME_VALUE)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        },
        searchKeywords = "thread last pipe pipeline"
    ),

    ELVIS("?:", ::fnCoalesce, ANY,
        signatures {
            signature {
                description(R.string.desc_coalesce)
                arg(ANY, NAME_VALUE)
                arg(ANY, NAME_VALUES, ArgModifier.VARARG)
            }
        },
        searchKeywords = "coalesce elvis default nil null"
    ),

    COALESCE("coalesce", ::fnCoalesce, ANY,
        signatures {
            signature {
                description(R.string.desc_coalesce)
                arg(ANY, NAME_VALUE)
                arg(ANY, NAME_VALUES, ArgModifier.VARARG)
            }
        },
        searchKeywords = "?: elvis default nil null"
    ),

    IF_LET("if-let", ::fnIfLet, ANY,
        signatures {
            signature {
                description(R.string.desc_if_let)
                arg(ANY, NAME_BINDINGS)
                arg(ANY, NAME_TRUE_BRANCH)
                arg(ANY, NAME_FALSE_BRANCH, ArgModifier.OPTIONAL)
            }
        },
        searchKeywords = "if let when nil null"
    ),

    WHEN_LET("when-let", ::fnWhenLet, ANY,
        signatures {
            signature {
                description(R.string.desc_when_let)
                arg(ANY, NAME_BINDINGS)
                arg(ANY, NAME_BODY)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        },
        searchKeywords = "when let if nil null"
    ),

    TOGGLE("toggle!", ::fnToggle, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_toggle)
                arg(SYMBOL, NAME_VARIABLE)
            }
        },
        searchKeywords = "toggle not boolean flip"
    ),

    ADD_ASSIGN("+=", ::fnAddAssign, ANY,
        signatures {
            signature {
                description(R.string.desc_add_assign)
                arg(SYMBOL, NAME_VARIABLE)
                arg(ANY, NAME_VALUE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "add assign plus increase increment"
    ),

    SUB_ASSIGN("-=", ::fnSubAssign, NUMBER,
        signatures {
            signature {
                description(R.string.desc_sub_assign)
                arg(SYMBOL, NAME_VARIABLE)
                arg(ANY, NAME_VALUE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "subtract assign minus decrease decrement"
    ),

    UPDATE("update!", ::fnUpdate, ANY,
        signatures {
            signature {
                description(R.string.desc_update_bang)
                arg(SYMBOL, NAME_VARIABLE)
                arg(ANY, NAME_FUNCTION)
                arg(ANY, NAME_ARGS, ArgModifier.VARARG)
            }
        },
        searchKeywords = "update assign modify mutate"
    ),

    TRY_OR("try-or", ::fnTryOr, ANY,
        signatures {
            signature {
                description(R.string.desc_try_or)
                arg(ANY, NAME_FORM)
                arg(ANY, NAME_FALLBACK, ArgModifier.OPTIONAL)
            }
        },
        searchKeywords = "try catch error exception fallback default"
    ),

    TRY("try", ::fnTry, ANY,
        signatures {
            signature {
                description(R.string.desc_try)
                arg(ANY, NAME_BODY)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        },
        searchKeywords = "try catch error exception"
    );

    override fun getId(): String = name

    override fun isMacroSpec(): Boolean = true

    override val functionName: Symbol?
        get() = Flx.getSymbol(this)

    override fun call(ctx: Ctx, args: Array<Any>): Any =
        Evaluator.evalMacro(ctx, this, args)

    override fun eval(ctx: Ctx): Any = this

    override fun hasArgs(): Boolean {
        signatureSpecs.forEach { signature -> if (signature.argSpecs.isNotEmpty()) return true }
        return false
    }

    override fun checkArgs(argValues: Array<Any>): SignatureSpec? {
        signatureSpecs.forEach { signatureSpec -> if (signatureSpec.checkArgs(argValues)) return signatureSpec }
        return null
    }

    override fun getArgError(ctx: Ctx, args: Array<Any>): ArgError {
        signatureSpecs.forEach { signatureSpec ->
            if (signatureSpec.checkArgsCount(args)) {
                val argError = signatureSpec.getArgError(args)
                if (argError != null) return argError
            }
        }
        throw InvalidNumberOfArgsException(ctx, symbol, args.size, signatureSpecs[0].getArgsCount())
    }

    fun isDeprecated(): Boolean = style == FunctionStyle.DEPRECATED_FUNCTION

    companion object {

        fun internSymbols() {
            entries.forEach { value ->
                if (value.style != FunctionStyle.DEPRECATED_FUNCTION) {
                    // Not bound in the root context: the symbol evaluates to its built-in itself.
                    Flx.internSymbol(MacroSymbol(value))
                } else {
                    // Not what its name means, but still what a call of the name's macro falls back
                    // to when the arguments fit: `(if c a b)` is the deprecated three-argument `if`.
                    Flx.internOverload(value)
                }
            }
        }

        fun get(symbol: String, id: String? = null): FunctionFormSpec? {
            if (id != null) {
                entries.forEach { value -> if (value.name == id) return@get replace(value) }
            } else {
                entries.forEach { value -> if (value.symbol == symbol) return@get replace(value) }
            }
            return null
        }

        private fun replace(macroSpec: MacroSpec): MacroSpec =
            when (macroSpec) {
                IF -> CONTROL_FLOW_IF_ELSE
                else -> macroSpec
            }
    }
}