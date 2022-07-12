package wtf.nebula.client.feature.module.movement

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketExplosion
import wtf.nebula.client.event.packet.PacketReceiveEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class Velocity : Module(ModuleCategory.MOVEMENT, "removes velocity its 1am i hate this") {
    val cancel by bool("Cancel", false)
    val rods by bool("Fishing Rods", true)

    @EventListener
    private val packetReceiveListener = listener<PacketReceiveEvent> {
        if (it.packet is SPacketEntityVelocity) {
            if (cancel) {
                it.cancelled = true
                return@listener
            }

            val packet = it.packet

            packet.motionX = 0
            packet.motionY = 0
            packet.motionZ = 0
        }

        else if (it.packet is SPacketExplosion) {
            if (cancel) {
                it.cancelled = true
                return@listener
            }

            val packet = it.packet

            packet.motionX = 0.0f
            packet.motionY = 0.0f
            packet.motionZ = 0.0f
        }
    }
}