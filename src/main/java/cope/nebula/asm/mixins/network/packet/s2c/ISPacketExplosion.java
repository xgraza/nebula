package cope.nebula.asm.mixins.network.packet.s2c;

import net.minecraft.network.play.server.SPacketExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketExplosion.class)
public interface ISPacketExplosion {
    @Accessor void setMotionX(float motionX);
    @Accessor void setMotionY(float motionY);
    @Accessor void setMotionZ(float motionZ);
}
