package cope.nebula.util.renderer;

import cope.nebula.client.feature.module.render.CustomFont;
import cope.nebula.client.ui.font.AWTFontRenderer;
import cope.nebula.util.Globals;
import net.minecraft.client.gui.FontRenderer;

/**
 * A simple utility for font rendering
 *
 * @author aesthetical
 * @since 3/12/22
 */
public class FontUtil implements Globals {
    /**
     * Draws a string with or without shadow depending on the CustomFont setting
     * @param text The text to render
     * @param x The x coordinate
     * @param y The y coordinate
     * @param color The text color
     */
    public static void drawString(String text, float x, float y, int color) {
        boolean shadowed = false;
        if (getCurrentFontRenderer() instanceof AWTFontRenderer) {
            shadowed = CustomFont.shadow.getValue();
        }

        drawString(text, shadowed, x, y, color);
    }

    /**
     * Draws a string with a shadow
     * @param text The text to render
     * @param x The x coordinate
     * @param y The y coordinate
     * @param color The text color
     */
    public static void drawShadowedString(String text, float x, float y, int color) {
        drawString(text, true, x, y, color);
    }

    /**
     * Draws a string
     * @param text The text to render
     * @param shadow If to use a shadow
     * @param x The x coordinate
     * @param y The y coordinate
     * @param color The text color
     */
    public static void drawString(String text, boolean shadow, float x, float y, int color) {
        getCurrentFontRenderer().drawString(text, x, y, color, shadow);
    }

    /**
     * Gets the width of text
     * @param text The text to measure
     * @return the width this text is
     */
    public static int getWidth(String text) {
        return getCurrentFontRenderer().getStringWidth(text);
    }

    /**
     * Gets the height of the current font
     * @return the font height of the current font renderer
     */
    public static int getHeight() {
        return getCurrentFontRenderer().FONT_HEIGHT;
    }

    /**
     * Gets the current font renderer
     * @return the current font renderer to use
     */
    public static FontRenderer getCurrentFontRenderer() {
        if (CustomFont.fontRenderer != null) {
            return CustomFont.fontRenderer;
        }

        return mc.fontRenderer;
    }
}
