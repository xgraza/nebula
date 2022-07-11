package wtf.nebula.client.feature.module.world

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import wtf.nebula.client.event.player.motion.MotionEvent
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class Avoid : Module(ModuleCategory.WORLD, "Avoids shit") {
    val cacti by bool("Cacti", true)
    val unloaded by bool("Unloaded", true)
    val void by bool("Void", true)

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (void && mc.player.posY <= 0.0) {
            mc.player.motionY = 0.0
        }
    }

    @EventListener
    private val motionListener = listener<MotionEvent> {
        if (unloaded && !mc.world.isBlockLoaded(mc.player.position)) {
            it.cancel()
        }
    }
}