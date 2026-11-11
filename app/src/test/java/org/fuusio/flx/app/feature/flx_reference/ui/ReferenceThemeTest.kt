package org.fuusio.flx.app.feature.flx_reference.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ReferenceThemeTest {

    private val dark = ReferenceColors(
        background = 0xFF101418.toInt(),
        foreground = 0xFFE0E2E8.toInt(),
        primary = 0xFFA8C8FF.toInt(),
        codeBackground = 0xFF272A2F.toInt(),
        border = 0xFF43474E.toInt(),
        isDark = true,
    )

    @Test
    fun `colours become CSS variables without the alpha channel`() {
        val css = dark.toCss()
        assertTrue(css.contains("color-scheme: dark"))
        assertTrue(css.contains("--flx-bg: #101418"))
        assertTrue(css.contains("--flx-fg: #e0e2e8"))
        assertTrue(css.contains("--flx-primary: #a8c8ff"))
        assertTrue(css.contains("--flx-code-bg: #272a2f"))
        assertTrue(css.contains("--flx-border: #43474e"))
    }

    @Test
    fun `a light theme says so`() {
        assertTrue(dark.copy(isDark = false).toCss().contains("color-scheme: light"))
    }

    @Test
    fun `the theme block of a page is replaced and the rest is kept`() {
        val page = "<style>/*FLX_THEME*/:root { old }/*END_FLX_THEME*/</style><p>text</p>"
        assertEquals(
            "<style>/*FLX_THEME*/:root { new }/*END_FLX_THEME*/</style><p>text</p>",
            page.withThemeCss(":root { new }"),
        )
    }

    @Test
    fun `a page without a theme block is left alone`() {
        assertEquals("<p>x</p>", "<p>x</p>".withThemeCss(":root { new }"))
    }

    @Test
    fun `the generated reference page has a theme block and no dangling anchor links`() {
        val html = File("src/main/assets/flx-reference.html").readText()
        assertEquals(1, Regex(Regex.escape("/*FLX_THEME*/")).findAll(html).count())
        assertTrue(html.contains("<style id=\"flx-theme\">"))
        val ids = Regex("id=\"([^\"]+)\"").findAll(html).map { it.groupValues[1] }.toSet()
        val missing = Regex("href=\"flx-anchor:([^\"]+)\"").findAll(html).map { it.groupValues[1] }.filter { it !in ids }.toList()
        assertTrue("links without a target: $missing", missing.isEmpty())
        assertFalse(html.contains("@@BODY@@"))
    }
}
