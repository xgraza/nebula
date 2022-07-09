package wtf.nebula.client.event.packet

import me.bush.eventbuskotlin.Event
import net.minecraft.network.Packet

open class BasePacketEvent(val packet: Packet<*>, val dir: PacketDirection) : Event() {
    override val cancellable: Boolean = true
}