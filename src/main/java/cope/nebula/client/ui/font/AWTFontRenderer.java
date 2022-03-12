package cope.nebula.client.ui.font;

import cope.nebula.util.Globals;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import static net.minecraftforge.fml.client.config.GuiUtils.colorCodes;
import static org.lwjgl.opengl.GL11.*;

/**
 * An implementation of FontRenderer to render our custom AWT font
 *
 * @author aesthetical
 * @since 3/12/22
 */
public class AWTFontRenderer extends FontRenderer implements Globals {
    private final AWTFont font;

    public AWTFontRenderer(AWTFont font) {
        super(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.renderEngine, false);
        this.font = font;

        FONT_HEIGHT = font.getFont().getSize() - 9;
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
        float red = (color >> 16 & 0xff) / 255f;
        float green = (color >> 8 & 0xff) / 255f;
        float blue = (color & 0xff) / 255f;

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(red, green, blue, alpha);

        // bind textures
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(font.getTexture().getGlTextureId());
        glBindTexture(3553, font.getTexture().getGlTextureId());

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

                    colorCode = colorCodes[colorCode];
                    GlStateManager.color((float) (colorCode >> 16 & 0xFF) / 255.0f, (float) (colorCode >> 8 & 0xFF) / 255.0f, (float) (colorCode & 0xFF) / 255.0f, alpha);
                }

                ++i;
                continue;
            }

            font.drawCharacter(charData, (float) x1, (float) y1);

            x1 += charData.getWidth() - 8;
        }

        GlStateManager.popMatrix();

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

}
