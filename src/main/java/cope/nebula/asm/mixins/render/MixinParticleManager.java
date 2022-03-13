package cope.nebula.asm.mixins.render;

import cope.nebula.client.feature.module.render.AntiRender;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class MixinParticleManager {
    @Inject(method = "renderParticles", at = @At("HEAD"), cancellable = true)
    public void renderParticles(Entity entityIn, float partialTicks, CallbackInfo info) {
        if (AntiRender.INSTANCE.isOn() && AntiRender.particles.getValue()) {
            info.cancel();
        }
    }
}
