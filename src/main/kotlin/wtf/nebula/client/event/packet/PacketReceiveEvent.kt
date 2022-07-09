package wtf.nebula.client.event.packet

import net.minecraft.network.Packet

class PacketReceiveEvent(packet: Packet<*>) : BasePacketEvent(packet, PacketDirection.RECEIVE)