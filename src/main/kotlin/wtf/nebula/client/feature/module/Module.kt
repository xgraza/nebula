package wtf.nebula.client.feature.module

import org.lwjgl.input.Keyboard
import wtf.nebula.client.config.ConfigurableFeature
import wtf.nebula.util.animation.Animation
import wtf.nebula.util.animation.Easing

open class Module(val category: ModuleCategory, val description: String) : ConfigurableFeature() {
    var bind by bind("Bind", Keyboard.KEY_NONE)

    val animation = Animation()
        .setEase(Easing.QUINTIC_IN_OUT)
        .setSpeed(2500.0f)

    fun isActive(): Boolean {
        return toggled && animation.value >= animation.min
    }
}