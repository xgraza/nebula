package cope.nebula.client.feature.module.movement;

import cope.nebula.client.events.StepEvent;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.renderer.FontUtil;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static cope.nebula.client.feature.module.world.Timer.setTimerSpeed;

public class Step extends Module {
    private static final double[] ONE_BLOCK_NCP = { 0.42, 0.753 };
    private static final double[] ONE_HALF_BLOCK_NCP = { 0.42, 0.75, 1.0, 1.16, 1.23, 1.2 };
    private static final double[] TWO_BLOCK_NCP = { 0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43 };
    private static final double[] TWO_HALF_BLOCK_NCP = { 0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907 };

    public Step() {
        super("Step", ModuleCategory.MOVEMENT, "Steps up blocks");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.NCP);
    public static final Value<Boolean> timer = new Value<>("Timer", true);
    public static final Value<Float> height = new Value<>("Height", 2.0f, 1.0f, 2.5f);

    private boolean usingTimer = false;

    @Override
    public String getDisplayInfo() {
        return FontUtil.formatText(mode.getValue().name());
    }

    @Override
    protected void onDeactivated() {
        if (!nullCheck()) {
            mc.player.stepHeight = 0.6f;

            if (usingTimer) {
                setTimerSpeed(1.0f);
                usingTimer = false;
            }
        }
    }

    @Override
    public void onTick() {
        if (usingTimer && mc.player.onGround) {
            setTimerSpeed(1.0f);
            usingTimer = false;
        }

        mc.player.stepHeight = height.getValue();
    }

    @SubscribeEvent
    public void onStep(StepEvent event) {
        if (mode.getValue().equals(Mode.NCP)) {
            double stepHeight = mc.player.getEntityBoundingBox().minY - mc.player.posY;
            if (stepHeight > height.getValue()) {
                return;
            }

            double[] values = getStepHeightValues(stepHeight);

            if (values.length > 1) {
                if (timer.getValue()) {
                    // thanks doogie
                    setTimerSpeed(1.0f / (values.length + 1.0f));
                    usingTimer = true;
                }

                // stop sprinting
                if (mc.player.isSprinting()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SPRINTING));
                }

                for (double val : values) {
                    mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + val, mc.player.posZ, false));
                }
            }
        }
    }

    private double[] getStepHeightValues(double height) {
        // shit code
        if (height == 1.0) {
            return ONE_BLOCK_NCP;
        } else if (height == 1.5) {
            return ONE_HALF_BLOCK_NCP;
        } else if (height == 2.0) {
            return TWO_BLOCK_NCP;
        } else if (height == 2.5) {
            return TWO_HALF_BLOCK_NCP;
        }

        return new double[] {};
    }

    public enum Mode {
        NCP, VANILLA
    }
}
