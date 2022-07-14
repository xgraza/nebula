package wtf.nebula.client.feature.module.movement

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketExplosion
import wtf.nebula.client.event.packet.PacketReceiveEvent
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.event.world.PushOutOfBlocksEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class Velocity : Module(ModuleCategory.MOVEMENT, "removes velocity its 1am i hate this") {
    val cancel by bool("Cancel", false)
    val noPush by bool("No Push", true)
    val rods by bool("Fishing Rods", true)

    private var entityCollisionReduction = -1.0f

    override fun onDeactivated() {
        super.onDeactivated()

        if (entityCollisionReduction != -1.0f) {
            mc.player.entityCollisionReduction = entityCollisionReduction
            entityCollisionReduction = -1.0f
        }
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (noPush) {
            if (entityCollisionReduction == -1.0f) {
                entityCollisionReduction = mc.player.entityCollisionReduction
            }

            mc.player.entityCollisionReduction = 1.0f
        }
    }

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

    @EventListener
    private val pushOutOfBlocksListener = listener<PushOutOfBlocksEvent> {
        it.cancelled = noPush
    }
}