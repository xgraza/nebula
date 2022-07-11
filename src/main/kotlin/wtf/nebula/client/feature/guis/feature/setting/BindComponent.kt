package wtf.nebula.client.feature.guis.feature.setting

import com.mojang.realmsclient.gui.ChatFormatting
import org.lwjgl.input.Keyboard
import wtf.nebula.client.config.setting.BindSetting
import wtf.nebula.client.feature.guis.common.Component
import wtf.nebula.client.feature.guis.feature.FeatureListGuiScreen

class BindComponent(val bind: BindSetting) : Component(bind.name) {
    var listening = false
        set(value) {
            field = value
            FeatureListGuiScreen.pauseKeyPresses = value
        }

    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val textY = (y + (height / 2.0) - (mc.fontRenderer.FONT_HEIGHT / 2.0)).toFloat()

        mc.fontRenderer.drawStringWithShadow(if (listening) "Listening..." else name, (x + 2.3).toFloat(), textY, -1)

        if (!listening) {
            val keyName = Keyboard.getKeyName(bind.value)
            mc.fontRenderer.drawStringWithShadow(
                ChatFormatting.GRAY.toString() + keyName,
                (x + width - (mc.fontRenderer.getStringWidth(keyName) + 4.0)).toFloat(),
                textY,
                -1
            )
        }
    }

    override fun mouseClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (isMouseInBounds(mouseX, mouseY)) {
            listening = !listening
            playClickSound()
        }
    }

    override fun mouseScroll(mouseX: Int, mouseY: Int, scroll: Int) {

    }

    override fun mouseRelease(mouseX: Int, mouseY: Int, state: Int) {

    }

    override fun keyTyped(keyTyped: Char, keyCode: Int) {
        if (listening) {
            listening = false
            bind.value = if (keyCode == Keyboard.KEY_ESCAPE) Keyboard.KEY_NONE else keyCode
        }
    }
}