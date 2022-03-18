package cope.nebula.client.feature.module.render;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.ui.click.ClickGUIScreen;
import cope.nebula.client.value.Value;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Module {
    public static ClickGUI INSTANCE;

    public ClickGUI() {
        super("ClickGUI", ModuleCategory.RENDER, "Opens the ClickGUI");
        bind.setValue(Keyboard.KEY_RSHIFT);

        INSTANCE = this;
    }

    public static final Value<Boolean> animations = new Value<>("Animations", true);
    public static final Value<Double> speed = new Value<>(animations, "Speed", 2.5, 1.0, 5.0);

    public static final Value<Boolean> shader = new Value<>("Shader", true);

    @Override
    protected void onActivated() {
        if (nullCheck()) {
            disable();
            return;
        }

        // the actual ClickGUi code might be the most garbage shit ive ever written
        mc.displayGuiScreen(ClickGUIScreen.getInstance());
    }
}
