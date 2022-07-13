package wtf.nebula.client.event.input

import me.bush.eventbuskotlin.Event
import net.minecraft.client.settings.KeyBinding

class KeyBindingPressedEvent(val keyBinding: KeyBinding) : Event() {
    override val cancellable: Boolean = false

    var pressed = keyBinding.isPressed
}