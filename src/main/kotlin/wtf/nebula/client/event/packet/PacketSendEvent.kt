package wtf.nebula.client.event.packet

import net.minecraft.network.Packet

class PacketSendEvent(packet: Packet<*>) : BasePacketEvent(packet, PacketDirection.SEND)