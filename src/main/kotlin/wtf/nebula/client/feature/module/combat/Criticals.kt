package wtf.nebula.client.feature.module.combat

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketUseEntity
import org.lwjgl.input.Keyboard
import wtf.nebula.client.event.packet.PacketSendEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class Criticals : Module(ModuleCategory.COMBAT, "Lands critical hits") {
    init {
        bind = Keyboard.KEY_C
    }

    val mode by setting("Mode", Mode.PACKET)
    val stopSprint by bool("Stop Sprint", true)

    @EventListener
    private val packetSendListener = listener<PacketSendEvent> {
        if (it.packet is CPacketUseEntity) {
            val packet = it.packet

            if (packet.action == CPacketUseEntity.Action.ATTACK) {

                if (stopSprint && mc.player.isSprinting) {
                    mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING))
                }

                when (mode) {
                    Mode.PACKET -> {
                        position(0.0624)
                        position(0.0)
                    }

                    Mode.STRICTNCP -> {
                        position(0.11)
                        position(0.1100013579)
                        position(0.0000013579)
                    }

                    Mode.MOTION -> {
                        mc.player.motionY = 0.11
                    }
                }
            }
        }
    }

    private fun position(y: Double, minY: Boolean = false) {
        var start = mc.player.posY
        if (minY) {
            start = mc.player.entityBoundingBox.minY
        }

        mc.player.connection.sendPacket(CPacketPlayer.Position(
            mc.player.posX, start + y, mc.player.posZ, false))
    }

    enum class Mode {
        PACKET, STRICTNCP, MOTION
    }
}