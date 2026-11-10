package org.fuusio.flx.core

import org.fuusio.flx.FlxUnitTest
import org.fuusio.flx.core.error.FlxException
import org.fuusio.flx.core.function.FunctionSpec
import org.fuusio.flx.core.type.Color
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * FLX has no reflection: a program cannot load a class by name, create instances of it, or call
 * Java (or Android) methods and fields that FLX does not define itself.
 */
class NoReflectionTest : FlxUnitTest() {

    @Test
    fun `the reflection built-ins are gone`() {
        val symbols = FunctionSpec.entries.map { it.symbol }.toSet()
        listOf("Class.forName", "Class.new", ".getClass", "getClass", ".invoke", "invoke",
            ".getProperty", "getProperty", ".setProperty", "setProperty")
            .forEach { assertFalse(it in symbols, it) }
    }

    @Test
    fun `calls that used reflection fail`() {
        listOf(
            "(Class.forName \"java.lang.Runtime\")",
            "(.exec \"ls\")",
            "(.getBytes \"text\")",
            "(invoke \"text\" \"length\")",
        ).forEach { source -> assertThrows(FlxException::class.java, { eval(source) }, source) }
    }

    @Test
    fun `dot built-ins still work`() {
        assertEquals(5, eval("(.length \"Hello\")"))
    }

    @Test
    fun `is compares type names without loading classes`() {
        assertTrue(eval("(is \"abc\" \"java.lang.String\")") as Boolean)
        assertTrue(eval("(is \"abc\" \"CharSequence\")") as Boolean)
        assertFalse(eval("(is \"abc\" \"java.lang.Runtime\")") as Boolean)
    }

    @Test
    fun `colours work without android graphics`() {
        val color = Color(0xFE, 0x3F, 0x00)
        assertEquals(0xFFFE3F00.toInt(), color.colorInt)
        assertEquals(listOf(0xFF, 0xFE, 0x3F, 0x00), listOf(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()))
        assertEquals("#7FFE3F00", color.alpha(0.5f).toLiteral())
        assertEquals("#00000000", Color.transparent.toLiteral())
        assertEquals("#FFFE3F00", Color.hilla.toLiteral())
    }
}
