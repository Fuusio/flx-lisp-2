package org.fuusio.flx.core

import org.fuusio.flx.Flx
import org.fuusio.flx.FlxUnitTest
import org.fuusio.flx.core.error.FlxException
import org.fuusio.flx.core.error.ObjectException
import org.fuusio.flx.core.macro.MacroSpec
import org.fuusio.flx.core.`object`.MethodCall
import org.fuusio.flx.core.`object`.ObjectDeclaration
import org.fuusio.flx.core.`object`.ObjectInstance
import org.fuusio.flx.core.`object`.ObjectMethod
import org.fuusio.flx.core.`object`.PropertyDeclaration
import org.fuusio.flx.core.`object`.PropertyGetter
import org.fuusio.flx.core.`object`.PropertySetter
import org.fuusio.flx.core.function.FunctionSpec
import org.fuusio.flx.core.vm.Ctx
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** FLX objects as the FLX Visual Editor builds them: declarations, instances, methods and properties. */
class ObjectTest : FlxUnitTest() {

    private fun sym(name: String) = Flx.getSymbol(name)

    /** `Counter`: `val step` (2), `var count` (0), and `(method add [n] (= count (+ count (* n step))))`. */
    private fun counter(name: String = "Counter") = ObjectDeclaration(
        name, 1,
        listOf(
            PropertyDeclaration(sym("step"), isMutable = false, initialValue = list(FunctionSpec.ADDITION, 1L, 1L)),
            PropertyDeclaration(sym("count"), isMutable = true, initialValue = 0L),
        ),
        listOf(
            ObjectMethod(
                sym("add"), arrayOf(sym("n")),
                list(MacroSpec.ASSIGN, sym("count"), list(FunctionSpec.ADDITION, sym("count"), list(FunctionSpec.MULTIPLICATION, sym("n"), sym("step")))),
            ),
            ObjectMethod(sym("break"), emptyArray(), list(MacroSpec.ASSIGN, sym("step"), 5L)),
        ),
    )

    /** Binds `Name.new` as the compiled program does, then evaluates [forms] in the same scope. */
    private fun run(ctx: Ctx, declaration: ObjectDeclaration, vararg forms: Any): Any {
        list(MacroSpec.VAL, sym("${declaration.name}.new"), declaration).eval(ctx)
        var result: Any = Null
        forms.forEach { result = it.eval(ctx) }
        return result
    }

    private fun new(name: String = "Counter") = list(sym("$name.new"))

    @Test
    fun `instances get evaluated initial values, and methods change their vars`() {
        val ctx = ctx()
        val result = run(
            ctx, counter(),
            list(MacroSpec.VAL, sym("c"), new()),
            list(MethodCall("add"), sym("c"), 3L),
            list(MethodCall("add"), sym("c"), 1L),
            list(PropertyGetter("count"), sym("c")),
        )
        assertEquals(8L, result)
        assertEquals(2L, list(PropertyGetter("step"), sym("c")).eval(ctx))
    }

    @Test
    fun `each instance has its own properties`() {
        val ctx = ctx()
        run(ctx, counter(), list(MacroSpec.VAL, sym("a"), new()), list(MacroSpec.VAL, sym("b"), new()))
        list(PropertySetter("count"), sym("a"), 10L).eval(ctx)
        assertEquals(10L, list(PropertyGetter("count"), sym("a")).eval(ctx))
        assertEquals(0L, list(PropertyGetter("count"), sym("b")).eval(ctx))
        assertTrue((sym("a").eval(ctx) as ObjectInstance).toString().startsWith("Counter{step = 2"))
    }

    @Test
    fun `methods are found on the instance's own object, so two objects can share a name`() {
        val ctx = ctx()
        val other = ObjectDeclaration(
            "Other", 2, listOf(PropertyDeclaration(sym("count"), true, 100L)),
            listOf(ObjectMethod(sym("add"), arrayOf(sym("n")), list(MacroSpec.ASSIGN, sym("count"), list(FunctionSpec.SUBTRACTION, sym("count"), sym("n"))))),
        )
        run(ctx, counter(), list(MacroSpec.VAL, sym("a"), new()))
        run(ctx, other, list(MacroSpec.VAL, sym("b"), new("Other")))
        list(MethodCall("add"), sym("a"), 1L).eval(ctx)
        list(MethodCall("add"), sym("b"), 1L).eval(ctx)
        assertEquals(2L, list(PropertyGetter("count"), sym("a")).eval(ctx))
        assertEquals(99L, list(PropertyGetter("count"), sym("b")).eval(ctx))
    }

