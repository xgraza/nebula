package wtf.nebula.client.feature.module.miscellaneous

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.network.play.client.CPacketCustomPayload
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket
import wtf.nebula.client.event.packet.PacketSendEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class NoFML : Module(ModuleCategory.MISCELLANEOUS, "forge... ahahhaha") {
    val handshake by bool("Handshake Packet", true)
    val branding by bool("Branding", true)

    @EventListener
    private val packetSendListener = listener<PacketSendEvent> {
        if (it.packet is FMLProxyPacket) {
            it.cancelled = handshake
        }

        // see NetHandlerPlayClient#handleJoinGame last line
        else if (it.packet is CPacketCustomPayload) {
            val packet = it.packet
            if (packet.channelName == "MC|Brand" && packet.bufferData.isWritable && branding) {

                // override packet buffer
                packet.bufferData.clear()
                packet.bufferData.writeString("vanilla")
            }
        }
    }
}