package wtf.nebula.client.event

import me.bush.eventbuskotlin.Event

open class TickEvent : Event() {
    override val cancellable: Boolean
        get() = false
}