package cope.nebula.asm.duck;

import net.minecraft.network.Packet;

public interface INetworkManager {
    void sendPacketSilent(Packet<?> packetIn);
}
