package cope.nebula.asm.mixins.client;

import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Timer.class)
public interface ITimer {
    @Accessor void setTickLength(float tickLength);
}
