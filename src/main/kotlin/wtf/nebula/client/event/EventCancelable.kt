package wtf.nebula.client.event

import me.bush.eventbuskotlin.Event

open class EventCancelable : Event() {
    override val cancellable: Boolean = true
}