    @Test
    fun `misuse is reported as FLX errors`() {
        val ctx = ctx()
        run(ctx, counter(), list(MacroSpec.VAL, sym("c"), new()))
        assertThrows(ObjectException::class.java) { list(PropertySetter("step"), sym("c"), 1L).eval(ctx) }
        assertThrows(ObjectException::class.java) { list(PropertyGetter("missing"), sym("c")).eval(ctx) }
        assertThrows(ObjectException::class.java) { list(MethodCall("missing"), sym("c")).eval(ctx) }
        assertThrows(ObjectException::class.java) { list(MethodCall("add"), 5L, 1L).eval(ctx) }
        assertThrows(ObjectException::class.java) { list(MethodCall("add"), sym("c")).eval(ctx) }
        assertThrows(ObjectException::class.java) { list(sym("Counter.new"), 1L).eval(ctx) }
        // A method cannot change a val either: `=` refuses it as everywhere in FLX.
        assertThrows(FlxException::class.java) { list(MethodCall("break"), sym("c")).eval(ctx) }
    }

    @Test
    fun `methods see the program's definitions`() {
        val ctx = ctx()
        list(MacroSpec.VAL, sym("factor"), 10L).eval(ctx)
        val scaled = ObjectDeclaration(
            "Scaled", 3, listOf(PropertyDeclaration(sym("value"), true, 4L)),
            listOf(ObjectMethod(sym("scaled"), emptyArray(), list(FunctionSpec.MULTIPLICATION, sym("value"), sym("factor")))),
        )
        assertEquals(40L, run(ctx, scaled, list(MethodCall("scaled"), new("Scaled"))))
    }

    /** `Rect [w h]`: `val area (* w h)`, `(method twice [] (* 2 (.area this)))`. */
    private fun rect() = ObjectDeclaration(
        "Rect", 4,
        listOf(PropertyDeclaration(sym("area"), isMutable = false, initialValue = list(FunctionSpec.MULTIPLICATION, sym("w"), sym("h")))),
        listOf(
            ObjectMethod(sym("perimeter"), emptyArray(), list(FunctionSpec.MULTIPLICATION, 2L, list(FunctionSpec.ADDITION, sym("w"), sym("h")))),
            ObjectMethod(sym("sum"), emptyArray(), list(FunctionSpec.ADDITION, list(PropertyGetter("area"), sym("this")), list(MethodCall("perimeter"), sym("this")))),
        ),
        parameters = listOf(sym("w"), sym("h")),
    )

    @Test
    fun `constructor args are the instance's parameters, and this is the instance`() {
        val ctx = ctx()
        val result = run(
            ctx, rect(),
            list(MacroSpec.VAL, sym("r"), list(sym("Rect.new"), 3L, 4L)),
            list(MethodCall("sum"), sym("r")),
        )
        // area 12 + perimeter 14
        assertEquals(26L, result)
        assertEquals(12L, list(PropertyGetter("area"), sym("r")).eval(ctx))
        val error = assertThrows(ObjectException::class.java) { list(sym("Rect.new"), 3L).eval(ctx) }
        assertTrue(error.message!!.contains("takes 2 args (w h), not 1"), error.message)
    }

    @Test
    fun `string templates are str calls of their parts`() {
        val ctx = ctx()
        list(MacroSpec.VAL, sym("count"), 3L).eval(ctx)
        val template = StringTemplate.compile("Count: \${count}, next \${(+ count 1)}!")
        assertEquals("Count: 3, next 4!", template.eval(ctx))
        assertEquals("plain", StringTemplate.compile("plain"))
        assertEquals("a \${b}", StringTemplate.compile("a \\\${b}"))
        // Braces and strings inside the form do not end it.
        assertEquals("v x}", StringTemplate.compile("v \${(.get {\"k\" \"x\"} \"k\")}}").eval(ctx))
        assertThrows(StringTemplateException::class.java) { StringTemplate.compile("open \${count") }
        assertThrows(StringTemplateException::class.java) { StringTemplate.compile("two \${a b}") }
    }

    @Test
    fun `wrong args say what was expected and what came`() {
        val ctx = ctx()
        val error = assertThrows(org.fuusio.flx.core.error.InvalidArgTypeException::class.java) {
            list(FunctionSpec.ARRAY__SUB_ARRAY, 5L, 1L).eval(ctx)
        }
        assertTrue(error.message!!.contains("needs an array as"), error.message)
        assertTrue(error.message!!.contains("got a number (5)"), error.message)
        val failed = list(FunctionSpec.ADDITION, 1L, list(FunctionSpec.ARRAY__SUB_ARRAY, 5L, 1L))
        val thrown = assertThrows(org.fuusio.flx.core.error.FlxException::class.java) { failed.eval(ctx) }
        // The innermost form is the one that failed.
        assertTrue(org.fuusio.flx.core.error.FormTrace.formOf(thrown) === (failed as org.fuusio.flx.core.type.impl.ImmutableList).elements[2])
    }
}
