package cope.nebula.client.feature.module.movement;

import cope.nebula.client.events.MotionEvent;
import cope.nebula.client.events.MotionUpdateEvent;
import cope.nebula.client.events.MotionUpdateEvent.Era;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.internal.math.Vec2d;
import cope.nebula.util.world.entity.player.MotionUtil;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketPlayerPosLook.EnumFlags;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Set;

public class PacketFly extends Module {
    public PacketFly() {
        super("PacketFly", ModuleCategory.MOVEMENT, "Makes you fly with packets");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.SETBACK);
    public static final Value<Double> factor = new Value<>("Factor", 1.0, 0.1, 5.0);

    private int tpId = 0;

    @SubscribeEvent
    public void onMotion(MotionEvent event) {
        event.setX(mc.player.motionX);
        event.setY(mc.player.motionY);
        event.setZ(mc.player.motionZ);
    }

    @SubscribeEvent
    public void onMotionUpdate(MotionUpdateEvent event) {
        if (event.getEra().equals(Era.POST)) {
            if (mc.gameSettings.keyBindJump.isPressed()) {
                mc.player.motionY = mc.player.ticksExisted % 6 == 0 ? 0.01 : 0.02;
            } else if (mc.gameSettings.keyBindSneak.isPressed()) {
                mc.player.motionY = -0.02;
            } else {
                mc.player.motionY = 0.0;
            }

            if (MotionUtil.isMoving()) {
                Vec2d motion = MotionUtil.strafe(factor.getValue());

                if (mode.getValue().equals(Mode.FACTOR)) {
                    double factorX = motion.getX() / factor.getValue();
                    double factorZ = motion.getZ() / factor.getValue();

                    mc.player.motionX = motion.getX();
                    mc.player.motionZ = motion.getZ();

                    for (int i = 0; i < factor.getValue().intValue(); ++i) {
                        sendPacket(mc.player.posX + (factorX * i), mc.player.posY + mc.player.motionY, mc.player.posZ + (factorZ * i), false);
                    }

                    sendPacket(mc.player.posX + mc.player.motionX, mc.player.posY + mc.player.motionY, mc.player.posZ + mc.player.motionZ, true);
                } else if (mode.getValue().equals(Mode.SETBACK)) {
                    mc.player.motionX = motion.getX();
                    mc.player.motionZ = motion.getZ();

                    sendPacket(mc.player.posX + mc.player.motionX, mc.player.posY + mc.player.motionY, mc.player.posZ + mc.player.motionZ, true);
                }
            } else {
                mc.player.motionX = 0.0;
                mc.player.motionZ = 0.0;
            }
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        switch (event.getDirection()) {
            case INCOMING: {
                if (event.getPacket() instanceof SPacketPlayerPosLook) {
                    SPacketPlayerPosLook packet = event.getPacket();

                    tpId = packet.getTeleportId();

                    double x = packet.getX();
                    double y = packet.getY();
                    double z = packet.getZ();

                    float yaw = packet.getYaw();
                    float pitch = packet.getPitch();

                    Set<EnumFlags> flags = packet.getFlags();

                    if (flags.contains(EnumFlags.X)) {
                        x += mc.player.posX;
                    } else {
                        mc.player.motionX = 0.0;
                    }

                    if (flags.contains(EnumFlags.Y)) {
                        y += mc.player.posY;
                    } else {
                        mc.player.motionY = 0.0;
                    }

                    if (flags.contains(EnumFlags.Z)) {
                        z += mc.player.posZ;
                    } else {
                        mc.player.motionZ = 0.0;
                    }

                    if (flags.contains(EnumFlags.X_ROT)) {
                        pitch += mc.player.rotationPitch;
                    }

                    if (flags.contains(EnumFlags.Y_ROT)) {
                        yaw += mc.player.rotationYaw;
                    }

                    mc.player.setPositionAndRotation(x, y, z, yaw, pitch);

                    mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpId));
                    mc.player.connection.sendPacket(new PositionRotation(mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                }
                break;
            }
        }
    }

    private void sendPacket(double x, double y, double z, boolean bounds) {
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpId++));
        mc.player.connection.sendPacket(new PositionRotation(x, y, z, mc.player.rotationYaw, mc.player.rotationPitch, false));

        if (bounds) {
            mc.player.connection.sendPacket(new PositionRotation(x, y - 1337.0, z, mc.player.rotationYaw, mc.player.rotationPitch, false));
        }
    }

    public enum Mode {
        FACTOR, SETBACK
    }
}