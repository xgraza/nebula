package cope.nebula.client.ui.font;

import cope.nebula.util.Globals;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import static org.lwjgl.opengl.GL11.*;

/**
 * An implementation of FontRenderer to render our custom AWT font
 *
 * @author aesthetical
 * @since 3/12/22
 */
public class AWTFontRenderer extends FontRenderer implements Globals {
    private final AWTFont font;

    private final int[] customColorCodes = new int[32];

    public AWTFontRenderer(AWTFont font) {
        super(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.renderEngine, false);
        this.font = font;

        FONT_HEIGHT = font.getFont().getSize() - 9;
        registerCustomColorCodes();
    }

    @Override
    public int drawString(String text, int x, int y, int color) {
        return drawString(text, x, y, color, false);
    }

    @Override
    public int drawStringWithShadow(String text, float x, float y, int color) {
        int width = drawString(text, x + 1.0f, y + 1.0f, color, true);
        return Math.min(width, drawString(text, x, y, color, false));
    }

    @Override
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        if (color == 0x20FFFFFF) {
            color = 0xFFFFFF;
        }

        if ((color & 0xFC000000) == 0) {
            color |= 0xFF000000;
        }

        if (dropShadow) {
            color = (color & 0xFCFCFC) >> 2 | color & 0xFF000000;
        }

        double x1 = x - 1.5;
        double y1 = y - 0.5;

        x1 *= 2.0;
        y1 = (y1 - 3.0) * 2.0;

        float alpha = (color >> 24 & 0xff) / 255f;

        glPushMatrix();
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color((float) (color >> 16 & 0xFF) / 255.0f, (float) (color >> 8 & 0xFF) / 255.0f, (float) (color & 0xFF) / 255.0f, alpha);

        // bind textures
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(font.getTexture().getGlTextureId());
        glBindTexture(GL_TEXTURE_2D, font.getTexture().getGlTextureId());

        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);

            Glyph charData = font.getGlyphs()[c];
            if (charData == null) {
                continue;
            }

            if (c == '\u00a7') {
                int colorCode = 21;
                try {
                    colorCode = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
                } catch (Exception ignored) { }

                if (colorCode < 16) {
                    if (colorCode < 0 || colorCode > 15) {
                        colorCode = 15;
                    }

                    if (dropShadow) {
                        colorCode += 16;
                    }

                    colorCode = customColorCodes[colorCode];
                    GlStateManager.color((float) (colorCode >> 16 & 0xFF) / 255.0f, (float) (colorCode >> 8 & 0xFF) / 255.0f, (float) (colorCode & 0xFF) / 255.0f, alpha);
                }

                ++i;
                continue;
            }

            font.drawCharacter(charData, (float) x1, (float) y1);

            x1 += charData.getWidth() - 8;
        }

        glPopMatrix();

        return (int) (x1 / 2.0);
    }

    @Override
    public int getStringWidth(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);

            Glyph charData = font.getGlyphs()[c];
            if (charData == null) {
                continue;
            }

            if (c == '\u00a7') {
                ++i;
                continue; // ignore color codes
            }

            width += charData.getWidth() - 8;
        }

        return width / 2;
    }

    /**
     * Registers all custom colors used for formatting
     */
    private void registerCustomColorCodes() {
        for (int index = 0; index < 32; ++index) {
            int noClue = (index >> 3 & 1) * 85;
            int red = (index >> 2 & 1) * 170 + noClue;
            int green = (index >> 1 & 1) * 170 + noClue;
            int blue = (index >> 0 & 1) * 170 + noClue;

            if (index == 6) {
                red += 85;
            }

            if (index >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }

            this.customColorCodes[index] = (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
        }
    }
}
