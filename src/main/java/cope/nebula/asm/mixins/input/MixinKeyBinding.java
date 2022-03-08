package cope.nebula.asm.mixins.input;

import cope.nebula.client.events.KeyDownEvent;
import cope.nebula.util.Globals;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class MixinKeyBinding {
    @Shadow private boolean pressed;
    @Shadow private int keyCode;

    @Inject(method = "isKeyDown", at = @At("RETURN"), cancellable = true)
    public void isKeyDown(CallbackInfoReturnable<Boolean> info) {
        KeyDownEvent event = new KeyDownEvent(keyCode);
        Globals.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.setReturnValue(pressed);
        } else {
            info.setReturnValue(info.getReturnValue());
        }
    }
}
