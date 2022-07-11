package wtf.nebula.client.event.player.motion

import me.bush.eventbuskotlin.Event

class MotionEvent(var x: Double, var y: Double, var z: Double) : Event() {
    override val cancellable: Boolean = true
}