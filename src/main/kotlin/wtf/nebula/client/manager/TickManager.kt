package wtf.nebula.client.manager

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraftforge.fml.common.gameevent.TickEvent
import wtf.nebula.util.Globals

class TickManager : Globals {
    private var speed = 1.0f

    fun setTimerSpeed(speedIn: Float) {
        speed = speedIn
    }

    fun reset() {
        speed = 1.0f
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        mc.timer.tickLength = 50.0f / speed
    }


}