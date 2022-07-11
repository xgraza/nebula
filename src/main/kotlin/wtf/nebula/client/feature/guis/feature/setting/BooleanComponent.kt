package wtf.nebula.client.feature.guis.feature.setting

import wtf.nebula.client.config.setting.Setting
import wtf.nebula.client.feature.guis.common.Component

class BooleanComponent(val setting: Setting<Boolean>) : Component(setting.name) {
    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val textY = (y + (height / 2.0) - (mc.fontRenderer.FONT_HEIGHT / 2.0)).toFloat()
        mc.fontRenderer.drawStringWithShadow(name, (x + 2.3).toFloat(), textY, -1)
    }

    override fun mouseClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (isMouseInBounds(mouseX, mouseY)) {
            playClickSound()
            setting.value = !setting.value
        }
    }

    override fun mouseScroll(mouseX: Int, mouseY: Int, scroll: Int) {

    }

    override fun mouseRelease(mouseX: Int, mouseY: Int, state: Int) {

    }

    override fun keyTyped(keyTyped: Char, keyCode: Int) {

    }
}