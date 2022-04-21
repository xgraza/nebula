package cope.nebula.asm.mixins.client;

import cope.nebula.client.events.MiddleClickEvent;
import cope.nebula.client.feature.module.world.MultiTask;
import cope.nebula.util.Globals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "middleClickMouse", at = @At("HEAD"), cancellable = true)
    public void middleClickMouse(CallbackInfo info) {
        if (Globals.EVENT_BUS.post(new MiddleClickEvent())) {
            info.cancel();
        }
    }

    @Redirect(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isHandActive()Z"))
    public boolean isHandActiveHook(EntityPlayerSP player) {
        return MultiTask.INSTANCE.isOff() && player.isHandActive();
    }

    @Redirect(method = "rightClickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;getIsHittingBlock()Z"))
    public boolean getIsHittingBlockHook(PlayerControllerMP controllerMP) {
        return MultiTask.INSTANCE.isOff() && controllerMP.getIsHittingBlock();
    }
}
