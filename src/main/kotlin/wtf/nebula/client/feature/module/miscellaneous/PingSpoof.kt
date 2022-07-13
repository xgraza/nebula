package wtf.nebula.client.feature.module.miscellaneous

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketConfirmTransaction
import net.minecraft.network.play.client.CPacketKeepAlive
import wtf.nebula.client.event.packet.PacketSendEvent
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import java.util.concurrent.ConcurrentHashMap

class PingSpoof : Module(ModuleCategory.MISCELLANEOUS, "spoofs ur ping") {
    val delay by double("Delay", 100.0, 25.0..5000.0)

    private val packets = ConcurrentHashMap<Long, Packet<*>>()

    override fun getDisplayInfo(): String? {
        return delay.toString()
    }

    override fun onDeactivated() {
        super.onDeactivated()
        packets.clear()
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        for ((time, packet) in packets) {
            if (System.currentTimeMillis() - time >= delay.toLong()) {
                mc.player.connection.networkManager.dispatchPacket(packet, null)
                packets.remove(time)
            }
        }
    }

    @EventListener
    private val sendPacketListener = listener<PacketSendEvent> {
        if (it.packet is CPacketKeepAlive || it.packet is CPacketConfirmTransaction) {
            packets[System.currentTimeMillis()] = it.packet
            it.cancel()
        }
    }
}