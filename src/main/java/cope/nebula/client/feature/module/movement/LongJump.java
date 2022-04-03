package cope.nebula.client.feature.module.movement;

import cope.nebula.client.events.MotionEvent;
import cope.nebula.client.events.MotionUpdateEvent;
import cope.nebula.client.events.MotionUpdateEvent.Era;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.internal.math.Vec2d;
import cope.nebula.util.world.entity.player.MotionUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LongJump extends Module {
    public LongJump() {
        super("LongJump", ModuleCategory.MOVEMENT, "Jumps long");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.NCP);
    public static final Value<Boolean> waitForDamage = new Value<>("WaitForDamage", false);
    public static final Value<Double> boost = new Value<>("Boost", 10.0, 0.1, 20.0);
    public static final Value<Integer> timeout = new Value<>("Timeout", 4, 0, 10);
    public static final Value<Boolean> disable = new Value<>("Disable", true);

    private int stage = 1;
    private double moveSpeed = 0.0;
    private double distance = 0.0;

    private int timeoutTicks = 0;
    private int airTicks = 0;

    @Override
    protected void onActivated() {
        stage = 1;
        moveSpeed = 0.0;
        distance = 0.0;

        timeoutTicks = 0;
        airTicks = 0;
    }

    @Override
    public void onTick() {
        if (waitForDamage.getValue()) {
            if (mc.player.hurtTime == 0) {
                timeoutTicks = -1;
            } else {
                timeoutTicks = timeout.getValue() + 1;
            }
        }

        ++timeoutTicks;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMotionUpdate(MotionUpdateEvent event) {
        if (event.getEra().equals(Era.PRE)) {
            distance = Math.sqrt(Math.pow(mc.player.posX - mc.player.prevPosX, 2) + Math.pow(mc.player.posZ - mc.player.prevPosZ, 2));
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.INCOMING)) {
            if (event.getPacket() instanceof SPacketPlayerPosLook && disable.getValue()) {
                disable();
            }
        } else if (event.getDirection().equals(Direction.OUTGOING)) {
            if (event.getPacket() instanceof CPacketPlayer && timeoutTicks < timeout.getValue()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onMotion(MotionEvent event) {
        if (timeoutTicks < timeout.getValue()) {
            event.setX(mc.player.motionX = 0.0);
            event.setZ(mc.player.motionZ = 0.0);

            return;
        }

        if (mode.getValue().equals(Mode.NCP)) {
            if (mc.player.onGround) {
                stage = 2;
            }

            if (stage == 1) {
                moveSpeed = boost.getValue() * MotionUtil.getBaseNCPSpeed();
            } else if (stage == 2) {
                if (mc.player.onGround && MotionUtil.isMoving()) {
                    double jumpHeight = MotionUtil.getJumpHeight(false);

                    mc.player.motionY = jumpHeight;
                    event.setY(jumpHeight);

                    moveSpeed *= 2.149;
                }
            } else if (stage == 3) {
                double diff = 0.66 * (distance - MotionUtil.getBaseNCPSpeed());
                moveSpeed = distance - diff;
            } else {
                moveSpeed = distance - distance / 159.0;
            }

            moveSpeed = Math.max(moveSpeed, MotionUtil.getBaseNCPSpeed());

            Vec2d vec = MotionUtil.strafe(moveSpeed);

            event.setX(vec.getX());
            event.setZ(vec.getZ());

            ++stage;
        } else if (mode.getValue().equals(Mode.VANILLA)) {
            if (mc.player.onGround && MotionUtil.isMoving()) {
                double jumpHeight = MotionUtil.getJumpHeight(true);

                moveSpeed = MotionUtil.getBaseNCPSpeed();

                mc.player.motionY = jumpHeight;
                event.setY(jumpHeight);
            }

            if (moveSpeed < 0.0) {
                moveSpeed = boost.getValue();
                airTicks = 1;
            }

            if (!mc.player.onGround) {
                double speed = MotionUtil.getBaseNCPSpeed() * boost.getValue();

                ++airTicks;
                if (airTicks > 0) {
                    moveSpeed = speed - (airTicks / 100.0);
                }
            }

            Vec2d vec = MotionUtil.strafe(moveSpeed);

            event.setX(vec.getX());
            event.setZ(vec.getZ());
        }
    }

    public enum Mode {
        NCP,
        VANILLA
    }
}
