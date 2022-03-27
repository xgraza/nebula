package cope.nebula.asm.mixins.client;

import cope.nebula.client.events.MiddleClickEvent;
import cope.nebula.util.Globals;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "middleClickMouse", at = @At("HEAD"), cancellable = true)
    public void middleClickMouse(CallbackInfo info) {
        if (Globals.EVENT_BUS.post(new MiddleClickEvent())) {
            info.cancel();
        }
    }
}
