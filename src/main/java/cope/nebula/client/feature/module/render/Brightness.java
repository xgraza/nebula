package cope.nebula.client.feature.module.render;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;

public class Brightness extends Module {
    private float oldGamma = -1.0f;

    public Brightness() {
        super("Brightness", ModuleCategory.RENDER, "Makes the game brighter");
    }

    @Override
    protected void onActivated() {
        if (oldGamma == -1.0f) {
            oldGamma = mc.gameSettings.gammaSetting;
        }
    }

    @Override
    protected void onDeactivated() {
        if (oldGamma != -1.0f) {
            mc.gameSettings.gammaSetting = oldGamma;
            oldGamma = -1.0f;
        }
    }

    @Override
    public void onTick() {
        mc.gameSettings.gammaSetting = 100.0f;
    }
}
