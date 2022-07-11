package wtf.nebula.client.manager

import wtf.nebula.util.Globals

class TickManager : Globals {
    var speed = 1.0f
        set(value) {
            field = value
            mc.timer.tickLength = 50.0f / value
        }

    fun reset() {
        speed = 1.0f
    }
}