package wtf.nebula.asm.hooks;

import net.minecraft.network.Packet;
import wtf.nebula.client.Nebula;
import wtf.nebula.client.event.packet.PacketReceiveEvent;
import wtf.nebula.client.event.packet.PacketSendEvent;

public class NetworkManagerHook {

    public static boolean sendPacket(Packet<?> packet) {
        PacketSendEvent event = new PacketSendEvent(packet);
        Nebula.Companion.getBUS().post(event);
        return event.getCancelled();
    }

    public static boolean channelRead0(Packet<?> packet) {
        PacketReceiveEvent event = new PacketReceiveEvent(packet);
        Nebula.Companion.getBUS().post(event);
        return event.getCancelled();
    }
}
