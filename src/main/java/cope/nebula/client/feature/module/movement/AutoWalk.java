package cope.nebula.client.feature.module.movement;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import net.minecraft.client.settings.KeyBinding;

public class AutoWalk extends Module {
    public AutoWalk() {
        super("AutoWalk", ModuleCategory.MOVEMENT, "Automatically walks");
    }

    @Override
    protected void onDeactivated() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
    }

    @Override
    public void onTick() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
    }
}
