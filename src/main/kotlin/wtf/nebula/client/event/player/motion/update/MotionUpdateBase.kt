package wtf.nebula.client.event.player.motion.update

import me.bush.eventbuskotlin.Event

open class MotionUpdateBase(val era: Era) : Event() {
    override val cancellable: Boolean = false
}