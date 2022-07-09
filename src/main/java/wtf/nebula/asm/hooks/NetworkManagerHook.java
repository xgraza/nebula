package wtf.nebula.asm.hooks;

import net.minecraft.network.Packet;
import wtf.nebula.client.Nebula;
import wtf.nebula.client.event.packet.PacketReceiveEvent;
import wtf.nebula.client.event.packet.PacketSendEvent;

public class NetworkManagerHook {

    public static boolean sendPacket(Packet<?> packet) {
        return Nebula.Companion.getBUS().post(new PacketSendEvent(packet));
    }

    public static boolean channelRead0(Packet<?> packet) {
        return Nebula.Companion.getBUS().post(new PacketReceiveEvent(packet));
    }
}
