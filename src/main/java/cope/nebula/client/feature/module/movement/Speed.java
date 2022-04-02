package cope.nebula.client.feature.module.movement;

import cope.nebula.client.events.MotionEvent;
import cope.nebula.client.events.MotionUpdateEvent;
import cope.nebula.client.events.MotionUpdateEvent.Era;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.feature.module.render.hud.impl.Info;
import cope.nebula.client.value.Value;
import cope.nebula.util.internal.math.Vec2d;
import cope.nebula.util.renderer.FontUtil;
import cope.nebula.util.world.entity.player.MotionUtil;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static cope.nebula.client.feature.module.world.Timer.setTimerSpeed;

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
        if (event.getEra().equals(Era.PRE)) {
            distance = Math.sqrt(Math.pow(mc.player.posX - mc.player.prevPosX, 2) + Math.pow(mc.player.posZ - mc.player.prevPosZ, 2));
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.INCOMING) && event.getPacket() instanceof SPacketPlayerPosLook) {
            strafeStage = 4;
            distance = 0;

            sendChatMessage("MoveSpeed: " + moveSpeed + " (" + Info.getSpeedInKmh() + "kmh)" + ", OddStage: " + slowdown);
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

                        event.setX(event.getX() * 1.19);
                        event.setZ(event.getZ() * 1.19);
                    }
                }
            }

            if (strafeStage == 1) {
                moveSpeed = 1.35 * getBaseNCPSpeed() - 0.01;
            } else if (strafeStage == 2) {
                if (MotionUtil.isMoving() && mc.player.onGround) {
                    double motionY = getJumpHeight(mode.getValue().equals(Mode.STRICT_STRAFE));

                    mc.player.motionY = motionY;
                    event.setY(motionY);

                    double acceleration = 2.149;
                    if (mc.player.ticksExisted % 2 == 0) {
                        acceleration = 1.9828;
                    }

                    moveSpeed *= acceleration;
                }
            } else if (strafeStage == 3) {
                double diff = 0.76 * (distance - getBaseNCPSpeed());
                moveSpeed = distance - diff;
            } else {
                if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0)).isEmpty()) {
                    strafeStage = MotionUtil.isMoving() ? 3 : 1;
                }

                moveSpeed = distance - distance / 159.0;
            }

            moveSpeed = Math.max(moveSpeed, getBaseNCPSpeed());

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
        double y = strict ? 0.42 : 0.3995;
        if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
            y += (mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1;
        }

        return y;
    }


    public enum Mode {
        STRAFE, STRICT_STRAFE
    }
}
