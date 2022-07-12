package wtf.nebula.asm.hooks;

import net.minecraft.entity.player.EntityPlayer;
import wtf.nebula.client.Nebula;
import wtf.nebula.util.rotation.Rotation;

public class RenderPlayerHook {
    private static float renderPitch;
    private static float renderYaw;
    private static float renderHeadYaw;
    private static float prevRenderHeadYaw;
    private static float lastRenderHeadYaw = 0.0f;
    private static float prevRenderPitch;
    private static float lastRenderPitch = 0.0f;

    public static void updatePlayerRotations(EntityPlayer player) {
        if (Nebula.Companion.getRotationManager().getRotation().getValid()) {
            renderPitch = player.rotationPitch;
            renderYaw = player.rotationYaw;

            renderHeadYaw = player.rotationYawHead;

            prevRenderHeadYaw = player.prevRotationYawHead;
            prevRenderPitch = player.prevRotationPitch;

            Rotation rotation = Nebula.Companion.getRotationManager().getRotation();

            player.rotationYawHead = rotation.getYaw();
            player.renderYawOffset = rotation.getYaw();

            player.rotationPitch = rotation.getPitch();
            player.prevRotationPitch = rotation.getPitch();
        }
    }

    public static void resetPlayerRotations(EntityPlayer player) {
        if (Nebula.Companion.getRotationManager().getRotation().getValid()) {
//            lastRenderHeadYaw = player.rotationYawHead;
//            lastRenderPitch = player.rotationPitch;
//
            player.rotationPitch = renderPitch;
            player.prevRotationPitch = prevRenderPitch;
        }
    }
}
