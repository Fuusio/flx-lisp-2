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

package org.fuusio.flx.core.function

import org.fuusio.flx.Flx
import org.fuusio.flx.R
import org.fuusio.flx.core.*
import org.fuusio.flx.core.error.ArgError
import org.fuusio.flx.core.error.InvalidNumberOfArgsException
import org.fuusio.flx.core.type.ArgModifier
import org.fuusio.flx.core.type.SignatureSpec
import org.fuusio.flx.core.type.CoreType
import org.fuusio.flx.core.type.CoreType.*
import org.fuusio.flx.core.vm.Ctx

@Suppress("DEPRECATION")
enum class FunctionSpec(
    override val symbol: String,
    override val function: Function,
    override val outputType: CoreType,
    override val signatureSpecs: Array<SignatureSpec>,
    override val style: FunctionStyle = FunctionStyle.LISP_FUNCTION,
    override val searchKeywords: String = "",
): FunctionFormSpec {

    NOOP("", ::fnNoop, UNIT,
        signatures { signature { description(R.string.desc_noop) } },
    ),

    OBJECT__IS("is", ::fnObjectIs, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_object__is)
                arg(ANY, NAME_INSTANCE)
                arg(STRING, NAME_OBJECT_NAME)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Object,instance"
    ),

    NUMBER__HAS_DECIMAL(".hasDecimals", ::fnNumberHasDecimals, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_number__has_decimals)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Number,decimal"
    ),

    NUMBER__IS_WHOLE_NUMBER(".isWholeNumber", ::fnNumberIsWholeNumber, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_number__is_whole_number)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Number,decimal"
    ),

    CALCULATOR_NEW("Calculator.new", ::fnCalculatorNew, CLASS,
        signatures {
            signature {
                description(R.string.desc_calculator_new)
            }
        }
    ),

    CALCULATOR__GET_OPERAND1(".operand1", ::fnCalculatorOperand1, ANY,
        signatures {
            signature {
                description(R.string.desc_calculator__operand1)
                arg(CALCULATOR, NAME_CALCULATOR)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Calculator"
    ),

    CALCULATOR__GET_OPERAND2(".operand2", ::fnCalculatorOperand2, ANY,
        signatures {
            signature {
                description(R.string.desc_calculator__operand2)
                arg(CALCULATOR, NAME_CALCULATOR)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Calculator"
    ),

    CALCULATOR__GET_OPERATOR(".operator", ::fnCalculatorOperator, ANY,
        signatures {
            signature {
                description(R.string.desc_calculator__operator)
                arg(CALCULATOR, NAME_CALCULATOR)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Calculator"
    ),

    CALCULATOR__DECIMAL_POINT(".decimalPoint", ::fnCalculatorDecimalPoint, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_calculator__decimal_point)
                arg(CALCULATOR, NAME_CALCULATOR)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Calculator"
    ),

    CALCULATOR__HAS_ERROR(".hasError", ::fnCalculatorHasError, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_calculator__enter)
                arg(CALCULATOR, NAME_CALCULATOR)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Calculator"
    ),

    CALCULATOR__EQUALS(".enter", ::fnCalculatorEquals, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_calculator__enter)
                arg(CALCULATOR, NAME_CALCULATOR)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Calculator"
    ),

    CALCULATOR__TOGGLE_SIGN(".toggleSign", ::fnCalculatorToggleSign, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_calculator__toggleSign)
                arg(CALCULATOR, NAME_CALCULATOR)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Calculator"
    ),

    CALCULATOR__PERCENTAGE(".percentage", ::fnCalculatorPercentage, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_calculator__percentage)
                arg(CALCULATOR, NAME_CALCULATOR)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Calculator"
    ),

    CALCULATOR__CLEAR(".clear", ::fnCalculatorClear, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_calculator__clear)
                arg(CALCULATOR, NAME_CALCULATOR)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Calculator"
    ),

    CALCULATOR__CLEAR_ENTRY(".clearEntry", ::fnCalculatorClearEntry, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_calculator__clear_entry)
                arg(CALCULATOR, NAME_CALCULATOR)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Calculator"
    ),

    CALCULATOR__ADDITION(".add", ::fnCalculatorAddition, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_calculator__addition)
                arg(CALCULATOR, NAME_CALCULATOR)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Calculator"
    ),

    CALCULATOR__SUBTRACTION(".subtract", ::fnCalculatorSubtraction, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_calculator__subtraction)
                arg(CALCULATOR, NAME_CALCULATOR)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Calculator"
    ),

    CALCULATOR__MULTIPLICATION(".multiply", ::fnCalculatorMultiplication, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_calculator__multiplication)
                arg(CALCULATOR, NAME_CALCULATOR)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Calculator"
    ),

    CALCULATOR__DIVISION(".divide", ::fnCalculatorDivision, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_calculator__division)
                arg(CALCULATOR, NAME_CALCULATOR)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Calculator"
    ),

    CALCULATOR__DIGIT(".digit", ::fnCalculatorDigit, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_calculator__digit)
                arg(CALCULATOR, NAME_CALCULATOR)
                arg(NUMBER, NAME_DIGIT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Calculator"
    ),


    ANY__GET_CLASS_NAME(".getClassName", ::fnAnyGetClassName, STRING,
        signatures {
            signature {
                description(R.string.desc_any__get_class_name)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Class",
    ),


    IS("is", ::fnIsInstance, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_is_instance)
                arg(ANY, NAME_OBJECT)
                arg(ANY, NAME_CLASS)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION
    ),

    LOG_D("Log.d", ::fnLogD, NULL,
        signatures {
            signature {
                description(R.string.desc_log_d)
                arg(ANY, NAME_MESSAGE)
            }
        }
    ),

    LOG_I("Log.i", ::fnLogI, NULL,
        signatures {
            signature {
                description(R.string.desc_log_i)
                arg(ANY, NAME_MESSAGE)
            }
        }
    ),

    LOG_W("Log.w", ::fnLogW, NULL,
        signatures {
            signature {
                description(R.string.desc_log_w)
                arg(ANY, NAME_MESSAGE)
            }
        }
    ),

    LOG_E("Log.e", ::fnLogE, NULL,
        signatures {
            signature {
                description(R.string.desc_log_e)
                arg(ANY, NAME_MESSAGE)
            }
        }
    ),

    ADDITION("+", ::fnAddition, NUMBER,
        signatures {
            signature {
                description(R.string.desc_addition)
                arg(NUMBER, NAME_NUMBERS, ArgModifier.VARARG)
            }
        }
    ),

    PERCENTAGE("%", ::fnPercentage, NUMBER,
        signatures {
            signature {
                description(R.string.desc_percentage)
                arg(NUMBER, NAME_PERCENTAGE)
                arg(NUMBER, NAME_TOTAL)
            }
        }
    ),

    SUBTRACTION("-", ::fnSubtraction, NUMBER,
        signatures {
            signature {
                description(R.string.desc_subtraction)
                arg(NUMBER, NAME_NUMBERS, ArgModifier.VARARG)
            }
        }
    ),

    MULTIPLICATION("*", ::fnMultiplication, NUMBER,
        signatures {
            signature {
                description(R.string.desc_multiplication)
                arg(NUMBER, NAME_NUMBERS, ArgModifier.VARARG)
            }
        }
    ),

    DIVISION("/", ::fnDivision, NUMBER,
        signatures {
            signature {
                description(R.string.desc_division)
                arg(NUMBER, NAME_NUMBERS, ArgModifier.VARARG)
            }
        }
    ),

    EQUAL("==", ::fnEqual, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_equals)
                    arg(ANY, NAME_OBJECT)
                    arg(ANY, NAME_OBJECT,ArgModifier.VARARG)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "equal",
    ),

    GREATER_THAN(">", ::fnGreaterThan, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_greater_than)
                arg(NUMBER, NAME_NUMBER)
                arg(NUMBER, NAME_NUMBER, ArgModifier.VARARG)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "greater",
    ),

    GREATER_THAN_OR_EQUAL(">=", ::fnGreaterThanOrEqual, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_greater_than_or_equal)
                arg(NUMBER, NAME_NUMBER)
                arg(NUMBER, NAME_NUMBER, ArgModifier.VARARG)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "greater,equal",
    ),





    LESSER_THAN("<", ::fnLesserThan, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_lesser_than)
                    arg(NUMBER, NAME_NUMBER)
                    arg(NUMBER, NAME_NUMBER, ArgModifier.VARARG)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "lesser",
    ),

    LESSER_THAN_OR_EQUAL("<=", ::fnLesserThanOrEqual, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_lesser_than_or_equal)
                    arg(NUMBER, NAME_NUMBER)
                    arg(NUMBER, NAME_NUMBER,ArgModifier.VARARG)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "greater,equal",
    ),

    ABS("abs", ::fnAbs, NUMBER,
        signatures {
            signature {
                description(R.string.desc_abs)
                arg(NUMBER, NAME_X)
            }
        }
    ),

    ACOS("acos", ::fnAcos, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_acos)
                arg(NUMBER, NAME_X)
            }
        }
    ),

    AND("and", ::fnAnd, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_and)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        }
    ),

    ANY__IS_ARRAY(".isArray", ::fnAnyIsArray, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_array)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ASIN("asin", ::fnAsin, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_asin)
                arg(NUMBER, NAME_X)
            }
        }
    ),

    ATAN("atan", ::fnAtan, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_atan)
                arg(NUMBER, NAME_X)
            }
        }
    ),

    ATAN2("atan2", ::fnAtan2, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_atan2)
                arg(NUMBER, NAME_Y)
                arg(NUMBER, NAME_X)
            }
        },
        searchKeywords = "Math,Number,angle"
    ),

    HYPOT("hypot", ::fnHypot, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_hypot)
                arg(NUMBER, NAME_X)
                arg(NUMBER, NAME_Y)
                arg(NUMBER, NAME_Z, ArgModifier.OPTIONAL)
            }
        },
        searchKeywords = "Math,Number,vector"
    ),

    CLAMP("clamp", ::fnNumberClamp, NUMBER,
        signatures {
            signature {
                description(R.string.desc_clamp)
                arg(NUMBER, NAME_VALUE)
                arg(NUMBER, NAME_MIN)
                arg(NUMBER, NAME_MAX)
            }
        },
        searchKeywords = "Math,Number,coerceIn"
    ),

    NUMBER__COERCE_IN(".coerceIn", ::fnNumberClamp, NUMBER,
        signatures {
            signature {
                description(R.string.desc_clamp)
                arg(NUMBER, NAME_VALUE)
                arg(NUMBER, NAME_MIN)
                arg(NUMBER, NAME_MAX)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Math,Number,clamp"
    ),

    STRING__PLUS("+", ::fnStringPlus, STRING,
        signatures {
            signature {
                description(R.string.desc_string__plus)
                arg(STRING, NAME_STRING)
                arg(ANY, NAME_ANY)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__IS_BLANK(".isBlank", ::fnStringIsBlank, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_string__is_blank)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    BIT_AND("and", ::fnNumberBitAnd, NUMBER,
        signatures {
            signature {
                description(R.string.desc_bit_and)
                arg(NUMBER, NAME_NUMBER)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Int,Long,bit",
    ),

    BIT_NOT("bitNot", ::fnNumberInv, NUMBER,
        signatures {
            signature {
                description(R.string.desc_bit_not)
                arg(NUMBER, NAME_INT)
            }
        },
        searchKeywords = "Int,Long,bit"
    ),

    NUMBER__INV(".inv", ::fnNumberInv, NUMBER,
        signatures {
            signature {
                description(R.string.desc_number__inv)
                arg(NUMBER, NAME_INT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Int,Long,bit"
    ),

    BIT_OR("or", ::fnNumberBitOr, NUMBER,
        signatures {
            signature {
                description(R.string.desc_bit_or)
                arg(NUMBER, NAME_NUMBER)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Int,Long,bit"
    ),

    NUMBER__SHL(".shl", ::fnNumberShl, NUMBER,
        signatures {
            signature {
                description(R.string.desc_number__shl)
                arg(NUMBER, NAME_NUMBER)
                arg(INT, NAME_N)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Number,bit",
    ),

    NUMBER__SHR(".shr", ::fnNumberShr, NUMBER,
        signatures {
            signature {
                description(R.string.desc_number__shr)
                arg(NUMBER, NAME_NUMBER)
                arg(INT, NAME_N)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Number,bit",
    ),

    NUMBER__USHR(".ushr", ::fnNumberUshr, NUMBER,
        signatures {
            signature {
                description(R.string.desc_number__shr)
                arg(NUMBER, NAME_NUMBER)
                arg(INT, NAME_N)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Int,Long,bit",
    ),

    BIT_XOR("xor", ::fnNumberBitXor, NUMBER,
        signatures {
            signature {
                description(R.string.desc_bit_xor)
                    arg(NUMBER, NAME_INT)
                    arg(NUMBER, NAME_INT)
                    arg(NUMBER, NAME_INTS,ArgModifier.VARARG)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Int,Long,bit"
    ),

    ANY__IS_BOOLEAN(".isBoolean", ::fnAnyIsBoolean, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__boolean)
                arg(ANY, NAME_FORM)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ANY__IS_BYTE(".isByte", ::fnAnyIsByte, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_byte)
                arg(ANY, NAME_FORM)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    STRING__BUT_LAST(".butLast", ::fnStringButlast, STRING,
        signatures {
            signature {
                description(R.string.desc_string__butlast)
                arg(NUMBER, NAME_N)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    // The method that the deprecated `capitalize` is replaced with, so not deprecated itself.
    STRING__CAPITALIZE(".capitalize", ::fnStringCapitalize, STRING,
        signatures {
            signature {
                description(R.string.desc_string__capitalize)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__CHOP(".chop", ::fnStringChop, STRING,
        signatures {
            signature {
                description(R.string.desc_string__chop)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    CEIL("ceil", ::fnCeil, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_ceil)
                arg(NUMBER, NAME_X)
            }
        }
    ),

    COLOR("color", ::fnColor, CoreType.COLOR,
        signatures {
            signature {
                description(R.string.desc_color)
                arg(NUMBER, NAME_RED)
                arg(NUMBER, NAME_GREEN)
                arg(NUMBER, NAME_BLUE)
                arg(NUMBER, NAME_ALPHA, ArgModifier.OPTIONAL)
            }
        }
    ),

    COMPARE("compare", ::fnCompare, LONG,
        signatures {
            signature {
                description(R.string.desc_compare)
                    arg(STRING, NAME_STRING_1)
                    arg(STRING, NAME_STRING_2)
            }
        }
    ),

    CONJ("conj", ::fnConj, ANY,
        signatures {
            signature {
                description(R.string.desc_conj_0)
                arg(SEQUENCE, NAME_COLLECTION)
                arg(ANY, NAME_ITEM)
            }
            signature {
                description(R.string.desc_conj_1)
                arg(SET, NAME_COLLECTION)
                arg(ANY, NAME_ITEM)
            }
        }
    ),

    ARRAY_ADD(".add", ::fnArrayAdd, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_array__add)
                arg(ARRAY, NAME_ARRAY)
                arg(ANY, NAME_ITEM)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Array"
    ),

    LIST_ADD(".add", ::fnListAdd, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_list__add)
                arg(LIST, NAME_LIST)
                arg(ANY, NAME_ITEM)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "List"
    ),

    SET_ADD(".add", ::fnSetAdd, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_list__add)
                arg(SET, NAME_SET)
                arg(ANY, NAME_ITEM)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Set"
    ),

    ARRAY_ADD_ALL(".addAll", ::fnArrayAddAll, ARRAY,
        signatures {
            signature {
                description(R.string.desc_array__add_all)
                arg(ARRAY, NAME_ARRAY)
                arg(COLLECTION, NAME_ITEMS)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Array"
    ),

    LIST_ADD_ALL(".addAll", ::fnListAddAll, LIST,
        signatures {
            signature {
                description(R.string.desc_list__add_all)
                arg(LIST, NAME_LIST)
                arg(COLLECTION, NAME_ITEMS)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "List"
    ),

    SET_ADD_ALL(".addAll", ::fnSetAddAll, SET,
        signatures {
            signature {
                description(R.string.desc_list__add_all)
                arg(SET, NAME_SET)
                arg(COLLECTION, NAME_ITEMS)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Set"
    ),

    COLLECTION__REMOVE(".remove", ::fnCollectionRemove, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_collection__remove)
                arg(ARRAY, NAME_ARRAY)
                arg(ANY, NAME_ITEM)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Collection,Array,List,Set"
    ),

    ARRAY_REMOVE(".remove", ::fnArrayRemove, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_array__remove)
                arg(ARRAY, NAME_ARRAY)
                arg(ANY, NAME_ITEM)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Array"
    ),

    LIST_REMOVE(".remove", ::fnListRemove, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_list__remove)
                arg(LIST, NAME_LIST)
                arg(ANY, NAME_ITEM)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "List"
    ),

    SET_REMOVE(".remove", ::fnSetRemove, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_list__remove)
                arg(SET, NAME_SET)
                arg(ANY, NAME_ITEM)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Set"
    ),

    COLLECTION_CLEAR(".clear", ::fnCollectionClear, UNIT,
        signatures {
            signature {
                description(R.string.desc_collection__clear)
                arg(COLLECTION, NAME_COLLECTION)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Collection,Array,List,Set,Map"
    ),

    COLLECTION_IS_EMPTY(".isEmpty", ::fnCollectionIsEmpty, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_collection__is_empty)
                arg(COLLECTION, NAME_COLLECTION)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Collection,Array,List,Set,Map"
    ),

    COLLECTION_IS_NOT_EMPTY(".isNotEmpty", ::fnCollectionIsNotEmpty, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_collection__is_not_empty)
                arg(COLLECTION, NAME_COLLECTION)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Collection,Array,List,Set,Map"
    ),

    CONS("cons", ::fnCons, LIST,
        signatures {
            signature {
                description(R.string.desc_cons_0)
                arg(ANY, NAME_ITEM)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_cons_1)
                arg(ANY, NAME_ITEM)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_cons_2)
                arg(ANY, NAME_ITEM)
                arg(SET, NAME_SET)
            }
        }
    ),

    COS("cos", ::fnCos, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_cos)
                    arg(NUMBER, NAME_X)
            }
        }
    ),

    DATE("date", ::fnDate, CoreType.DATE,
        signatures {
            signature {
                description(R.string.desc_date)
            }
        }
    ),

    GET_YEAR(".year", ::fnDateGetYear, LONG,
        signatures {
            signature {
                description(R.string.desc_date_year)
                arg(CoreType.DATE, NAME_DATE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Date",
    ),

    GET_MONTH(".month", ::fnDateGetMonth, LONG,
        signatures {
            signature {
                description(R.string.desc_date_month)
                arg(CoreType.DATE, NAME_DATE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Date",
    ),

    GET_DAY(".day", ::fnDateGetDay, LONG,
        signatures {
            signature {
                description(R.string.desc_date_day_of_month)
                arg(CoreType.DATE, NAME_DATE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Date",
    ),

    GET_HOURS(".hour", ::fnDateGetHour, LONG,
        signatures {
            signature {
                description(R.string.desc_date_hours)
                    arg(CoreType.DATE, NAME_DATE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Date",
    ),

    GET_MINUTES(".minute", ::fnDateGetMinute, LONG,
        signatures {
            signature {
                description(R.string.desc_date_minutes)
                    arg(CoreType.DATE, NAME_DATE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Date",
    ),

    GET_SECONDS(".second", ::fnDateGetSecond, LONG,
        signatures {
            signature {
                description(R.string.desc_date_seconds)
                    arg(CoreType.DATE, NAME_DATE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Date",
    ),

    GET_MILLISECONDS(".millisecond", ::fnDateGetMillisecond, LONG,
        signatures {
            signature {
                description(R.string.desc_date_seconds)
                arg(CoreType.DATE, NAME_DATE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Date",
    ),

    ANY__IS_DOUBLE(".isDouble", ::fnAnyIsDoubleQ, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_double)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    STRING__DROP(".drop", ::fnStringDrop, ANY,
        signatures {
            signature {
                description(R.string.desc_sequence__drop)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(NUMBER, NAME_N)
            }
            signature {
                description(R.string.desc_string__drop)
                arg(STRING, NAME_STRING)
                arg(NUMBER, NAME_N)
            }
            signature {
                description(R.string.desc_sequence__drop)
                arg(NUMBER, NAME_N)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_string__drop)
                arg(NUMBER, NAME_N)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,String",
    ),

    STRING__DROP_LAST(".dropLast", ::fnStringDropLast, ANY,
        signatures {
            signature {
                description(R.string.desc_sequence__drop_last)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(NUMBER, NAME_N)
            }
            signature {
                description(R.string.desc_string__drop_last)
                arg(STRING, NAME_STRING)
                arg(NUMBER, NAME_N)
            }
            signature {
                description(R.string.desc_sequence__drop_last)
                arg(NUMBER, NAME_N)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_string__drop_last)
                arg(NUMBER, NAME_N)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,String",
    ),

    STRING__ENDS_WITH(".endsWith", ::fnStringEndsWith, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_string__ends_with)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_POSTFIX)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    EPOCH_TIME("epochTime", ::fnEpochTime, LONG,
        signatures {
            signature {
                description(R.string.desc_epoch_time)
            }
        }
    ),

    EVAL("eval", ::fnEval, ANY,
        signatures {
            signature {
                description(R.string.desc_eval)
                    arg(ANY, NAME_OBJECT)
            }
        }
    ),

    EVAL_STRING("evalString", ::fnEvalString, ANY,
        signatures {
            signature {
                description(R.string.desc_eval_string)
                    arg(STRING, NAME_STRING)
            }
        }
    ),

    NUMBER__IS_EVEN(".isEven", ::fnNumberIsEven, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_number__is_even)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Number",
    ),

    EXP("exp", ::fnExp, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_exp)
                    arg(NUMBER, NAME_X)
            }
        }
    ),

    ANY__IS_FALSE(".isFalse", ::fnAnyIsFalse, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_false)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    COLLECTION__FILTER(".filter", ::fnCollectionFilter, LIST,
        signatures {
            signature {
                description(R.string.desc_sequence__filter)
                arg(SEQUENCE, NAME_COLLECTION)
                arg(FUNCTION, NAME_PREDICATE)
            }
            signature {
                description(R.string.desc_set__filter)
                arg(SET, NAME_COLLECTION)
                arg(FUNCTION, NAME_PREDICATE)
            }
            signature {
                description(R.string.desc_sequence__filter)
                arg(SEQUENCE, NAME_COLLECTION)
                arg(KEYWORD, NAME_KEY)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,Set,Collection"
    ),

    SEQUENCE__FIRST(".first", ::fnSequenceFirst, ANY,
        signatures {
            signature {
                description(R.string.desc_sequence__first)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_string__first)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String,Sequence"
    ),

    FLOOR("floor", ::fnFloor, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_floor)
                arg(NUMBER, NAME_X)
            }
        }
    ),

    FORMAT("format", ::fnFormat, NULL,
        signatures {
            signature {
                description(R.string.desc_format)
                arg(ANY, NAME_OBJECT)
                arg(ANY, NAME_FORMAT_ARGS, ArgModifier.VARARG)
            }
        }
    ),

    GET("get", ::fnGet, ANY,
        signatures {
            signature {
                description(R.string.desc_get_0)
                arg(JSON_OBJECT, NAME_JSON)
                arg(ANY, NAME_KEY)
            }
            signature {
                description(R.string.desc_get_1)
                arg(JSON_OBJECT, NAME_JSON)
                arg(ANY, NAME_KEY)
                arg(ANY, NAME_FALLBACK)
            }
            signature {
                description(R.string.desc_get_2)
                arg(MAP, NAME_MAP)
                arg(ANY, NAME_KEY)
            }
            signature {
                description(R.string.desc_get_3)
                arg(MAP, NAME_MAP)
                arg(ANY, NAME_KEY)
                arg(ANY, NAME_FALLBACK)
            }
        }
    ),

    MAP__GET(".get", ::fnMapGet, ANY,
        signatures {
            signature {
                description(R.string.desc_map__get_0)
                arg(MAP, NAME_MAP)
                arg(ANY, NAME_KEY)
            }
            signature {
                description(R.string.desc_map__get_1)
                arg(MAP, NAME_MAP)
                arg(ANY, NAME_KEY)
                arg(ANY, NAME_FALLBACK)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Map"
    ),

    JSON__GET(".get", ::fnJsonGet, ANY,
        signatures {
            signature {
                description(R.string.desc_json__get_0)
                arg(JSON_OBJECT, NAME_JSON)
                arg(ANY, NAME_KEY)
            }
            signature {
                description(R.string.desc_json__get_1)
                arg(JSON_OBJECT, NAME_JSON)
                arg(ANY, NAME_KEY)
                arg(ANY, NAME_FALLBACK)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "JSON"
    ),

    ASSOC("assoc", ::fnMapAssoc, MAP,
        signatures {
            signature {
                description(R.string.desc_assoc)
                arg(MAP, NAME_MAP)
                arg(ANY, NAME_KEY, ArgModifier.REPEATABLE)
                arg(ANY, NAME_VALUE, ArgModifier.REPEATABLE)
            }
        },
        searchKeywords = "Map"
    ),

    MAP__PUT(".put", ::fnMapAssoc, MAP,
        signatures {
            signature {
                description(R.string.desc_map__put)
                arg(MAP, NAME_MAP)
                arg(ANY, NAME_KEY)
                arg(ANY, NAME_VALUE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Map"
    ),

    DISSOC("dissoc", ::fnMapDissoc, MAP,
        signatures {
            signature {
                description(R.string.desc_dissoc)
                arg(MAP, NAME_MAP)
                arg(ANY, NAME_KEY)
                arg(ANY, NAME_KEYS, ArgModifier.VARARG)
            }
        },
        searchKeywords = "Map"
    ),

    MAP__REMOVE_KEY(".removeKey", ::fnMapDissoc, MAP,
        signatures {
            signature {
                description(R.string.desc_map__remove_key)
                arg(MAP, NAME_MAP)
                arg(ANY, NAME_KEY)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Map"
    ),

    MERGE("merge", ::fnMapMerge, MAP,
        signatures {
            signature {
                description(R.string.desc_merge)
                arg(MAP, NAME_MAP)
                arg(MAP, NAME_MAPS, ArgModifier.VARARG)
            }
        },
        searchKeywords = "Map"
    ),

    KEYS("keys", ::fnMapKeys, ARRAY,
        signatures {
            signature {
                description(R.string.desc_map__keys)
                arg(MAP, NAME_MAP)
            }
            signature {
                description(R.string.desc_json__keys)
                arg(JSON_OBJECT, NAME_JSON)
            }
        },
        searchKeywords = "Map,JSON"
    ),

    MAP__KEYS(".keys", ::fnMapKeys, ARRAY,
        signatures {
            signature {
                description(R.string.desc_map__keys)
                arg(MAP, NAME_MAP)
            }
            signature {
                description(R.string.desc_json__keys)
                arg(JSON_OBJECT, NAME_JSON)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Map,JSON"
    ),

    VALS("vals", ::fnMapValues, ARRAY,
        signatures {
            signature {
                description(R.string.desc_map__values)
                arg(MAP, NAME_MAP)
            }
            signature {
                description(R.string.desc_json__values)
                arg(JSON_OBJECT, NAME_JSON)
            }
        },
        searchKeywords = "Map,JSON"
    ),

    MAP__VALUES(".values", ::fnMapValues, ARRAY,
        signatures {
            signature {
                description(R.string.desc_map__values)
                arg(MAP, NAME_MAP)
            }
            signature {
                description(R.string.desc_json__values)
                arg(JSON_OBJECT, NAME_JSON)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Map,JSON"
    ),

    CONTAINS_KEY("containsKey", ::fnMapContainsKey, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_map__contains_key)
                arg(MAP, NAME_MAP)
                arg(ANY, NAME_KEY)
            }
            signature {
                description(R.string.desc_json__contains_key)
                arg(JSON_OBJECT, NAME_JSON)
                arg(ANY, NAME_KEY)
            }
        },
        searchKeywords = "Map,JSON"
    ),

    MAP__CONTAINS_KEY(".containsKey", ::fnMapContainsKey, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_map__contains_key)
                arg(MAP, NAME_MAP)
                arg(ANY, NAME_KEY)
            }
            signature {
                description(R.string.desc_json__contains_key)
                arg(JSON_OBJECT, NAME_JSON)
                arg(ANY, NAME_KEY)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Map,JSON"
    ),

    ASSOC_IN("assocIn", ::fnAssocIn, ANY,
        signatures {
            signature {
                description(R.string.desc_assoc_in)
                arg(ANY, NAME_COLLECTION)
                arg(STRING, NAME_PATH)
                arg(ANY, NAME_VALUE)
            }
            signature {
                description(R.string.desc_assoc_in)
                arg(ANY, NAME_COLLECTION)
                arg(SEQUENCE, NAME_PATH)
                arg(ANY, NAME_VALUE)
            }
        },
        searchKeywords = "Map,Array,List,JSON"
    ),

    UPDATE_IN("updateIn", ::fnUpdateIn, ANY,
        signatures {
            signature {
                description(R.string.desc_update_in)
                arg(ANY, NAME_COLLECTION)
                arg(STRING, NAME_PATH)
                arg(FUNCTION, NAME_FUNCTION)
            }
            signature {
                description(R.string.desc_update_in)
                arg(ANY, NAME_COLLECTION)
                arg(SEQUENCE, NAME_PATH)
                arg(FUNCTION, NAME_FUNCTION)
            }
        },
        searchKeywords = "Map,Array,List,JSON"
    ),


    IDENTICAL("===", ::fnIdentical, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_identical)
                arg(ANY, NAME_FORM_1)
                arg(ANY, NAME_FORM_2)
            }
        }
    ),

    NUMBER__DEC(".dec", ::fnNumberDec, NUMBER,
        signatures {
            signature {
                description(R.string.desc_number__dec)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Number",
    ),

    NUMBER__INC(".inc", ::fnNumberInc, NUMBER,
        signatures {
            signature {
                description(R.string.number__desc_inc)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Number",
    ),

    STRING__INDEX_OF(".indexOf", ::fnStringIndexOf, INT,
        signatures {
            signature {
                description(R.string.desc_sequence__index_of_0)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(ANY, NAME_ITEM)
            }
            signature {
                description(R.string.desc_sequence__index_of_1)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(ANY, NAME_ITEM)
                arg(NUMBER, NAME_INDEX)
            }
            signature {
                description(R.string.desc_string__index_of_0)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_SUBSTRING)
            }
            signature {
                description(R.string.desc_string__index_of)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_SUBSTRING)
                arg(NUMBER, NAME_INDEX)
            }
            signature {
                description(R.string.desc_string__index_of_0)
                arg(STRING, NAME_STRING)
                arg(CHAR, NAME_CHAR)
            }
            signature {
                description(R.string.desc_string__index_of)
                arg(STRING, NAME_STRING)
                arg(CHAR, NAME_CHAR)
                arg(NUMBER, NAME_INDEX)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,String"
    ),

    SEQUENCE__LAST(".last", ::fnSequenceLast, ANY,
        signatures {
            signature {
                description(R.string.desc_last_0)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_last_1)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String,Sequence"
    ),

    ANY__IS_JSON_ARRAY(".isJsonArray", ::fnAnyIsJsonArray, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_json_array)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "JSON",
    ),

    ANY__IS_JSON_ELEMENT(".isJsonElement", ::fnAnyIsJsonElement, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_json_element)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "JSON",
    ),

    ANY__IS_JSON_OBJECT(".isJsonObject", ::fnAnyIsJsonObject, BOOLEAN,
        signatures {
            signature {
                description(R.string.any__desc_is_json_object)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "JSON",
    ),

    ANY__IS_JSON_PRIMITIVE(".isJsonPrimitive", ::fnAnyIsJsonPrimitive, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_json_primitive)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "JSON",
    ),

    ANY__IS_LIST(".isList", ::fnAnyIsList, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_list)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    LOG("log", ::fnLog, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_log)
                arg(NUMBER, NAME_X)
                arg(NUMBER, NAME_BASE)
            }
        }
    ),

    ANY__IS_LONG(".isLong", ::fnAnyIsLong, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_long)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    STRING__TO_LOWER_CASE(".toLowerCase", ::fnStringToLowerCase, STRING,
        signatures {
            signature {
                description(R.string.desc_string__lower_case)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    ANY__IS_MAP(".isMap", ::fnAnyIsMap, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_map)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    STRING__MATCHES(".matches", ::fnStringMatches, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_matches_0)
                arg(STRING, NAME_STRING)
                arg(REGEX, NAME_REGEX)
            }
            signature {
                description(R.string.desc_matches_1)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_REGEX)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    MAX("max", ::fnMax, NUMBER,
        signatures {
            signature {
                description(R.string.desc_max)
                arg(NUMBER, NAME_NUMBERS, ArgModifier.VARARG)
            }
        }
    ),

    MIN("min", ::fnMin, NUMBER,
        signatures {
            signature {
                description(R.string.desc_min)
                arg(NUMBER, NAME_NUMBERS, ArgModifier.VARARG)
            }
        }
    ),

    NUMBER__IS_NEGATIVE(".isNegative", ::fnAnyIsNegative, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_number__is_negative)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Number"
    ),

    ANY__IS_NULL(".isNull", ::fnAnyIsNull, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_null)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ANY__IS_NOT_NULL(".isNotNull", ::fnAnyIsNotNull, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_not_null)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    NOT("not", ::fnNot, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_not)
                arg(ANY, NAME_OBJECT)
            }
        }
    ),

    NOT_EQUAL("!=", ::fnNotEqual, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_not_equals)
                arg(ANY, NAME_OBJECT)
                arg(ANY, NAME_OBJECT, ArgModifier.VARARG)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "not,equal",
    ),

    STRING__CHAR_AT(".charAt", ::fnStringCharAt, CHAR,
        signatures {
            signature {
                description(R.string.desc_string__char_at)
                arg(STRING, NAME_STRING)
                arg(NUMBER, NAME_INDEX)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String"
    ),

    NTH("nth", ::fnSequenceGet, ANY,
        signatures {
            signature {
                description(R.string.desc_sequence__get)
                arg(INDEXABLE, NAME_SEQUENCE)
                arg(NUMBER, NAME_INDEX)
            }
        }
    ),

    SEQUENCE__GET(".get", ::fnSequenceGet, ANY,
        signatures {
            signature {
                description(R.string.desc_sequence__get)
                arg(INDEXABLE, NAME_SEQUENCE)
                arg(NUMBER, NAME_INDEX)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "List,Array,Set"
    ),

    ASSOC_AT("assocAt", ::fnSequenceSetAt, SEQUENCE,
        signatures {
            signature {
                description(R.string.desc_sequence__set_at)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(NUMBER, NAME_INDEX)
                arg(ANY, NAME_VALUE)
            }
        },
        searchKeywords = "List,Array,Sequence"
    ),

    SEQUENCE__SET_AT(".setAt", ::fnSequenceSetAt, SEQUENCE,
        signatures {
            signature {
                description(R.string.desc_sequence__set_at)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(NUMBER, NAME_INDEX)
                arg(ANY, NAME_VALUE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "List,Array,Sequence"
    ),

    REMOVE_AT("removeAt", ::fnSequenceRemoveAt, SEQUENCE,
        signatures {
            signature {
                description(R.string.desc_sequence__remove_at)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(NUMBER, NAME_INDEX)
            }
        },
        searchKeywords = "List,Array,Sequence"
    ),

    SEQUENCE__REMOVE_AT(".removeAt", ::fnSequenceRemoveAt, SEQUENCE,
        signatures {
            signature {
                description(R.string.desc_sequence__remove_at)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(NUMBER, NAME_INDEX)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "List,Array,Sequence"
    ),

    INSERT_AT("insertAt", ::fnSequenceInsertAt, SEQUENCE,
        signatures {
            signature {
                description(R.string.desc_sequence__insert_at)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(NUMBER, NAME_INDEX)
                arg(ANY, NAME_VALUE)
            }
        },
        searchKeywords = "List,Array,Sequence"
    ),

    SEQUENCE__INSERT_AT(".insertAt", ::fnSequenceInsertAt, SEQUENCE,
        signatures {
            signature {
                description(R.string.desc_sequence__insert_at)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(NUMBER, NAME_INDEX)
                arg(ANY, NAME_VALUE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "List,Array,Sequence"
    ),

    NUMBER__IS_ODD(".isOdd", ::fnNumberIsOdd, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_number__is_odd)
                arg(LONG, NAME_INTEGER)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Number"
    ),

    OR("or", ::fnOr, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_or)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        }
    ),

    NUMBER__IS_POSITIVE(".isPositive", ::fnNumberIsPositive, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_number__is_positive)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Number"
    ),

    STRING__PARSE_BOOLEAN(".parseBoolean", ::fnStringParseBoolean, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_string__parse_boolean)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__PARSE_BYTE(".parseByte", ::fnStringParseByte, ANY,
        signatures {
            signature {
                description(R.string.desc_string__parse_byte)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__PARSE_DOUBLE(".parseDouble", ::fnStringParseDouble, ANY,
        signatures {
            signature {
                description(R.string.desc_string__parse_double)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__PARSE_FLOAT(".parseFloat", ::fnStringParseFloat, ANY,
        signatures {
            signature {
                description(R.string.desc_string__parse_float)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__PARSE_INT(".parseInt", ::fnStringParseInt, ANY,
        signatures {
            signature {
                description(R.string.desc_string__parse_int)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__PARSE_LONG(".parseLong", ::fnStringParseLong, ANY,
        signatures {
            signature {
                description(R.string.desc_string__parse_long)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__PARSE_SHORT(".parseShort", ::fnStringParseShort, ANY,
        signatures {
            signature {
                description(R.string.desc_string__parse_long)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    NUMBER__POW("pow", ::fnNumberPow, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_number__pow)
                arg(NUMBER, NAME_X)
                arg(NUMBER, NAME_Y)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Number,Math"
    ),

    PRINT("print", ::fnPrint, NULL,
        signatures {
            signature {
                description(R.string.desc_print)
                arg(ANY, NAME_OBJECT)
                arg(ANY, NAME_FORMAT_ARGS, ArgModifier.VARARG)
            }
        }
    ),

    PRINTLN("println", ::fnPrintln, NULL,
        signatures {
            signature {
                description(R.string.desc_println)
                arg(ANY, NAME_OBJECT)
                arg(ANY, NAME_FORMAT_ARGS, ArgModifier.VARARG)
            }
        }
    ),

    RAND("rand", ::fnRand, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_rand)
                arg(NUMBER, NAME_MAX, ArgModifier.OPTIONAL)
            }
        },
        searchKeywords = "Math,Random"
    ),

    RAND_INT("randInt", ::fnRandInt, LONG,
        signatures {
            signature {
                description(R.string.desc_rand_int_0)
                arg(NUMBER, NAME_MAX)
            }
            signature {
                description(R.string.desc_rand_int_1)
                arg(NUMBER, NAME_MIN)
                arg(NUMBER, NAME_MAX)
            }
        },
        searchKeywords = "Math,Random,Int,Long"
    ),

    RAND_CHOICE("randChoice", ::fnRandChoice, ANY,
        signatures {
            signature {
                description(R.string.desc_rand_choice)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_rand_choice)
                arg(SET, NAME_SET)
            }
        },
        searchKeywords = "Random,Array,List,Set,Sequence"
    ),

    SHUFFLE("shuffle", ::fnShuffle, ARRAY,
        signatures {
            signature {
                description(R.string.desc_shuffle)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_shuffle)
                arg(SET, NAME_SET)
            }
        },
        searchKeywords = "Random,Array,List,Set,Sequence"
    ),

    PARSE("parse", ::fnParse, ANY,
        signatures {
            signature {
                description(R.string.desc_parse_string)
                arg(STRING, NAME_LITERAL)
            }
        }
    ),

    STRING_EVAL(".eval", ::fnStringParseEval, ANY,
        signatures {
            signature {
                description(R.string.desc_string__parse_eval)
                arg(STRING, NAME_LITERAL)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String"
    ),

    STRING__IS_EMPTY(".isEmpty", ::fnStringIsEmpty, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_string__is_empty)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String"
    ),

    STRING__IS_NOT_EMPTY(".isNotEmpty", ::fnStringIsNotEmpty, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_string__is_not_empty)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String"
    ),

    STRING__LENGTH(".length", ::fnStringLength, INT,
        signatures {
            signature {
                description(R.string.desc_string__length)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String"
    ),

    COMPILE(".compile", ::fnCompile, PARSED_FORM,
        signatures {
            signature {
                description(R.string.desc_string__parse_eval)
                arg(STRING, NAME_LITERAL)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION
    ),

    REM("rem", ::fnRem, NUMBER,
        signatures {
            signature {
                description(R.string.desc_rem)
                arg(NUMBER, NAME_NUMBER)
                arg(NUMBER, NAME_DIVIDER)
            }
        }
    ),

    STRING__REPEAT(".repeat", ::fnStringRepeat, STRING,
        signatures {
            signature {
                description(R.string.desc_string__repeat)
                arg(STRING, NAME_STRING)
                arg(NUMBER, NAME_N)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__REPLACE_CHAR(".replaceChar", ::fnStringReplaceChar, STRING,
        signatures {
            signature {
                description(R.string.desc_string__replace_char)
                arg(STRING, NAME_STRING)
                arg(CHAR, NAME_OLD_CHAR)
                arg(CHAR, NAME_NEW_CHAR)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__REPLACE_FIRST_REGEX(".replaceFirstRegex", ::fnStringReplaceFirstRegex, STRING,
        signatures {
            signature {
                description(R.string.desc_string__replace_first_regex_0)
                arg(STRING, NAME_STRING)
                arg(REGEX, NAME_REGEX)
                arg(STRING, NAME_REPLACEMENT)
            }
            signature {
                description(R.string.desc_string__replace_first_regex_1)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_REGEX)
                arg(STRING, NAME_REPLACEMENT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__REPLACE_FIRST_STRING(".replaceFirstString", ::fnStringReplaceFirstString, STRING,
        signatures {
            signature {
                description(R.string.desc_string__replace_first_string)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_SUBSTRING)
                arg(STRING, NAME_REPLACEMENT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__REPLACE_REGEX(".replaceRegex", ::fnStringReplaceRegex, STRING,
        signatures {
            signature {
                description(R.string.desc_string__replace_regex)
                arg(STRING, NAME_STRING)
                arg(REGEX, NAME_REGEX)
                arg(STRING, NAME_REPLACEMENT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__REPLACE(".replace", ::fnStringReplace, STRING,
        signatures {
            signature {
                description(R.string.desc_string__replace)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_SUBSTRING)
                arg(STRING, NAME_REPLACEMENT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String"
    ),

    SEQUENCE__REST(".rest", ::fnSequenceRest, SEQUENCE,
        signatures {
            signature {
                description(R.string.desc_sequence__rest)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_string__rest)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,String"
    ),

    STRING__REVERSE(".reverse", ::fnStringReverse, ANY,
        signatures {
            signature {
                description(R.string.desc_sequence__reverse)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_string__reverse)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,String"
    ),

    ROUND("round", ::fnRound, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_round)
                arg(NUMBER, NAME_X)
            }
        }
    ),

    ANY__IS_SET(".isSet", ::fnAnyIsSet, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_set)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    NUMBER__SIGN(".sign", ::fnNumberSign, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_number__sign)
                arg(NUMBER, NAME_X)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Number",
    ),

    SQRT("sqrt", ::fnSqrt, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_sqrt)
                arg(NUMBER, NAME_X)
            }
        }
    ),

    SIN("sin", ::fnSin, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_sin)
                arg(NUMBER, NAME_X)
            }
        }
    ),

    SIZEABLE__SIZE(".size", ::fnSizeableSize, LONG,
        signatures {
            signature {
                description(R.string.desc_collection__size)
                arg(SIZEABLE, NAME_COLLECTION)
            }
            signature {
                description(R.string.desc_string__size)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String,Collection"
    ),

    STRING__SPLIT(".split", ::fnStringSplit, ARRAY,
        signatures {
            signature {
                description(R.string.desc_split_0)
                arg(STRING, NAME_STRING)
                arg(REGEX, NAME_REGEX)
            }
            signature {
                description(R.string.desc_split_1)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_REGEX)
            }
            signature {
                description(R.string.desc_split_2)
                arg(STRING, NAME_STRING)
                arg(REGEX, NAME_REGEX)
                arg(LONG, NAME_LIMIT)
            }
            signature {
                description(R.string.desc_split_3)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_REGEX)
                arg(LONG, NAME_LIMIT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String"
    ),

    STRING__SPLIT_LINES(".splitLines", ::fnStringSplitLines, ARRAY,
        signatures {
            signature {
                description(R.string.desc_split_lines)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String"
    ),

    STRING__STARTS_WITH(".startsWith", ::fnStringStartsWith, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_starts_with)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_PREFIX)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String"
    ),

    CONC("conc", ::fnStr, STRING,
        signatures {
            signature {
                description(R.string.desc_string__concatenate)
                arg(ANY, NAME_OBJECT, ArgModifier.VARARG)
            }
        }
    ),

    STR("str", ::fnStr, STRING,
        signatures {
            signature {
                description(R.string.desc_str)
                arg(ANY, NAME_OBJECT, ArgModifier.VARARG)
            }
        }
    ),

    ANY__IS_STRING(".isString", ::fnAnyIsString, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_string)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ARRAY__SUB_ARRAY(".subArray", ::fnArraySubArray, ARRAY,
        signatures {
            signature {
                description(R.string.desc_subarray_0)
                arg(ARRAY, NAME_ARRAY)
                arg(LONG, NAME_START)
            }
            signature {
                description(R.string.desc_subarray_1)
                arg(ARRAY, NAME_ARRAY)
                arg(LONG, NAME_START)
                arg(LONG, NAME_END)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    SUBSCRIBE(".subscribe", ::fnObservableSubscribe, SUBSCRIPTION,
        signatures {
            signature {
                description(R.string.desc_observer__subscribe)
                arg(OBSERVABLE, NAME_OBSERVABLE)
                arg(CALLBACK, NAME_OBSERVER_CALLBACK)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Observable,Subscription"
    ),

    UNSUBSCRIBE(".unsubscribe", ::fnSubscriptionUnsubscribe, OBSERVABLE,
        signatures {
            signature {
                description(R.string.desc_subscription__unsubscribe)
                arg(SUBSCRIPTION, NAME_SUBSCRIPTION)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Observable,Subscription"
    ),

    TIMER_OBSERVABLE("timerObservable", ::fnTimerObservable, OBSERVABLE,
        signatures {
            signature {
                description(R.string.desc_timer_observable)
                arg(NUMBER, NAME_PERIOD)
            }
        }
    ),

    PERIODIC_TIMER_OBSERVABLE("periodicTimerObservable", ::fnPeriodicTimerObservable, OBSERVABLE,
        signatures {
            signature {
                description(R.string.desc_periodic_timer_observable)
                arg(NUMBER, NAME_INITIAL_DELAY)
                arg(NUMBER, NAME_PERIOD)
            }
        }
    ),

    START_OBSERVING(".start", ::fnSubscriptionStart, SUBSCRIPTION,
        signatures {
            signature {
                description(R.string.desc_subscription__start)
                arg(SUBSCRIPTION, NAME_SUBSCRIPTION)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Observable,Subscription"
    ),

    PAUSE_OBSERVING(".pause", ::fnSubscriptionPause, SUBSCRIPTION,
        signatures {
            signature {
                description(R.string.desc_subscription__pause)
                arg(SUBSCRIPTION, NAME_SUBSCRIPTION)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Observable,Subscription"
    ),

    RESUME_OBSERVING(".resume", ::fnSubscriptionResume, SUBSCRIPTION,
        signatures {
            signature {
                description(R.string.desc_subscription__resume)
                arg(SUBSCRIPTION, NAME_SUBSCRIPTION)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Observable,Subscription"
    ),

    STOP_OBSERVING(".stop", ::fnSubscriptionStop, SUBSCRIPTION,
        signatures {
            signature {
                description(R.string.desc_subscription__stop)
                arg(SUBSCRIPTION, NAME_SUBSCRIPTION)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Observable,Subscription"
    ),

    STRING__SUBSTRING(".substring", ::fnStringSubstring, STRING,
        signatures {
            signature {
                description(R.string.desc_substring_0)
                arg(STRING, NAME_STRING)
                arg(LONG, NAME_START)
            }
            signature {
                description(R.string.desc_substring_1)
                arg(STRING, NAME_STRING)
                arg(LONG, NAME_START)
                arg(LONG, NAME_END)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__CONTAINS(".contains", ::fnStringContains, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_contains_0)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_SUBSTRING)
            }
            signature {
                description(R.string.desc_contains_1)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_SUBSTRING)
                arg(BOOLEAN, NAME_IS_CASE_IGNORED)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String"
    ),

    ANY__IS_SYMBOL(".isSymbol", ::fnAnyIsSymbol, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_is_symbol)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    STRING__TAIL(".tail", ::fnStringTail, STRING,
        signatures {
            signature {
                description(R.string.desc_tail)
                arg(STRING, NAME_STRING)
                arg(NUMBER, NAME_N)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String"
    ),

    STRING__TAKE(".take", ::fnStringTake, ANY,
        signatures {
            signature {
                description(R.string.desc_sequence__take)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(NUMBER, NAME_N)
            }
            signature {
                description(R.string.desc_string__take)
                arg(STRING, NAME_STRING)
                arg(NUMBER, NAME_N)
            }
            signature {
                description(R.string.desc_sequence__take)
                arg(NUMBER, NAME_N)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_string__take)
                arg(NUMBER, NAME_N)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,String"
    ),

    REDUCE("reduce", ::fnReduce, ANY,
        signatures {
            signature {
                description(R.string.desc_reduce_0)
                arg(FUNCTION, NAME_FUNCTION)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_reduce_1)
                arg(FUNCTION, NAME_FUNCTION)
                arg(ANY, NAME_INITIAL_VALUE)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_reduce_0)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(FUNCTION, NAME_FUNCTION)
            }
            signature {
                description(R.string.desc_reduce_1)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(ANY, NAME_INITIAL_VALUE)
                arg(FUNCTION, NAME_FUNCTION)
            }
        },
        searchKeywords = "Sequence,Array,List,fold,aggregate"
    ),

    SEQUENCE__REDUCE(".reduce", ::fnReduce, ANY,
        signatures {
            signature {
                description(R.string.desc_reduce_0)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(FUNCTION, NAME_FUNCTION)
            }
            signature {
                description(R.string.desc_reduce_1)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(ANY, NAME_INITIAL_VALUE)
                arg(FUNCTION, NAME_FUNCTION)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,fold,aggregate"
    ),

    FOLD("fold", ::fnReduce, ANY,
        signatures {
            signature {
                description(R.string.desc_reduce_1)
                arg(FUNCTION, NAME_FUNCTION)
                arg(ANY, NAME_INITIAL_VALUE)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_reduce_1)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(ANY, NAME_INITIAL_VALUE)
                arg(FUNCTION, NAME_FUNCTION)
            }
            signature {
                description(R.string.desc_reduce_0)
                arg(FUNCTION, NAME_FUNCTION)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
        },
        searchKeywords = "Sequence,Array,List,reduce,aggregate"
    ),

    SEQUENCE__FOLD(".fold", ::fnReduce, ANY,
        signatures {
            signature {
                description(R.string.desc_reduce_1)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(ANY, NAME_INITIAL_VALUE)
                arg(FUNCTION, NAME_FUNCTION)
            }
            signature {
                description(R.string.desc_reduce_0)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(FUNCTION, NAME_FUNCTION)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,reduce,aggregate"
    ),

    FIND("find", ::fnSequenceFind, ANY,
        signatures {
            signature {
                description(R.string.desc_sequence__find)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(FUNCTION, NAME_PREDICATE)
            }
            signature {
                description(R.string.desc_sequence__find)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(KEYWORD, NAME_KEY)
            }
            signature {
                description(R.string.desc_sequence__find)
                arg(SET, NAME_SET)
                arg(FUNCTION, NAME_PREDICATE)
            }
            signature {
                description(R.string.desc_sequence__find)
                arg(FUNCTION, NAME_PREDICATE)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__find)
                arg(KEYWORD, NAME_KEY)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__find)
                arg(FUNCTION, NAME_PREDICATE)
                arg(SET, NAME_SET)
            }
        },
        searchKeywords = "Sequence,Array,List,Set,search"
    ),

    SEQUENCE__FIND(".find", ::fnSequenceFind, ANY,
        signatures {
            signature {
                description(R.string.desc_sequence__find)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(FUNCTION, NAME_PREDICATE)
            }
            signature {
                description(R.string.desc_sequence__find)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(KEYWORD, NAME_KEY)
            }
            signature {
                description(R.string.desc_sequence__find)
                arg(SET, NAME_SET)
                arg(FUNCTION, NAME_PREDICATE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,Set,search"
    ),

    ANY_MATCH("any", ::fnSequenceAny, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_sequence__any)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(FUNCTION, NAME_PREDICATE)
            }
            signature {
                description(R.string.desc_sequence__any)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(KEYWORD, NAME_KEY)
            }
            signature {
                description(R.string.desc_sequence__any)
                arg(SET, NAME_SET)
                arg(FUNCTION, NAME_PREDICATE)
            }
            signature {
                description(R.string.desc_sequence__any)
                arg(FUNCTION, NAME_PREDICATE)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__any)
                arg(KEYWORD, NAME_KEY)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__any)
                arg(FUNCTION, NAME_PREDICATE)
                arg(SET, NAME_SET)
            }
        },
        searchKeywords = "Sequence,Array,List,Set,predicate"
    ),

    SEQUENCE__ANY(".any", ::fnSequenceAny, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_sequence__any)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(FUNCTION, NAME_PREDICATE)
            }
            signature {
                description(R.string.desc_sequence__any)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(KEYWORD, NAME_KEY)
            }
            signature {
                description(R.string.desc_sequence__any)
                arg(SET, NAME_SET)
                arg(FUNCTION, NAME_PREDICATE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,Set,predicate"
    ),

    ALL("all", ::fnSequenceAll, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_sequence__all)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(FUNCTION, NAME_PREDICATE)
            }
            signature {
                description(R.string.desc_sequence__all)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(KEYWORD, NAME_KEY)
            }
            signature {
                description(R.string.desc_sequence__all)
                arg(SET, NAME_SET)
                arg(FUNCTION, NAME_PREDICATE)
            }
            signature {
                description(R.string.desc_sequence__all)
                arg(FUNCTION, NAME_PREDICATE)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__all)
                arg(KEYWORD, NAME_KEY)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__all)
                arg(FUNCTION, NAME_PREDICATE)
                arg(SET, NAME_SET)
            }
        },
        searchKeywords = "Sequence,Array,List,Set,predicate"
    ),

    SEQUENCE__ALL(".all", ::fnSequenceAll, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_sequence__all)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(FUNCTION, NAME_PREDICATE)
            }
            signature {
                description(R.string.desc_sequence__all)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(KEYWORD, NAME_KEY)
            }
            signature {
                description(R.string.desc_sequence__all)
                arg(SET, NAME_SET)
                arg(FUNCTION, NAME_PREDICATE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,Set,predicate"
    ),

    COUNT("count", ::fnSequenceCount, LONG,
        signatures {
            signature {
                description(R.string.desc_collection__size)
                arg(SIZEABLE, NAME_COLLECTION)
            }
            signature {
                description(R.string.desc_string__size)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_sequence__count)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(FUNCTION, NAME_PREDICATE)
            }
            signature {
                description(R.string.desc_sequence__count)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(KEYWORD, NAME_KEY)
            }
            signature {
                description(R.string.desc_sequence__count)
                arg(SET, NAME_SET)
                arg(FUNCTION, NAME_PREDICATE)
            }
            signature {
                description(R.string.desc_sequence__count)
                arg(FUNCTION, NAME_PREDICATE)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__count)
                arg(KEYWORD, NAME_KEY)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__count)
                arg(FUNCTION, NAME_PREDICATE)
                arg(SET, NAME_SET)
            }
        },
        searchKeywords = "Sequence,Array,List,Set,size"
    ),

    SEQUENCE__COUNT(".count", ::fnSequenceCount, LONG,
        signatures {
            signature {
                description(R.string.desc_collection__size)
                arg(SIZEABLE, NAME_COLLECTION)
            }
            signature {
                description(R.string.desc_string__size)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_sequence__count)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(FUNCTION, NAME_PREDICATE)
            }
            signature {
                description(R.string.desc_sequence__count)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(KEYWORD, NAME_KEY)
            }
            signature {
                description(R.string.desc_sequence__count)
                arg(SET, NAME_SET)
                arg(FUNCTION, NAME_PREDICATE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,Set,size"
    ),

    SORT("sort", ::fnSequenceSort, SEQUENCE,
        signatures {
            signature {
                description(R.string.desc_sequence__sort)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__sort)
                arg(SET, NAME_SET)
            }
        },
        searchKeywords = "Sequence,Array,List,Set"
    ),

    SEQUENCE__SORT(".sort", ::fnSequenceSort, SEQUENCE,
        signatures {
            signature {
                description(R.string.desc_sequence__sort)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__sort)
                arg(SET, NAME_SET)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,Set"
    ),

    SORT_BY("sortBy", ::fnSequenceSortBy, SEQUENCE,
        signatures {
            signature {
                description(R.string.desc_sequence__sort_by)
                arg(FUNCTION, NAME_KEY_FN)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__sort_by)
                arg(KEYWORD, NAME_KEY)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__sort_by)
                arg(STRING, NAME_KEY)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__sort_by)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(FUNCTION, NAME_KEY_FN)
            }
            signature {
                description(R.string.desc_sequence__sort_by)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(KEYWORD, NAME_KEY)
            }
            signature {
                description(R.string.desc_sequence__sort_by)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(STRING, NAME_KEY)
            }
        },
        searchKeywords = "Sequence,Array,List,Map,sort"
    ),

    SEQUENCE__SORT_BY(".sortBy", ::fnSequenceSortBy, SEQUENCE,
        signatures {
            signature {
                description(R.string.desc_sequence__sort_by)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(FUNCTION, NAME_KEY_FN)
            }
            signature {
                description(R.string.desc_sequence__sort_by)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(KEYWORD, NAME_KEY)
            }
            signature {
                description(R.string.desc_sequence__sort_by)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(STRING, NAME_KEY)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,Map,sort"
    ),

    DISTINCT("distinct", ::fnSequenceDistinct, SEQUENCE,
        signatures {
            signature {
                description(R.string.desc_sequence__distinct)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
        },
        searchKeywords = "Sequence,Array,List,unique"
    ),

    SEQUENCE__DISTINCT(".distinct", ::fnSequenceDistinct, SEQUENCE,
        signatures {
            signature {
                description(R.string.desc_sequence__distinct)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,unique"
    ),

    GROUP_BY("groupBy", ::fnSequenceGroupBy, MAP,
        signatures {
            signature {
                description(R.string.desc_sequence__group_by)
                arg(FUNCTION, NAME_KEY_FN)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__group_by)
                arg(KEYWORD, NAME_KEY)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__group_by)
                arg(STRING, NAME_KEY)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__group_by)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(FUNCTION, NAME_KEY_FN)
            }
            signature {
                description(R.string.desc_sequence__group_by)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(KEYWORD, NAME_KEY)
            }
            signature {
                description(R.string.desc_sequence__group_by)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(STRING, NAME_KEY)
            }
        },
        searchKeywords = "Sequence,Array,List,Map,group"
    ),

    SEQUENCE__GROUP_BY(".groupBy", ::fnSequenceGroupBy, MAP,
        signatures {
            signature {
                description(R.string.desc_sequence__group_by)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(FUNCTION, NAME_KEY_FN)
            }
            signature {
                description(R.string.desc_sequence__group_by)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(KEYWORD, NAME_KEY)
            }
            signature {
                description(R.string.desc_sequence__group_by)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(STRING, NAME_KEY)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,Map,group"
    ),

    RANGE("range", ::fnRange, ARRAY,
        signatures {
            signature {
                description(R.string.desc_range_0)
                arg(NUMBER, NAME_END)
            }
            signature {
                description(R.string.desc_range_1)
                arg(NUMBER, NAME_START)
                arg(NUMBER, NAME_END)
            }
            signature {
                description(R.string.desc_range_2)
                arg(NUMBER, NAME_START)
                arg(NUMBER, NAME_END)
                arg(NUMBER, NAME_STEP)
            }
        },
        searchKeywords = "Sequence,Array,Number"
    ),

    SUM("sum", ::fnSequenceSum, NUMBER,
        signatures {
            signature {
                description(R.string.desc_sequence__sum)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__sum)
                arg(SET, NAME_SET)
            }
        },
        searchKeywords = "Sequence,Array,List,Set,Number,Math"
    ),

    SEQUENCE__SUM(".sum", ::fnSequenceSum, NUMBER,
        signatures {
            signature {
                description(R.string.desc_sequence__sum)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__sum)
                arg(SET, NAME_SET)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,Set,Number,Math"
    ),

    AVERAGE("average", ::fnSequenceAverage, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_sequence__average)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__average)
                arg(SET, NAME_SET)
            }
        },
        searchKeywords = "Sequence,Array,List,Set,Number,Math,mean"
    ),

    SEQUENCE__AVERAGE(".average", ::fnSequenceAverage, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_sequence__average)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__average)
                arg(SET, NAME_SET)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,Set,Number,Math,mean"
    ),

    JOIN("join", ::fnSequenceJoin, STRING,
        signatures {
            signature {
                description(R.string.desc_sequence__join_0)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__join_1)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(ANY, NAME_SEPARATOR)
            }
        },
        searchKeywords = "Sequence,Array,List,String"
    ),

    SEQUENCE__JOIN(".join", ::fnSequenceJoin, STRING,
        signatures {
            signature {
                description(R.string.desc_sequence__join_0)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_sequence__join_1)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(ANY, NAME_SEPARATOR)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Sequence,Array,List,String"
    ),

    TAN("tan", ::fnTan, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_tan)
                arg(NUMBER, NAME_X)
            }
        }
    ),

    ANY__TO_ARRAY(".toArray", ::fnAnyToArray, ARRAY,
        signatures {
            signature {
                description(R.string.desc_to_array)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ANY__TO_BOOLEAN(".toBoolean", ::fnAnyToBoolean, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_to_boolean)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ANY__TO_BYTE(".toByte", ::fnAnyToByte, BYTE,
        signatures {
            signature {
                description(R.string.desc_number__to_byte)
                arg(NUMBER, NAME_NUMBER)
            }
            signature {
                description(R.string.desc_string__to_byte)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_json__to_byte)
                arg(JSON_PRIMITIVE, NAME_JSON_PRIMITIVE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ANY__TO_CHAR(".toChar", ::fnAnyToChar, CHAR,
        signatures {
            signature {
                description(R.string.desc_any__to_char)
                arg(ANY, NAME_OBJECT)
            }
            signature {
                description(R.string.desc_json__to_char)
                arg(JSON_PRIMITIVE, NAME_JSON_PRIMITIVE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ANY__TO_DOUBLE(".toDouble", ::fnAnyToDouble, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_number__to_double)
                arg(NUMBER, NAME_NUMBER)
            }
            signature {
                description(R.string.desc_string__to_double)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_json__to_double)
                arg(JSON_PRIMITIVE, NAME_JSON_PRIMITIVE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ANY__TO_FLOAT(".toFloat", ::fnAnyToFloat, FLOAT,
        signatures {
            signature {
                description(R.string.desc_number__to_float)
                arg(NUMBER, NAME_NUMBER)
            }
            signature {
                description(R.string.desc_string__to_float)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_json__to_float)
                arg(JSON_PRIMITIVE, NAME_JSON_PRIMITIVE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ANY__TO_INT(".toInt", ::fnAnyToInt, INT,
        signatures {
            signature {
                description(R.string.desc_number__to_int)
                arg(NUMBER, NAME_NUMBER)
            }
            signature {
                description(R.string.desc_string__to_int)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_json__to_int)
                arg(JSON_PRIMITIVE, NAME_JSON_PRIMITIVE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ANY__TO_JSON(".toJson", ::fnAnyToJson, JSON_OBJECT,
        signatures {
            signature {
                description(R.string.desc_string__to_json)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_map__to_json)
                arg(MAP, NAME_MAP)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Map"
    ),

    ANY__TO_LIST(".toList", ::fnAnyToList, LIST,
        signatures {
            signature {
                description(R.string.desc_any__to_list)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ANY__TO_LONG(".toLong", ::fnAnyToLong, LONG,
        signatures {
            signature {
                description(R.string.desc_number__to_long)
                arg(NUMBER, NAME_NUMBER)
            }
            signature {
                description(R.string.desc_string__to_long)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_json__to_long)
                arg(JSON_PRIMITIVE, NAME_JSON_PRIMITIVE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ANY__TO_MAP(".toMap", ::fnAnyToMap, MAP,
        signatures {
            signature {
                description(R.string.desc_any__to_map)
                arg(JSON_OBJECT, NAME_JSON)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ANY__TO_NUMBER(".toNumber", ::fnAnyToNumber, NUMBER,
        signatures {
            signature {
                description(R.string.desc_number__to_number)
                arg(NUMBER, NAME_NUMBER)
            }
            signature {
                description(R.string.desc_string__to_number)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_json__to_number)
                arg(JSON_PRIMITIVE, NAME_JSON_PRIMITIVE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ANY__TO_SET(".toSet", ::fnAnyToSet, SET,
        signatures {
            signature {
                description(R.string.desc_any__to_set)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ANY__TO_SHORT(".toShort", ::fnAnyToShort, SHORT,
        signatures {
            signature {
                description(R.string.desc_number__to_short)
                arg(NUMBER, NAME_NUMBER)
            }
            signature {
                description(R.string.desc_string__to_short)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_json__to_short)
                arg(JSON_PRIMITIVE, NAME_JSON_PRIMITIVE)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ANY__TO_STRING(".toString", ::fnAnyToString, STRING,
        signatures {
            signature {
                description(R.string.desc_any__to_string)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    ANY__IS_TRUE(".isTrue", ::fnAnyIsTrue, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_true)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
    ),

    NUMBER__IS_ZERO(".isZero", ::fnNumberIsZero, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_number__is_zero)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "Number",
    ),

    STRING__TO_UPPER_CASE(".toUpperCase", ::fnStringToUpperCase, STRING,
        signatures {
            signature {
                description(R.string.desc_upper_case)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__TRIM(".trim", ::fnStringTrim, STRING,
        signatures {
            signature {
                description(R.string.desc_string__trim)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__TRIM_START(".trimStart", ::fnStringTrimStart, STRING,
        signatures {
            signature {
                description(R.string.desc_string__trimStart)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    STRING__TRIM_END(".trimEnd", ::fnStringTrimEnd, STRING,
        signatures {
            signature {
                description(R.string.desc_string__trimEnd)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.KOTLIN_FUNCTION,
        searchKeywords = "String",
    ),

    XOR("xor", ::fnXor, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_xor)
                arg(ANY, NAME_FORMS, ArgModifier.VARARG)
            }
        }
    ),

    // =============================================================================================
    //  DEPRECATED
    // =============================================================================================

    @Deprecated("")
    JSON_GET(".get", ::fnJsonGet, ANY,
        signatures {
            signature {
                description(R.string.desc_json__get_0)
                arg(JSON_OBJECT, NAME_JSON)
                arg(ANY, NAME_KEY)
            }
            signature {
                description(R.string.desc_json__get_1)
                arg(JSON_OBJECT, NAME_JSON)
                arg(ANY, NAME_KEY)
                arg(ANY, NAME_FALLBACK)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    REPEAT_STRING("repeatString", ::fnStringRepeat, STRING,
        signatures {
            signature {
                description(R.string.desc_string__repeat)
                arg(STRING, NAME_STRING)
                arg(NUMBER, NAME_N)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    REPLACE_STRING("replace", ::fnStringReplace, STRING,
        signatures {
            signature {
                description(R.string.desc_string__replace)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_SUBSTRING)
                arg(STRING, NAME_REPLACEMENT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION
    ),

    @Deprecated("")
    REST("rest", ::fnSequenceRest, SEQUENCE,
        signatures {
            signature {
                description(R.string.desc_sequence__rest)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_string__rest)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    REVERSE("reverse", ::fnStringReverse, ANY,
        signatures {
            signature {
                description(R.string.desc_sequence__reverse)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_string__reverse)
                arg(STRING, NAME_STRING)
            }
        },
        searchKeywords = "Sequence,Array,List,String"
    ),

    @Deprecated("")
    REPLACE_CHAR("replaceChar", ::fnStringReplaceChar, STRING,
        signatures {
            signature {
                description(R.string.desc_string__replace_char)
                arg(STRING, NAME_STRING)
                arg(CHAR, NAME_OLD_CHAR)
                arg(CHAR, NAME_NEW_CHAR)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    REPLACE_FIRST_REGEX("replaceFirstRegex", ::fnStringReplaceFirstRegex, STRING,
        signatures {
            signature {
                description(R.string.desc_string__replace_first_regex_0)
                arg(STRING, NAME_STRING)
                arg(REGEX, NAME_REGEX)
                arg(STRING, NAME_REPLACEMENT)
            }
            signature {
                description(R.string.desc_string__replace_first_regex_1)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_REGEX)
                arg(STRING, NAME_REPLACEMENT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    REPLACE_FIRST_STRING("replaceFirstString", ::fnStringReplaceFirstString, STRING,
        signatures {
            signature {
                description(R.string.desc_string__replace_first_string)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_SUBSTRING)
                arg(STRING, NAME_REPLACEMENT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    REPLACE_REGEX("replaceRegex", ::fnStringReplaceRegex, STRING,
        signatures {
            signature {
                description(R.string.desc_string__replace_regex)
                arg(STRING, NAME_STRING)
                arg(REGEX, NAME_REGEX)
                arg(STRING, NAME_REPLACEMENT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    CHAR_AT("charAt", ::fnStringCharAt, CHAR,
        signatures {
            signature {
                description(R.string.desc_string__char_at)
                arg(STRING, NAME_STRING)
                arg(NUMBER, NAME_INDEX)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_POS("isPositive", ::fnNumberIsPositive, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_number__is_positive)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    POW("pow", ::fnNumberPow, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_number__pow)
                arg(NUMBER, NAME_X)
                arg(NUMBER, NAME_Y)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    PARSE_BOOLEAN("parseBoolean", ::fnStringParseBoolean, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_string__parse_boolean)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    PARSE_BYTE("parseByte", ::fnStringParseByte, ANY,
        signatures {
            signature {
                description(R.string.desc_string__parse_byte)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    PARSE_DOUBLE("parseDouble", ::fnStringParseDouble, ANY,
        signatures {
            signature {
                description(R.string.desc_string__parse_double)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    PARSE_FLOAT("parseFloat", ::fnStringParseFloat, ANY,
        signatures {
            signature {
                description(R.string.desc_string__parse_float)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    PARSE_INT("parseInt", ::fnStringParseInt, ANY,
        signatures {
            signature {
                description(R.string.desc_string__parse_int)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    PARSE_LONG("parseLong", ::fnStringParseLong, ANY,
        signatures {
            signature {
                description(R.string.desc_string__parse_long)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_MAP("isMap", ::fnAnyIsMap, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_map)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    MATCHES("matches", ::fnStringMatches, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_matches_0)
                arg(STRING, NAME_STRING)
                arg(REGEX, NAME_REGEX)
            }
            signature {
                description(R.string.desc_matches_1)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_REGEX)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_NEGATIVE("isNegative", ::fnAnyIsNegative, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_number__is_negative)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_NULL("isNull", ::fnAnyIsNull, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_null)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_NOT_NULL("isNotNull", ::fnAnyIsNotNull, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_not_null)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_EMPTY("isEmpty", ::fnSizableIsEmpty, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_is_empty_0)
                arg(SIZEABLE, NAME_COLLECTION)
            }
            signature {
                description(R.string.desc_is_empty_1)
                arg(STRING, NAME_STRING)
            }
        }
    ), // TODO

    @Deprecated("")
    IS_NOT_EMPTY("isNotEmpty", ::fnSizableIsNotEmpty, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_is_not_empty_0)
                arg(SIZEABLE, NAME_COLLECTION)
            }
            signature {
                description(R.string.desc_is_not_empty_1)
                arg(STRING, NAME_STRING)
            }
        }
    ), // TODO

    @Deprecated("")
    IS_JSON_ARRAY("isJsonArray", ::fnAnyIsJsonArray, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_json_array)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_JSON_ELEMENT("isJsonElement", ::fnAnyIsJsonElement, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_json_element)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_JSON_OBJECT("isJsonObject", ::fnAnyIsJsonObject, BOOLEAN,
        signatures {
            signature {
                description(R.string.any__desc_is_json_object)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_JSON_PRIMITIVE("isJsonPrimitive", ::fnAnyIsJsonPrimitive, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_json_primitive)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_LIST("isList", ::fnAnyIsList, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_list)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    INDEX_OF("indexOf", ::fnStringIndexOf, INT,
        signatures {
            signature {
                description(R.string.desc_sequence__index_of_0)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(ANY, NAME_ITEM)
            }
            signature {
                description(R.string.desc_sequence__index_of_1)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(ANY, NAME_ITEM)
                arg(NUMBER, NAME_INDEX)
            }
            signature {
                description(R.string.desc_string__index_of_0)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_SUBSTRING)
            }
            signature {
                description(R.string.desc_string__index_of)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_SUBSTRING)
                arg(NUMBER, NAME_INDEX)
            }
            signature {
                description(R.string.desc_string__index_of_0)
                arg(STRING, NAME_STRING)
                arg(CHAR, NAME_CHAR)
            }
            signature {
                description(R.string.desc_string__index_of)
                arg(STRING, NAME_STRING)
                arg(CHAR, NAME_CHAR)
                arg(NUMBER, NAME_INDEX)
            }
        },
        searchKeywords = "Sequence,Array,List,String"
    ),

    @Deprecated("")
    DEC("dec", ::fnNumberDec, NUMBER,
        signatures {
            signature {
                description(R.string.desc_number__dec)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    INC("inc", ::fnNumberInc, NUMBER,
        signatures {
            signature {
                description(R.string.number__desc_inc)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_LONG("isLong", ::fnAnyIsLong, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_long)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_LOWER_CASE("toLowerCase", ::fnStringToLowerCase, STRING,
        signatures {
            signature {
                description(R.string.desc_string__lower_case)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    LAST("last", ::fnSequenceLast, ANY,
        signatures {
            signature {
                description(R.string.desc_last_0)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_last_1)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION
    ),

    @Deprecated("")
    SIGN("sign", ::fnNumberSign, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_number__sign)
                arg(NUMBER, NAME_X)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_SET("isSet", ::fnAnyIsSet, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_set)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),


    @Deprecated("")
    IS_INSTANCE("isInstance", ::fnIsInstance, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_is_instance)
                arg(ANY, NAME_OBJECT)
                arg(ANY, NAME_CLASS)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),




    @Deprecated("")
    IS_ARRAY("isArray", ::fnAnyIsArray, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_array)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    BLANK("isBlank", ::fnStringIsBlank, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_string__is_blank)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_BOOLEAN("isBoolean", ::fnAnyIsBoolean, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__boolean)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    SHR("shr", ::fnNumberShr, LONG,
        signatures {
            signature {
                description(R.string.desc_number__shr)
                arg(NUMBER, NAME_NUMBER)
                arg(INT, NAME_N)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    SHL("shl", ::fnNumberShl, LONG,
        signatures {
            signature {
                description(R.string.desc_number__shl)
                arg(NUMBER, NAME_NUMBER)
                arg(INT, NAME_N)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    SIZE("size", ::fnSizeableSize, LONG,
        signatures {
            signature {
                description(R.string.desc_collection__size)
                arg(SIZEABLE, NAME_COLLECTION)
            }
            signature {
                description(R.string.desc_string__size)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    SPLIT("split", ::fnStringSplit, ARRAY,
        signatures {
            signature {
                description(R.string.desc_split_0)
                arg(STRING, NAME_STRING)
                arg(REGEX, NAME_REGEX)
            }
            signature {
                description(R.string.desc_split_1)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_REGEX)
            }
            signature {
                description(R.string.desc_split_2)
                arg(STRING, NAME_STRING)
                arg(REGEX, NAME_REGEX)
                arg(LONG, NAME_LIMIT)
            }
            signature {
                description(R.string.desc_split_3)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_REGEX)
                arg(LONG, NAME_LIMIT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    SPLIT_LINES("splitLines", ::fnStringSplitLines, ARRAY,
        signatures {
            signature {
                description(R.string.desc_split_lines)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    STARTS_WITH("startsWith", ::fnStringStartsWith, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_starts_with)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_PREFIX)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    SUB_ARRAY("subArray", ::fnArraySubArray, ARRAY,
        signatures {
            signature {
                description(R.string.desc_subarray_0)
                arg(ARRAY, NAME_ARRAY)
                arg(LONG, NAME_START)
            }
            signature {
                description(R.string.desc_subarray_1)
                arg(ARRAY, NAME_ARRAY)
                arg(LONG, NAME_START)
                arg(LONG, NAME_END)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    TAKE("take", ::fnStringTake, ANY,
        signatures {
            signature {
                description(R.string.desc_sequence__take)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(NUMBER, NAME_N)
            }
            signature {
                description(R.string.desc_string__take)
                arg(STRING, NAME_STRING)
                arg(NUMBER, NAME_N)
            }
            signature {
                description(R.string.desc_sequence__take)
                arg(NUMBER, NAME_N)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_string__take)
                arg(NUMBER, NAME_N)
                arg(STRING, NAME_STRING)
            }
        },
        searchKeywords = "Sequence,Array,List,String"
    ),

    @Deprecated("")
    TAIL("tail", ::fnStringTail, STRING,
        signatures {
            signature {
                description(R.string.desc_tail)
                arg(STRING, NAME_STRING)
                arg(NUMBER, NAME_N)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_SYMBOL("isSymbol", ::fnAnyIsSymbol, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_is_symbol)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_ARRAY("toArray", ::fnAnyToArray, ARRAY,
        signatures {
            signature {
                description(R.string.desc_to_array)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_BOOLEAN("toBoolean", ::fnAnyToBoolean, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_to_boolean)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_BYTE("toByte", ::fnAnyToByte, BYTE,
        signatures {
            signature {
                description(R.string.desc_number__to_byte)
                arg(NUMBER, NAME_NUMBER)
            }
            signature {
                description(R.string.desc_string__to_byte)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_json__to_byte)
                arg(JSON_PRIMITIVE, NAME_JSON_PRIMITIVE)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_CHAR("toChar", ::fnAnyToChar, CHAR,
        signatures {
            signature {
                description(R.string.desc_any__to_char)
                arg(ANY, NAME_OBJECT)
            }
            signature {
                description(R.string.desc_json__to_char)
                arg(JSON_PRIMITIVE, NAME_JSON_PRIMITIVE)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_DOUBLE("toDouble", ::fnAnyToDouble, DOUBLE,
        signatures {
            signature {
                description(R.string.desc_number__to_double)
                arg(NUMBER, NAME_NUMBER)
            }
            signature {
                description(R.string.desc_string__to_double)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_json__to_double)
                arg(JSON_PRIMITIVE, NAME_JSON_PRIMITIVE)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_FLOAT("toFloat", ::fnAnyToFloat, FLOAT,
        signatures {
            signature {
                description(R.string.desc_number__to_float)
                arg(NUMBER, NAME_NUMBER)
            }
            signature {
                description(R.string.desc_string__to_float)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_json__to_float)
                arg(JSON_PRIMITIVE, NAME_JSON_PRIMITIVE)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_INT("toInt", ::fnAnyToInt, INT,
        signatures {
            signature {
                description(R.string.desc_number__to_int)
                arg(NUMBER, NAME_NUMBER)
            }
            signature {
                description(R.string.desc_string__to_int)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_json__to_int)
                arg(JSON_PRIMITIVE, NAME_JSON_PRIMITIVE)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_JSON("toJson", ::fnAnyToJson, JSON_OBJECT,
        signatures {
            signature {
                description(R.string.desc_string__to_json)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_map__to_json)
                arg(MAP, NAME_MAP)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_LIST("toList", ::fnAnyToList, LIST,
        signatures {
            signature {
                description(R.string.desc_any__to_list)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_LONG("toLong", ::fnAnyToLong, LONG,
        signatures {
            signature {
                description(R.string.desc_number__to_long)
                arg(NUMBER, NAME_NUMBER)
            }
            signature {
                description(R.string.desc_string__to_long)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_json__to_long)
                arg(JSON_PRIMITIVE, NAME_JSON_PRIMITIVE)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    SUBSTRING("substring", ::fnStringSubstring, STRING,
        signatures {
            signature {
                description(R.string.desc_substring_0)
                arg(STRING, NAME_STRING)
                arg(LONG, NAME_START)
            }
            signature {
                description(R.string.desc_substring_1)
                arg(STRING, NAME_STRING)
                arg(LONG, NAME_START)
                arg(LONG, NAME_END)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_STRING("isString", ::fnAnyIsString, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_string)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    CONTAINS("contains", ::fnStringContains, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_contains_0)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_SUBSTRING)
            }
            signature {
                description(R.string.desc_contains_1)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_SUBSTRING)
                arg(BOOLEAN, NAME_IS_CASE_IGNORED)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_MAP("toMap", ::fnAnyToMap, MAP,
        signatures {
            signature {
                description(R.string.desc_any__to_map)
                arg(JSON_OBJECT, NAME_JSON)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_NUMBER("toNumber", ::fnAnyToNumber, NUMBER,
        signatures {
            signature {
                description(R.string.desc_number__to_number)
                arg(NUMBER, NAME_NUMBER)
            }
            signature {
                description(R.string.desc_string__to_number)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_json__to_number)
                arg(JSON_PRIMITIVE, NAME_JSON_PRIMITIVE)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_SET("toSet", ::fnAnyToSet, SET,
        signatures {
            signature {
                description(R.string.desc_any__to_set)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_SHORT("toShort", ::fnAnyToShort, SHORT,
        signatures {
            signature {
                description(R.string.desc_number__to_short)
                arg(NUMBER, NAME_NUMBER)
            }
            signature {
                description(R.string.desc_string__to_short)
                arg(STRING, NAME_STRING)
            }
            signature {
                description(R.string.desc_json__to_short)
                arg(JSON_PRIMITIVE, NAME_JSON_PRIMITIVE)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_STRING("toString", ::fnAnyToString, STRING,
        signatures {
            signature {
                description(R.string.desc_any__to_string)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_TRUE("isTrue", ::fnAnyIsTrue, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_true)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TO_UPPER_CASE("toUpperCase", ::fnStringToUpperCase, STRING,
        signatures {
            signature {
                description(R.string.desc_upper_case)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_ZERO("isZero", ::fnNumberIsZero, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_number__is_zero)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TRIM("trim", ::fnStringTrim, STRING,
    signatures {
        signature {
            description(R.string.desc_string__trim)
            arg(STRING, NAME_STRING)
        }
    },
    style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TRIM_START("trimStart", ::fnStringTrimStart, STRING,
    signatures {
        signature {
            description(R.string.desc_string__trimStart)
            arg(STRING, NAME_STRING)
        }
    },
    style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_BYTE("isByte", ::fnAnyIsByte, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_byte)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    CHOP("chop", ::fnStringChop, STRING,
        signatures {
            signature {
                description(R.string.desc_string__chop)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    CAPITALIZE("capitalize", ::fnStringCapitalize, STRING,
        signatures {
            signature {
                description(R.string.desc_string__capitalize)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    BUT_LAST("butLast", ::fnStringButlast, STRING,
        signatures {
            signature {
                description(R.string.desc_string__butlast)
                arg(NUMBER, NAME_N)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_DOUBLE("isDouble", ::fnAnyIsDoubleQ, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_double)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    DROP("drop", ::fnStringDrop, ANY,
        signatures {
            signature {
                description(R.string.desc_sequence__drop)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(NUMBER, NAME_N)
            }
            signature {
                description(R.string.desc_string__drop)
                arg(STRING, NAME_STRING)
                arg(NUMBER, NAME_N)
            }
            signature {
                description(R.string.desc_sequence__drop)
                arg(NUMBER, NAME_N)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_string__drop)
                arg(NUMBER, NAME_N)
                arg(STRING, NAME_STRING)
            }
        },
        searchKeywords = "Sequence,Array,List,String",
    ),

    DROP_LAST("dropLast", ::fnStringDropLast, ANY,
        signatures {
            signature {
                description(R.string.desc_sequence__drop_last)
                arg(SEQUENCE, NAME_SEQUENCE)
                arg(NUMBER, NAME_N)
            }
            signature {
                description(R.string.desc_string__drop_last)
                arg(STRING, NAME_STRING)
                arg(NUMBER, NAME_N)
            }
            signature {
                description(R.string.desc_sequence__drop_last)
                arg(NUMBER, NAME_N)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_string__drop_last)
                arg(NUMBER, NAME_N)
                arg(STRING, NAME_STRING)
            }
        },
        searchKeywords = "Sequence,Array,List,String",
    ),

    @Deprecated("")
    ENDS_WITH("endsWith", ::fnStringEndsWith, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_string__ends_with)
                arg(STRING, NAME_STRING)
                arg(STRING, NAME_POSTFIX)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_EVEN("isEven", ::fnNumberIsEven, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_number__is_even)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_ODD("isOdd", ::fnNumberIsOdd, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_number__is_even)
                arg(NUMBER, NAME_NUMBER)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    IS_FALSE("isFalse", ::fnAnyIsFalse, BOOLEAN,
        signatures {
            signature {
                description(R.string.desc_any__is_false)
                arg(ANY, NAME_OBJECT)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    FILTER("filter", ::fnCollectionFilter, LIST,
        signatures {
            signature {
                description(R.string.desc_sequence__filter)
                arg(SEQUENCE, NAME_COLLECTION)
                arg(FUNCTION, NAME_PREDICATE)
            }
            signature {
                description(R.string.desc_set__filter)
                arg(SET, NAME_COLLECTION)
                arg(FUNCTION, NAME_PREDICATE)
            }
            signature {
                description(R.string.desc_sequence__filter)
                arg(SEQUENCE, NAME_COLLECTION)
                arg(KEYWORD, NAME_KEY)
            }
            signature {
                description(R.string.desc_sequence__filter)
                arg(FUNCTION, NAME_PREDICATE)
                arg(SEQUENCE, NAME_COLLECTION)
            }
            signature {
                description(R.string.desc_set__filter)
                arg(FUNCTION, NAME_PREDICATE)
                arg(SET, NAME_COLLECTION)
            }
            signature {
                description(R.string.desc_sequence__filter)
                arg(KEYWORD, NAME_KEY)
                arg(SEQUENCE, NAME_COLLECTION)
            }
        },
        searchKeywords = "Sequence,Array,List,Set,Collection"
    ),

    @Deprecated("")
    FIRST("first", ::fnSequenceFirst, ANY,
        signatures {
            signature {
                description(R.string.desc_sequence__first)
                arg(SEQUENCE, NAME_SEQUENCE)
            }
            signature {
                description(R.string.desc_string__first)
                arg(STRING, NAME_STRING)
            }
        },
        style = FunctionStyle.DEPRECATED_FUNCTION,
    ),

    @Deprecated("")
    TRIM_END("trimEnd", ::fnStringTrimEnd, STRING,
    signatures {
        signature {
            description(R.string.desc_string__trimEnd)
            arg(STRING, NAME_STRING)
        }
    },
    style = FunctionStyle.DEPRECATED_FUNCTION,
    );

    override fun getId(): String = this.name

    override fun isMacroSpec(): Boolean = false

    override val functionName: Symbol?
        get() = Flx.getSymbol(this)

    // Callers of call() pass values, already evaluated — (map inc xs), a function held in a
    // variable — so they are not evaluated a second time (nor copied into a second array).
    override fun call(ctx: Ctx, args: Array<Any>): Any =
        Evaluator.callSpec(ctx, this, args)

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
                    Flx.internSymbol(FunctionSymbol(value))
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

        private fun replace(functionSpec: FunctionSpec): FunctionSpec =
            when (functionSpec) {
                BLANK -> STRING__IS_BLANK
                BUT_LAST -> STRING__BUT_LAST
                CAPITALIZE -> STRING__CAPITALIZE
                CHAR_AT -> STRING__CHAR_AT
                CHOP -> STRING__CHOP
                CONTAINS -> STRING__CONTAINS
                DEC -> NUMBER__DEC
                ENDS_WITH -> STRING__ENDS_WITH
                FIRST -> SEQUENCE__FIRST
                INC -> NUMBER__INC
                IS_ARRAY -> ANY__IS_ARRAY
                IS_BOOLEAN -> ANY__IS_BOOLEAN
                IS_BYTE -> ANY__IS_BYTE
                IS_DOUBLE -> ANY__IS_DOUBLE
                IS_EVEN -> NUMBER__IS_EVEN
                IS_FALSE -> ANY__IS_FALSE
                IS_INSTANCE -> IS
                IS_JSON_ARRAY -> ANY__IS_JSON_ARRAY
                IS_JSON_ELEMENT -> ANY__IS_JSON_ELEMENT
                IS_JSON_OBJECT -> ANY__IS_JSON_OBJECT
                IS_JSON_PRIMITIVE -> ANY__IS_JSON_PRIMITIVE
                IS_LIST -> ANY__IS_LIST
                IS_LONG -> ANY__IS_LONG
                IS_MAP -> ANY__IS_MAP
                IS_NEGATIVE -> NUMBER__IS_NEGATIVE
                IS_NULL -> ANY__IS_NULL
                IS_NOT_NULL -> ANY__IS_NOT_NULL
                IS_ODD -> NUMBER__IS_ODD
                IS_POS -> NUMBER__IS_POSITIVE
                IS_SET -> ANY__IS_SET
                IS_STRING -> ANY__IS_STRING
                IS_SYMBOL -> ANY__IS_SYMBOL
                IS_TRUE -> ANY__IS_TRUE
                IS_ZERO -> NUMBER__IS_ZERO
                JSON_GET -> JSON__GET
                LAST -> SEQUENCE__LAST
                MATCHES -> STRING__MATCHES
                PARSE_BOOLEAN -> STRING__PARSE_BOOLEAN
                PARSE_BYTE -> STRING__PARSE_DOUBLE
                PARSE_DOUBLE -> STRING__PARSE_DOUBLE
                PARSE_FLOAT -> STRING__PARSE_FLOAT
                PARSE_INT -> STRING__PARSE_INT
                PARSE_LONG -> STRING__PARSE_LONG
                POW -> NUMBER__POW
                REPEAT_STRING -> STRING__REPEAT
                REPLACE_CHAR -> STRING__REPLACE_CHAR
                REPLACE_FIRST_REGEX -> STRING__REPLACE_FIRST_REGEX
                REPLACE_FIRST_STRING -> STRING__REPLACE_FIRST_STRING
                REPLACE_REGEX -> STRING__REPLACE_REGEX
                REPLACE_STRING -> STRING__REPLACE
                REST -> SEQUENCE__REST
                SHL -> NUMBER__SHL
                SHR -> NUMBER__SHR
                SIGN -> NUMBER__SIGN
                SIZE -> SIZEABLE__SIZE
                SPLIT -> STRING__SPLIT
                SPLIT_LINES -> STRING__SPLIT_LINES
                STARTS_WITH -> STRING__STARTS_WITH
                SUB_ARRAY -> ARRAY__SUB_ARRAY
                SUBSTRING -> STRING__SUBSTRING
                TAIL -> STRING__TAIL
                TO_ARRAY -> ANY__TO_ARRAY
                TO_BOOLEAN -> ANY__TO_BOOLEAN
                TO_BYTE -> ANY__TO_BYTE
                TO_CHAR -> ANY__TO_CHAR
                TO_DOUBLE -> ANY__TO_DOUBLE
                TO_FLOAT -> ANY__TO_FLOAT
                TO_INT -> ANY__TO_INT
                TO_JSON -> ANY__TO_JSON
                TO_LIST -> ANY__TO_LIST
                TO_LONG -> ANY__TO_LONG
                TO_LOWER_CASE -> STRING__TO_LOWER_CASE
                TO_MAP -> ANY__TO_MAP
                TO_NUMBER -> ANY__TO_NUMBER
                TO_SHORT -> ANY__TO_SHORT
                TO_SET -> ANY__TO_SET
                TO_STRING -> ANY__TO_STRING
                TO_UPPER_CASE -> STRING__TO_UPPER_CASE
                TRIM -> STRING__TRIM
                TRIM_END -> STRING__TRIM_END
                TRIM_START -> STRING__TRIM_START
                else -> functionSpec
            }
    }
}