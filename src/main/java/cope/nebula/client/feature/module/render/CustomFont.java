package cope.nebula.client.feature.module.render;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.ui.font.AWTFont;
import cope.nebula.client.ui.font.AWTFontRenderer;
import cope.nebula.client.value.Value;

import java.awt.*;

public class CustomFont extends Module {
    public static AWTFontRenderer fontRenderer;

    public CustomFont() {
        super("Font", ModuleCategory.RENDER, "Configures the client's font");
    }

    public static final Value<String> font = new Value<>("Font", "Product Sans");
    public static final Value<Boolean> shadow = new Value<>("Shadow", true);
    public static final Value<Style> style = new Value<>("Style", Style.PLAIN);
    public static final Value<Integer> size = new Value<>("Size", 20, 16, 24);

    @Override
    protected void onActivated() {
        fontRenderer = new AWTFontRenderer(new AWTFont(new Font(font.getValue(), style.getValue().style, size.getValue())));
    }

    @Override
    protected void onDeactivated() {
        fontRenderer = null;
    }

    public enum Style {
        PLAIN(Font.PLAIN),
        BOLD(Font.BOLD),
        ITALIC(Font.ITALIC),
        BOLDED_ITALIC(Font.BOLD + Font.ITALIC);

        private final int style;

        Style(int style) {
            this.style = style;
        }
    }
}
