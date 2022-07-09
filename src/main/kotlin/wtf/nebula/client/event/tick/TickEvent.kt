package wtf.nebula.client.event.tick

import me.bush.eventbuskotlin.Event

open class TickEvent : Event() {
    override val cancellable: Boolean
        get() = false
}