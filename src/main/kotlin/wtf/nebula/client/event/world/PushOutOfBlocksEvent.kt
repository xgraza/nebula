package wtf.nebula.client.event.world

import me.bush.eventbuskotlin.Event

class PushOutOfBlocksEvent : Event() {
    override val cancellable: Boolean = false
}