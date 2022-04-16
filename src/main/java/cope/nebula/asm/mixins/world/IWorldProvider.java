package cope.nebula.asm.mixins.world;

import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldProvider.class)
public interface IWorldProvider {
    @Accessor("lightBrightnessTable") float[] getLightBrightnessTable();
}
