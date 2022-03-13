package cope.nebula.asm.mixins.render.entity;

import cope.nebula.client.feature.module.render.AntiRender;
import net.minecraft.client.renderer.entity.RenderXPOrb;
import net.minecraft.entity.item.EntityXPOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderXPOrb.class)
public class MixinRenderXPOrb {
    @Inject(method = "doRender(Lnet/minecraft/entity/item/EntityXPOrb;DDDFF)V", at = @At("HEAD"), cancellable = true)
    public void doRender(EntityXPOrb entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (AntiRender.INSTANCE.isOn() && AntiRender.xp.getValue()) {
            info.cancel();
        }
    }
}
