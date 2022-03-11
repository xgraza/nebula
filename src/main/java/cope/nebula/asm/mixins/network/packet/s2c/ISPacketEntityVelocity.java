package cope.nebula.asm.mixins.network.packet.s2c;

import net.minecraft.network.play.server.SPacketEntityVelocity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketEntityVelocity.class)
public interface ISPacketEntityVelocity {
    @Accessor void setMotionX(int motionX);
    @Accessor void setMotionY(int motionY);
    @Accessor void setMotionZ(int motionZ);
}
