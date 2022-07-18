package wtf.nebula.client.feature.guis.feature.setting

import wtf.nebula.client.config.setting.Setting
import wtf.nebula.client.feature.guis.common.Component
import wtf.nebula.util.render.RenderUtil
import java.awt.Color

class ColorComponent(val setting: Setting<Color>) : Component(setting.name) {
    private var expanded = false

    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        RenderUtil.rect(x, y, width, 15.0, Color(41, 41, 41).rgb)

        val textY = (y + (15.0 / 2.0) - (mc.fontRenderer.FONT_HEIGHT / 2.0)).toFloat()
        mc.fontRenderer.drawStringWithShadow(name, (x + 2.3).toFloat(), textY, -1)

        val indicator = if (expanded) "-" else "+"
        mc.fontRenderer.drawStringWithShadow(indicator, (x + width - (mc.fontRenderer.getStringWidth(indicator) + 4.0)).toFloat(), textY, -1)

        if (!expanded) {
            height = 15.0
        }

        else {
            height = 15.0 + COLOR_PICKER_HEIGHT
        }
    }

    override fun mouseClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (isMouseWithinBounds(mouseX, mouseY, x, y, width, 15.0)) {
            playClickSound()
            expanded = !expanded
        }
    }

    override fun mouseScroll(mouseX: Int, mouseY: Int, scroll: Int) {

    }

    override fun mouseRelease(mouseX: Int, mouseY: Int, state: Int) {

    }

    override fun keyTyped(keyTyped: Char, keyCode: Int) {

    }

    companion object {
        private const val COLOR_PICKER_HEIGHT = 100.0
    }
}