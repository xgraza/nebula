package cope.nebula.asm.mixins.world;

import cope.nebula.client.feature.module.render.AntiRender;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public class MixinWorld {
    @Inject(method = "spawnParticle(Lnet/minecraft/util/EnumParticleTypes;ZDDDDDD[I)V", at = @At("HEAD"), cancellable = true)
    public void spawnParticle(EnumParticleTypes particleType, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int[] parameters, CallbackInfo info) {
        if (AntiRender.INSTANCE.isOn() && AntiRender.explosions.getValue() && AntiRender.EXPLOSION_PARTICLES.contains(particleType)) {
            info.cancel();
        }
    }
}
