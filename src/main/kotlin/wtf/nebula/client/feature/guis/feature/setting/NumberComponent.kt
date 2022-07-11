package wtf.nebula.client.feature.guis.feature.setting

import com.mojang.realmsclient.gui.ChatFormatting
import org.lwjgl.input.Mouse
import wtf.nebula.client.config.setting.NumberSetting
import wtf.nebula.client.feature.guis.common.Component
import wtf.nebula.client.feature.module.render.Colors
import wtf.nebula.util.render.RenderUtil
import java.awt.Color
import kotlin.math.roundToInt

class NumberComponent(val setting: NumberSetting<Number>) : Component(setting.name) {
    private var dragging = false
    private val difference = setting.max.toFloat() - setting.min.toFloat()

    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (dragging) {
            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            else {
                setValue(mouseX)
            }
        }

        val percent = (setting.value.toFloat() - setting.min.toFloat()) / difference
        val barWidth = if (setting.value.toFloat() <= setting.min.toFloat()) 0.0 else width * percent

        // Color(191, 52, 52, 140).rgb
        RenderUtil.rect(x, y, barWidth, height, Colors.color())

        val textY = (y + (height / 2.0) - (mc.fontRenderer.FONT_HEIGHT / 2.0)).toFloat()
        mc.fontRenderer.drawStringWithShadow(name, (x + 2.3).toFloat(), textY, -1)

        mc.fontRenderer.drawStringWithShadow(
            ChatFormatting.GRAY.toString() + setting.value.toString(),
            (x + width - (mc.fontRenderer.getStringWidth(setting.value.toString()) + 4.0)).toFloat(),
            textY,
            -1)
    }

    override fun mouseClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (isMouseInBounds(mouseX, mouseY)) {
            dragging = true

            playClickSound()
            setValue(mouseX)
        }
    }

    override fun mouseScroll(mouseX: Int, mouseY: Int, scroll: Int) {

    }

    override fun mouseRelease(mouseX: Int, mouseY: Int, state: Int) {
        if (dragging && state == 0) {
            dragging = false
        }
    }

    override fun keyTyped(keyTyped: Char, keyCode: Int) {

    }

    private fun setValue(mouseX: Int) {
        val percent = ((mouseX.toDouble() - x) / width.toFloat()).toFloat()

        when (setting.value) {
            is Float -> {
                val result = setting.min.toFloat() + difference * percent
                setting.value = (10.0f * result).roundToInt() / 10.0f
            }
            is Double -> {
                val result = setting.min.toDouble() + difference * percent
                setting.value = (10.0 * result).roundToInt() / 10.0
            }
            else -> {
                setting.value = (setting.min.toInt() + difference * percent).roundToInt()
            }
        }

        if (setting.value.toFloat() > setting.max.toFloat()) {
            setting.value = setting.max
        }

        if (setting.value.toFloat() < setting.min.toFloat()) {
            setting.value = setting.min
        }
    }
}