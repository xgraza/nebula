package wtf.nebula.client.feature.module.movement

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.util.ext.isMoving

class TickShift : Module(ModuleCategory.MOVEMENT, "legit just use timer lel") {
    val ticks by int("Ticks", 20, 1..120)
    val boost by float("Boost", 1.0f, 0.5f..5.0f)

    private var boostTicks = 0

    override fun getDisplayInfo(): String? {
        return boostTicks.toString()
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (mc.player.isMoving()) {
            --boostTicks

            if (boostTicks <= 0) {
                boostTicks = 0
                Nebula.serverManager.speed = 1.0f
            }

            else {
                Nebula.serverManager.speed = 1.0f + boost
            }
        }

        else {
            ++boostTicks
            if (boostTicks > ticks) {
                boostTicks = ticks
            }
        }
    }
}