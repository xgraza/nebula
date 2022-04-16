package cope.nebula.client.feature.module.render;

import cope.nebula.asm.mixins.world.IWorldProvider;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import net.minecraft.world.WorldProvider;

public class Brightness extends Module {
    public Brightness() {
        super("Brightness", ModuleCategory.RENDER, "Makes the game brighter");
    }

    private final float[] lightMap = new float[16];

    @Override
    protected void onActivated() {
        if (mc.world != null) {
            WorldProvider provider = mc.world.provider;
            for (int i = 0; i < 16; ++i) {
                lightMap[i] = ((IWorldProvider) provider).getLightBrightnessTable()[i];
                ((IWorldProvider) provider).getLightBrightnessTable()[i] = 1.0f;
            }
        }
    }

    @Override
    protected void onDeactivated() {
        if (mc.world != null) {
            WorldProvider provider = mc.world.provider;
            for (int i = 0; i < 16; ++i) {
                ((IWorldProvider) provider).getLightBrightnessTable()[i] = lightMap[i];
                lightMap[i] = 0.0f;
            }
        }
    }

    @Override
    public void onTick() {
        WorldProvider provider = mc.world.provider;
        for (int i = 0; i < 16; ++i) {
            ((IWorldProvider) provider).getLightBrightnessTable()[i] = 1.0f;
        }
    }
}
