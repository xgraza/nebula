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
        int colour = color;

        if (colour == 0x20FFFFFF || colour == -1) {
            colour = 0xFFFFFFFF;
        }

        if (dropShadow) {
            colour = (colour & 0xFCFCFC) >> 2 | colour & 0xFF000000;
        }

        double x1 = x - 1.5;
        double y1 = y - 0.5;

        x1 *= 2.0;
        y1 = (y1 - 3.0) * 2.0;

        float alpha = (colour >> 24 & 0xff) / 255f;

        glPushMatrix();
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);

        // bind textures
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(font.getTexture().getGlTextureId());
        glBindTexture(GL_TEXTURE_2D, font.getTexture().getGlTextureId());

        GlStateManager.color((colour >> 16 & 0xff) / 255f, (colour >> 8 & 0xff) / 255f, (colour & 0xff) / 255f, alpha);

        int length = text.length();
        for (int i = 0; i < length; ++i) {
            char c = text.charAt(i);

            Glyph glyph = font.getGlyphs()[c];
            if (glyph == null) {
                continue;
            }

            if (c == '\u00a7' && i < length) {
                int colorIndex = 21;
                try {
                    colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
                } catch (Exception ignored) { }

                if (colorIndex < 16) {
                    if (colorIndex < 0 || colorIndex > 15) {
                        colorIndex = 15;
                    }

                    if (dropShadow) {
                        colorIndex += 16;
                    }

                    int colorCode = customColorCodes[colorIndex];
                    GlStateManager.color((colorCode >> 16 & 0xff) / 255f, (colorCode >> 8 & 0xff) / 255f, (colorCode & 0xff) / 255f, alpha);
                }

                ++i;
                continue;
            }

            font.drawCharacter(glyph, (float) x1, (float) y1);

            x1 += glyph.getWidth() - 8;
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
        for (int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i >> 0 & 1) * 170 + j;

            if (i == 6) {
                k += 85;
            }

            if (mc.gameSettings.anaglyph) {
                int j1 = (k * 30 + l * 59 + i1 * 11) / 100;
                int k1 = (k * 30 + l * 70) / 100;
                int l1 = (k * 30 + i1 * 70) / 100;
                k = j1;
                l = k1;
                i1 = l1;
            }

            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            customColorCodes[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }
    }
}
