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
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static cope.nebula.client.feature.module.world.Timer.setTimerSpeed;

public class Speed extends Module {
    public Speed() {
        super("Speed", ModuleCategory.MOVEMENT, "Makes you go faster");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.STRAFE);
    public static final Value<Boolean> boost = new Value<>("Boost", false);
    public static final Value<Boolean> timer = new Value<>("Timer", false);

    private int strafeStage = 4;
    private double moveSpeed = 0.0;
    private double distance = 0.0;

    private float explosionStrength = 0.0f;
    private int damageBoostTicks = -1;

    private boolean lagback = false;

    @Override
    protected void onDeactivated() {
        strafeStage = 4;
        moveSpeed = 0.0;
        distance = 0.0;
        damageBoostTicks = -1;

        lagback = false;
    }

    @Override
    public void onTick() {
        if (mode.getValue().equals(Mode.YPORT)) {
            if (lagback) {
                lagback = false;
                return;
            }

            if (MotionUtil.isMoving()) {
                if (mc.player.onGround) {
                    mc.player.jump();
                } else {
                    mc.player.motionY = -1.0;
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.INCOMING)) {
            if (event.getPacket() instanceof SPacketExplosion || event.getPacket() instanceof SPacketEntityVelocity) {
                damageBoostTicks = 20;

                if (event.getPacket() instanceof SPacketExplosion) {
                    explosionStrength = ((SPacketExplosion) event.getPacket()).getStrength();
                } else {
                    explosionStrength = 2.0f;
                }
            }

            // rubberband, reset speed
            if (event.getPacket() instanceof SPacketPlayerPosLook) {
                strafeStage = 4;
                lagback = true;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMotionUpdate(MotionUpdateEvent event) {
        if (event.getEra().equals(Era.PRE)) {
            distance = Math.sqrt(Math.pow(mc.player.prevPosX - mc.player.posX, 2) + Math.pow(mc.player.prevPosZ - mc.player.posZ, 2));

            if (mode.getValue().equals(Mode.ONGROUND) && !lagback) {
                event.setY(event.getY() + 0.1);
                lagback = false;
            }
        }
    }

    @SubscribeEvent
    public void onMotion(MotionEvent event) {
        if (mode.getValue().equals(Mode.STRAFE) || mode.getValue().equals(Mode.STRICTSTRAFE)) {
            if (MotionUtil.isMoving()) {
                if (mc.player.onGround) {
                    strafeStage = 2;
                }

                if (!lagback) {
                    if (boost.getValue() && damageBoostTicks > 0) {
                        --damageBoostTicks;
                        moveSpeed *= (explosionStrength / 10.0);
                    }

                    if (timer.getValue() && moveSpeed < getBaseNCPSpeed() && strafeStage != 2) {
                        setTimerSpeed(1.15f);

                        mc.player.motionX *= 1.11;
                        mc.player.motionZ *= 1.11;
                    } else {
                        setTimerSpeed(1.0f);
                    }
                }
            }

            if (strafeStage == 1) {
                moveSpeed = (1.38 * getBaseNCPSpeed()) - 0.01;

                strafeStage = 2;
            } else if (strafeStage == 2) {
                if (MotionUtil.isMoving() && mc.player.onGround) {
                    event.setY(mc.player.motionY = getJumpHeight(mode.getValue().equals(Mode.STRICTSTRAFE)));
                    moveSpeed *= 2.149;
                }

                strafeStage = 3;
            } else if (strafeStage == 3) {
                double scaled = 0.66 * (distance - getBaseNCPSpeed());
                moveSpeed = distance - scaled;

                strafeStage = 4;
            } else {
                if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0)).isEmpty() || (mc.player.collidedVertically && strafeStage > 1)) {
                    strafeStage = 1;
                }

                double divisor = mode.getValue().equals(Mode.STRAFE) ? 159.0 : 149.0;
                moveSpeed = distance - distance / divisor;
                lagback = false;
            }

            // i input random numbers until they work
            double maxMoveSpeed = 0.0;
            if (mode.getValue().equals(Mode.STRICTSTRAFE)) {
                maxMoveSpeed = 0.4672;
            } else {
                maxMoveSpeed = 0.551;
            }

            moveSpeed = MathHelper.clamp(moveSpeed, getBaseNCPSpeed(), maxMoveSpeed);

            Vec2d motion = MotionUtil.strafe(moveSpeed);

            event.setX(mc.player.motionX = motion.getX());
            event.setZ(mc.player.motionZ = motion.getZ());
        } else if (mode.getValue().equals(Mode.ONGROUND)) {
            Vec2d motion = MotionUtil.strafe(0.5);

            event.setX(mc.player.motionX = motion.getX());
            event.setZ(mc.player.motionZ = motion.getZ());
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
        double y = strict ? 0.42 : 0.3995;
        if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
            y += (mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1;
        }

        return y;
    }


    public enum Mode {
        STRAFE, STRICTSTRAFE, YPORT, ONGROUND
    }
}
