package cope.nebula.asm.mixins.render.ui;

import cope.nebula.client.feature.module.render.AntiRender;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.toasts.GuiToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiToast.class)
public class MixinGuiToast {
    @Inject(method = "drawToast", at = @At("HEAD"), cancellable = true)
    public void drawToast(ScaledResolution resolution, CallbackInfo info) {
        if (AntiRender.INSTANCE.isOn() && AntiRender.toasts.getValue()) {
            info.cancel();
        }
    }
}
