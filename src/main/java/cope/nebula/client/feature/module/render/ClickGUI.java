package cope.nebula.client.feature.module.render;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.ui.click.ClickGUIScreen;

public class ClickGUI extends Module {
    public static ClickGUI INSTANCE;

    public ClickGUI() {
        super("ClickGUI", ModuleCategory.RENDER, "Opens the ClickGUI");
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

    @Override
    protected void onDeactivated() {
        if (!nullCheck()) {
            mc.displayGuiScreen(null);
        }
    }
}
