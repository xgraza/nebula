package wtf.nebula.client.feature.guis.common

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.init.SoundEvents
import wtf.nebula.util.Globals

abstract class Component(val name: String) : Globals {
    var x = 0.0
    var y = 0.0
    var width = 0.0
    var height = 0.0

    val children = mutableListOf<Component>()

    abstract fun render(mouseX: Int, mouseY: Int, partialTicks: Float)
    abstract fun mouseClick(mouseX: Int, mouseY: Int, mouseButton: Int)
    abstract fun mouseScroll(mouseX: Int, mouseY: Int, scroll: Int)
    abstract fun mouseRelease(mouseX: Int, mouseY: Int, state: Int)
    abstract fun keyTyped(keyTyped: Char, keyCode: Int)

    fun isMouseInBounds(mouseX: Int, mouseY: Int): Boolean {
        return isMouseWithinBounds(mouseX, mouseY, x, y, width, height)
    }

    open fun isVisible(): Boolean {
        return true
    }

    companion object : Globals {

        fun isMouseWithinBounds(mouseX: Int, mouseY: Int, x: Double, y: Double, w: Double, h: Double): Boolean {
            return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h
        }

        fun playClickSound() {
            mc.soundHandler.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f))
        }
    }
}