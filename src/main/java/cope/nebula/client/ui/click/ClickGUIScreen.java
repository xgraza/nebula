package cope.nebula.client.ui.click;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.feature.module.render.ClickGUI;
import cope.nebula.client.shader.two.BlurShader;
import cope.nebula.client.ui.click.component.Panel;
import cope.nebula.util.Globals;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClickGUIScreen extends GuiScreen implements Globals {
    private static ClickGUIScreen INSTANCE;

    private final ArrayList<Panel> panels = new ArrayList<>();
    private final BlurShader blurShader = new BlurShader();

    public ClickGUIScreen() {
        double x = 4.0;

        for (ModuleCategory category : ModuleCategory.values()) {
            List<Module> modules = getNebula().getModuleManager().getModules()
                    .stream().filter((module) -> module.getCategory().equals(category))
                    .collect(Collectors.toList());

            if (!modules.isEmpty()) {
                panels.add(new Panel(x, category, modules));
                x += 116.0;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (ClickGUI.shader.getValue()) {
            blurShader.render(partialTicks);
        }

        panels.forEach((panel) -> panel.drawComponent(mouseX, mouseY));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        panels.forEach((panel) -> panel.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        panels.forEach((panel) -> panel.mouseReleased(mouseX, mouseY, state));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        ClickGUI.INSTANCE.disable();
    }

    public static ClickGUIScreen getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGUIScreen();
        }

        return INSTANCE;
    }
}
