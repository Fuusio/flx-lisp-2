package org.fuusio.flx.app.feature.flx_reference.ui

/** The colours the reference page is drawn with, as ARGB ints (`Color.toArgb()`). */
internal data class ReferenceColors(
    val background: Int,
    val foreground: Int,
    val primary: Int,
    val codeBackground: Int,
    val border: Int,
    val isDark: Boolean,
)

private const val THEME_BEGIN = "/*FLX_THEME*/"
private const val THEME_END = "/*END_FLX_THEME*/"

private fun hex(argb: Int) = "#%06x".format(argb and 0xFFFFFF)

/** The CSS variables of the page's theme, for the `<style id="flx-theme">` element. */
internal fun ReferenceColors.toCss(): String =
    ":root { color-scheme: ${if (isDark) "dark" else "light"}; " +
        "--flx-bg: ${hex(background)}; --flx-fg: ${hex(foreground)}; --flx-primary: ${hex(primary)}; " +
        "--flx-code-bg: ${hex(codeBackground)}; --flx-border: ${hex(border)}; }"

/** The page with its default (light) theme block replaced by [css]. */
internal fun String.withThemeCss(css: String): String {
    val begin = indexOf(THEME_BEGIN)
    val end = indexOf(THEME_END)
    if (begin < 0 || end < begin) return this
    return substring(0, begin + THEME_BEGIN.length) + css + substring(end)
}
