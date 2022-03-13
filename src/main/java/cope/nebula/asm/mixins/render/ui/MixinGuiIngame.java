package cope.nebula.asm.mixins.render.ui;

import cope.nebula.client.feature.module.render.AntiRender;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {
    @Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
    public void renderVignette(float lightLevel, ScaledResolution scaledRes, CallbackInfo info) {
        if (AntiRender.INSTANCE.isOn() && AntiRender.vignette.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "renderPumpkinOverlay", at = @At("HEAD"), cancellable = true)
    public void renderPumpkinOverlay(ScaledResolution scaledRes, CallbackInfo info) {
        if (AntiRender.INSTANCE.isOn() && AntiRender.blocks.getValue()) {
            info.cancel();
        }
    }
}
