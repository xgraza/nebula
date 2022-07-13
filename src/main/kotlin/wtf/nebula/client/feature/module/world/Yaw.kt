package wtf.nebula.client.feature.module.world

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import java.lang.Math.round

class Yaw : Module(ModuleCategory.WORLD, "Locks your yaw") {
    @EventListener
    private val tickListener = listener<TickEvent> {
        mc.player.rotationYaw = round((mc.player.rotationYaw + 1.0f) / 45.0f) * 45.0f;
    }
}