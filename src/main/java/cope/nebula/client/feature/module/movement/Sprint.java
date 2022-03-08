package cope.nebula.client.feature.module.movement;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", ModuleCategory.MOVEMENT, "Makes you automatically sprint");
    }

    @Override
    protected void onDeactivated() {
        mc.player.setSprinting(false);
    }

    @Override
    public void onTick() {
        mc.player.setSprinting(!mc.player.isHandActive() &&
                !mc.player.isSneaking() &&
                mc.player.getFoodStats().getFoodLevel() > 6 &&
                !mc.player.collidedHorizontally &&
                mc.player.moveForward > 0.0f);
    }
}
