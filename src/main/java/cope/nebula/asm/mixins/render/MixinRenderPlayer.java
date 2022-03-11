package cope.nebula.asm.mixins.render;

import cope.nebula.client.Nebula;
import cope.nebula.client.events.RenderPlayerEvent;
import cope.nebula.util.Globals;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public class MixinRenderPlayer {
    private float prevYaw = Float.NaN, prevPitch = Float.NaN;

    @Inject(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V", at = @At("HEAD"))
    public void doRenderPre(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (Globals.EVENT_BUS.post(new RenderPlayerEvent()) && entity.equals(Globals.mc.player)) {
            prevYaw = entity.rotationYaw;
            prevPitch = entity.rotationPitch;

            float yaw = Nebula.getInstance().getRotationManager().getYaw();
            float pitch = Nebula.getInstance().getRotationManager().getPitch();

            entity.rotationYaw = yaw;
            entity.rotationYawHead = yaw;
            entity.renderYawOffset = yaw;
            entity.prevRotationYaw = yaw;
            entity.prevRotationYawHead = yaw;

            entity.rotationPitch = pitch;
            entity.prevRotationPitch = pitch;
        }
    }

    @Inject(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V", at = @At("TAIL"))
    public void doRenderPost(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (!Float.isNaN(prevYaw) && !Float.isNaN(prevPitch)) {
            entity.rotationYaw = prevYaw;
            entity.rotationYawHead = prevYaw;

            entity.prevRotationYaw = prevYaw;
            entity.prevRotationYawHead = prevYaw;

            entity.rotationPitch = prevPitch;
            entity.prevRotationPitch = prevPitch;

            // reset previous rotations
            prevPitch = Float.NaN;
            prevYaw = Float.NaN;
        }
    }
}
