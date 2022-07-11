package wtf.nebula.client.event.input

import me.bush.eventbuskotlin.Event
import net.minecraft.util.MovementInput

class MovementInputEvent(val movementInput: MovementInput) : Event() {
    override val cancellable: Boolean = false
}