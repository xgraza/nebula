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
import cope.nebula.util.renderer.FontUtil;
import cope.nebula.util.world.entity.player.MotionUtil;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static cope.nebula.client.feature.module.world.Timer.setTimerSpeed;

/**
 * Credits: https://github.com/Sxmurai/not-konas-src/blob/main/src/main/java/me/darki/konas/module/modules/movement/Speed.java
 *
 * I again, cannot make movement hacks for shit
 */
public class Speed extends Module {
    public Speed() {
        super("Speed", ModuleCategory.MOVEMENT, "Makes you go faster");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.STRAFE);
    public static final Value<Boolean> timer = new Value<>("Timer", false);

    private int strafeStage = 4;
    private double moveSpeed = 0.0;
    private double distance = 0.0;

    private int speedUpTicks = 0;

    private int timerTicks = 0;
    private boolean slowdown = false;

    @Override
    public String getDisplayInfo() {
        return FontUtil.formatText(mode.getValue().name());
    }

    @Override
    protected void onDeactivated() {
        strafeStage = 4;
        moveSpeed = 0.0;
        distance = 0.0;
        timerTicks = 0;

        setTimerSpeed(1.0f);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMotionUpdate(MotionUpdateEvent event) {
        if (event.getEra().equals(Era.PRE)) {
            distance = Math.sqrt(
                    (mc.player.posX - mc.player.prevPosX) * (mc.player.posX - mc.player.prevPosX) +
                    (mc.player.posZ - mc.player.prevPosZ) * (mc.player.posZ - mc.player.prevPosZ));
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.INCOMING) && event.getPacket() instanceof SPacketPlayerPosLook) {
            strafeStage = 4;
            moveSpeed = MotionUtil.getBaseNCPSpeed();
            distance = 0;
        }
    }

    @SubscribeEvent
    public void onMotion(MotionEvent event) {
        if (mode.getValue().equals(Mode.STRAFE)) {
            switch (strafeStage) {
                case 1: {
                    moveSpeed = 1.35 * MotionUtil.getBaseNCPSpeed() - 0.01;
                    break;
                }

                case 2: {
                    if (MotionUtil.isMoving() && mc.player.onGround) {
                        mc.player.motionY = MotionUtil.getJumpHeight(false);
                        event.setY(mc.player.motionY);

                        moveSpeed *= slowdown ? 1.395 : 1.6835;
                    }
                    break;
                }

                case 3: {
                    double adjusted = 0.66 * (distance - MotionUtil.getBaseNCPSpeed());
                    moveSpeed = distance - adjusted;
                    slowdown = !slowdown;
                    break;
                }

                default: {
                    List<AxisAlignedBB> boxes = mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0));
                    if ((boxes.size() > 0 || mc.player.collidedVertically) && strafeStage > 0) {
                        strafeStage = MotionUtil.isMoving() ? 1 : 0;
                    }

                    moveSpeed = distance - distance / 159.0;
                    break;
                }
            }

            moveSpeed = Math.max(moveSpeed, MotionUtil.getBaseNCPSpeed());

            Vec2d motion = MotionUtil.strafe(moveSpeed);

            event.setX(motion.getX());
            event.setZ(motion.getZ());

            ++strafeStage;
        } else if (mode.getValue().equals(Mode.STRICT_STRAFE)) {
            if (timer.getValue()) {
                if (MotionUtil.isMoving()) {
                    setTimerSpeed(1.05f);

                    mc.player.motionX *= 1.019;
                    mc.player.motionZ *= 1.019;
                } else {
                    setTimerSpeed(1.0f);
                }
            }

            if (MotionUtil.isMoving() && mc.player.onGround) {
                strafeStage = 2;
            }

            if (round(mc.player.posY - (int) mc.player.posY, 3) == round(0.138D, 3)) {
                mc.player.motionY -= 0.08D;
                event.setY(event.getY() - 0.09316090325960147D);
                mc.player.posY -= 0.09316090325960147D;
            }

            if (strafeStage == 1 && MotionUtil.isMoving()) {
                strafeStage = 2;
                moveSpeed = 1.38 * MotionUtil.getBaseNCPSpeed() - 0.01;
            } else if (strafeStage == 2) {
                strafeStage = 3;
                mc.player.motionY = MotionUtil.getJumpHeight(true);
                event.setY(mc.player.motionY);

                moveSpeed *= 2.149;
            } else if (strafeStage == 3) {
                strafeStage = 4;
                double adjusted = 0.66 * (distance - MotionUtil.getBaseNCPSpeed());
                moveSpeed = distance - adjusted;
            } else {
                List<AxisAlignedBB> boxes = mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0));
                if (boxes.size() > 0 || mc.player.collidedVertically) {
                    strafeStage = MotionUtil.isMoving() ? 1 : 0;
                }

                moveSpeed = distance - distance / 159.0;
            }

            if (moveSpeed < MotionUtil.getBaseNCPSpeed()) {
                moveSpeed = MotionUtil.getBaseNCPSpeed();
            } else {
                moveSpeed = Math.min(moveSpeed, speedUpTicks > 25 ? 0.511 : 0.5);
            }

            ++speedUpTicks;
            if (speedUpTicks > 50) {
                speedUpTicks = 0;
            }

            Vec2d motion = MotionUtil.strafe(moveSpeed);

            event.setX(motion.getX());
            event.setZ(motion.getZ());
        }
    }

    private double round(double value, int places) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public enum Mode {
        STRAFE, STRICT_STRAFE
    }
}
