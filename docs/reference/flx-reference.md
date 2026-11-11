# FLX Lisp Reference

FLX is a small Lisp dialect for Kotlin and Android. This is the reference for the language and its built-in library: 42 macros and 246 functions, plus 79 deprecated names.

Contents

- [Part I: The language](#part-i-the-language)
    - [How to read this reference](#how-to-read-this-reference)
    - [A quick tour](#a-quick-tour)
    - [Syntax and literals](#syntax-and-literals)
    - [Evaluation](#evaluation)
    - [Calling styles](#calling-styles)
    - [Numbers](#numbers)
    - [Truth, equality and `nil`](#truth-equality-and-nil)
    - [Variables and scope](#variables-and-scope)
    - [Functions and closures](#functions-and-closures)
    - [Control flow and loops](#control-flow-and-loops)
    - [Collections](#collections)
    - [Strings](#strings)
    - [Errors](#errors)
    - [Quoting and `eval`](#quoting-and-eval)
    - [Help in the REPL](#help-in-the-repl)
    - [Running FLX from Kotlin](#running-flx-from-kotlin)
    - [Value types](#value-types)
- [Part II: Macros](#part-ii-macros)
- [Part III: Functions](#part-iii-functions)
- [Appendix A: Deprecated names](#appendix-a-deprecated-names)
- [Appendix B: Index](#appendix-b-index)
- [Appendix C: Known quirks](#appendix-c-known-quirks)

---

# Part I: The language

## How to read this reference

Every entry in Parts II and III shows its call form, the type it returns, a description and its parameters:

```clojure
(filter collection predicate)  ;; → List
```

| Notation | Meaning |
|---|---|
| `name` | A required argument. |
| `[name]` | An optional argument. |
| `name…` | The argument may be repeated (zero or more extra values). |
| `;; → Type` | The type of the result. |

A name can have several entries when it accepts different kinds of arguments, for example `.add` for arrays, lists and sets. Pick the entry whose parameter types match your values. Types are described in [Value types](#value-types).

Examples in this document were run against the library; the results shown are what the REPL prints. A `;; =>` comment shows a result.

## A quick tour

```clojure
(fun square [x] (* x x))                  ;; defines a function

(map square [1 2 3 4])                    ;; => (1 4 9 16)
(reduce + (map square [1 2 3 4]))         ;; => 30

(-> 3 (+ 4) (* 2))                        ;; => 14
(->> (range 10) (filter .isEven) (map (fn [x] (* x x))) (sum))   ;; => 120

(val person {:name "Ada" :age 36})        ;; an immutable value
(get person :name)                        ;; => "Ada"
(assoc person :age 37)                    ;; => {:name "Ada" :age 37}; person is unchanged

(let [a 1 b 2] (+ a b))                   ;; => 3

(loop [i 1 acc 0]
  (if-else (> i 100) acc (recur (+ i 1) (+ acc i))))   ;; => 5050

(try (/ 1 0) (catch err (str "failed: " err)))         ;; => "failed: Divided by zero."
(try-or (/ 1 0) 0)                                      ;; => 0
```

## Syntax and literals

A program is a sequence of **forms**. A form is a literal, a symbol, or a list. Whitespace separates forms; **commas are not whitespace**, so write `[1 2 3]`, not `[1, 2, 3]`.

| Literal | Examples | Notes |
|---|---|---|
| Integer | `42`, `-7` | An `Int`; the result of an operation that does not fit becomes a `Long`. |
| `Long`, `Byte` | `12L`, `12B` | A suffix selects the type. |
| Hexadecimal | `0xFF` | `255`. |
| Radix | `2r1010` | A number written in another base: `10`. |
| Double | `3.14`, `1e3` | There is no `f` suffix; use `.toFloat`. |
| String | `"hello\tworld"` | Escapes: `\n \t \r \b \f \0 \\ \' \" \uXXXX`. See [Strings](#strings). |
| Character | `'a'`, `'\n'` | A single `Char` between single quotes. |
| Keyword | `:name` | Evaluates to itself. Used as map keys. |
| Regular expression | `#"a+b"` | A `Regex`. |
| Boolean | `true`, `false` | |
| Nil | `nil` | The absence of a value. |
| Array | `[1 2 3]` | Elements are evaluated: `[(+ 1 2) 4]` is `[3 4]`. |
| Map | `{:a 1 :b 2}` | Keys and values are evaluated. |
| Set | `#{1 2 3}` | Elements are evaluated. |
| List | `(1 2 3)` | A list is a *call* when its first element can be called. See [Evaluation](#evaluation). |

**Comments** start with two semicolons and run to the end of the line:

```clojure
;; this is a comment
(+ 1 2)   ;; => 3
```

A single `;` does not start a comment.

**Symbols** name variables and functions. They may contain letters, digits and most punctuation: `my-var`, `if-else`, `->>`, `.toUpperCase`, `toggle!`. A name beginning with `:` is a keyword.

There is no `'x` shorthand for `quote`; write `(quote x)`. See [Quoting and `eval`](#quoting-and-eval).

## Evaluation

A **list** whose first element is a function, macro or a symbol that names one is a call: the first element is the operator and the rest are its arguments.

```clojure
(+ 1 2 3)            ;; => 6
(+ "a" "b")          ;; => "ab"
```

- A **function** call evaluates every argument, left to right, then calls the function.
- A **macro** call passes the argument forms *unevaluated*; the macro evaluates what it needs. `if`, `fun`, `let`, `loop`, `and`-like short-circuiting forms and `try` are macros.
- A list whose first element is not callable, such as `(1 2 3)`, evaluates to itself. To build a list of data that starts with a symbol, use `quote`.
- Arrays, maps and sets evaluate their elements and give back a new collection.
- A **symbol** evaluates to the value bound to it. An unbound symbol is an error:

```clojure
(undefined-fn 1)     ;; error: Symbol 'undefined-fn' does not have an assigned value.
```

A built-in used as a *value*, not in call position, evaluates to its specification, so `(reduce + 0 [1 2 3])` passes `+` as a function.

A function that is defined with the name of a built-in takes that name over for calls in the scope where it is defined:

```clojure
(fun sum [a b] (+ a b))
(sum 40 2)           ;; => 42
```

`and` and `or` stop as soon as the answer is known, so `(and false (/ 1 0))` is `false` and does not divide.

## Calling styles

Functions come in two styles, which you can tell apart by their names.

**Lisp-style** functions have plain names and take their arguments in order: `(filter xs pred)`, `(reduce + 0 xs)`, `(str "a" 1)`, `(map f xs)`.

**Method-style** functions have a leading dot and take the *receiver* as their first argument: `(.toUpperCase "abc")`, `(.isEven 4)`, `(.size [1 2])`. They read like Kotlin methods, and because the receiver comes first they fit in `->`:

```clojure
(.toUpperCase "abc")                    ;; => "ABC"
(-> [1 2 3] (.reverse) (.first))        ;; => 3
```

Many sequence functions exist in both styles (`reduce` and `.reduce`, `sort` and `.sort`) and several accept the collection and the function in either order: `(filter [1 2 3 4] .isEven)` and `(filter .isEven [1 2 3 4])` both give `(2 4)`.

A method-style function can be passed by name wherever a function is expected:

```clojure
(map .inc [1 2 3])            ;; => (2 3 4)
(map .toUpperCase ["a" "b"])  ;; => ("A" "B")
```

A **keyword** can be used as a function argument to select a key from maps:

```clojure
(map :a [{:a 2} {:a 1}])                ;; => (2 1)
(sortBy [{:a 2} {:a 1}] :a)             ;; => [{:a 1} {:a 2}]
```

A keyword is *not* callable in head position: `(:a m)` is not a lookup. Use `(get m :a)`.

## Numbers

- `+`, `-`, `*` and `%` take any number of arguments, `(- 5)` negates.
- `/` always returns a `Double`: `(/ 10 4)` is `2.5` and `(/ 10 5)` is `2.0`. Dividing by zero is an error (`Divided by zero.`).
- `rem` is the remainder: `(rem 7 3)` is `1`.
- **`%` is not a remainder.** `(% percentage total)` is a percentage of a total: `(% 50 200)` is `100.0`. Use `rem` for the remainder.
- An `Int` that overflows becomes a `Long`: `(+ 2147483647 1)` is `2147483648`.
- `round`, `floor`, `ceil` and `pow` return a `Double`: `(round 2.5)` is `2.0`, `(pow 2 10)` is `1024.0`.
- `sum` of an empty sequence is `0`; `average` of an empty sequence is `0.0`.

Convert with `.toInt`, `.toLong`, `.toDouble`, `.toFloat`, `.toNumber` and the `.parse…` functions. The `.parseInt` family returns `nil` when the text is not a number:

```clojure
(.parseInt "12")     ;; => 12
(.parseInt "x")      ;; => nil
(.toInt 1.7)         ;; => 1
```

## Truth, equality and `nil`

Only `false` and `nil` count as false in `if`, `when` and similar. Everything else, including `0`, `""` and `[]`, counts as true:

```clojure
(if 0 1 2)           ;; => 1
(if "" 1 2)          ;; => 1
(if nil 1 2)         ;; => 2
```

`and`, `or`, `not` and `xor` return `true` or `false`, not one of their operands: `(or nil 3)` is `true`. To pick the first usable value, use `coalesce` or `?:`:

```clojure
(?: nil nil 7)       ;; => 7
(coalesce nil 5)     ;; => 5
```

`==` compares values: `(== "a" "a")` and `(== [1 2] [1 2])` are `true`, and `(== 1 1L)` is `true`. An `Int` and a `Double` are different values, so `(== 1 1.0)` is `false`, and `1` and `1.0` are different map keys and set elements. `===` tests whether two values are the same object. `!=` is the negation of `==`.

`(get m key default)` returns `default` for a missing key; without one it returns `nil`.

## Variables and scope

`val` creates an immutable binding and `var` a mutable one. Defining a name twice in the same scope is an error, and so is assigning to a `val`:

```clojure
(val x 1)
(= x 2)         ;; error: Symbol 'x' is already defined to be an immutable variable. It cannot be defined again.

(var n 5)
(= n 6)         ;; n is 6
(++ n)          ;; 7
(-= n 2)        ;; 5
(update! n .inc)  ;; 6
```

`let` binds names for the duration of its body and may shadow outer names:

```clojure
(let [x 1] (let [x 2] x))      ;; => 2
```

`let` bindings are plain symbols; there is no destructuring (`[a b]` is not accepted as a binding target). The value forms are evaluated *before* the names are bound, so a binding cannot use an earlier one from the same `let`; nest the forms instead:

```clojure
(let [x 1] (let [y (+ x 1)] y))      ;; => 2
```

The in-place macros `=`, `++`, `--`, `+=`, `-=`, `update!` and `toggle!` change a variable that already exists.

## Functions and closures

`fun` defines a named function, `fn` an anonymous one. Functions are closures: they keep the variables of the scope they were created in.

```clojure
(fun greet [name] (str "Hello, " name "!"))
(greet "FLX")                          ;; => "Hello, FLX!"

(val mk (fn [n] (fn [x] (+ x n))))
((mk 3) 4)                             ;; => 7

(fun twice [f x] (f (f x)))
(twice .inc 1)                         ;; => 3
```

A call with the wrong number of arguments is an error: `'f' takes 2 args, not 1`. Each call uses stack space, so a loop that may run many iterations should use `recur` (below).

Functions are values and can be passed to `map`, `filter`, `reduce` and the like. There are no rest parameters; every parameter is required.

**Recursion.** A function may call itself by name. `recur` restarts the enclosing `loop` or function without growing the stack:

```clojure
(fun fact [n] (if-else (< n 2) 1 (* n (fact (- n 1)))))
(fact 10)                              ;; => 3628800

(fun count-down [n acc]
  (if-else (== n 0) acc (recur (- n 1) (+ acc n))))
(count-down 1000 0)                    ;; => 500500
```

`recur` must be the last thing the function or loop body evaluates (tail position).

`return` leaves the current function early:

```clojure
(fun f [x]
  (when (> x 0) (return "pos"))
  "non-pos")
(f 1)                                  ;; => "pos"
```

## Control flow and loops

```clojure
(if-else (> 1 2) "a" "b")                          ;; => "b"
(if true 1)                                        ;; => 1; with no else branch and a false condition the result is nil
(when (> 3 2) "yes")                               ;; => "yes"
(when (> 1 2) "a" (> 3 2) "b")                     ;; => "b" (condition/form pairs; the first true one wins)
(switch 2 1 "one" 2 "two" else "other")            ;; => "two"
(if-let [v (get {:a 1} :b)] v "none")              ;; => "none"
```

Loops:

```clojure
(var t 0)
(for i 0 5 1 (+= t i))                 ;; t is 15; for takes: variable start end step form…
                                       ;; and runs from start to end, both included
(forEach x [1 2 3] (println x))        ;; prints 1, 2, 3
(repeat 3 (println "x"))               ;; three times
(var i 0)
(while (< i 3) (++ i))                 ;; i is 3
```

`for` runs from `start` to `end` **inclusive**, adding `step` each time, so `(for i 0 3 1 …)` runs four times. The `step` is required.

`loop` binds names like `let` and is restarted by `recur`:

```clojure
(loop [i 1 acc 0]
  (if-else (> i 3) acc (recur (+ i 1) (+ acc i))))   ;; => 6
```

`->` and `->>` thread a value through a series of calls, as the first or as the last argument:

```clojure
(-> 3 (+ 4) (* 2))                                   ;; => 14
(->> [1 2 3] (map .inc) (reduce +))                  ;; => 9
```

## Collections

| Type | Literal | Notes |
|---|---|---|
| `Array` | `[1 2 3]` | Indexed, ordered. |
| `List` | `(quote (1 2 3))` | Ordered. Many functions such as `map` and `filter` return a list, printed as `(…)`. |
| `Map` | `{:a 1}` | Key-value pairs. Do not rely on the order of keys. |
| `Set` | `#{1 2}` | Unique elements. |

There are no `list`, `array` or `set` constructor functions. Write a literal, or convert with `.toList`, `.toArray` and `.toSet`:

```clojure
(.toSet [1 1 2])           ;; => #{1 2}
(.toList #{1 2})           ;; => (1 2)
```

Functions such as `assoc`, `dissoc`, `conj`, `assocIn`, `updateIn`, `assocAt` and `removeAt` return a **new** collection and leave their argument alone:

```clojure
(val m {:a 1})
(assoc m :b 2)             ;; => {:a 1 :b 2}
m                          ;; => {:a 1}

(conj [1 2] 3)             ;; => [1 2 3]
(assocIn {:a {:b 1}} [:a :b] 5)           ;; => {:a {:b 5}}
(updateIn {:a {:b 1}} [:a :b] .inc)       ;; => {:a {:b 2}}
(assocAt [1 2 3] 1 9)                     ;; => [1 9 3]
```

Reading: `nth` for arrays and lists, `get` for maps (`.get` for both):

```clojure
(nth [10 20 30] 1)         ;; => 20
(get {:a 1} :a)            ;; => 1
(.get [1 2 3] 1)           ;; => 2
```

`nth` with an index out of range is an error.

Sequence functions work on arrays, lists and sets, and several on strings:

```clojure
(filter [1 2 3 4] .isEven)      ;; => (2 4)
(map + [1 2] [10 20])           ;; => (11 22)
(reduce + 0 [1 2 3])            ;; => 6
(sort [3 1 2])                  ;; => [1 2 3]
(groupBy [1 2 3 4] .isEven)     ;; => {false [1 3] true [2 4]}
(range 5)                       ;; => [0 1 2 3 4]
(range 0 10 3)                  ;; => [0 3 6 9]
(take 2 [1 2 3])                ;; => [1 2]
(distinct [1 1 2])              ;; => [1 2]
(count [1 2 3])                 ;; => 3
```

`map` is a macro that accepts several sequences and calls the function with one element from each. `.keys`, `keys` and `vals` return arrays.

## Strings

Strings are written in double quotes and may contain the escapes `\n \t \r \b \f \0 \\ \' \" \uXXXX`.

**Templates.** `${…}` inside a string evaluates one FLX form and inserts its text. `\${` is a literal `${`.

```clojure
"Count: ${(+ 1 2)}"              ;; => "Count: 3"
```

`str` joins the text of any values; `nil` gives an empty string:

```clojure
(str "a" :k 'c' 1.5 nil true)    ;; => "a:kc1.5true"
(str "n=" 5)                     ;; => "n=5"
(format "%.2f" 3.14159)          ;; => "3.14"
(format "%5d|%-5s|" 42 "ab")     ;; => "   42|ab   |"
```

String methods take the string first: `(.substring "hello" 1 3)` is `"el"`, `(.split "a,b" ",")` is `["a" "b"]`, `(.replace "aXb" "X" "-")` is `"a-b"`. A method called with `nil` is an error, so guard values that may be `nil` with `?:`.

## Errors

Runtime errors carry a message. `try` evaluates its body and, when the last form is `(catch name handler…)`, binds the error **message as a string** to `name` and evaluates the handler. With no error, `try` returns the value of its last body form.

```clojure
(try (/ 1 0) (catch err (str "failed: " err)))    ;; => "failed: Divided by zero."
(try (nth [1] 3) (catch e e))                     ;; => "'nth' failed: index 3 is out of range for a size of 1"
```

`try-or` is the short form with a fallback value:

```clojure
(try-or (/ 1 0) 0)         ;; => 0
```

Typical messages name the function and the problem: `'f' takes 2 args, not 1`, `'/' needs a number as 'number*', got a string ("a")`, `Symbol 'x' does not have an assigned value.` Errors raised by Kotlin code behind a built-in are turned into FLX errors, so a program cannot crash the host.

## Quoting and `eval`

`quote` returns its argument unevaluated. `lazy` does the same, for code you evaluate later with `eval`:

```clojure
(quote (a b))                    ;; => (a b)
(quote [1 x])                    ;; => [1 x]
(eval (quote (+ 1 2)))           ;; => 3
```

Source text can be turned into forms and evaluated:

```clojure
(parse "(+ 1 2)")                ;; => (+ 1 2)
(eval (parse "(+ 1 2)"))         ;; => 3
(evalString "(* 6 7)")           ;; => 42
(.eval "(+ 1 2)")                ;; => 3
```

FLX code cannot call Java or Android APIs by reflection; the only functions available are the ones listed in this reference, and the ones a host app registers.

## Help in the REPL

`?` and `help` print the call form of a built-in:

```clojure
(? +)            ;; => (+  number*: Number) ⇨ Number
(? .toUpperCase) ;; => (.toUpperCase  string: String) ⇨ String
```

## Running FLX from Kotlin

Add the library, initialise the global context once, then evaluate source text:

```kotlin
import org.fuusio.flx.Flx
import org.fuusio.flx.Program
import org.fuusio.flx.core.toLiteral
import org.fuusio.flx.core.vm.Ctx

if (!Flx.isInitialized()) {
    Flx.ctx = Ctx().apply { program = Program() }
}

val result = Flx.eval("(reduce + [1 2 3 4])")
println(result.toLiteral())  // 10
```

For isolated sessions and deep recursion, run code in a `FlxVM`; see the project README.

## Value types

The type names used in the signatures:

| Type | Meaning |
|---|---|
| `Any` | Any value. |
| `Number`, `Int`, `Long`, `Short`, `Byte`, `Double`, `Float` | Numbers. |
| `Boolean`, `Char`, `String`, `Keyword`, `Regex` | Scalars and text. |
| `Array`, `List`, `Set`, `Map`, `Collection` | Collections. `Collection` is any of the first three. |
| `Sequence`, `Indexable`, `Sizeable` | Anything that can be iterated, indexed or counted: arrays, lists, sets (and, for some functions, strings). |
| `Function` | A function value: from `fn` or `fun`, a built-in, a keyword or a method-style name such as `.inc`. |
| `Symbol`, `FunctionFormSymbol` | A name; the name of a built-in. |
| `Callback`, `HttpCallback` | Code run when an event happens. |
| `Date`, `Color` | A point in time; a colour. |
| `Observable`, `Subscription` | Timers and event sources. |
| `JsonElement`, `JsonObject`, `JsonArray`, `JsonPrimitive` | JSON values. |
| `Null`, `Unit` | `nil`; "no result" (printed as `kotlin.Unit`). |
| `Info` | Help text. |



---

# Part II: Macros

Macros receive their arguments **unevaluated** and decide themselves what to evaluate, and when. That is what lets `if` skip the branch it does not take, and `fun` bind a name instead of looking it up.

## Defining functions and callbacks

### `callback`

```clojure
(callback code args…)  ;; → Callback
```

Creates a new `Callback` with the given **`code`** and optional callback **`parameters`**. Each parameter is defined as a `Symbol`. Each callback parameter can be referenced in the callbacks's code.

Parameters: `code` Any, `args` Symbol.

### `code`

```clojure
(code form…)  ;; → Code
```

Creates an instance of `Code` object. Code is a code block that contains zero or more forms. Each given **`form`** is executed one by one in the given order. Returns the evaluated value of the last executed **`form`**.

Parameters: `form` Any.

### `fn`

```clojure
(fn [param…] body…)  ;; → Function
```

Defines a new anonymous function with zero or more **`args`** and a function **`body`**. Each arg is defined as a `Symbol`.

Parameters: `args` Array, `body` Any, `form` Any.

### `fun`

```clojure
(fun name [param…] body…)  ;; → Symbol
```

Defines a new function with the given **`name`** symbol, zero or more **`args`** for function arguments, and a function **`body`** form. Each arg is defined as a `Symbol`

Parameters: `name` Symbol, `args` Array, `body` Any, `form` Any.

### `httpCallback`

```clojure
(httpCallback code)  ;; → HttpCallback
```

Creates a new `HttpCallback` with the given **`code`**. A `HttpCallback` instance has an implicit parameter `response` that can be referenced as in the callbacks's code. The parameter has type `HttpResult`.

Parameters: `code` Any.

### `method`

```clojure
(method name [param…] body…)  ;; → Symbol
```

Defines a new object method with the given **`name`** symbol, zero or more **`args`** for method arguments, and a method **`body`** form. Each arg is defined as a `Symbol`

Parameters: `name` Symbol, `args` Array, `body` Any, `form` Any.

### `proc`

```clojure
(proc args code)  ;; → Callback
```

Defines a new procedure with the given **`args`** array and **`code`**.

Parameters: `args` Array, `code` Any.

## Variables and assignment

### `++`

```clojure
(++ variable)  ;; → Number
```

Increases the number value of **`variable`** by 1

Parameters: `variable` Symbol.

### `+=`

```clojure
(+= variable value)  ;; → Any
```

Adds **`value`** to **`variable`** in place (`(= variable (+ variable value))`) and returns the new value.

Parameters: `variable` Symbol, `value` Any.

### `--`

```clojure
(-- variable)  ;; → Number
```

Decreases the number value of **`variable`** by 1

Parameters: `variable` Symbol.

### `-=`

```clojure
(-= variable value)  ;; → Number
```

Subtracts **`value`** from **`variable`** in place (`(= variable (- variable value))`) and returns the new value.

Parameters: `variable` Symbol, `value` Any.

### `=`

```clojure
(= variable value)  ;; → Any
```

Assigns the given **`value`** to a **`variable`**.

Parameters: `variable` Symbol, `value` Any.

### `let`

```clojure
(let [name value …] body…)  ;; → Any
```

Performs the value assignments defined in **`bindings`** and executes each **`form`** one by one. E.g. **`(let [x 1 y 2] (+ x y) (* x y)`**. The value of the last executed form is returned.

Parameters: `bindings` Any, `form` Any.

### `toggle!`

```clojure
(toggle! variable)  ;; → Boolean
```

Negates the boolean value of **`variable`** in place (`(= variable (not variable))`) and returns the new value.

Parameters: `variable` Symbol.

### `update!`

```clojure
(update! variable function args…)  ;; → Any
```

Updates **`variable`** in place by applying **`function`** to its current value and any additional **`args`** (`(= variable (function variable args...))`), returning the new value.

Parameters: `variable` Symbol, `function` Any, `args` Any.

### `val`

```clojure
(val name value)  ;; → Any
```

Creates an immutable value with the given **`symbol`** name and assigns the given **`value`** for it.

Parameters: `symbol` Symbol, `value` Any.

### `var`

```clojure
(var name [value])  ;; → Any
```

Creates an mutable variable with the given **`symbol`** name and assigns the optional **`value?`** for it.

Parameters: `symbol` Symbol, `value` Any.

## Conditionals and control flow

### `?:`

```clojure
(?: value value…)  ;; → Any
```

Evaluates arguments one by one and returns the first non-`nil` value, or `nil` if all are `nil`.

Parameters: `value` Any, `value` Any.

### `coalesce`

```clojure
(coalesce value value…)  ;; → Any
```

Evaluates arguments one by one and returns the first non-`nil` value, or `nil` if all are `nil`.

Parameters: `value` Any, `value` Any.

### `do`

```clojure
(do form…)  ;; → Any
```

Executes each **`form`** one by one in the given order. Returns the evaluated value of the last executed **`form`**.

Parameters: `form` Any.

### `if`

```clojure
(if condition trueBranch falseBranch)  ;; → Any
```

**Deprecated.** Still works; use `if-else` for the three-argument form.

If the **`condition`** evaluates to `true` then **`trueBranch`** is executed, otherwise **`falseBranch`**. Returns the value of the evaluated form.

Parameters: `condition` Any, `trueBranch` Any, `falseBranch` Any.

```clojure
(if condition trueBranch)  ;; → Any
```

If the **`condition`** evaluates to `true` then **`trueBranch`** is executed and the value from evaluation is returned.

Parameters: `condition` Any, `trueBranch` Any.

### `if-else`

```clojure
(if-else condition trueBranch falseBranch)  ;; → Any
```

If the **`condition`** evaluates to `true` then **`trueBranch`** is executed, otherwise **`falseBranch`**. Returns the value of the evaluated form.

Parameters: `condition` Any, `trueBranch` Any, `falseBranch` Any.

### `if-let`

```clojure
(if-let [name expression] then [else])  ;; → Any
```

Evaluates the binding in **`bindings`** `[symbol expr]` and, if the value is non-`nil` and not `false`, binds `symbol` and evaluates **`trueBranch`**; otherwise evaluates **`falseBranch`** (or returns `nil`).

Parameters: `bindings` Any, `trueBranch` Any, `falseBranch` Any.

### `return`

```clojure
(return)  ;; → Unit
```

Returns from executing the current code block without returning any value.

```clojure
(return value)  ;; → Unit
```

Returns from executing the current code block and return the optional **`value`**.

Parameters: `value` Any.

### `switch`

```clojure
(switch value case result … [else default])  ;; → Any
```

When function returns the evaluation result of the **`form`** for which the **`case`** value equals to the given **`argument`** value. If no equal case value exists, the evaluated value of `else` form is returned.

Parameters: `argument` Any, `case` Any, `form` Any.

### `when`

```clojure
(when condition result … )  ;; → Any
```

When function returns the evaluated value of **`form`** for the first **`condition`** that evaluates to value `true`.

Parameters: `condition` Any, `form` Any.

### `when-let`

```clojure
(when-let [name expression] body…)  ;; → Any
```

Evaluates the binding in **`bindings`** `[symbol expr]` and, if the value is non-`nil` and not `false`, binds `symbol` and evaluates each **`body`** **`form`**, returning the last value; otherwise returns `nil`.

Parameters: `bindings` Any, `body` Any, `form` Any.

## Loops and recursion

### `for`

```clojure
(for index start end step body…)  ;; → Any
```

Implements a for loop which executes the **`form`** for **`start`** to **`end`** indices. Both arguments **`start`** and **`end`** has to evaluate to a `Number`.

Parameters: `indexVar` Any, `start` Any, `end` Any, `step` Any, `form` Any, `form` Any.

### `forEach`

```clojure
(forEach name sequence body…)  ;; → Any
```

Iterates over the elements in the **`sequence`** and on each iteration assigns the next element to the **`symbol`** before evaluating the given **`form`**.

Parameters: `symbol` Symbol, `sequence` Any, `form` Any, `form` Any.

### `loop`

```clojure
(loop [name value …] body…)  ;; → Any
```

Binds **`bindings`** like `let` and evaluates each **`form`**. If the last form evaluates to **`(recur value*)`**, the bindings take those values and the forms are evaluated again, without using more stack. E.g. **`(loop [i 0 sum 0] (if-else (> i 10) sum (recur (+ i 1) (+ sum i))))`**.

Parameters: `bindings` Any, `form` Any.

### `recur`

```clojure
(recur form…)  ;; → Any
```

Evaluates each **`form`** and restarts the enclosing `loop`, or the enclosing function, with the results as its new binding or parameter values. Must be the value the loop or function body returns (tail position).

Parameters: `form` Any.

### `repeat`

```clojure
(repeat n body…)  ;; → Any
```

Executes the given **`form`** for **`n`** times. Argument **`n`** has to evaluate to a `Number`.

Parameters: `n` Any, `form` Any, `form` Any.

### `while`

```clojure
(while condition body…)  ;; → Any
```

Implements a while loop. First, the **`condition`** expression is evaluated, and if the result is true, then the **`form`** is evaluated.

Parameters: `condition` Any, `form` Any, `form` Any.

## Threading and mapping

### `->`

```clojure
(-> value form…)  ;; → Any
```

Threads **`value`** as the first argument through each **`form`** in sequence.

Parameters: `value` Any, `form` Any.

### `->>`

```clojure
(->> value form…)  ;; → Any
```

Threads **`value`** as the last argument through each **`form`** in sequence.

Parameters: `value` Any, `form` Any.

### `map`

```clojure
(map function sequence sequence…)  ;; → List
```

Returns a `List` consisting of the results of applying the given **`function`** on the set of nth items (0, 1, , … N) of each **`sequence`**.

Parameters: `function` Any, `sequence` Any, `sequence` Any.

## Error handling

### `try`

```clojure
(try body… (catch name handler…))  ;; → Any
```

Evaluates **`body`** forms and returns the last result. If an error occurs and a trailing `(catch err handler...)` form is present, binds `err` to the error message and evaluates `handler` forms; otherwise returns `nil`.

Parameters: `body` Any, `form` Any.

### `try-or`

```clojure
(try-or form [fallback])  ;; → Any
```

Evaluates **`form`** and returns its result, or evaluates and returns **`fallback`** (or `nil` if omitted) if an error occurs.

Parameters: `form` Any, `fallback` Any.

## Quoting

### `lazy`

```clojure
(lazy form)  ;; → Any
```

Returns the given **`form`** without evaluating it. A lazy form can be evaluated later on with `(eval form)` function.

Parameters: `form` Any.

### `quote`

```clojure
(quote form)  ;; → Any
```

Returns the given **`form`** without evaluating it.

Parameters: `form` Any.

## Help

### `?`

```clojure
(? symbol)  ;; → Info
```

Shows help for the function with the name specified by the given **`symbol`**.

Parameters: `symbol` FunctionFormSymbol.

### `help`

```clojure
(help symbol)  ;; → Info
```

Shows help for the function with the name specified by the given **`symbol`**.

Parameters: `symbol` FunctionFormSymbol.


---

# Part III: Functions

Functions evaluate all their arguments first. A function named with a leading dot, such as `.toUpperCase`, is *method-style*: its first argument is the receiver. See [Calling styles](#calling-styles).

## Numbers and math

### `%`

```clojure
(% percentage total)  ;; → Number
```

Calculates the proportionate part of the given **`total`** numeric value specified by the given **`percentage`**.

Parameters: `percentage` Number, `total` Number.

### `*`

```clojure
(* number…)  ;; → Number
```

Multiplication of the given **`number`** arguments.

Parameters: `number` Number.

### `+`

```clojure
(+ number…)  ;; → Number
```

Addition of the given **`number`** arguments.

Parameters: `number` Number.

### `-`

```clojure
(- number…)  ;; → Number
```

Subtraction of the given **`number`** arguments.

Parameters: `number` Number.

### `/`

```clojure
(/ number…)  ;; → Number
```

Division of the given **`number`** arguments.

Parameters: `number` Number.

### `abs`

```clojure
(abs x)  ;; → Number
```

Returns absolute value of number **`x`**.

Parameters: `x` Number.

### `acos`

```clojure
(acos x)  ;; → Double
```

Returns the acos of **`x`**.

Parameters: `x` Number.

### `asin`

```clojure
(asin x)  ;; → Double
```

Returns the asin of **`x`**.

Parameters: `x` Number.

### `atan`

```clojure
(atan x)  ;; → Double
```

Returns the atan of **`x`**.

Parameters: `x` Number.

### `atan2`

```clojure
(atan2 y x)  ;; → Double
```

Returns the angle `theta` in radians from the conversion of rectangular coordinates (**`x`**, **`y`**) to polar coordinates (`r`, `theta`).

Parameters: `y` Number, `x` Number.

### `ceil`

```clojure
(ceil x)  ;; → Double
```

Returns the least integer greater than or equal to **`x`**.

Parameters: `x` Number.

### `clamp`

```clojure
(clamp value min max)  ;; → Number
```

Clamps the given **`value`** to the inclusive range [**`min`**, **`max`**].

Parameters: `value` Number, `min` Number, `max` Number.

### `.coerceIn`

```clojure
(.coerceIn value min max)  ;; → Number
```

Clamps the given **`value`** to the inclusive range [**`min`**, **`max`**].

Parameters: `value` Number, `min` Number, `max` Number.

### `cos`

```clojure
(cos x)  ;; → Double
```

Returns the cos of **`x`**.

Parameters: `x` Number.

### `.dec`

```clojure
(.dec number)  ;; → Number
```

Returns a value that is **`number`** decreased by 1.

Parameters: `number` Number.

### `exp`

```clojure
(exp x)  ;; → Double
```

Computes Euler's number **e** raised to the power of the value **`x`**.

Parameters: `x` Number.

### `floor`

```clojure
(floor x)  ;; → Double
```

Returns the next lowest integer value, i.e., the last largest integer not greater than the **`x`** value, by rounding up value if necessary.

Parameters: `x` Number.

### `hypot`

```clojure
(hypot x y [z])  ;; → Double
```

Returns the Euclidean norm `sqrt(x² + y²)` (or `sqrt(x² + y² + z²)` when **`z`** is provided) without intermediate overflow or underflow.

Parameters: `x` Number, `y` Number, `z` Number.

### `.inc`

```clojure
(.inc number)  ;; → Number
```

Returns a value that is **`number`** increased by 1.

Parameters: `number` Number.

### `.isEven`

```clojure
(.isEven number)  ;; → Boolean
```

Tests if the given **`number`** is even.

Parameters: `number` Number.

### `.isNegative`

```clojure
(.isNegative number)  ;; → Boolean
```

Tests if the given **`number`** is negative.

Parameters: `number` Number.

### `.isOdd`

```clojure
(.isOdd integer)  ;; → Boolean
```

Tests if the given **`number`** is odd.

Parameters: `integer` Long.

### `.isPositive`

```clojure
(.isPositive number)  ;; → Boolean
```

Tests if the given **`number`** is positive.

Parameters: `number` Number.

### `.isZero`

```clojure
(.isZero number)  ;; → Boolean
```

Tests if the given **`number`** is zero.

Parameters: `number` Number.

### `log`

```clojure
(log x base)  ;; → Double
```

Computes the logarithm of the value **`x`** to the given **`base`**.

Parameters: `x` Number, `base` Number.

### `max`

```clojure
(max number…)  ;; → Number
```

Return the maximum **`number`** among the given **`number`** arguments.

Parameters: `number` Number.

### `min`

```clojure
(min number…)  ;; → Number
```

Return the minimum **`number`** among the given **`number`** arguments.

Parameters: `number` Number.

### `pow`

```clojure
(pow x y)  ;; → Double
```

Raises **`x`** value to the power **`y`**.

Parameters: `x` Number, `y` Number.

### `rem`

```clojure
(rem number divider)  ;; → Number
```

Remainder of dividing the first **`number`** by the **`divider`**.

Parameters: `number` Number, `divider` Number.

### `round`

```clojure
(round x)  ;; → Double
```

Rounds the given value **`x`** towards the closest integer with ties rounded towards even integer.

Parameters: `x` Number.

### `.sign`

```clojure
(.sign x)  ;; → Double
```

Returns the sign of the given value **`number`**: `-1` if the value is negative, `0` if the value is zero, and `1`if the value is positive.

Parameters: `x` Number.

### `sin`

```clojure
(sin x)  ;; → Double
```

Returns the sin of **`x`**.

Parameters: `x` Number.

### `sqrt`

```clojure
(sqrt x)  ;; → Double
```

Returns the square root of **`x`**.

Parameters: `x` Number.

### `tan`

```clojure
(tan x)  ;; → Double
```

Returns the tan of the given **`x`**.

Parameters: `x` Number.

## Comparison

### `!=`

```clojure
(!= object object…)  ;; → Boolean
```

Returns `true` if the given **`object`** arguments do not equal with each other, otherwise `false`.

Parameters: `object` Any, `object` Any.

### `<`

```clojure
(< number number…)  ;; → Boolean
```

Returns `true` if **`number`** values are in monotonically increasing order, otherwise `false`.

Parameters: `number` Number, `number` Number.

### `<=`

```clojure
(<= number number…)  ;; → Boolean
```

Returns `true` if **`number`** values are in monotonically non-decreasing order, otherwise `false`.

Parameters: `number` Number, `number` Number.

### `==`

```clojure
(== object object…)  ;; → Boolean
```

Tests if all the given **`object`** arguments are equal to each other.

Parameters: `object` Any, `object` Any.

### `===`

```clojure
(=== form1 form2)  ;; → Boolean
```

Tests if **`form1`** and **`form2`** are the same object.

Parameters: `form1` Any, `form2` Any.

### `>`

```clojure
(> number number…)  ;; → Boolean
```

Returns `true` if **`number`** values are in monotonically decreasing order, otherwise `false`.

Parameters: `number` Number, `number` Number.

### `>=`

```clojure
(>= number number…)  ;; → Boolean
```

Returns `true` if **`number`** values are in monotonically non-increasing order, otherwise `false`.

Parameters: `number` Number, `number` Number.

### `compare`

```clojure
(compare string1 string2)  ;; → Long
```

Returns a negative number, zero, or a positive number when **`string1`** is logically (less than), (equal to), or (greater than) **`string2`**.

Parameters: `string1` String, `string2` String.

## Logic

### `and`

```clojure
(and form…)  ;; → Boolean
```

Evaluates **`object`** arguments one by one, from left to right. If a **`object`** evaluates to *logical false* (i.e. `false` or `Null`) then `false` is returned and remaining **`object`** arguments are not evaluated, otherwise `true` is returned. If no **`object`** arguments are given, returns `true`.

Parameters: `form` Any.

### `not`

```clojure
(not object)  ;; → Boolean
```

Returns `false` if the given **`object`** evaluates to *logical true*, otherwise `true`.

Parameters: `object` Any.

### `or`

```clojure
(or form…)  ;; → Boolean
```

Evaluates all the given **`object`** arguments one by one, from left to right. If a **`object`** evaluates to *logical true* value (i.e. `true` or not `Null`), returns `true` and remaining **`object`** arguments are not evaluated. If no **`object`** evaluates to logical true, returns a `false`. If no **`object`** arguments are given, returns `false`"

Parameters: `form` Any.

### `xor`

```clojure
(xor form…)  ;; → Boolean
```

Evaluates **`object`** arguments one by one, from left to right. If only one **`object`** evaluates to *logical true* (i.e. `true` or not `Null`) value, returns `true`. If more than one **`object`** or none of them evaluates to logical true returns `false`. If no **`object`** arguments are given, returns `false`.

Parameters: `form` Any.

## Bitwise operations

### `and`

```clojure
(and number number)  ;; → Number
```

Performs a bitwise *and* comparison of corresponding bits of **`number1`** and **`number2`**. Returns an `Int` or `Long`.

Parameters: `number` Number, `number` Number.

### `bitNot`

```clojure
(bitNot int)  ;; → Number
```

Inverts the bits of the given **`number`** value. Returns an `Int` or `Long`.

Parameters: `int` Number.

### `.inv`

```clojure
(.inv int)  ;; → Number
```

Inverts the bits of the given **`number`** value. Returns an `Int` or `Long`.

Parameters: `int` Number.

### `or`

```clojure
(or number number)  ;; → Number
```

Performs a bitwise *or* comparison of corresponding bits of of **`number1`** and **`number2`**. Returns an `Int` or `Long`.

Parameters: `number` Number, `number` Number.

### `.shl`

```clojure
(.shl number n)  ;; → Number
```

Signed shift left. Shifts the bit pattern of the given **`number`** to the left by **`n`** number of specified bits, and zero bits are shifted into the low-order positions. Returns an `Int` or `Long`.

Parameters: `number` Number, `n` Int.

### `.shr`

```clojure
(.shr number n)  ;; → Number
```

Signed shift right. Shifts the bit pattern of the given **`number`** to the right by **`n`** number of specified bits. Returns an `Int` or `Long`.

Parameters: `number` Number, `n` Int.

### `.ushr`

```clojure
(.ushr number n)  ;; → Number
```

Signed shift right. Shifts the bit pattern of the given **`number`** to the right by **`n`** number of specified bits. Returns an `Int` or `Long`.

Parameters: `number` Number, `n` Int.

### `xor`

```clojure
(xor int int int…)  ;; → Number
```

Performs a bitwise *xor* comparison of corresponding bits of **`number1`** and **`number2`**. Returns an `Int` or `Long`.

Parameters: `int` Number, `int` Number, `int` Number.

## String functions

### `+`

```clojure
(+ string any)  ;; → String
```

Returns a `String` obtained by concatenating the given **`string`** with the string representation of the given **`any`** object.

Parameters: `string` String, `any` Any.

### `.butLast`

```clojure
(.butLast n string)  ;; → String
```

Returns a copy of the given **`string`** without the last **`n`** characters. Returns an empty `String` if **`n`** is greater than the length of **`string`**.

Parameters: `n` Number, `string` String.

### `.capitalize`

```clojure
(.capitalize string)  ;; → String
```

Returns a copy of the given **`string`** having its first letter upper cased, or the original **`string`**, if it is empty or already starts with an upper case letter.

Parameters: `string` String.

### `.charAt`

```clojure
(.charAt string index)  ;; → Char
```

Returns the `Char` at the location specified by **`index`** in the given **`string`**.

Parameters: `string` String, `index` Number.

### `.chop`

```clojure
(.chop string)  ;; → String
```

Creates a copy of the given **`string`** where the last character of it is removed.

Parameters: `string` String.

### `conc`

```clojure
(conc object…)  ;; → String
```

Converts the given **`object`** values to `String` values and concatenates them into a single `String`.

Parameters: `object` Any.

### `.contains`

```clojure
(.contains string substring)  ;; → Boolean
```

Returns `true` if the given **`string`** contains the specified **`substring`**, otherwise `false`.

Parameters: `string` String, `substring` String.

```clojure
(.contains string substring isCaseIgnored)  ;; → Boolean
```

Returns `true` if the given **`string`** contains the specified **`substring`**, otherwise `false`. Argument **`caseIsIgnored`** specifies whether matching is case insensitive (`true`) or sensitive (`false`).

Parameters: `string` String, `substring` String, `isCaseIgnored` Boolean.

### `.endsWith`

```clojure
(.endsWith string postfix)  ;; → Boolean
```

Tests if the **`string`** ends with the specified **`postfix`**.

Parameters: `string` String, `postfix` String.

### `format`

```clojure
(format object formatArg…)  ;; → Null
```

Formats the string evaluated from **`object`** using the given **`formatArgs`**. Eg. `(format "Hello %1s" "World!")`.

Parameters: `object` Any, `formatArg` Any.

### `.isBlank`

```clojure
(.isBlank string)  ;; → Boolean
```

Returns true if the given **`string`** is empty or consists solely of whitespace characters.

Parameters: `string` String.

### `.isEmpty`

```clojure
(.isEmpty string)  ;; → Boolean
```

Checks if the given **`string`** is empty.

Parameters: `string` String.

### `.isNotEmpty`

```clojure
(.isNotEmpty string)  ;; → Boolean
```

Checks if the given **`string`** is not empty.

Parameters: `string` String.

### `.length`

```clojure
(.length string)  ;; → Int
```

Returns the length of the given **`string`**.

Parameters: `string` String.

### `.matches`

```clojure
(.matches string regex)  ;; → Boolean
```

Return `true` if the given regular expression matches the given **`string`**, otherwise **`false`**. The regular expression is defined as a `Regex`.

Parameters: `string` String, `regex` Regex.

```clojure
(.matches string regex)  ;; → Boolean
```

Return `true` if the given regular expression matches the given **`string`**, otherwise **`false`**. The regular expression is defined as a `String`.

Parameters: `string` String, `regex` String.

### `.parseBoolean`

```clojure
(.parseBoolean string)  ;; → Boolean
```

Parses the given **`string`** to a `Boolean` value. Returns `Null` if parsing fails.

Parameters: `string` String.

### `.parseByte`

```clojure
(.parseByte string)  ;; → Any
```

Parses the given **`string`** to a `Byte` value. Returns `Null` if parsing fails.

Parameters: `string` String.

### `.parseDouble`

```clojure
(.parseDouble string)  ;; → Any
```

Parses the given **`string`** to a `Double` value. Returns `Null` if parsing fails.

Parameters: `string` String.

### `.parseFloat`

```clojure
(.parseFloat string)  ;; → Any
```

Parses the given **`string`** to a `Float` value. Returns `Null` if parsing fails.

Parameters: `string` String.

### `.parseInt`

```clojure
(.parseInt string)  ;; → Any
```

Parses the given **`string`** to an `Int` value. Returns `Null` if parsing fails.

Parameters: `string` String.

### `.parseLong`

```clojure
(.parseLong string)  ;; → Any
```

Parses the given **`string`** to a `Long` value. Returns `Null` if parsing fails.

Parameters: `string` String.

### `.parseShort`

```clojure
(.parseShort string)  ;; → Any
```

Parses the given **`string`** to a `Long` value. Returns `Null` if parsing fails.

Parameters: `string` String.

### `.repeat`

```clojure
(.repeat string n)  ;; → String
```

Repeat the **`string`** for **`n`** times.

Parameters: `string` String, `n` Number.

### `.replace`

```clojure
(.replace string substring replacement)  ;; → String
```

Replaces all the occurrences of the specified **`substring`** in the **`string`** with the given **`replacement`**.

Parameters: `string` String, `substring` String, `replacement` String.

### `.replaceChar`

```clojure
(.replaceChar string oldChar newChar)  ;; → String
```

Replaces all the occurrences of the given **`oldChar`** with **`newChar`** in the given **`string`**.

Parameters: `string` String, `oldChar` Char, `newChar` Char.

### `.replaceFirstRegex`

```clojure
(.replaceFirstRegex string regex replacement)  ;; → String
```

Replaces the first occurrence of the substring matching the specified regular expression with the **`replacement`** in the given **`string`**. The regular expression is defined by the given **`regex`** given as a `Regex`.

Parameters: `string` String, `regex` Regex, `replacement` String.

```clojure
(.replaceFirstRegex string regex replacement)  ;; → String
```

Replaces the first occurrence of the substring matching the specified regular expression with the **`replacement`** in the given **`string`**. The regular expression is defined by the given **`regex`** given as a `String`.

Parameters: `string` String, `regex` String, `replacement` String.

### `.replaceFirstString`

```clojure
(.replaceFirstString string substring replacement)  ;; → String
```

Replaces the first occurrence of the specified **`substring`** in the **`string`** with the given **`replacement`**.

Parameters: `string` String, `substring` String, `replacement` String.

### `.replaceRegex`

```clojure
(.replaceRegex string regex replacement)  ;; → String
```

Replaces all the occurrences of the substring matching the specified regular expression with the **`replacement`** in the given **`string`**. The regular expression is defined by the given **`regex`** given as a `Regex`.

Parameters: `string` String, `regex` Regex, `replacement` String.

### `.split`

```clojure
(.split string regex)  ;; → Array
```

Splits the given **`string`** around matches of the given regular expression defined by the `Regex` based **`regex`** argument. Resulting substrings are returned as an `Array`.

Parameters: `string` String, `regex` Regex.

```clojure
(.split string regex)  ;; → Array
```

Splits the given **`string`** around matches of the given regular expression defined by the `String` based **`regex`** argument. Resulting substrings are returned as an `Array`.

Parameters: `string` String, `regex` String.

```clojure
(.split string regex limit)  ;; → Array
```

Splits the given **`string`** around matches of the given regular expression defined by the `Regex` based **`regex`** argument. Resulting substrings are returned as an `Array`. Argument **`limit`** specifies the maximum number of substrings returned.

Parameters: `string` String, `regex` Regex, `limit` Long.

```clojure
(.split string regex limit)  ;; → Array
```

Splits the given **`string`** around matches of the given regular expression defined by the `String` based **`regex`** argument. Resulting substrings are returned as an `Array`. Argument **`limit`** specifies the maximum number of substrings returned.

Parameters: `string` String, `regex` String, `limit` Long.

### `.splitLines`

```clojure
(.splitLines string)  ;; → Array
```

Splits the given **`string`** to an `Array` of substrings based on line break characters.

Parameters: `string` String.

### `.startsWith`

```clojure
(.startsWith string prefix)  ;; → Boolean
```

Tests if the **`string`** starts with the specified **`prefix`**.

Parameters: `string` String, `prefix` String.

### `str`

```clojure
(str object…)  ;; → String
```

Converts the given **`object`** values to `String` values and concatenates them to a single `String`.

Parameters: `object` Any.

### `.substring`

```clojure
(.substring string start)  ;; → String
```

Returns the substring of **`string`** beginning at **`start`** inclusive, and ending at the length of string, exclusive.

Parameters: `string` String, `start` Long.

```clojure
(.substring string start end)  ;; → String
```

Returns the substring of **`string`** beginning at **`start`** inclusive, and ending at **`end`** (defaults to length of string), exclusive.

Parameters: `string` String, `start` Long, `end` Long.

### `.tail`

```clojure
(.tail string n)  ;; → String
```

Returns a copy of the given **`string`** that contains the last **`n`** characters of it.

Parameters: `string` String, `n` Number.

### `.toLowerCase`

```clojure
(.toLowerCase string)  ;; → String
```

Creates a copy of the given **`string`** where all the characters of it are lower cased.

Parameters: `string` String.

### `.toUpperCase`

```clojure
(.toUpperCase string)  ;; → String
```

Creates a copy of the given **`string`** where all the characters of it are upper cased.

Parameters: `string` String.

### `.trim`

```clojure
(.trim string)  ;; → String
```

Returns a sub sequence of the given **`string`** having leading and trailing white space characters removed.

Parameters: `string` String.

### `.trimEnd`

```clojure
(.trimEnd string)  ;; → String
```

Returns a sub sequence of the given **`string`** having trailing white space characters removed.

Parameters: `string` String.

### `.trimStart`

```clojure
(.trimStart string)  ;; → String
```

Returns a sub sequence of the given **`string`** having leading white space characters removed.

Parameters: `string` String.

## Sequences and collections

### `.add`

```clojure
(.add array item)  ;; → Boolean
```

Adds the given **`item`** to the given **`array`** (an `Array`). Returns a `Boolean` value indicating if adding succeeded.

Parameters: `array` Array, `item` Any.

```clojure
(.add list item)  ;; → Boolean
```

Adds the given **`item`** to the given **`list`** (a `List`). Returns a `Boolean` value indicating if adding succeeded.

Parameters: `list` List, `item` Any.

```clojure
(.add set item)  ;; → Boolean
```

Adds the given **`item`** to the given **`list`** (a `List`). Returns a `Boolean` value indicating if adding succeeded.

Parameters: `set` Set, `item` Any.

### `.addAll`

```clojure
(.addAll array items)  ;; → Array
```

Adds all the **`items`** (an `Collection`) to the given **`array`** (an `Array`). Returns the **`array`**.

Parameters: `array` Array, `items` Collection.

```clojure
(.addAll list items)  ;; → List
```

Adds all the **`items`** (a `Collection`) to the given **`list`** (a `List`). Returns the **`list`**.

Parameters: `list` List, `items` Collection.

```clojure
(.addAll set items)  ;; → Set
```

Adds all the **`items`** (a `Collection`) to the given **`list`** (a `List`). Returns the **`list`**.

Parameters: `set` Set, `items` Collection.

### `.all`

```clojure
(.all sequence predicate)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for all elements of **`collection`**, otherwise `false`.

Parameters: `sequence` Sequence, `predicate` Function.

```clojure
(.all sequence key)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for all elements of **`collection`**, otherwise `false`.

Parameters: `sequence` Sequence, `key` Keyword.

```clojure
(.all set predicate)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for all elements of **`collection`**, otherwise `false`.

Parameters: `set` Set, `predicate` Function.

### `all`

```clojure
(all sequence predicate)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for all elements of **`collection`**, otherwise `false`.

Parameters: `sequence` Sequence, `predicate` Function.

```clojure
(all sequence key)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for all elements of **`collection`**, otherwise `false`.

Parameters: `sequence` Sequence, `key` Keyword.

```clojure
(all set predicate)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for all elements of **`collection`**, otherwise `false`.

Parameters: `set` Set, `predicate` Function.

```clojure
(all predicate sequence)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for all elements of **`collection`**, otherwise `false`.

Parameters: `predicate` Function, `sequence` Sequence.

```clojure
(all key sequence)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for all elements of **`collection`**, otherwise `false`.

Parameters: `key` Keyword, `sequence` Sequence.

```clojure
(all predicate set)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for all elements of **`collection`**, otherwise `false`.

Parameters: `predicate` Function, `set` Set.

### `.any`

```clojure
(.any sequence predicate)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for at least one element of **`collection`**, otherwise `false`.

Parameters: `sequence` Sequence, `predicate` Function.

```clojure
(.any sequence key)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for at least one element of **`collection`**, otherwise `false`.

Parameters: `sequence` Sequence, `key` Keyword.

```clojure
(.any set predicate)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for at least one element of **`collection`**, otherwise `false`.

Parameters: `set` Set, `predicate` Function.

### `any`

```clojure
(any sequence predicate)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for at least one element of **`collection`**, otherwise `false`.

Parameters: `sequence` Sequence, `predicate` Function.

```clojure
(any sequence key)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for at least one element of **`collection`**, otherwise `false`.

Parameters: `sequence` Sequence, `key` Keyword.

```clojure
(any set predicate)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for at least one element of **`collection`**, otherwise `false`.

Parameters: `set` Set, `predicate` Function.

```clojure
(any predicate sequence)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for at least one element of **`collection`**, otherwise `false`.

Parameters: `predicate` Function, `sequence` Sequence.

```clojure
(any key sequence)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for at least one element of **`collection`**, otherwise `false`.

Parameters: `key` Keyword, `sequence` Sequence.

```clojure
(any predicate set)  ;; → Boolean
```

Returns `true` if **`predicate`** evaluates to *logical true* for at least one element of **`collection`**, otherwise `false`.

Parameters: `predicate` Function, `set` Set.

### `assocAt`

```clojure
(assocAt sequence index value)  ;; → Sequence
```

Returns a new `Sequence` (`Array` or `List`) with the item at **`index`** in **`sequence`** replaced by **`value`**.

Parameters: `sequence` Sequence, `index` Number, `value` Any.

### `.average`

```clojure
(.average sequence)  ;; → Double
```

Returns the arithmetic mean (`Double`) of all numbers in **`collection`** (`0.0` if empty).

Parameters: `sequence` Sequence.

```clojure
(.average set)  ;; → Double
```

Returns the arithmetic mean (`Double`) of all numbers in **`collection`** (`0.0` if empty).

Parameters: `set` Set.

### `average`

```clojure
(average sequence)  ;; → Double
```

Returns the arithmetic mean (`Double`) of all numbers in **`collection`** (`0.0` if empty).

Parameters: `sequence` Sequence.

```clojure
(average set)  ;; → Double
```

Returns the arithmetic mean (`Double`) of all numbers in **`collection`** (`0.0` if empty).

Parameters: `set` Set.

### `.clear`

```clojure
(.clear collection)  ;; → Unit
```

Clears all the items from the given **`collection`** (a `Collection`).

Parameters: `collection` Collection.

### `conj`

```clojure
(conj collection item)  ;; → Any
```

Returns a new `List` by adding the given **`item`** to the given **`collection`** which is `List`or `Array`.

Parameters: `collection` Sequence, `item` Any.

```clojure
(conj collection item)  ;; → Any
```

Returns a new `Set` by adding the given **`item`** to the given **`set`**.

Parameters: `collection` Set, `item` Any.

### `cons`

```clojure
(cons item sequence)  ;; → List
```

Returns a new `List` where **`item`** is the first element and the given **`sequence`** (a `Sequence`) is the rest.

Parameters: `item` Any, `sequence` Sequence.

```clojure
(cons item string)  ;; → List
```

Returns a new `List` where **`item`** is the first element and the given **`string`** (a `String`) is the rest.

Parameters: `item` Any, `string` String.

```clojure
(cons item set)  ;; → List
```

Returns a new `List` where **`item`** is the first element and the given **`set`** (a `Set`) is the rest.

Parameters: `item` Any, `set` Set.

### `.count`

```clojure
(.count collection)  ;; → Long
```

Returns the size of the given **`collection`** (a `Collection`).

Parameters: `collection` Sizeable.

```clojure
(.count string)  ;; → Long
```

Returns the size of the given **`string`** (a `String`).

Parameters: `string` String.

```clojure
(.count sequence predicate)  ;; → Long
```

Returns the number of elements in **`collection`** for which **`predicate`** evaluates to *logical true*.

Parameters: `sequence` Sequence, `predicate` Function.

```clojure
(.count sequence key)  ;; → Long
```

Returns the number of elements in **`collection`** for which **`predicate`** evaluates to *logical true*.

Parameters: `sequence` Sequence, `key` Keyword.

```clojure
(.count set predicate)  ;; → Long
```

Returns the number of elements in **`collection`** for which **`predicate`** evaluates to *logical true*.

Parameters: `set` Set, `predicate` Function.

### `count`

```clojure
(count collection)  ;; → Long
```

Returns the size of the given **`collection`** (a `Collection`).

Parameters: `collection` Sizeable.

```clojure
(count string)  ;; → Long
```

Returns the size of the given **`string`** (a `String`).

Parameters: `string` String.

```clojure
(count sequence predicate)  ;; → Long
```

Returns the number of elements in **`collection`** for which **`predicate`** evaluates to *logical true*.

Parameters: `sequence` Sequence, `predicate` Function.

```clojure
(count sequence key)  ;; → Long
```

Returns the number of elements in **`collection`** for which **`predicate`** evaluates to *logical true*.

Parameters: `sequence` Sequence, `key` Keyword.

```clojure
(count set predicate)  ;; → Long
```

Returns the number of elements in **`collection`** for which **`predicate`** evaluates to *logical true*.

Parameters: `set` Set, `predicate` Function.

```clojure
(count predicate sequence)  ;; → Long
```

Returns the number of elements in **`collection`** for which **`predicate`** evaluates to *logical true*.

Parameters: `predicate` Function, `sequence` Sequence.

```clojure
(count key sequence)  ;; → Long
```

Returns the number of elements in **`collection`** for which **`predicate`** evaluates to *logical true*.

Parameters: `key` Keyword, `sequence` Sequence.

```clojure
(count predicate set)  ;; → Long
```

Returns the number of elements in **`collection`** for which **`predicate`** evaluates to *logical true*.

Parameters: `predicate` Function, `set` Set.

### `.distinct`

```clojure
(.distinct sequence)  ;; → Sequence
```

Returns a new `Sequence` containing the distinct elements of **`collection`**, preserving first-seen order.

Parameters: `sequence` Sequence.

### `distinct`

```clojure
(distinct sequence)  ;; → Sequence
```

Returns a new `Sequence` containing the distinct elements of **`collection`**, preserving first-seen order.

Parameters: `sequence` Sequence.

### `.drop`

```clojure
(.drop sequence n)  ;; → Any
```

Returns a new `Sequence` containing all but the first **`n`** elements of **`sequence`**.

Parameters: `sequence` Sequence, `n` Number.

```clojure
(.drop string n)  ;; → Any
```

Returns a new `String` of all but the first **`n`** items in the given **`string`**.

Parameters: `string` String, `n` Number.

```clojure
(.drop n sequence)  ;; → Any
```

Returns a new `Sequence` containing all but the first **`n`** elements of **`sequence`**.

Parameters: `n` Number, `sequence` Sequence.

```clojure
(.drop n string)  ;; → Any
```

Returns a new `String` of all but the first **`n`** items in the given **`string`**.

Parameters: `n` Number, `string` String.

### `drop`

```clojure
(drop sequence n)  ;; → Any
```

Returns a new `Sequence` containing all but the first **`n`** elements of **`sequence`**.

Parameters: `sequence` Sequence, `n` Number.

```clojure
(drop string n)  ;; → Any
```

Returns a new `String` of all but the first **`n`** items in the given **`string`**.

Parameters: `string` String, `n` Number.

```clojure
(drop n sequence)  ;; → Any
```

Returns a new `Sequence` containing all but the first **`n`** elements of **`sequence`**.

Parameters: `n` Number, `sequence` Sequence.

```clojure
(drop n string)  ;; → Any
```

Returns a new `String` of all but the first **`n`** items in the given **`string`**.

Parameters: `n` Number, `string` String.

### `.dropLast`

```clojure
(.dropLast sequence n)  ;; → Any
```

Returns a new `Sequence` containing all but the last **`n`** elements of **`sequence`**.

Parameters: `sequence` Sequence, `n` Number.

```clojure
(.dropLast string n)  ;; → Any
```

Returns a new `String` of all but the last **`n`** items in the given **`string`**.

Parameters: `string` String, `n` Number.

```clojure
(.dropLast n sequence)  ;; → Any
```

Returns a new `Sequence` containing all but the last **`n`** elements of **`sequence`**.

Parameters: `n` Number, `sequence` Sequence.

```clojure
(.dropLast n string)  ;; → Any
```

Returns a new `String` of all but the last **`n`** items in the given **`string`**.

Parameters: `n` Number, `string` String.

### `dropLast`

```clojure
(dropLast sequence n)  ;; → Any
```

Returns a new `Sequence` containing all but the last **`n`** elements of **`sequence`**.

Parameters: `sequence` Sequence, `n` Number.

```clojure
(dropLast string n)  ;; → Any
```

Returns a new `String` of all but the last **`n`** items in the given **`string`**.

Parameters: `string` String, `n` Number.

```clojure
(dropLast n sequence)  ;; → Any
```

Returns a new `Sequence` containing all but the last **`n`** elements of **`sequence`**.

Parameters: `n` Number, `sequence` Sequence.

```clojure
(dropLast n string)  ;; → Any
```

Returns a new `String` of all but the last **`n`** items in the given **`string`**.

Parameters: `n` Number, `string` String.

### `.filter`

```clojure
(.filter collection predicate)  ;; → List
```

Returns a `List` containing the items from the given **`collection`** for which the **`predicate`** function evaluates to *logical true* (i.e. `true` or not `Null`.

Parameters: `collection` Sequence, `predicate` Function.

```clojure
(.filter collection predicate)  ;; → List
```

Returns a `List` containing the items from the given **`set`** for which the **`predicate`** function evaluates to *logical true* (i.e. `true` or not `Null`.

Parameters: `collection` Set, `predicate` Function.

```clojure
(.filter collection key)  ;; → List
```

Returns a `List` containing the items from the given **`collection`** for which the **`predicate`** function evaluates to *logical true* (i.e. `true` or not `Null`.

Parameters: `collection` Sequence, `key` Keyword.

### `filter`

```clojure
(filter collection predicate)  ;; → List
```

Returns a `List` containing the items from the given **`collection`** for which the **`predicate`** function evaluates to *logical true* (i.e. `true` or not `Null`.

Parameters: `collection` Sequence, `predicate` Function.

```clojure
(filter collection predicate)  ;; → List
```

Returns a `List` containing the items from the given **`set`** for which the **`predicate`** function evaluates to *logical true* (i.e. `true` or not `Null`.

Parameters: `collection` Set, `predicate` Function.

```clojure
(filter collection key)  ;; → List
```

Returns a `List` containing the items from the given **`collection`** for which the **`predicate`** function evaluates to *logical true* (i.e. `true` or not `Null`.

Parameters: `collection` Sequence, `key` Keyword.

```clojure
(filter predicate collection)  ;; → List
```

Returns a `List` containing the items from the given **`collection`** for which the **`predicate`** function evaluates to *logical true* (i.e. `true` or not `Null`.

Parameters: `predicate` Function, `collection` Sequence.

```clojure
(filter predicate collection)  ;; → List
```

Returns a `List` containing the items from the given **`set`** for which the **`predicate`** function evaluates to *logical true* (i.e. `true` or not `Null`.

Parameters: `predicate` Function, `collection` Set.

```clojure
(filter key collection)  ;; → List
```

Returns a `List` containing the items from the given **`collection`** for which the **`predicate`** function evaluates to *logical true* (i.e. `true` or not `Null`.

Parameters: `key` Keyword, `collection` Sequence.

### `.find`

```clojure
(.find sequence predicate)  ;; → Any
```

Returns the first element in **`collection`** for which **`predicate`** evaluates to *logical true*, or `Null` if none matches.

Parameters: `sequence` Sequence, `predicate` Function.

```clojure
(.find sequence key)  ;; → Any
```

Returns the first element in **`collection`** for which **`predicate`** evaluates to *logical true*, or `Null` if none matches.

Parameters: `sequence` Sequence, `key` Keyword.

```clojure
(.find set predicate)  ;; → Any
```

Returns the first element in **`collection`** for which **`predicate`** evaluates to *logical true*, or `Null` if none matches.

Parameters: `set` Set, `predicate` Function.

### `find`

```clojure
(find sequence predicate)  ;; → Any
```

Returns the first element in **`collection`** for which **`predicate`** evaluates to *logical true*, or `Null` if none matches.

Parameters: `sequence` Sequence, `predicate` Function.

```clojure
(find sequence key)  ;; → Any
```

Returns the first element in **`collection`** for which **`predicate`** evaluates to *logical true*, or `Null` if none matches.

Parameters: `sequence` Sequence, `key` Keyword.

```clojure
(find set predicate)  ;; → Any
```

Returns the first element in **`collection`** for which **`predicate`** evaluates to *logical true*, or `Null` if none matches.

Parameters: `set` Set, `predicate` Function.

```clojure
(find predicate sequence)  ;; → Any
```

Returns the first element in **`collection`** for which **`predicate`** evaluates to *logical true*, or `Null` if none matches.

Parameters: `predicate` Function, `sequence` Sequence.

```clojure
(find key sequence)  ;; → Any
```

Returns the first element in **`collection`** for which **`predicate`** evaluates to *logical true*, or `Null` if none matches.

Parameters: `key` Keyword, `sequence` Sequence.

```clojure
(find predicate set)  ;; → Any
```

Returns the first element in **`collection`** for which **`predicate`** evaluates to *logical true*, or `Null` if none matches.

Parameters: `predicate` Function, `set` Set.

### `.first`

```clojure
(.first sequence)  ;; → Any
```

Returns the first item in the given **`sequence`** which may not be empty.

Parameters: `sequence` Sequence.

```clojure
(.first string)  ;; → Any
```

Returns the first character int the given **`string`** which may not be empty.

Parameters: `string` String.

### `.fold`

```clojure
(.fold sequence initialValue function)  ;; → Any
```

Accumulates the elements of **`collection`** from left to right using the binary **`function`**, starting with **`initialValue`**.

Parameters: `sequence` Sequence, `initialValue` Any, `function` Function.

```clojure
(.fold sequence function)  ;; → Any
```

Accumulates the elements of **`collection`** from left to right using the binary **`function`**, starting with the first element (returns `Null` if empty).

Parameters: `sequence` Sequence, `function` Function.

### `fold`

```clojure
(fold function initialValue sequence)  ;; → Any
```

Accumulates the elements of **`collection`** from left to right using the binary **`function`**, starting with **`initialValue`**.

Parameters: `function` Function, `initialValue` Any, `sequence` Sequence.

```clojure
(fold sequence initialValue function)  ;; → Any
```

Accumulates the elements of **`collection`** from left to right using the binary **`function`**, starting with **`initialValue`**.

Parameters: `sequence` Sequence, `initialValue` Any, `function` Function.

```clojure
(fold function sequence)  ;; → Any
```

Accumulates the elements of **`collection`** from left to right using the binary **`function`**, starting with the first element (returns `Null` if empty).

Parameters: `function` Function, `sequence` Sequence.

### `.get`

```clojure
(.get sequence index)  ;; → Any
```

Returns the item at the location specified by **`index`** in the given **`sequence`** (e.g. `Array` or `List`).

Parameters: `sequence` Indexable, `index` Number.

### `.groupBy`

```clojure
(.groupBy sequence keyFn)  ;; → Map
```

Groups the elements of **`collection`** into a `Map` of `Array`s keyed by the result of **`keyFn`** (a function, keyword, or key string).

Parameters: `sequence` Sequence, `keyFn` Function.

```clojure
(.groupBy sequence key)  ;; → Map
```

Groups the elements of **`collection`** into a `Map` of `Array`s keyed by the result of **`keyFn`** (a function, keyword, or key string).

Parameters: `sequence` Sequence, `key` Keyword.

```clojure
(.groupBy sequence key)  ;; → Map
```

Groups the elements of **`collection`** into a `Map` of `Array`s keyed by the result of **`keyFn`** (a function, keyword, or key string).

Parameters: `sequence` Sequence, `key` String.

### `groupBy`

```clojure
(groupBy keyFn sequence)  ;; → Map
```

Groups the elements of **`collection`** into a `Map` of `Array`s keyed by the result of **`keyFn`** (a function, keyword, or key string).

Parameters: `keyFn` Function, `sequence` Sequence.

```clojure
(groupBy key sequence)  ;; → Map
```

Groups the elements of **`collection`** into a `Map` of `Array`s keyed by the result of **`keyFn`** (a function, keyword, or key string).

Parameters: `key` Keyword, `sequence` Sequence.

```clojure
(groupBy key sequence)  ;; → Map
```

Groups the elements of **`collection`** into a `Map` of `Array`s keyed by the result of **`keyFn`** (a function, keyword, or key string).

Parameters: `key` String, `sequence` Sequence.

```clojure
(groupBy sequence keyFn)  ;; → Map
```

Groups the elements of **`collection`** into a `Map` of `Array`s keyed by the result of **`keyFn`** (a function, keyword, or key string).

Parameters: `sequence` Sequence, `keyFn` Function.

```clojure
(groupBy sequence key)  ;; → Map
```

Groups the elements of **`collection`** into a `Map` of `Array`s keyed by the result of **`keyFn`** (a function, keyword, or key string).

Parameters: `sequence` Sequence, `key` Keyword.

```clojure
(groupBy sequence key)  ;; → Map
```

Groups the elements of **`collection`** into a `Map` of `Array`s keyed by the result of **`keyFn`** (a function, keyword, or key string).

Parameters: `sequence` Sequence, `key` String.

### `.indexOf`

```clojure
(.indexOf sequence item)  ;; → Int
```

Returns the index of the first occurrence of **`item`** in **`sequence`**, or `-1` if not found.

Parameters: `sequence` Sequence, `item` Any.

```clojure
(.indexOf sequence item index)  ;; → Int
```

Returns the index of the first occurrence of **`item`** in **`sequence`** starting at **`index`**, or `-1` if not found.

Parameters: `sequence` Sequence, `item` Any, `index` Number.

```clojure
(.indexOf string substring)  ;; → Int
```

Returns the index within the given **`string`** of the first occurrence of the specified **`substring`**, or `-1` if not found.

Parameters: `string` String, `substring` String.

```clojure
(.indexOf string substring index)  ;; → Int
```

Gets the index of the specified **`substring`** in the given **`string`**. Searching starts from the specified start **`index`**. Returns `-1` if the **`substring`** is not found.

Parameters: `string` String, `substring` String, `index` Number.

```clojure
(.indexOf string char)  ;; → Int
```

Returns the index within the given **`string`** of the first occurrence of the specified **`substring`**, or `-1` if not found.

Parameters: `string` String, `char` Char.

```clojure
(.indexOf string char index)  ;; → Int
```

Gets the index of the specified **`substring`** in the given **`string`**. Searching starts from the specified start **`index`**. Returns `-1` if the **`substring`** is not found.

Parameters: `string` String, `char` Char, `index` Number.

### `indexOf`

```clojure
(indexOf sequence item)  ;; → Int
```

Returns the index of the first occurrence of **`item`** in **`sequence`**, or `-1` if not found.

Parameters: `sequence` Sequence, `item` Any.

```clojure
(indexOf sequence item index)  ;; → Int
```

Returns the index of the first occurrence of **`item`** in **`sequence`** starting at **`index`**, or `-1` if not found.

Parameters: `sequence` Sequence, `item` Any, `index` Number.

```clojure
(indexOf string substring)  ;; → Int
```

Returns the index within the given **`string`** of the first occurrence of the specified **`substring`**, or `-1` if not found.

Parameters: `string` String, `substring` String.

```clojure
(indexOf string substring index)  ;; → Int
```

Gets the index of the specified **`substring`** in the given **`string`**. Searching starts from the specified start **`index`**. Returns `-1` if the **`substring`** is not found.

Parameters: `string` String, `substring` String, `index` Number.

```clojure
(indexOf string char)  ;; → Int
```

Returns the index within the given **`string`** of the first occurrence of the specified **`substring`**, or `-1` if not found.

Parameters: `string` String, `char` Char.

```clojure
(indexOf string char index)  ;; → Int
```

Gets the index of the specified **`substring`** in the given **`string`**. Searching starts from the specified start **`index`**. Returns `-1` if the **`substring`** is not found.

Parameters: `string` String, `char` Char, `index` Number.

### `.insertAt`

```clojure
(.insertAt sequence index value)  ;; → Sequence
```

Returns a new `Sequence` (`Array` or `List`) with **`value`** inserted at **`index`** in **`sequence`**.

Parameters: `sequence` Sequence, `index` Number, `value` Any.

### `insertAt`

```clojure
(insertAt sequence index value)  ;; → Sequence
```

Returns a new `Sequence` (`Array` or `List`) with **`value`** inserted at **`index`** in **`sequence`**.

Parameters: `sequence` Sequence, `index` Number, `value` Any.

### `.isEmpty`

```clojure
(.isEmpty collection)  ;; → Boolean
```

Checks if the given **`collection`** (a `Collection`) is empty. Returns a **`Boolean`**.

Parameters: `collection` Collection.

### `isEmpty`

```clojure
(isEmpty collection)  ;; → Boolean
```

Returns true if the given **`collection`** is empty.

Parameters: `collection` Sizeable.

```clojure
(isEmpty string)  ;; → Boolean
```

Returns true if the given **`string`** is empty (contains no characters).

Parameters: `string` String.

### `.isNotEmpty`

```clojure
(.isNotEmpty collection)  ;; → Boolean
```

Checks if the given **`collection`** (a `Collection`) is not empty. Returns a **`Boolean`**.

Parameters: `collection` Collection.

### `isNotEmpty`

```clojure
(isNotEmpty collection)  ;; → Boolean
```

Returns true if the given **`collection`** is not empty.

Parameters: `collection` Sizeable.

```clojure
(isNotEmpty string)  ;; → Boolean
```

Returns true if the given **`string`** is not empty (contains no characters).

Parameters: `string` String.

### `.join`

```clojure
(.join sequence)  ;; → String
```

Joins the elements of **`collection`** into a single `String`.

Parameters: `sequence` Sequence.

```clojure
(.join sequence separator)  ;; → String
```

Joins the elements of **`collection`** into a single `String` separated by **`separator`**.

Parameters: `sequence` Sequence, `separator` Any.

### `join`

```clojure
(join sequence)  ;; → String
```

Joins the elements of **`collection`** into a single `String`.

Parameters: `sequence` Sequence.

```clojure
(join sequence separator)  ;; → String
```

Joins the elements of **`collection`** into a single `String` separated by **`separator`**.

Parameters: `sequence` Sequence, `separator` Any.

### `.last`

```clojure
(.last sequence)  ;; → Any
```

Returns the last item in the given **`sequence`** which may not be empty.

Parameters: `sequence` Sequence.

```clojure
(.last string)  ;; → Any
```

Returns the last item in the given **`string`** which may not be empty.

Parameters: `string` String.

### `nth`

```clojure
(nth sequence index)  ;; → Any
```

Returns the item at the location specified by **`index`** in the given **`sequence`** (e.g. `Array` or `List`).

Parameters: `sequence` Indexable, `index` Number.

### `range`

```clojure
(range end)  ;; → Array
```

Returns an `Array` of integers from `0` (inclusive) to **`end`** (exclusive).

Parameters: `end` Number.

```clojure
(range start end)  ;; → Array
```

Returns an `Array` of integers from **`start`** (inclusive) to **`end`** (exclusive).

Parameters: `start` Number, `end` Number.

```clojure
(range start end step)  ;; → Array
```

Returns an `Array` of integers from **`start`** (inclusive) to **`end`** (exclusive) advancing by **`step`**.

Parameters: `start` Number, `end` Number, `step` Number.

### `.reduce`

```clojure
(.reduce sequence function)  ;; → Any
```

Accumulates the elements of **`collection`** from left to right using the binary **`function`**, starting with the first element (returns `Null` if empty).

Parameters: `sequence` Sequence, `function` Function.

```clojure
(.reduce sequence initialValue function)  ;; → Any
```

Accumulates the elements of **`collection`** from left to right using the binary **`function`**, starting with **`initialValue`**.

Parameters: `sequence` Sequence, `initialValue` Any, `function` Function.

### `reduce`

```clojure
(reduce function sequence)  ;; → Any
```

Accumulates the elements of **`collection`** from left to right using the binary **`function`**, starting with the first element (returns `Null` if empty).

Parameters: `function` Function, `sequence` Sequence.

```clojure
(reduce function initialValue sequence)  ;; → Any
```

Accumulates the elements of **`collection`** from left to right using the binary **`function`**, starting with **`initialValue`**.

Parameters: `function` Function, `initialValue` Any, `sequence` Sequence.

```clojure
(reduce sequence function)  ;; → Any
```

Accumulates the elements of **`collection`** from left to right using the binary **`function`**, starting with the first element (returns `Null` if empty).

Parameters: `sequence` Sequence, `function` Function.

```clojure
(reduce sequence initialValue function)  ;; → Any
```

Accumulates the elements of **`collection`** from left to right using the binary **`function`**, starting with **`initialValue`**.

Parameters: `sequence` Sequence, `initialValue` Any, `function` Function.

### `.remove`

```clojure
(.remove array item)  ;; → Boolean
```

Removes the given **`item`** from the given **`collection`** (`Array`, `List`, or `Set`). Returns a `Boolean` value indicating if removing succeeded.

Parameters: `array` Array, `item` Any.

```clojure
(.remove array item)  ;; → Boolean
```

Removes the given **`item`** from the given **`array`** (an `Array`). Returns a `Boolean` value indicating if removing succeeded.

Parameters: `array` Array, `item` Any.

```clojure
(.remove list item)  ;; → Boolean
```

Removes the given **`item`** from the given **`list`** (a `List`). Returns a `Boolean` value indicating if removing succeeded.

Parameters: `list` List, `item` Any.

```clojure
(.remove set item)  ;; → Boolean
```

Removes the given **`item`** from the given **`list`** (a `List`). Returns a `Boolean` value indicating if removing succeeded.

Parameters: `set` Set, `item` Any.

### `.removeAt`

```clojure
(.removeAt sequence index)  ;; → Sequence
```

Returns a new `Sequence` (`Array` or `List`) with the item at **`index`** removed from **`sequence`**.

Parameters: `sequence` Sequence, `index` Number.

### `removeAt`

```clojure
(removeAt sequence index)  ;; → Sequence
```

Returns a new `Sequence` (`Array` or `List`) with the item at **`index`** removed from **`sequence`**.

Parameters: `sequence` Sequence, `index` Number.

### `.rest`

```clojure
(.rest sequence)  ;; → Sequence
```

Returns a copy of the given **`sequence`** that does not contain the first item of it.

Parameters: `sequence` Sequence.

```clojure
(.rest string)  ;; → Sequence
```

Returns a copy of the given **`string`** that does not contain the first character of it.

Parameters: `string` String.

### `.reverse`

```clojure
(.reverse sequence)  ;; → Any
```

Returns a new `Sequence` with the elements of **`sequence`** in reverse order.

Parameters: `sequence` Sequence.

```clojure
(.reverse string)  ;; → Any
```

Reverses the characters in the given **`string`**.

Parameters: `string` String.

### `reverse`

```clojure
(reverse sequence)  ;; → Any
```

Returns a new `Sequence` with the elements of **`sequence`** in reverse order.

Parameters: `sequence` Sequence.

```clojure
(reverse string)  ;; → Any
```

Reverses the characters in the given **`string`**.

Parameters: `string` String.

### `.setAt`

```clojure
(.setAt sequence index value)  ;; → Sequence
```

Returns a new `Sequence` (`Array` or `List`) with the item at **`index`** in **`sequence`** replaced by **`value`**.

Parameters: `sequence` Sequence, `index` Number, `value` Any.

### `.size`

```clojure
(.size collection)  ;; → Long
```

Returns the size of the given **`collection`** (a `Collection`).

Parameters: `collection` Sizeable.

```clojure
(.size string)  ;; → Long
```

Returns the size of the given **`string`** (a `String`).

Parameters: `string` String.

### `.sort`

```clojure
(.sort sequence)  ;; → Sequence
```

Returns a new `Sequence` with the elements of **`collection`** sorted in ascending order.

Parameters: `sequence` Sequence.

```clojure
(.sort set)  ;; → Sequence
```

Returns a new `Sequence` with the elements of **`collection`** sorted in ascending order.

Parameters: `set` Set.

### `sort`

```clojure
(sort sequence)  ;; → Sequence
```

Returns a new `Sequence` with the elements of **`collection`** sorted in ascending order.

Parameters: `sequence` Sequence.

```clojure
(sort set)  ;; → Sequence
```

Returns a new `Sequence` with the elements of **`collection`** sorted in ascending order.

Parameters: `set` Set.

### `.sortBy`

```clojure
(.sortBy sequence keyFn)  ;; → Sequence
```

Returns a new `Sequence` with the elements of **`collection`** sorted in ascending order by the key returned by **`keyFn`** (a function, keyword, or key string).

Parameters: `sequence` Sequence, `keyFn` Function.

```clojure
(.sortBy sequence key)  ;; → Sequence
```

Returns a new `Sequence` with the elements of **`collection`** sorted in ascending order by the key returned by **`keyFn`** (a function, keyword, or key string).

Parameters: `sequence` Sequence, `key` Keyword.

```clojure
(.sortBy sequence key)  ;; → Sequence
```

Returns a new `Sequence` with the elements of **`collection`** sorted in ascending order by the key returned by **`keyFn`** (a function, keyword, or key string).

Parameters: `sequence` Sequence, `key` String.

### `sortBy`

```clojure
(sortBy keyFn sequence)  ;; → Sequence
```

Returns a new `Sequence` with the elements of **`collection`** sorted in ascending order by the key returned by **`keyFn`** (a function, keyword, or key string).

Parameters: `keyFn` Function, `sequence` Sequence.

```clojure
(sortBy key sequence)  ;; → Sequence
```

Returns a new `Sequence` with the elements of **`collection`** sorted in ascending order by the key returned by **`keyFn`** (a function, keyword, or key string).

Parameters: `key` Keyword, `sequence` Sequence.

```clojure
(sortBy key sequence)  ;; → Sequence
```

Returns a new `Sequence` with the elements of **`collection`** sorted in ascending order by the key returned by **`keyFn`** (a function, keyword, or key string).

Parameters: `key` String, `sequence` Sequence.

```clojure
(sortBy sequence keyFn)  ;; → Sequence
```

Returns a new `Sequence` with the elements of **`collection`** sorted in ascending order by the key returned by **`keyFn`** (a function, keyword, or key string).

Parameters: `sequence` Sequence, `keyFn` Function.

```clojure
(sortBy sequence key)  ;; → Sequence
```

Returns a new `Sequence` with the elements of **`collection`** sorted in ascending order by the key returned by **`keyFn`** (a function, keyword, or key string).

Parameters: `sequence` Sequence, `key` Keyword.

```clojure
(sortBy sequence key)  ;; → Sequence
```

Returns a new `Sequence` with the elements of **`collection`** sorted in ascending order by the key returned by **`keyFn`** (a function, keyword, or key string).

Parameters: `sequence` Sequence, `key` String.

### `.subArray`

```clojure
(.subArray array start)  ;; → Array
```

Returns the subarray of **`array`** beginning at **`start`** inclusive, and ending at the length of array, exclusive.

Parameters: `array` Array, `start` Long.

```clojure
(.subArray array start end)  ;; → Array
```

Returns the subarray of **`array`** beginning at **`start`** inclusive, and ending at **`end`**, exclusive.

Parameters: `array` Array, `start` Long, `end` Long.

### `.sum`

```clojure
(.sum sequence)  ;; → Number
```

Returns the sum of all numbers in **`collection`** (`0` if empty).

Parameters: `sequence` Sequence.

```clojure
(.sum set)  ;; → Number
```

Returns the sum of all numbers in **`collection`** (`0` if empty).

Parameters: `set` Set.

### `sum`

```clojure
(sum sequence)  ;; → Number
```

Returns the sum of all numbers in **`collection`** (`0` if empty).

Parameters: `sequence` Sequence.

```clojure
(sum set)  ;; → Number
```

Returns the sum of all numbers in **`collection`** (`0` if empty).

Parameters: `set` Set.

### `.take`

```clojure
(.take sequence n)  ;; → Any
```

Returns a new `Sequence` containing the first **`n`** elements of **`sequence`**.

Parameters: `sequence` Sequence, `n` Number.

```clojure
(.take string n)  ;; → Any
```

Returns a sub sequence of the given **`string`** containing only max **`n`** first characters of the original `String`.

Parameters: `string` String, `n` Number.

```clojure
(.take n sequence)  ;; → Any
```

Returns a new `Sequence` containing the first **`n`** elements of **`sequence`**.

Parameters: `n` Number, `sequence` Sequence.

```clojure
(.take n string)  ;; → Any
```

Returns a sub sequence of the given **`string`** containing only max **`n`** first characters of the original `String`.

Parameters: `n` Number, `string` String.

### `take`

```clojure
(take sequence n)  ;; → Any
```

Returns a new `Sequence` containing the first **`n`** elements of **`sequence`**.

Parameters: `sequence` Sequence, `n` Number.

```clojure
(take string n)  ;; → Any
```

Returns a sub sequence of the given **`string`** containing only max **`n`** first characters of the original `String`.

Parameters: `string` String, `n` Number.

```clojure
(take n sequence)  ;; → Any
```

Returns a new `Sequence` containing the first **`n`** elements of **`sequence`**.

Parameters: `n` Number, `sequence` Sequence.

```clojure
(take n string)  ;; → Any
```

Returns a sub sequence of the given **`string`** containing only max **`n`** first characters of the original `String`.

Parameters: `n` Number, `string` String.

## Maps and JSON

### `assoc`

```clojure
(assoc map key… value…)  ;; → Map
```

Returns a new `Map` with the given **`key`** and **`value`** pairs associated in the **`map`**.

Parameters: `map` Map, `key` Any, `value` Any.

### `assocIn`

```clojure
(assocIn collection path value)  ;; → Any
```

Returns a copy of the nested **`collection`** with the value at **`path`** (a slash-separated string like `"a/0/b"` or a sequence of keys/indices) replaced by **`value`**.

Parameters: `collection` Any, `path` String, `value` Any.

```clojure
(assocIn collection path value)  ;; → Any
```

Returns a copy of the nested **`collection`** with the value at **`path`** (a slash-separated string like `"a/0/b"` or a sequence of keys/indices) replaced by **`value`**.

Parameters: `collection` Any, `path` Sequence, `value` Any.

### `.containsKey`

```clojure
(.containsKey map key)  ;; → Boolean
```

Returns `true` if the given **`map`** contains the specified **`key`**, otherwise `false`.

Parameters: `map` Map, `key` Any.

```clojure
(.containsKey json key)  ;; → Boolean
```

Returns `true` if the given **`json`** object contains the specified **`key`**, otherwise `false`.

Parameters: `json` JsonObject, `key` Any.

### `containsKey`

```clojure
(containsKey map key)  ;; → Boolean
```

Returns `true` if the given **`map`** contains the specified **`key`**, otherwise `false`.

Parameters: `map` Map, `key` Any.

```clojure
(containsKey json key)  ;; → Boolean
```

Returns `true` if the given **`json`** object contains the specified **`key`**, otherwise `false`.

Parameters: `json` JsonObject, `key` Any.

### `dissoc`

```clojure
(dissoc map key key…)  ;; → Map
```

Returns a new `Map` with the given **`key`** arguments removed from the **`map`**.

Parameters: `map` Map, `key` Any, `key` Any.

### `.get`

```clojure
(.get map key)  ;; → Any
```

Gets the value for the given **`key`** in the **`map`** (a `Map`). Returns the item or **`Null`** if the **`key`** has no assigned value.

Parameters: `map` Map, `key` Any.

```clojure
(.get map key fallback)  ;; → Any
```

Gets the value for the given **`key`** in the **`map`** (a `Map`). Returns the **`fallback`** if the **`key`** has no assigned value.

Parameters: `map` Map, `key` Any, `fallback` Any.

```clojure
(.get json key)  ;; → Any
```

Gets the property for the given **`key`** in the **`json`** (a `JSONObject`). Returns the property or **`Null`** if the JSON does not contain a property with **`key`**.

Parameters: `json` JsonObject, `key` Any.

```clojure
(.get json key fallback)  ;; → Any
```

Gets the property for the given **`key`** in the **`json`** (a `JSONObject`). Returns the **`fallback`** if the JSON does not contain a property with **`key`**.

Parameters: `json` JsonObject, `key` Any, `fallback` Any.

### `get`

```clojure
(get json key)  ;; → Any
```

Gets the property with **`key`** from a **`json`**. Value `Null` is returned if property with **`key`** is not found. Use forward slash separated keys to define a path for nested properties e.g. "employees/developers/Mike"

Parameters: `json` JsonObject, `key` Any.

```clojure
(get json key fallback)  ;; → Any
```

Gets the property with **`key`** from a **`json`**. The given **`fallback`** is returned if property with **`key`** is not found. Use forward slash separated keys to define a path for nested properties e.g. "employees/developers/Mike"

Parameters: `json` JsonObject, `key` Any, `fallback` Any.

```clojure
(get map key)  ;; → Any
```

Gets the property with **`key`** from a **`map`**. Value `Null` is returned if property with **`key`** is not found.

Parameters: `map` Map, `key` Any.

```clojure
(get map key fallback)  ;; → Any
```

Gets the property with **`key`** from a **`map`**. The given **`fallback`** is returned if property with **`key`** is not found.

Parameters: `map` Map, `key` Any, `fallback` Any.

### `.isJsonArray`

```clojure
(.isJsonArray object)  ;; → Boolean
```

Tests if the given **`object`** is instance of `JsonArray`.

Parameters: `object` Any.

### `.isJsonElement`

```clojure
(.isJsonElement object)  ;; → Boolean
```

Tests if the given **`object`** is instance of `JsonElement`.

Parameters: `object` Any.

### `.isJsonObject`

```clojure
(.isJsonObject object)  ;; → Boolean
```

Tests f the given **`object`** is instance of `JsonObject`.

Parameters: `object` Any.

### `.isJsonPrimitive`

```clojure
(.isJsonPrimitive object)  ;; → Boolean
```

Tests if the given **`object`** is instance of `JSonPrimitive`.

Parameters: `object` Any.

### `.isMap`

```clojure
(.isMap object)  ;; → Boolean
```

Tests if the given **`object`** is instance of `Map`

Parameters: `object` Any.

### `.keys`

```clojure
(.keys map)  ;; → Array
```

Returns an `Array` containing all keys of the given **`map`**.

Parameters: `map` Map.

```clojure
(.keys json)  ;; → Array
```

Returns an `Array` containing all property keys of the given **`json`** object.

Parameters: `json` JsonObject.

### `keys`

```clojure
(keys map)  ;; → Array
```

Returns an `Array` containing all keys of the given **`map`**.

Parameters: `map` Map.

```clojure
(keys json)  ;; → Array
```

Returns an `Array` containing all property keys of the given **`json`** object.

Parameters: `json` JsonObject.

### `merge`

```clojure
(merge map map…)  ;; → Map
```

Returns a new `Map` combining the entries of the given **`map`** arguments from left to right.

Parameters: `map` Map, `map` Map.

### `.put`

```clojure
(.put map key value)  ;; → Map
```

Returns a new `Map` with the given **`key`** and **`value`** associated in the **`map`**.

Parameters: `map` Map, `key` Any, `value` Any.

### `.removeKey`

```clojure
(.removeKey map key)  ;; → Map
```

Returns a new `Map` with the given **`key`** removed from the **`map`**.

Parameters: `map` Map, `key` Any.

### `.toJson`

```clojure
(.toJson string)  ;; → JsonObject
```

Converts a **`string`** **`JsonObject`**.

Parameters: `string` String.

```clojure
(.toJson map)  ;; → JsonObject
```

Converts a **`map`** to **`JsonObject`**.

Parameters: `map` Map.

### `.toMap`

```clojure
(.toMap json)  ;; → Map
```

Converts the given **`json`** to `Map`.

Parameters: `json` JsonObject.

### `updateIn`

```clojure
(updateIn collection path function)  ;; → Any
```

Returns a copy of the nested **`collection`** with the value at **`path`** (a slash-separated string like `"a/0/b"` or a sequence of keys/indices) transformed by applying **`function`** to the current value.

Parameters: `collection` Any, `path` String, `function` Function.

```clojure
(updateIn collection path function)  ;; → Any
```

Returns a copy of the nested **`collection`** with the value at **`path`** (a slash-separated string like `"a/0/b"` or a sequence of keys/indices) transformed by applying **`function`** to the current value.

Parameters: `collection` Any, `path` Sequence, `function` Function.

### `vals`

```clojure
(vals map)  ;; → Array
```

Returns an `Array` containing all values of the given **`map`**.

Parameters: `map` Map.

```clojure
(vals json)  ;; → Array
```

Returns an `Array` containing all property values of the given **`json`** object.

Parameters: `json` JsonObject.

### `.values`

```clojure
(.values map)  ;; → Array
```

Returns an `Array` containing all values of the given **`map`**.

Parameters: `map` Map.

```clojure
(.values json)  ;; → Array
```

Returns an `Array` containing all property values of the given **`json`** object.

Parameters: `json` JsonObject.

## Type tests and conversions

### `.getClassName`

```clojure
(.getClassName object)  ;; → String
```

Returns the fully qualified Java class name for the given **`object`**.

Parameters: `object` Any.

### `.hasDecimals`

```clojure
(.hasDecimals number)  ;; → Boolean
```

Tests if the given **`number`** has decimals.

Parameters: `number` Number.

### `is`

```clojure
(is instance objectName)  ;; → Boolean
```

Tests if the given **`instance`** is an instance of the specified `Object`. The `Object is specified by the given **`object`**.

Parameters: `instance` Any, `objectName` String.

```clojure
(is object class)  ;; → Boolean
```

Checks if the given **`object`** is an instance of the Java `Class` specified by **`class`** object.

Parameters: `object` Any, `class` Any.

### `.isArray`

```clojure
(.isArray object)  ;; → Boolean
```

Test if the given **`object`** is an `Array`.

Parameters: `object` Any.

### `.isBoolean`

```clojure
(.isBoolean form)  ;; → Boolean
```

Tests if the given **`object`** is a `Boolean`.

Parameters: `form` Any.

### `.isByte`

```clojure
(.isByte form)  ;; → Boolean
```

Tests if the given **`object`** is a `Byte`.

Parameters: `form` Any.

### `.isDouble`

```clojure
(.isDouble object)  ;; → Boolean
```

Tests if the given **`object`** is instance of `Double`.

Parameters: `object` Any.

### `.isFalse`

```clojure
(.isFalse object)  ;; → Boolean
```

Tests if the given **`object`** is *logical false* (i.e. `false` or `Null`).

Parameters: `object` Any.

### `.isList`

```clojure
(.isList object)  ;; → Boolean
```

Tests if the given **`object`** is instance of `List`.

Parameters: `object` Any.

### `.isLong`

```clojure
(.isLong object)  ;; → Boolean
```

Test if the given **`object`** is instance of `Long`.

Parameters: `object` Any.

### `.isNotNull`

```clojure
(.isNotNull object)  ;; → Boolean
```

Tests if the given **`object`** is not `Null`.

Parameters: `object` Any.

### `.isNull`

```clojure
(.isNull object)  ;; → Boolean
```

Tests if the given **`object`** is `Null`.

Parameters: `object` Any.

### `.isSet`

```clojure
(.isSet object)  ;; → Boolean
```

Tests if the given **`object`** is instance of `Set`.

Parameters: `object` Any.

### `.isString`

```clojure
(.isString object)  ;; → Boolean
```

Tests if the given **`object`** is instance of `String`.

Parameters: `object` Any.

### `.isSymbol`

```clojure
(.isSymbol object)  ;; → Boolean
```

Tests if the given **`object`** is instance of `Symbol`.

Parameters: `object` Any.

### `.isTrue`

```clojure
(.isTrue object)  ;; → Boolean
```

Tests if the given **`object`** evaluates to *logical true* (i.e. `true` or not `Null`).

Parameters: `object` Any.

### `.isWholeNumber`

```clojure
(.isWholeNumber number)  ;; → Boolean
```

Tests if the given **`number`** is a whole number i.e. a number that does not have decimals.

Parameters: `number` Number.

### `.toArray`

```clojure
(.toArray object)  ;; → Array
```

Converts the given **`object`** to `Array`.

Parameters: `object` Any.

### `.toBoolean`

```clojure
(.toBoolean object)  ;; → Boolean
```

Converts the given **`object`** to `Boolean`.

Parameters: `object` Any.

### `.toByte`

```clojure
(.toByte number)  ;; → Byte
```

Converts a **`number`** to **`Byte`**.

Parameters: `number` Number.

```clojure
(.toByte string)  ;; → Byte
```

Converts a **`string`** to **`Byte`**.

Parameters: `string` String.

```clojure
(.toByte jsonPrimitive)  ;; → Byte
```

Converts a **`jsonPrimitive`** to **`Byte`**.

Parameters: `jsonPrimitive` JsonPrimitive.

### `.toChar`

```clojure
(.toChar object)  ;; → Char
```

Converts a **`object`** to `Char`.

Parameters: `object` Any.

```clojure
(.toChar jsonPrimitive)  ;; → Char
```

Converts a **`jsonPrimitive`** to `Char`.

Parameters: `jsonPrimitive` JsonPrimitive.

### `.toDouble`

```clojure
(.toDouble number)  ;; → Double
```

Converts a **`number`** to `Double`.

Parameters: `number` Number.

```clojure
(.toDouble string)  ;; → Double
```

Converts a **`string`** to `Double`.

Parameters: `string` String.

```clojure
(.toDouble jsonPrimitive)  ;; → Double
```

Converts a **`jsonPrimitive`** to `Double`.

Parameters: `jsonPrimitive` JsonPrimitive.

### `.toFloat`

```clojure
(.toFloat number)  ;; → Float
```

Converts a **`number`** to `Float`.

Parameters: `number` Number.

```clojure
(.toFloat string)  ;; → Float
```

Converts a **`string`** to `Float`.

Parameters: `string` String.

```clojure
(.toFloat jsonPrimitive)  ;; → Float
```

Converts a **`jsonPrimitive`** to `Float`.

Parameters: `jsonPrimitive` JsonPrimitive.

### `.toInt`

```clojure
(.toInt number)  ;; → Int
```

Converts a **`number`** to `Int`.

Parameters: `number` Number.

```clojure
(.toInt string)  ;; → Int
```

Converts a **`string`** to `Int`.

Parameters: `string` String.

```clojure
(.toInt jsonPrimitive)  ;; → Int
```

Converts a **`jsonPrimitive`** to `Int`.

Parameters: `jsonPrimitive` JsonPrimitive.

### `.toList`

```clojure
(.toList object)  ;; → List
```

Converts the given **`object`** to `List`.

Parameters: `object` Any.

### `.toLong`

```clojure
(.toLong number)  ;; → Long
```

Converts a **`number`** to `Long`.

Parameters: `number` Number.

```clojure
(.toLong string)  ;; → Long
```

Converts a **`string`** to `Long`.

Parameters: `string` String.

```clojure
(.toLong jsonPrimitive)  ;; → Long
```

Converts a **`jsonPrimitive`** to `Long`.

Parameters: `jsonPrimitive` JsonPrimitive.

### `.toNumber`

```clojure
(.toNumber number)  ;; → Number
```

Converts a **`number`** to `Number`.

Parameters: `number` Number.

```clojure
(.toNumber string)  ;; → Number
```

Converts a **`string`** to `Number`.

Parameters: `string` String.

```clojure
(.toNumber jsonPrimitive)  ;; → Number
```

Converts a **`jsonPrimitive`** to `Number`.

Parameters: `jsonPrimitive` JsonPrimitive.

### `.toSet`

```clojure
(.toSet object)  ;; → Set
```

Converts the given **`object`** to `Set`.

Parameters: `object` Any.

### `.toShort`

```clojure
(.toShort number)  ;; → Short
```

Converts a **`number`** to `Short`.

Parameters: `number` Number.

```clojure
(.toShort string)  ;; → Short
```

Converts a **`string`** to `Short`.

Parameters: `string` String.

```clojure
(.toShort jsonPrimitive)  ;; → Short
```

Converts a **`jsonPrimitive`** to `Short`.

Parameters: `jsonPrimitive` JsonPrimitive.

### `.toString`

```clojure
(.toString object)  ;; → String
```

Converts the given **`object`** to a `String` representation.

Parameters: `object` Any.

## Dates and time

### `date`

```clojure
(date)  ;; → Date
```

Gives the current date.

### `.day`

```clojure
(.day date)  ;; → Long
```

Returns the day of month represented by the given **`date`**.

Parameters: `date` Date.

### `epochTime`

```clojure
(epochTime)  ;; → Long
```

The epoch time as milliseconds.

### `.hour`

```clojure
(.hour date)  ;; → Long
```

Returns the hour represented by the given **`date`**.

Parameters: `date` Date.

### `.millisecond`

```clojure
(.millisecond date)  ;; → Long
```

Returns the number of seconds past the current minute represented by the given **`date`**.

Parameters: `date` Date.

### `.minute`

```clojure
(.minute date)  ;; → Long
```

Returns the number of minutes past the current hour represented by the given **`date`**.

Parameters: `date` Date.

### `.month`

```clojure
(.month date)  ;; → Long
```

Returns the month represented by the given **`date`**.

Parameters: `date` Date.

### `.second`

```clojure
(.second date)  ;; → Long
```

Returns the number of seconds past the current minute represented by the given **`date`**.

Parameters: `date` Date.

### `.year`

```clojure
(.year date)  ;; → Long
```

Returns the year represented by the given **`date`**.

Parameters: `date` Date.

## Random numbers

### `rand`

```clojure
(rand [max])  ;; → Double
```

Gives a rand double number between 0.0 – 1.0

Parameters: `max` Number.

### `randChoice`

```clojure
(randChoice sequence)  ;; → Any
```

Returns a randomly chosen element from the given **`sequence`** or **`set`**, or `Null` if it is empty.

Parameters: `sequence` Sequence.

```clojure
(randChoice set)  ;; → Any
```

Returns a randomly chosen element from the given **`sequence`** or **`set`**, or `Null` if it is empty.

Parameters: `set` Set.

### `randInt`

```clojure
(randInt max)  ;; → Long
```

Returns a random integer (`Long`) between `0` (inclusive) and **`max`** (exclusive).

Parameters: `max` Number.

```clojure
(randInt min max)  ;; → Long
```

Returns a random integer (`Long`) between **`min`** (inclusive) and **`max`** (exclusive).

Parameters: `min` Number, `max` Number.

### `shuffle`

```clojure
(shuffle sequence)  ;; → Array
```

Returns a new `Array` with the elements of the given **`sequence`** or **`set`** in random order.

Parameters: `sequence` Sequence.

```clojure
(shuffle set)  ;; → Array
```

Returns a new `Array` with the elements of the given **`sequence`** or **`set`** in random order.

Parameters: `set` Set.

## Printing and logging

### `Log.d`

```clojure
(Log.d message)  ;; → Null
```

Creates a log entry at debug level with **`message`**.

Parameters: `message` Any.

### `Log.e`

```clojure
(Log.e message)  ;; → Null
```

Creates a log entry at error level with **`message`**.

Parameters: `message` Any.

### `Log.i`

```clojure
(Log.i message)  ;; → Null
```

Creates a log entry at info level with **`message`**.

Parameters: `message` Any.

### `Log.w`

```clojure
(Log.w message)  ;; → Null
```

Creates a log entry at warning level with **`message`**.

Parameters: `message` Any.

### `print`

```clojure
(print object formatArg…)  ;; → Null
```

Prints the given **`object`** to system output as literal. Given **`formatArgs`** are applied as formatting args if the **`object`** evaluates to a format string.

Parameters: `object` Any, `formatArg` Any.

### `println`

```clojure
(println object formatArg…)  ;; → Null
```

Prints the given **`object`** to system output as literal with a new line. Given **`formatArgs`** are applied as formatting args if the **`object`** evaluates to a format string.

Parameters: `object` Any, `formatArg` Any.

## Evaluation and parsing

### `.compile`

```clojure
(.compile literal)  ;; → ParsedForm
```

Parses the **`literal`** `String` to a form and evaluates the form.

Parameters: `literal` String.

### `.eval`

```clojure
(.eval literal)  ;; → Any
```

Parses the **`literal`** `String` to a form and evaluates the form.

Parameters: `literal` String.

### `eval`

```clojure
(eval object)  ;; → Any
```

Evaluates the given **`object`** and returns the evaluated value.

Parameters: `object` Any.

### `evalString`

```clojure
(evalString string)  ;; → Any
```

Evaluates the given **`string`**. The **`string`** is first parsed to a form which is then evaluated.

Parameters: `string` String.

### `parse`

```clojure
(parse literal)  ;; → Any
```

Parses a form from the given **`string`**.

Parameters: `literal` String.

## Timers and observables

### `.pause`

```clojure
(.pause subscription)  ;; → Subscription
```

Pauses observing the `Observable` specified in the given **`subscription`**.

Parameters: `subscription` Subscription.

### `periodicTimerObservable`

```clojure
(periodicTimerObservable initialDelay period)  ;; → Observable
```

Creates and returns a `PeriodicTimerObservable` with the given **`initialDelay`** (in milliseconds) and update **`period`** (in milliseconds) parameters. `TimeObservable` starts periodically invoking the subscribed observer function or procedure having a signature `(timeNow:Long timeSinceStart:Long)` once the subscription to it activated using function `(subscription .start)`. Other observing control functions are: `(subscription .pause)`, `(subscription .resume)`, and `(subscription .stop)`. The last function also unsubscribes the observer from observing the `PeriodicTimerObservable`.

Parameters: `initialDelay` Number, `period` Number.

### `.resume`

```clojure
(.resume subscription)  ;; → Subscription
```

Resumes observing the `Observable` specified in the given **`subscription`**.

Parameters: `subscription` Subscription.

### `.start`

```clojure
(.start subscription)  ;; → Subscription
```

Start observing the `Observable` specified in the given **`subscription`**.

Parameters: `subscription` Subscription.

### `.stop`

```clojure
(.stop subscription)  ;; → Subscription
```

Stops observing the `Observable` specified in the given **`subscription`**. The observer is also unsubscibed from the `Observable`.

Parameters: `subscription` Subscription.

### `.subscribe`

```clojure
(.subscribe observable observerCallback)  ;; → Subscription
```

Subscribes the given **`observer`** `Callback` to receive callback invocations from the given **`observable`**. Returns a `Subscription` which can be used for cancelling the subscription.

Parameters: `observable` Observable, `observerCallback` Callback.

### `timerObservable`

```clojure
(timerObservable period)  ;; → Observable
```

Creates and returns a `TimerObservable` with the given expiration **`period`** (in milliseconds) parameters. `TimerObservable` is a single shot timer which, when expired, invokes the subscribed observer function or procedure having a signature `(timeNow:Long)`. The timer needs to be started using function `(subscription .start))`. Other observing control functions are: `(subscription .pause))`, `(subscription .resumne))`, and `(subscription .stop))`. The last function also unsubscribes the observer from observing the `TimerObservable`.

Parameters: `period` Number.

### `.unsubscribe`

```clojure
(.unsubscribe subscription)  ;; → Observable
```

Unsubscribes the given **`subscription`**. Returns the `Observable` which originally created the **`subscription`** object.

Parameters: `subscription` Subscription.

## Calculator

### `.add`

```clojure
(.add calculator)  ;; → Double
```

Inputs the addition operation , i.e. calculator button [+], to the given **`calculator`**.

Parameters: `calculator` Calculator.

### `Calculator.new`

```clojure
(Calculator.new)  ;; → Class
```

Creates a new instance of `Calculator`.

### `.clear`

```clojure
(.clear calculator)  ;; → Double
```

Inputs the clear action, i.e. calculator button [C], to the given **`calculator`**.

Parameters: `calculator` Calculator.

### `.clearEntry`

```clojure
(.clearEntry calculator)  ;; → Double
```

Inputs the clear entry action, i.e. calculator button [CE], to the given **`calculator`**.

Parameters: `calculator` Calculator.

### `.decimalPoint`

```clojure
(.decimalPoint calculator)  ;; → Double
```

Adds a decimal point (.) to the number inputted for given **`calculator`**.

Parameters: `calculator` Calculator.

### `.digit`

```clojure
(.digit calculator digit)  ;; → Double
```

Inputs the given *`digit`**, i.e. calculator buttons [0]…[9], to the given **`calculator`**.

Parameters: `calculator` Calculator, `digit` Number.

### `.divide`

```clojure
(.divide calculator)  ;; → Double
```

Inputs the division operation, i.e. calculator button [/], to the given **`calculator`**.

Parameters: `calculator` Calculator.

### `.enter`

```clojure
(.enter calculator)  ;; → Double
```

Inputs the enter action, i.e. calculator button [=], to the given **`calculator`**.

Parameters: `calculator` Calculator.

### `.hasError`

```clojure
(.hasError calculator)  ;; → Boolean
```

Inputs the enter action, i.e. calculator button [=], to the given **`calculator`**.

Parameters: `calculator` Calculator.

### `.multiply`

```clojure
(.multiply calculator)  ;; → Double
```

Inputs the multiplication operation, i.e. calculator button [*], to the given **`calculator`**.

Parameters: `calculator` Calculator.

### `.operand1`

```clojure
(.operand1 calculator)  ;; → Any
```

Gets the current first operand for given **`calculator`**. Returns the first operand as a `Double` or `Null` if there is no first operand at the moment.

Parameters: `calculator` Calculator.

### `.operand2`

```clojure
(.operand2 calculator)  ;; → Any
```

Gets the current second operand for given **`calculator`**. Returns the second operand as a `Double` or `Null` if there is no second operand at the moment.

Parameters: `calculator` Calculator.

### `.operator`

```clojure
(.operator calculator)  ;; → Any
```

Gets the current operator for given **`calculator`**. Returns the operator as a `String` or `Null` if there is no operator at the moment.

Parameters: `calculator` Calculator.

### `.percentage`

```clojure
(.percentage calculator)  ;; → Double
```

Inputs the percentage operation, i.e. calculator button [%], to the given **`calculator`**.

Parameters: `calculator` Calculator.

### `.subtract`

```clojure
(.subtract calculator)  ;; → Double
```

Inputs the addition operation, i.e. calculator button [-], to the given **`calculator`**.

Parameters: `calculator` Calculator.

### `.toggleSign`

```clojure
(.toggleSign calculator)  ;; → Double
```

Toggles the positive to negative or negative number to positive in the given **`calculator`**.

Parameters: `calculator` Calculator.

## Other functions

### `color`

```clojure
(color red green blue [alpha])  ;; → Color
```

Returns a `Color` defined by given **`red`**, **`green`**, **`blue`** and optional **`alpha`** component values. Each component value has to be in range [0..255].

Parameters: `red` Number, `green` Number, `blue` Number, `alpha` Number.


---

# Appendix A: Deprecated names

These older names still work but are deprecated. Use the replacement.

| Deprecated | Use instead |
|---|---|
| `butLast` | `.butLast` |
| `capitalize` | `.capitalize` |
| `charAt` | `.charAt` |
| `chop` | `.chop` |
| `contains` | `.contains` |
| `dec` | `.dec` |
| `endsWith` | `.endsWith` |
| `first` | `.first` |
| `inc` | `.inc` |
| `isArray` | `.isArray` |
| `isBlank` | `.isBlank` |
| `isBoolean` | `.isBoolean` |
| `isByte` | `.isByte` |
| `isDouble` | `.isDouble` |
| `isEven` | `.isEven` |
| `isFalse` | `.isFalse` |
| `isInstance` | `is` |
| `isJsonArray` | `.isJsonArray` |
| `isJsonElement` | `.isJsonElement` |
| `isJsonObject` | `.isJsonObject` |
| `isJsonPrimitive` | `.isJsonPrimitive` |
| `isList` | `.isList` |
| `isLong` | `.isLong` |
| `isMap` | `.isMap` |
| `isNegative` | `.isNegative` |
| `isNotNull` | `.isNotNull` |
| `isNull` | `.isNull` |
| `isOdd` | `.isOdd` |
| `isPositive` | `.isPositive` |
| `isSet` | `.isSet` |
| `isString` | `.isString` |
| `isSymbol` | `.isSymbol` |
| `isTrue` | `.isTrue` |
| `isZero` | `.isZero` |
| `last` | `.last` |
| `matches` | `.matches` |
| `parseBoolean` | `.parseBoolean` |
| `parseByte` | `.parseByte` |
| `parseDouble` | `.parseDouble` |
| `parseFloat` | `.parseFloat` |
| `parseInt` | `.parseInt` |
| `parseLong` | `.parseLong` |
| `repeatString` | `.repeat` |
| `replace` | `.replace` |
| `replaceChar` | `.replaceChar` |
| `replaceFirstRegex` | `.replaceFirstRegex` |
| `replaceFirstString` | `.replaceFirstString` |
| `replaceRegex` | `.replaceRegex` |
| `rest` | `.rest` |
| `shl` | `.shl` |
| `shr` | `.shr` |
| `sign` | `.sign` |
| `size` | `.size` |
| `split` | `.split` |
| `splitLines` | `.splitLines` |
| `startsWith` | `.startsWith` |
| `subArray` | `.subArray` |
| `substring` | `.substring` |
| `tail` | `.tail` |
| `toArray` | `.toArray` |
| `toBoolean` | `.toBoolean` |
| `toByte` | `.toByte` |
| `toChar` | `.toChar` |
| `toDouble` | `.toDouble` |
| `toFloat` | `.toFloat` |
| `toInt` | `.toInt` |
| `toJson` | `.toJson` |
| `toList` | `.toList` |
| `toLong` | `.toLong` |
| `toLowerCase` | `.toLowerCase` |
| `toMap` | `.toMap` |
| `toNumber` | `.toNumber` |
| `toSet` | `.toSet` |
| `toShort` | `.toShort` |
| `toString` | `.toString` |
| `toUpperCase` | `.toUpperCase` |
| `trim` | `.trim` |
| `trimEnd` | `.trimEnd` |
| `trimStart` | `.trimStart` |

# Appendix B: Index

| Name | Section |
|---|---|
| `!=` | Functions: Comparison |
| `%` | Functions: Numbers and math |
| `*` | Functions: Numbers and math |
| `+` | Functions: Numbers and math; Functions: Strings |
| `++` | Macros: Variables and assignment |
| `+=` | Macros: Variables and assignment |
| `-` | Functions: Numbers and math |
| `--` | Macros: Variables and assignment |
| `-=` | Macros: Variables and assignment |
| `->` | Macros: Threading and mapping |
| `->>` | Macros: Threading and mapping |
| `/` | Functions: Numbers and math |
| `<` | Functions: Comparison |
| `<=` | Functions: Comparison |
| `=` | Macros: Variables and assignment |
| `==` | Functions: Comparison |
| `===` | Functions: Comparison |
| `>` | Functions: Comparison |
| `>=` | Functions: Comparison |
| `?` | Macros: Help |
| `?:` | Macros: Conditionals and control flow |
| `abs` | Functions: Numbers and math |
| `acos` | Functions: Numbers and math |
| `.add` | Functions: Sequences and collections; Functions: Calculator |
| `.addAll` | Functions: Sequences and collections |
| `.all` | Functions: Sequences and collections |
| `all` | Functions: Sequences and collections |
| `and` | Functions: Logic; Functions: Bitwise operations |
| `.any` | Functions: Sequences and collections |
| `any` | Functions: Sequences and collections |
| `asin` | Functions: Numbers and math |
| `assoc` | Functions: Maps and JSON |
| `assocAt` | Functions: Sequences and collections |
| `assocIn` | Functions: Maps and JSON |
| `atan` | Functions: Numbers and math |
| `atan2` | Functions: Numbers and math |
| `.average` | Functions: Sequences and collections |
| `average` | Functions: Sequences and collections |
| `bitNot` | Functions: Bitwise operations |
| `.butLast` | Functions: Strings |
| `Calculator.new` | Functions: Calculator |
| `callback` | Macros: Defining functions and callbacks |
| `.capitalize` | Functions: Strings |
| `ceil` | Functions: Numbers and math |
| `.charAt` | Functions: Strings |
| `.chop` | Functions: Strings |
| `clamp` | Functions: Numbers and math |
| `.clear` | Functions: Sequences and collections; Functions: Calculator |
| `.clearEntry` | Functions: Calculator |
| `coalesce` | Macros: Conditionals and control flow |
| `code` | Macros: Defining functions and callbacks |
| `.coerceIn` | Functions: Numbers and math |
| `color` | Functions: Other functions |
| `compare` | Functions: Comparison |
| `.compile` | Functions: Evaluation and parsing |
| `conc` | Functions: Strings |
| `conj` | Functions: Sequences and collections |
| `cons` | Functions: Sequences and collections |
| `.contains` | Functions: Strings |
| `.containsKey` | Functions: Maps and JSON |
| `containsKey` | Functions: Maps and JSON |
| `cos` | Functions: Numbers and math |
| `.count` | Functions: Sequences and collections |
| `count` | Functions: Sequences and collections |
| `date` | Functions: Dates and time |
| `.day` | Functions: Dates and time |
| `.dec` | Functions: Numbers and math |
| `.decimalPoint` | Functions: Calculator |
| `.digit` | Functions: Calculator |
| `dissoc` | Functions: Maps and JSON |
| `.distinct` | Functions: Sequences and collections |
| `distinct` | Functions: Sequences and collections |
| `.divide` | Functions: Calculator |
| `do` | Macros: Conditionals and control flow |
| `.drop` | Functions: Sequences and collections |
| `drop` | Functions: Sequences and collections |
| `.dropLast` | Functions: Sequences and collections |
| `dropLast` | Functions: Sequences and collections |
| `.endsWith` | Functions: Strings |
| `.enter` | Functions: Calculator |
| `epochTime` | Functions: Dates and time |
| `.eval` | Functions: Evaluation and parsing |
| `eval` | Functions: Evaluation and parsing |
| `evalString` | Functions: Evaluation and parsing |
| `exp` | Functions: Numbers and math |
| `.filter` | Functions: Sequences and collections |
| `filter` | Functions: Sequences and collections |
| `.find` | Functions: Sequences and collections |
| `find` | Functions: Sequences and collections |
| `.first` | Functions: Sequences and collections |
| `floor` | Functions: Numbers and math |
| `fn` | Macros: Defining functions and callbacks |
| `.fold` | Functions: Sequences and collections |
| `fold` | Functions: Sequences and collections |
| `for` | Macros: Loops and recursion |
| `forEach` | Macros: Loops and recursion |
| `format` | Functions: Strings |
| `fun` | Macros: Defining functions and callbacks |
| `.get` | Functions: Sequences and collections; Functions: Maps and JSON |
| `get` | Functions: Maps and JSON |
| `.getClassName` | Functions: Type tests and conversions |
| `.groupBy` | Functions: Sequences and collections |
| `groupBy` | Functions: Sequences and collections |
| `.hasDecimals` | Functions: Type tests and conversions |
| `.hasError` | Functions: Calculator |
| `help` | Macros: Help |
| `.hour` | Functions: Dates and time |
| `httpCallback` | Macros: Defining functions and callbacks |
| `hypot` | Functions: Numbers and math |
| `if` | Macros: Conditionals and control flow |
| `if-else` | Macros: Conditionals and control flow |
| `if-let` | Macros: Conditionals and control flow |
| `.inc` | Functions: Numbers and math |
| `.indexOf` | Functions: Sequences and collections |
| `indexOf` | Functions: Sequences and collections |
| `.insertAt` | Functions: Sequences and collections |
| `insertAt` | Functions: Sequences and collections |
| `.inv` | Functions: Bitwise operations |
| `is` | Functions: Type tests and conversions |
| `.isArray` | Functions: Type tests and conversions |
| `.isBlank` | Functions: Strings |
| `.isBoolean` | Functions: Type tests and conversions |
| `.isByte` | Functions: Type tests and conversions |
| `.isDouble` | Functions: Type tests and conversions |
| `.isEmpty` | Functions: Strings; Functions: Sequences and collections |
| `isEmpty` | Functions: Sequences and collections |
| `.isEven` | Functions: Numbers and math |
| `.isFalse` | Functions: Type tests and conversions |
| `.isJsonArray` | Functions: Maps and JSON |
| `.isJsonElement` | Functions: Maps and JSON |
| `.isJsonObject` | Functions: Maps and JSON |
| `.isJsonPrimitive` | Functions: Maps and JSON |
| `.isList` | Functions: Type tests and conversions |
| `.isLong` | Functions: Type tests and conversions |
| `.isMap` | Functions: Maps and JSON |
| `.isNegative` | Functions: Numbers and math |
| `.isNotEmpty` | Functions: Strings; Functions: Sequences and collections |
| `isNotEmpty` | Functions: Sequences and collections |
| `.isNotNull` | Functions: Type tests and conversions |
| `.isNull` | Functions: Type tests and conversions |
| `.isOdd` | Functions: Numbers and math |
| `.isPositive` | Functions: Numbers and math |
| `.isSet` | Functions: Type tests and conversions |
| `.isString` | Functions: Type tests and conversions |
| `.isSymbol` | Functions: Type tests and conversions |
| `.isTrue` | Functions: Type tests and conversions |
| `.isWholeNumber` | Functions: Type tests and conversions |
| `.isZero` | Functions: Numbers and math |
| `.join` | Functions: Sequences and collections |
| `join` | Functions: Sequences and collections |
| `.keys` | Functions: Maps and JSON |
| `keys` | Functions: Maps and JSON |
| `.last` | Functions: Sequences and collections |
| `lazy` | Macros: Quoting |
| `.length` | Functions: Strings |
| `let` | Macros: Variables and assignment |
| `log` | Functions: Numbers and math |
| `Log.d` | Functions: Printing and logging |
| `Log.e` | Functions: Printing and logging |
| `Log.i` | Functions: Printing and logging |
| `Log.w` | Functions: Printing and logging |
| `loop` | Macros: Loops and recursion |
| `map` | Macros: Threading and mapping |
| `.matches` | Functions: Strings |
| `max` | Functions: Numbers and math |
| `merge` | Functions: Maps and JSON |
| `method` | Macros: Defining functions and callbacks |
| `.millisecond` | Functions: Dates and time |
| `min` | Functions: Numbers and math |
| `.minute` | Functions: Dates and time |
| `.month` | Functions: Dates and time |
| `.multiply` | Functions: Calculator |
| `not` | Functions: Logic |
| `nth` | Functions: Sequences and collections |
| `.operand1` | Functions: Calculator |
| `.operand2` | Functions: Calculator |
| `.operator` | Functions: Calculator |
| `or` | Functions: Logic; Functions: Bitwise operations |
| `parse` | Functions: Evaluation and parsing |
| `.parseBoolean` | Functions: Strings |
| `.parseByte` | Functions: Strings |
| `.parseDouble` | Functions: Strings |
| `.parseFloat` | Functions: Strings |
| `.parseInt` | Functions: Strings |
| `.parseLong` | Functions: Strings |
| `.parseShort` | Functions: Strings |
| `.pause` | Functions: Timers and observables |
| `.percentage` | Functions: Calculator |
| `periodicTimerObservable` | Functions: Timers and observables |
| `pow` | Functions: Numbers and math |
| `print` | Functions: Printing and logging |
| `println` | Functions: Printing and logging |
| `proc` | Macros: Defining functions and callbacks |
| `.put` | Functions: Maps and JSON |
| `quote` | Macros: Quoting |
| `rand` | Functions: Random numbers |
| `randChoice` | Functions: Random numbers |
| `randInt` | Functions: Random numbers |
| `range` | Functions: Sequences and collections |
| `recur` | Macros: Loops and recursion |
| `.reduce` | Functions: Sequences and collections |
| `reduce` | Functions: Sequences and collections |
| `rem` | Functions: Numbers and math |
| `.remove` | Functions: Sequences and collections |
| `.removeAt` | Functions: Sequences and collections |
| `removeAt` | Functions: Sequences and collections |
| `.removeKey` | Functions: Maps and JSON |
| `.repeat` | Functions: Strings |
| `repeat` | Macros: Loops and recursion |
| `.replace` | Functions: Strings |
| `.replaceChar` | Functions: Strings |
| `.replaceFirstRegex` | Functions: Strings |
| `.replaceFirstString` | Functions: Strings |
| `.replaceRegex` | Functions: Strings |
| `.rest` | Functions: Sequences and collections |
| `.resume` | Functions: Timers and observables |
| `return` | Macros: Conditionals and control flow |
| `.reverse` | Functions: Sequences and collections |
| `reverse` | Functions: Sequences and collections |
| `round` | Functions: Numbers and math |
| `.second` | Functions: Dates and time |
| `.setAt` | Functions: Sequences and collections |
| `.shl` | Functions: Bitwise operations |
| `.shr` | Functions: Bitwise operations |
| `shuffle` | Functions: Random numbers |
| `.sign` | Functions: Numbers and math |
| `sin` | Functions: Numbers and math |
| `.size` | Functions: Sequences and collections |
| `.sort` | Functions: Sequences and collections |
| `sort` | Functions: Sequences and collections |
| `.sortBy` | Functions: Sequences and collections |
| `sortBy` | Functions: Sequences and collections |
| `.split` | Functions: Strings |
| `.splitLines` | Functions: Strings |
| `sqrt` | Functions: Numbers and math |
| `.start` | Functions: Timers and observables |
| `.startsWith` | Functions: Strings |
| `.stop` | Functions: Timers and observables |
| `str` | Functions: Strings |
| `.subArray` | Functions: Sequences and collections |
| `.subscribe` | Functions: Timers and observables |
| `.substring` | Functions: Strings |
| `.subtract` | Functions: Calculator |
| `.sum` | Functions: Sequences and collections |
| `sum` | Functions: Sequences and collections |
| `switch` | Macros: Conditionals and control flow |
| `.tail` | Functions: Strings |
| `.take` | Functions: Sequences and collections |
| `take` | Functions: Sequences and collections |
| `tan` | Functions: Numbers and math |
| `timerObservable` | Functions: Timers and observables |
| `.toArray` | Functions: Type tests and conversions |
| `.toBoolean` | Functions: Type tests and conversions |
| `.toByte` | Functions: Type tests and conversions |
| `.toChar` | Functions: Type tests and conversions |
| `.toDouble` | Functions: Type tests and conversions |
| `.toFloat` | Functions: Type tests and conversions |
| `toggle!` | Macros: Variables and assignment |
| `.toggleSign` | Functions: Calculator |
| `.toInt` | Functions: Type tests and conversions |
| `.toJson` | Functions: Maps and JSON |
| `.toList` | Functions: Type tests and conversions |
| `.toLong` | Functions: Type tests and conversions |
| `.toLowerCase` | Functions: Strings |
| `.toMap` | Functions: Maps and JSON |
| `.toNumber` | Functions: Type tests and conversions |
| `.toSet` | Functions: Type tests and conversions |
| `.toShort` | Functions: Type tests and conversions |
| `.toString` | Functions: Type tests and conversions |
| `.toUpperCase` | Functions: Strings |
| `.trim` | Functions: Strings |
| `.trimEnd` | Functions: Strings |
| `.trimStart` | Functions: Strings |
| `try` | Macros: Error handling |
| `try-or` | Macros: Error handling |
| `.unsubscribe` | Functions: Timers and observables |
| `update!` | Macros: Variables and assignment |
| `updateIn` | Functions: Maps and JSON |
| `.ushr` | Functions: Bitwise operations |
| `val` | Macros: Variables and assignment |
| `vals` | Functions: Maps and JSON |
| `.values` | Functions: Maps and JSON |
| `var` | Macros: Variables and assignment |
| `when` | Macros: Conditionals and control flow |
| `when-let` | Macros: Conditionals and control flow |
| `while` | Macros: Loops and recursion |
| `xor` | Functions: Logic; Functions: Bitwise operations |
| `.year` | Functions: Dates and time |

# Appendix C: Known quirks

These behaviours are as the library works today. They are listed so that they do not surprise you.

- `%` is "percentage of", not remainder. Use `rem`.
- `/` always gives a `Double`.
- `(== 1 1.0)` is `false`; `1` and `1.0` are different keys.
- `and`, `or` return `true`/`false`, not an operand.
- A single `;` is not a comment; use `;;`.
- Commas are not whitespace.
- `.toJson` writes keyword keys as strings that keep the colon (`{":a":1}`), and cannot convert arrays or plain strings. Use string keys when producing JSON.
- A keyword is not callable: `(:a m)` does not look up `:a`; use `(get m :a)`.
- `.parseInt` and the other `.parse…` functions return `nil`, not an error, for text that is not a number.
- The value forms in a `let` are evaluated before any of its names are bound, so `(let [x 1 y (+ x 1)] y)` fails; nest the `let`s.
- `for` includes its end value.
- `proc` currently fails with an internal error, and `callback` evaluates its code form when it is created, so a callback body that uses its parameters cannot be built that way. Prefer `fn` for functions.
- `Log.d`, `Log.i`, `Log.w` and `Log.e` take one argument.
- Maps do not keep insertion order.

