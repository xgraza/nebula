package cope.nebula.asm.mixins.network.packet.s2c;

import net.minecraft.network.play.server.SPacketPlayerPosLook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketPlayerPosLook.class)
public interface ISPacketPlayerPosLook {
    @Accessor("yaw") void setYaw(float yaw);

    @Accessor("pitch") void setPitch(float pitch);
}
