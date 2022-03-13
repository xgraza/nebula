package cope.nebula.client.feature.module.movement;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;

public class Step extends Module {
    public Step() {
        super("Step", ModuleCategory.MOVEMENT, "Steps up blocks");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.NCP);
    public static final Value<Float> height = new Value<>("Height", 2.0f, 1.0f, 1.0f);

    @Override
    protected void onDeactivated() {
        mc.player.stepHeight = 0.6f;
    }

    @Override
    public void onTick() {
        if (mode.getValue().equals(Mode.VANILLA)) {
            mc.player.stepHeight = height.getValue() + 0.1f;
        }
    }

    // TODO: step event in Entity#move
    public void onStep() {

    }

    public enum Mode {
        NCP, VANILLA
    }
}
