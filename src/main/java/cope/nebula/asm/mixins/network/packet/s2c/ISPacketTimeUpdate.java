package cope.nebula.asm.mixins.network.packet.s2c;

import net.minecraft.network.play.server.SPacketTimeUpdate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketTimeUpdate.class)
public interface ISPacketTimeUpdate {
    @Accessor("worldTime") void setWorldTime(long worldTime);
}
