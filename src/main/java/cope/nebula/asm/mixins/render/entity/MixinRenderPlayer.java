package cope.nebula.asm.mixins.render.entity;

import cope.nebula.client.Nebula;
import cope.nebula.client.events.RenderPlayerEvent;
import cope.nebula.client.feature.module.render.LogoutSpots;
import cope.nebula.client.feature.module.render.Nametags;
import cope.nebula.util.Globals;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public class MixinRenderPlayer {
    private float renderPitch;
    private float renderYaw;
    private float renderHeadYaw;
    private float prevRenderHeadYaw;
    private float lastRenderHeadYaw = 0.0f;
    private float prevRenderPitch;
    private float lastRenderPitch = 0.0f;

    private boolean b = false;

    @Inject(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V", at = @At("HEAD"))
    public void doRenderPre(AbstractClientPlayer player, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (Globals.EVENT_BUS.post(new RenderPlayerEvent()) && player.equals(Globals.mc.player)) {
            b = true;

            float yaw = Nebula.getInstance().getRotationManager().getYaw();
            float pitch = Nebula.getInstance().getRotationManager().getPitch();

            renderPitch = player.rotationPitch;
            renderYaw = player.rotationYaw;

            renderHeadYaw = player.rotationYawHead;

            prevRenderHeadYaw = player.prevRotationYawHead;
            prevRenderPitch = player.prevRotationPitch;

            player.rotationPitch = pitch;
            player.rotationYaw = yaw;

            player.renderYawOffset = yaw;
            player.rotationYawHead = yaw;

            player.prevRotationYawHead = lastRenderHeadYaw;
            player.prevRotationPitch = lastRenderPitch;
        }
    }

    @Inject(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V", at = @At("TAIL"))
    public void doRenderPost(AbstractClientPlayer player, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (player.equals(Globals.mc.player) && b) {
            lastRenderHeadYaw = player.rotationYawHead;
            lastRenderPitch = player.rotationPitch;

            player.rotationPitch = renderPitch;
            player.rotationYaw = renderYaw;

            player.prevRotationPitch = prevRenderPitch;

            player.rotationYawHead = renderHeadYaw;
            player.prevRotationYawHead = prevRenderHeadYaw;

            b = false;
        }
    }

    @Inject(method = "renderEntityName(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDLjava/lang/String;D)V", at = @At("HEAD"), cancellable = true)
    public void renderEntityName(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo info) {
        if (LogoutSpots.spawnedFakes.stream().anyMatch(
                (fake) -> fake.getUniqueID().equals(entityIn.getUniqueID()))) {

            info.cancel();
            return;
        }

        if (Nametags.INSTANCE.isOn()) {
            info.cancel();
        }
    }
}
