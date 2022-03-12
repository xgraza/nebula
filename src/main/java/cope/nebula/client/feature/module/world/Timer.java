package cope.nebula.client.feature.module.world;

import cope.nebula.asm.mixins.client.IMinecraft;
import cope.nebula.asm.mixins.client.ITimer;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;

public class Timer extends Module {
    public Timer() {
        super("Timer", ModuleCategory.WORLD, "Makes game go vroom vroom");
    }

    public static final Value<Float> speed = new Value<>("Speed", 1.5f, 0.1f, 20.0f);

    @Override
    protected void onDeactivated() {
        if (!nullCheck()) {
            setTimerSpeed(1.0f);
        }
    }

    @Override
    public void onTick() {
        setTimerSpeed(speed.getValue());
    }

    /**
     * Sets the game's tick length
     * @param speed the speed the game should run at
     */
    public static void setTimerSpeed(float speed) {
        ((ITimer) ((IMinecraft) mc).getTimer()).setTickLength(50.0f / speed);
    }
}
