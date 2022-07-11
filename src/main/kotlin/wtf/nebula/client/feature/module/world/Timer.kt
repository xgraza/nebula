package wtf.nebula.client.feature.module.world

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraftforge.fml.common.gameevent.TickEvent
import wtf.nebula.client.Nebula
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class Timer : Module(ModuleCategory.WORLD, "Makes game go fast fast or slow slow") {
    val speed by float("Speed", 1.0f, 0.1f..20.0f)
    val tpsSync by bool("TPS Sync", false)

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (tpsSync) {
            Nebula.tickManager.setTimerSpeed(1.0f) // TODO
        }

        else {
            Nebula.tickManager.setTimerSpeed(speed)
        }
    }
}