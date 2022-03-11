package cope.nebula.client.manager;

import cope.nebula.asm.mixins.network.packet.c2s.ICPacketPlayer;
import cope.nebula.client.events.MotionUpdateEvent;
import cope.nebula.client.events.MotionUpdateEvent.Era;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.events.RenderPlayerEvent;
import cope.nebula.util.Globals;
import cope.nebula.util.internal.timing.Stopwatch;
import cope.nebula.util.internal.timing.TimeFormat;
import cope.nebula.util.world.entity.player.rotation.Rotation;
import cope.nebula.util.world.entity.player.rotation.RotationType;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Manages rotations being sent to the server
 *
 * @author aesthetical
 * @since 3/11/22
 */
public class RotationManager implements Globals {
    private Rotation rotation = new Rotation(Float.NaN, Float.NaN);
    private final Stopwatch stopwatch = new Stopwatch();

    public RotationManager() {
        EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.OUTGOING) && event.getPacket() instanceof CPacketPlayer) {
            ICPacketPlayer packet = event.getPacket();
            if (packet.isRotating() && rotation.isValid()) {
                packet.setYaw(rotation.getYaw());
                packet.setPitch(rotation.getPitch());
            }
        }
    }

    @SubscribeEvent
    public void onMotionUpdate(MotionUpdateEvent event) {
        if (event.getEra().equals(Era.PRE)) {
            if (rotation.isValid()) {
                if (stopwatch.hasElapsed(350L, false, TimeFormat.MILLISECONDS)) {
                    resetRotations();
                } else {
                    switch (rotation.getType()) {
                        case CLIENT:
                            mc.player.rotationYaw = rotation.getYaw();
                            mc.player.rotationPitch = rotation.getPitch();
                            break;

                        case SERVER:
                            event.setYaw(rotation.getYaw());
                            event.setPitch(rotation.getPitch());
                            event.setCanceled(true);
                            break;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent event) {
        if (rotation.isValid()) {
            event.setCanceled(true);
        }
    }

    public void setRotation(Rotation rotationIn) {
        rotation = rotationIn;
        stopwatch.resetTime();
    }

    public void resetRotations() {
        rotation = Rotation.INVALID_ROTATION;
    }

    public float getYaw() {
        return rotation.getYaw();
    }

    public float getPitch() {
        return rotation.getPitch();
    }
}
