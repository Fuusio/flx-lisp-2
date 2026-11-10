<p align="center">
  <img src="app/src/main/res/drawable/flx_logo_512x512px.png" alt="FLX Lisp logo" width="200" />
</p>

<h1 align="center">FLX Lisp</h1>

<p align="center">
  A small, embeddable Lisp for Kotlin and Android.
</p>

---

FLX Lisp is a Lisp dialect implemented in pure Kotlin. Embed it in an Android app to script your app,
let users write small programs, or explore Lisp interactively. The repository contains the FLX
library and a demo app with an interactive REPL.

## Features

- **Lisp syntax:** lists, vectors `[…]`, maps `{…}`, sets `#{…}`, keywords `:name` and strings.
- **Immutable collections:** values are shared safely between evaluations.
- **A large standard library:** about 350 built-in functions and 44 macros for arithmetic and
  math, strings, collections and sequences (`map`, `filter`, `reduce`, `sortBy`, `groupBy`, …),
  maps (`assoc`, `assocIn`, `updateIn`, …), JSON, dates, random numbers and logging.
- **Functions and closures:** `fun`, `fn`, `let`, `loop`/`recur`, threading macros `->` and `->>`,
  `if-let`, `when-let`, `try`/`catch` and `try-or`.
- **Method-style calls:** most functions can also be called as methods, e.g. `(.filter xs .isEven)`.
- **Objects:** declarations with properties and methods.
- **Kotlin interop:** Kotlin and Java classes can be exposed to FLX through proxies.
- **Timers and observables:** `timerObservable` and `periodicTimerObservable`, with `.subscribe`.
- **A virtual machine:** `FlxVM` evaluates programs on a large-stack worker thread, and can be
  started, paused, resumed and stopped.

## A taste of FLX

```clojure
(fun square [x] (* x x))

(val numbers (range 1 6))

(println (map square numbers))        ; prints [1 4 9 16 25]

(reduce + (map square numbers))       ; => 55

(-> 3 (+ 4) (* 2))                    ; => 14

(filter [1 2 3 4] .isEven)            ; => [2 4]

(map :name [{:name "A"} {:name "B"}]) ; => ["A" "B"]

(try-or (/ 10 0) 0)                   ; => 0

(loop [i 0 sum 0]
  (if-else (> i 10) sum (recur (+ i 1) (+ sum i))))  ; => 55
```

## Project structure

| Module | Description |
|---|---|
| `flx` | The FLX Lisp library: parser, evaluator, built-in functions and macros, and the VM (`org.fuusio.flx`). |
| `app` | The demo app (`org.fuusio.flx.app`), built with Jetpack Compose and the [Kide](https://www.fuusio.org/Kide) architecture. |

### The demo app

The demo app has a navigation drawer with these screens:

- **FLX Info:** an introduction to FLX, with a link to this repository.
- **FLX REPL:** an interactive read-eval-print loop with command history, examples, and copy,
  clear and reset actions.
- **Settings:** the theme (Light, Dark or System).
- **About:** version, contact and open-source licenses.

## Getting started

FLX is published on Maven Central. Add the dependency to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.fuusio.flx:flx:2.0.0")
}
```

Make sure `mavenCentral()` is among your repositories. FLX requires Android API level 29 or
higher.

### Evaluate FLX code

Initialise the global FLX context once, then evaluate source text:

```kotlin
import org.fuusio.flx.Flx
import org.fuusio.flx.Program
import org.fuusio.flx.core.toLiteral
import org.fuusio.flx.core.vm.Ctx

// Once, for example in Application.onCreate(). Setting the root context registers
// the built-in functions and macros.
if (!Flx.isInitialized()) {
    Flx.ctx = Ctx().apply { program = Program() }
}

val result = Flx.eval("(reduce + [1 2 3 4])")
println(result.toLiteral())  // 10
```

### Run a session in the VM

For an isolated session whose definitions can be thrown away, and for deep recursion, run the
code in a `FlxVM` with its own context:

```kotlin
import org.fuusio.flx.core.vm.FlxVM

val sessionCtx = Ctx(parent = Flx.ctx)
val vm = FlxVM(sessionCtx).also {
    sessionCtx.vm = it
    sessionCtx.program = Program()
    it.start()
}

// Blocks the calling thread; call it off the main thread.
val value = vm.evalWithLargeStack("(fun fact [n] (if-else (< n 2) 1 (* n (fact (- n 1))))) (fact 20)")

vm.stop()
```

What a program prints with `print` and `println` goes to `vm.out`, which you can replace with your
own `PrintStream` to capture output. The demo app's
[`LocalFlxRuntime`](app/src/main/java/org/fuusio/flx/app/feature/repl/data/LocalFlxRuntime.kt)
is a complete example.

## Building

Requirements:

- Android Studio with the Android SDK (compile SDK 37; min SDK 29 for the library, 33 for the demo app)
- JDK 21

```bash
./gradlew :app:assembleDebug        # build the demo app
./gradlew :flx:testDebugUnitTest    # run the library's unit tests
```

## Releasing

See [RELEASING.md](RELEASING.md) for how the library is published, and
[CHANGELOG.md](CHANGELOG.md) for what changed in each version.

## Contributing

Issues and pull requests are welcome at
[github.com/Fuusio/flx-lisp-2](https://github.com/Fuusio/flx-lisp-2).

## License

Copyright © 2016 - 2026 Marko Salmela

Licensed under the Apache License, Version 2.0. You may obtain a copy of the License at
<http://www.apache.org/licenses/LICENSE-2.0>.

Unless required by applicable law or agreed to in writing, software distributed under the License
is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing permissions and limitations under the
License.
