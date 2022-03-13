package cope.nebula.client.feature.module.movement;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;

public class Jesus extends Module {
    public Jesus() {
        super("Jesus", ModuleCategory.MOVEMENT, "Walks on water");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.TRAMPOLINE);
    public static final Value<Boolean> dip = new Value<>("Dip", true);
    public static final Value<Boolean> lava = new Value<>("Lava", false);

    private int waterTicks = 0;

    @Override
    public void onTick() {
        if (mode.getValue().equals(Mode.TRAMPOLINE)) {
            if (isInValidLiquid()) {
                ++waterTicks;
                if (waterTicks > 2) {
                    mc.player.jump();
                    mc.player.motionY += 7.0E-4;

                    waterTicks = 0;
                }
            }
        } else if (mode.getValue().equals(Mode.DOLPHIN)) {
            if (isInValidLiquid()) {
                mc.player.jump();
            }
        }
    }

    private boolean isInValidLiquid() {
        return mc.player.isInWater() || (lava.getValue() && mc.player.isInLava());
    }

    public enum Mode {
        NCP, STRICT, TRAMPOLINE, DOLPHIN
    }
}
