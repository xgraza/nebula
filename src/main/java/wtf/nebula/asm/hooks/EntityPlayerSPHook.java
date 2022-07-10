package wtf.nebula.asm.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import wtf.nebula.client.Nebula;
import wtf.nebula.client.event.motion.update.PostMotionUpdate;
import wtf.nebula.client.event.motion.update.PreMotionUpdate;

public class EntityPlayerSPHook {
    public static boolean playerSprintState = false;
    public static boolean playerSneakState = false;

    public static double prevX, prevY, prevZ;
    public static float prevYaw;
    public static float prevPitch;
    public static boolean lastGround;

    private static int posUpdateTicks = 0;

    public static boolean preMotionUpdate() {
        Minecraft mc = Minecraft.getMinecraft();
        PreMotionUpdate event = new PreMotionUpdate(
                mc.player.posX,
                mc.player.getEntityBoundingBox().minY,
                mc.player.posZ,
                mc.player.rotationYaw,
                mc.player.rotationPitch,
                mc.player.onGround
        );

        boolean canceled = Nebula.Companion.getBUS().post(event);

        if (canceled) {
            updateMotionPlayer(event);
        }

        return canceled;
    }

    public static void postMotionUpdate() {
        Nebula.Companion.getBUS().post(new PostMotionUpdate());
    }

    private static void updateMotionPlayer(PreMotionUpdate e) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;

        if (playerSprintState != player.isSprinting()) {
            playerSprintState = player.isSprinting();
            send(new CPacketEntityAction(player, player.isSprinting() ? Action.START_SPRINTING : Action.STOP_SPRINTING));
        }

        if (playerSneakState != player.isSneaking()) {
            playerSneakState = player.isSneaking();
            send(new CPacketEntityAction(player, player.isSprinting() ? Action.START_SNEAKING : Action.STOP_SNEAKING));
        }

        if (player.equals(Minecraft.getMinecraft().getRenderViewEntity())) {

            double x = e.getX();
            double y = e.getY();
            double z = e.getZ();

            float yaw = e.getYaw();
            float pitch = e.getPitch();

            boolean ground = e.getOnGround();

            double xDiff = x - prevX;
            double yDiff = y - prevY;
            double zDiff = z - prevZ;

            ++posUpdateTicks;
            boolean moved = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff > 9.0E-4 || posUpdateTicks >= 20;

            float yawDiff = yaw - prevYaw;
            float pitchDiff = pitch - prevPitch;

            boolean rotated = yawDiff != 0.0f || pitchDiff != 0.0f;

            if (player.isRiding()) {
                send(new PositionRotation(player.motionX, -999.0, player.motionZ, yaw, pitch, ground));
                moved = false;
            }

            else if (moved && rotated) {
                send(new PositionRotation(x, y, z, yaw, pitch, ground));
            }

            else if (moved) {
                send(new Position(x, y, z, ground));
            }

            else if (rotated) {
                send(new Rotation(yaw, pitch, ground));
            }

            else if (ground != lastGround) {
                send(new CPacketPlayer(ground));
            }

            if (moved) {
                prevX = x;
                prevY = y;
                prevZ = z;
                posUpdateTicks = 0;
            }

            if (rotated) {
                prevYaw = yaw;
                prevPitch = pitch;
            }

            lastGround = ground;
        }
    }

    private static void send(Packet<?> packet) {
        Minecraft.getMinecraft().player.connection.sendPacket(packet);
    }
}
