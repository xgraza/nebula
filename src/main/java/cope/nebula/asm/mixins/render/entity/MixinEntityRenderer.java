package cope.nebula.asm.mixins.render.entity;

import cope.nebula.client.feature.module.render.AntiRender;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Shadow private ItemStack itemActivationItem;

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void hurtCameraEffect(float partialTicks, CallbackInfo info) {
        if (AntiRender.INSTANCE.isOn() && AntiRender.hurtcam.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    public void setupFog(int startCoords, float partialTicks, CallbackInfo info) {
        if (AntiRender.INSTANCE.isOn() && AntiRender.fog.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "renderItemActivation", at = @At("HEAD"), cancellable = true)
    public void renderItemActivation(int p_190563_1_, int p_190563_2_, float p_190563_3_, CallbackInfo info) {
        if (AntiRender.INSTANCE.isOn() &&
                AntiRender.totems.getValue() &&
                itemActivationItem != null &&
                itemActivationItem.getItem().equals(Items.TOTEM_OF_UNDYING)) {

            info.cancel();
        }
    }
}
