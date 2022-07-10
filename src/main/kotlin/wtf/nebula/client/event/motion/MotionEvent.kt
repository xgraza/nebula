package wtf.nebula.client.event.motion

import me.bush.eventbuskotlin.Event

class MotionEvent(var x: Double, var y: Double, var z: Double) : Event() {
    override val cancellable: Boolean = false
}