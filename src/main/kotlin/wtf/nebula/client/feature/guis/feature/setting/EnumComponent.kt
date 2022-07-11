package wtf.nebula.client.feature.guis.feature.setting

import com.mojang.realmsclient.gui.ChatFormatting
import wtf.nebula.client.config.setting.Setting
import wtf.nebula.client.feature.guis.common.Component

class EnumComponent(val setting: Setting<Enum<*>>) : Component(setting.name) {
    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val textY = (y + (height / 2.0) - (mc.fontRenderer.FONT_HEIGHT / 2.0)).toFloat()
        mc.fontRenderer.drawStringWithShadow(name, (x + 2.3).toFloat(), textY, -1)

        val text = Setting.formatEnum(setting.value)
        mc.fontRenderer.drawStringWithShadow(
            ChatFormatting.GRAY.toString() + text,
            (x + width - (mc.fontRenderer.getStringWidth(text) + 4.0)).toFloat(),
            textY,
            -1)
    }

    override fun mouseClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (isMouseInBounds(mouseX, mouseY)) {
            if (mouseButton == 0) {
                setting.value = Setting.increase(setting.value)
            }

            else if (mouseButton == 1) {
                setting.value = Setting.decrease(setting.value)
            }
        }
    }

    override fun mouseScroll(mouseX: Int, mouseY: Int, scroll: Int) {

    }

    override fun mouseRelease(mouseX: Int, mouseY: Int, state: Int) {

    }

    override fun keyTyped(keyTyped: Char, keyCode: Int) {

    }
}