package wtf.nebula.client.feature.module.miscellaneous

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.network.play.client.CPacketConfirmTeleport
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketPlayerPosLook
import wtf.nebula.client.event.packet.PacketReceiveEvent
import wtf.nebula.client.event.packet.PacketSendEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class NoRotate : Module(ModuleCategory.MISCELLANEOUS, "fuck ncp") {
    private var server = false
    private var confirm = false

    private var yaw = 0.0f
    private var pitch = 0.0f

    @EventListener
    private val packetSendListener = listener<PacketSendEvent> {
        if (it.packet is CPacketConfirmTeleport) {
            if (server) {
                confirm = true
            }
        }

        else if (it.packet is CPacketPlayer.PositionRotation) {
            if (server && confirm) {
                it.packet.yaw = yaw
                it.packet.pitch = pitch

                yaw = 0.0f
                pitch = 0.0f

                server = false
                confirm = false
            }
        }
    }

    @EventListener
    private val packetReceiveListener = listener<PacketReceiveEvent> {
        if (it.packet is SPacketPlayerPosLook) {
            val packet = it.packet

            yaw = packet.getYaw()
            pitch = packet.getPitch()

            server = true

            if (packet.flags.contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
                yaw += mc.player.rotationYaw
            }

            if (packet.flags.contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
                pitch += mc.player.rotationPitch
            }

            packet.yaw = mc.player.rotationYaw
            packet.pitch = mc.player.rotationPitch
        }
    }
}