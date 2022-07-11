package wtf.nebula.client.feature.module

import com.mojang.realmsclient.gui.ChatFormatting
import org.lwjgl.input.Keyboard
import wtf.nebula.client.Nebula
import wtf.nebula.client.config.ConfigurableFeature
import wtf.nebula.client.event.ModuleToggledEvent
import wtf.nebula.util.animation.Animation
import wtf.nebula.util.animation.Easing

open class Module(val category: ModuleCategory, val description: String) : ConfigurableFeature() {
    var bind by bind("Bind", Keyboard.KEY_NONE)
    var drawn by bool("Drawn", true)

    val animation = Animation()
        .setEase(Easing.CUBIC_IN_OUT)
        .setSpeed(150.0f)
        .setValue(0.0f)
        .setMin(0.0f)
        .setMax(100.0f)

    override fun onActivated() {
        Nebula.BUS.post(ModuleToggledEvent(this))

        animation.isReversed = false
        animation.value = 0.0f
    }

    override fun onDeactivated() {
        Nebula.BUS.post(ModuleToggledEvent(this))

        animation.isReversed = true
    }

    open fun getDisplayInfo(): String? {
        return null
    }

    fun getInfo(): String {
        val displayInfo = getDisplayInfo() ?: return name

        return StringBuilder()
            .append(name)
            .append(" ")
            .append(ChatFormatting.GRAY.toString())
            .append("[")
            .append(ChatFormatting.WHITE.toString())
            .append(displayInfo)
            .append(ChatFormatting.GRAY.toString())
            .append("]")
            .toString()
    }

    fun isActive(): Boolean {
        if (!toggled) {
            return animation.value > 0.0f;
        }

        return toggled
    }
}