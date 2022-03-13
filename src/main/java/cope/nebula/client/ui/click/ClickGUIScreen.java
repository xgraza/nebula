package cope.nebula.client.ui.click;

import cope.nebula.client.feature.module.render.ClickGUI;
import cope.nebula.client.shader.two.BlurShader;
import net.minecraft.client.gui.GuiScreen;

public class ClickGUIScreen extends GuiScreen {
    private static ClickGUIScreen INSTANCE;

    private final BlurShader blurShader = new BlurShader();

    public ClickGUIScreen() {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        blurShader.render(partialTicks);
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
