package cope.nebula.client.feature.module.render;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.ui.click.ClickGUIScreen;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Module {
    public static ClickGUI INSTANCE;

    public ClickGUI() {
        super("ClickGUI", ModuleCategory.RENDER, "Opens the ClickGUI");
        bind.setValue(Keyboard.KEY_RSHIFT);

        INSTANCE = this;
    }

    @Override
    protected void onActivated() {
        if (nullCheck()) {
            disable();
            return;
        }

        mc.displayGuiScreen(ClickGUIScreen.getInstance());
    }
}
