package cope.nebula.asm.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface IMinecraft {
    @Accessor void setRightClickDelayTimer(int rightClickDelayTimer);

    @Accessor Timer getTimer();
}
