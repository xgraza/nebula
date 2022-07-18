package wtf.nebula.util.render

import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color
import java.awt.Color.*
import kotlin.math.ceil


fun Int.toColor(): Color {
    val rgba = ColorUtil.getColor(this)
    return Color(rgba[0], rgba[1], rgba[2], rgba[3])
}

operator fun Color.component1() = this.red
operator fun Color.component2() = this.green
operator fun Color.component3() = this.blue
operator fun Color.component4() = this.alpha

object ColorUtil {
    fun getColor(color: Int): FloatArray {
        val alpha = (color shr 24 and 255).toFloat() / 255.0f
        val red = (color shr 16 and 255).toFloat() / 255.0f
        val green = (color shr 8 and 255).toFloat() / 255.0f
        val blue = (color and 255).toFloat() / 255.0f
        return floatArrayOf(red, green, blue, alpha)
    }

    fun getColor(r: Int, g: Int, b: Int, a: Int): Int {
        return (r shl 16) + (g shl 8) + b + (a shl 24)
    }

    fun setColor(color: Int) {
        val rgba = getColor(color)
        glColor4f(rgba[0], rgba[1], rgba[2], rgba[3])
    }

    fun addAlpha(color: Int, alpha: Int): Int {
        // clear alpha value and add our alpha value
        return color and 0x00ffffff or alpha shl 24
    }

    fun toHSB(hue: Float, saturation: Float, brightness: Float): Int {
        return Color.getHSBColor(hue, saturation, brightness).rgb
    }

    fun rainbow(delay: Double, saturation: Float, brightness: Float): Int {
        val state = ceil((System.currentTimeMillis() + delay) / 20.0) % 360
        return toHSB((state / 360f).toFloat(), saturation, brightness)
    }
}