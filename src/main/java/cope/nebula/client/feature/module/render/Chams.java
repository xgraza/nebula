package cope.nebula.client.feature.module.render;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;

import java.awt.*;

public class Chams extends Module {
    public static Chams INSTANCE;

    public Chams() {
        super("Chams", ModuleCategory.RENDER, "Renders things through walls");
        INSTANCE = this;
    }

    public static final Value<Mode> players = new Value<>("Players", Mode.TEXTURE);
    public static final Value<Mode> crystals = new Value<>("Crystals", Mode.NONE);

    // color settings
    public static final Value<Color> playerColor = new Value<>("PlayerColor", new Color(255, 255, 255, 120));
    public static final Value<Color> playerXqzColor = new Value<>("PlayerXQZColor", new Color(217, 207, 207, 120));

    public static final Value<Color> crystalColor = new Value<>("PlayerColor", new Color(255, 255, 255, 120));
    public static final Value<Color> crystalXqzColor = new Value<>("PlayerXQZColor", new Color(217, 207, 207, 120));

    public void onRenderPlayerModel() {

    }

    public void onRenderCrystalModel() {

    }

    public enum Mode {
        /**
         * Does nothing
         */
        NONE,

        /**
         * Renders the entity texture through walls
         */
        TEXTURE,

        /**
         * Render the entity as a color through walls
         */
        COLORED,

        /**
         * Render the entity one color when it is visible to you, and a different color when it is not
         */
        XQZ
    }
}
