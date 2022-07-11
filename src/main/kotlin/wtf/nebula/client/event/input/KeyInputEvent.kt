package wtf.nebula.client.event.input

import me.bush.eventbuskotlin.Event

class KeyInputEvent(val keyCode: Int, val state: Boolean) : Event() {
    override val cancellable: Boolean
        get() = false
}