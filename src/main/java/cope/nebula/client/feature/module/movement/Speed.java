package cope.nebula.client.feature.module.movement;

import cope.nebula.client.events.MotionEvent;
import cope.nebula.client.events.MotionUpdateEvent;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.internal.math.Vec2d;
import cope.nebula.util.renderer.FontUtil;
import cope.nebula.util.world.entity.player.MotionUtil;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    private int strafeStage = 1;
    private double moveSpeed = 0.0;
    private double distance = 0.0;

    private int timerTicks = 0;
    private boolean slowdown = false;

    @Override
    public String getDisplayInfo() {
        return FontUtil.formatText(mode.getValue().name());
    }

    @Override
    protected void onDeactivated() {
        strafeStage = 1;
        moveSpeed = 0.0;
        distance = 0.0;
        timerTicks = 0;

        setTimerSpeed(1.0f);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMotionUpdate(MotionUpdateEvent event) {
        distance = Math.sqrt(Math.pow(mc.player.posX - mc.player.prevPosX, 2) + Math.pow(mc.player.posZ - mc.player.prevPosZ, 2));
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
        if (mode.getValue().equals(Mode.STRAFE) || mode.getValue().equals(Mode.STRICT_STRAFE)) {
            if (MotionUtil.isMoving()) {
                if (mc.player.onGround) {
                    strafeStage = 2; // jump
                }

                if (timer.getValue()) {
                    ++timerTicks;
                    if (timerTicks >= 10) {
                        timerTicks = 0;
                        setTimerSpeed(1.0f);
                    } else {
                        setTimerSpeed(1.15f);

                        event.setX(event.getX() * 1.019);
                        event.setZ(event.getZ() * 1.019);
                    }
                }

                if (mode.getValue().equals(Mode.STRICT_STRAFE)) {
                    if (round(mc.player.posY - (int) mc.player.posY, 3) == round(0.138, 3)) {
                        mc.player.motionY -= 0.08;
                        event.setY(event.getY() - 0.09316090325960147);
                        mc.player.motionY -= 0.09316090325960147;
                    }
                }
            }

            if (strafeStage == 1) {
                double multi = 1.35;
                if (mode.getValue().equals(Mode.STRICT_STRAFE)) {
                    multi = 1.38;
                }

                moveSpeed = multi * getBaseNCPSpeed() - 0.01;
            } else if (strafeStage == 2) {
                if (MotionUtil.isMoving() && mc.player.onGround) {
                    double motionY = getJumpHeight(mode.getValue().equals(Mode.STRICT_STRAFE));

                    mc.player.motionY = motionY;
                    event.setY(motionY);

                    if (mode.getValue().equals(Mode.STRAFE)) {
                        if (slowdown) {
                            moveSpeed *= 1.395;
                        } else {
                            moveSpeed *= 1.7835;
                        }
                    } else {
                        moveSpeed *= 2.149;
                    }
                }
            } else if (strafeStage == 3) {
                double diff = 0.66 * (distance - getBaseNCPSpeed());
                moveSpeed = distance - diff;
                slowdown = !slowdown;
            } else {
                if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0)).isEmpty()) {
                    strafeStage = 1;
                }

                moveSpeed = distance - distance / 159.0;
            }

            moveSpeed = Math.max(moveSpeed, getBaseNCPSpeed());

            // limit out strict strafe speed
            if (mode.getValue().equals(Mode.STRICT_STRAFE)) {
                moveSpeed = Math.min(moveSpeed, mc.player.ticksExisted % 6 == 0 ? 0.524 : 0.511);
            }

            Vec2d motion = MotionUtil.strafe(moveSpeed);

            event.setX(motion.getX());
            event.setZ(motion.getZ());

            ++strafeStage;
        }
    }

    /**
     * Gets the base NCP speed
     * @return the base NCP speed
     */
    private double getBaseNCPSpeed() {
        double baseSpeed = 0.2873;
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            baseSpeed *= 1.0 + 0.2 * (mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() + 1);
        }

        return baseSpeed;
    }

    /**
     * Gets the vanilla jump height
     * @param strict if to use a more strict y height
     * @return the vanilla jump height
     */
    private double getJumpHeight(boolean strict) {
        double y = strict ? 0.3995 : 0.3999999463558197;
        if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
            y += (mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1;
        }

        return y;
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
