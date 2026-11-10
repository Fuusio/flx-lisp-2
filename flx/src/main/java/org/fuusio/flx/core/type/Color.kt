@file:Suppress("unused")

package org.fuusio.flx.core.type

import org.fuusio.flx.core.EvaluableObject

data class Color(val colorInt: Int) : EvaluableObject() {

    // The components are computed here rather than with android.graphics.Color, so that the class
    // also works on the plain JVM (unit tests), where the Android classes are only stubs.
    constructor(red: Int, green: Int, blue: Int, alpha: Int = 255) :
            this(((alpha and 0xff) shl 24) or ((red and 0xff) shl 16) or ((green and 0xff) shl 8) or (blue and 0xff))

    fun getAlpha() = (colorInt ushr 24) and 0xff

    fun getRed() = (colorInt shr 16) and 0xff

    fun getGreen() = (colorInt shr 8) and 0xff

    fun getBlue() = colorInt and 0xff

    override fun isColor() = false

    override fun toLiteral(): String {
        val red = String.format("%02X", (0xff and getRed()))
        val green = String.format("%02X", (0xff and getGreen()))
        val blue = String.format("%02X", (0xff and getBlue()))
        val alpha = String.format("%02X", (0xff and getAlpha()))
        return "#$alpha$red$green$blue"
    }

    fun alpha(alpha: Float): Color {
        val normalisedAlpha  = when {
            alpha < 0 -> 0f
            alpha > 1.0 -> 1.0f
            else -> alpha
        }
        return Color(getRed(), getGreen(), getBlue(), (normalisedAlpha * 255f).toInt())
    }

    companion object {
        val hilla = Color(0xfe, 0x3f, 0x00)
        val white = Color(0xff, 0xff, 0xff)
        val black = Color(0x00, 0x00, 0x00)
        val blueGray050 = Color(0xEC, 0xEF, 0xF1)
        val blueGray300 = Color(0x90, 0xA4, 0xAE)
        val blueGray600 = Color(0x54, 0x6E, 0x7A)
        val blueGray800 = Color(0x37, 0x47, 0x4F)
        val blueGray900 = Color(0x26, 0x32, 0x38) // #263238
        val transparent = Color(0x00, 0x00, 0x00, alpha = 0x00)
    }
}