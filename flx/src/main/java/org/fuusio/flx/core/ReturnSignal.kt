package org.fuusio.flx.core

/**
 * How `(return value)` leaves the function it is in: thrown by `return` and caught where the
 * function was called (a function, a method, a callback's code, or the top-level form when there
 * is no function). The blocks between — `do`, `let`, the loops, `try` — let it pass, so a `return`
 * inside a loop ends the function, not just the loop.
 *
 * It has no stack trace, so throwing one costs about what making an object does.
 */
class ReturnSignal(val value: Any) : RuntimeException(null, null, false, false)
