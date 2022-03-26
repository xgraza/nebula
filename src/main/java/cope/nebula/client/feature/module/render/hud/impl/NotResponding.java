package cope.nebula.client.feature.module.render.hud.impl;

import cope.nebula.client.feature.module.render.hud.IHUDComponent;
import cope.nebula.util.renderer.FontUtil;
import net.minecraft.client.gui.ScaledResolution;

public class NotResponding implements IHUDComponent {
    @Override
    public void render(ScaledResolution resolution) {
        if (getNebula().getServerManager().isResponding()) {
            return;
        }

        long timeNotResponding = (System.currentTimeMillis() - getNebula().getServerManager().getLastTick()) / 1000L;

        String text = "Server has not been responding for " + timeNotResponding + " seconds.";
        FontUtil.drawString(text, (resolution.getScaledWidth() / 2.0f) - (FontUtil.getWidth(text) / 2.0f), 26.0f, -1);
    }

    @Override
    public String getName() {
        return "NotResponding";
    }

    @Override
    public boolean isAlwaysActive() {
        return true;
    }
}
