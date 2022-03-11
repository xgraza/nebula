package cope.nebula.asm.mixins.network.packet.c2s;

import net.minecraft.network.play.client.CPacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketPlayer.class)
public interface ICPacketPlayer {
    @Accessor("rotating") boolean isRotating();

    @Accessor void setYaw(float yaw);

    @Accessor void setPitch(float pitch);
}
