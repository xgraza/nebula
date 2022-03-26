package cope.nebula.client.feature.module.render.hud.impl;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.render.hud.IHUDComponent;
import cope.nebula.util.renderer.FontUtil;
import net.minecraft.client.gui.ScaledResolution;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Arraylist implements IHUDComponent {
    @Override
    public void render(ScaledResolution resolution) {
        List<Module> modules = getNebula().getModuleManager().getModules()
                .stream().filter((module) -> module.isDrawn() && module.isOn())
                .collect(Collectors.toList());

        if (!modules.isEmpty()) {
            modules.sort(Comparator.comparingInt((m) -> -FontUtil.getWidth(m.getDisplay())));

            double y = 2.0;
            double width = resolution.getScaledWidth_double();

            for (Module module : modules) {
                String text = module.getDisplay();
                FontUtil.drawString(text, (float) (width - FontUtil.getWidth(text) - 2.0f), (float) y, -1);

                y += (FontUtil.getHeight() + 2.0f);
            }
        }
    }

    @Override
    public String getName() {
        return "Arraylist";
    }

    @Override
    public boolean isAlwaysActive() {
        return false;
    }
}
