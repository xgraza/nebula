package cope.nebula.client.feature.module.render.hud.impl;

import cope.nebula.client.Nebula;
import cope.nebula.client.feature.module.render.hud.IHUDComponent;
import cope.nebula.util.renderer.FontUtil;
import net.minecraft.client.gui.ScaledResolution;

public class Watermark implements IHUDComponent {
    @Override
    public void render(ScaledResolution resolution) {
        FontUtil.drawString(Nebula.NAME + " v" + Nebula.VERSION, 2.0f, 2.0f, -1);
    }

    @Override
    public String getName() {
        return "Watermark";
    }

    @Override
    public boolean isAlwaysActive() {
        return true;
    }
}
