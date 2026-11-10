package org.fuusio.flx.core.error

import org.fuusio.flx.core.vm.Ctx

/** A built-in asked for more than it will make in one call: `(range 0 1000000000)`. */
class ResultTooLargeException(ctx: Ctx, private val functionName: String, private val limit: String) : FlxException(ctx) {

    override val message: String
        get() = "'$functionName' failed: the result would be larger than $limit"
}
