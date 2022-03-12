package cope.nebula.client.ui.font;

import net.minecraft.client.renderer.texture.DynamicTexture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.*;

/**
 * Represents a font class that uses Java's AWT library to render a custom font into minecraft
 *
 * @author aesthetical
 * @since 3/12/22
 */
public class AWTFont {
    private final Font font;
    private final DynamicTexture texture;
    private final Glyph[] glyphs;

    public AWTFont(Font font) {
        this.font = font;
        this.glyphs = new Glyph[font.getNumGlyphs()];
        this.texture = setupBitmap();
    }

    /**
     * Draws a character
     * @param glyph The glyph to draw
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public void drawCharacter(Glyph glyph, float x, float y) {
        float renderSRCX = glyph.getX() / 512.0f;
        float renderSRCY = glyph.getY() / 512.0f;
        float renderSRCWidth = glyph.getWidth() / 512.0f;
        float renderSRCHeight = glyph.getHeight() / 512.0f;

        glBegin(4);

        glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY);
        glVertex2d(x + glyph.getWidth(), y);
        glTexCoord2f(renderSRCX, renderSRCY);
        glVertex2d(x, y);
        glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight);
        glVertex2d(x, y + glyph.getHeight());
        glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight);
        glVertex2d(x, y + glyph.getHeight());
        glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY + renderSRCHeight);
        glVertex2d(x + glyph.getWidth(), y + glyph.getHeight());
        glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY);
        glVertex2d(x + glyph.getWidth(), y);

        glEnd();
    }

    /**
     * Creates a bitmap texture
     * @return a DynamicTexture of the bitmap
     */
    private DynamicTexture setupBitmap() {
        // create our bitmap image
        BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
        // get our graphics environment
        Graphics2D graphics = (Graphics2D) image.getGraphics();

        // configure the font we'll be using in this bitmap
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        // set our rendering hints for better looking fonts
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        // get our font metrics object
        FontMetrics metrics = graphics.getFontMetrics();

        int x = 0;
        int y = metrics.getHeight();

        for (int glyph = 0; glyph < font.getNumGlyphs(); ++glyph) {
            char glyphCharacter = (char) glyph;

            // if we cannot display this glyph, don't bother to write to the bitmap
            if (!font.canDisplay(glyphCharacter)) {
                continue;
            }

            Rectangle2D bounds = metrics.getStringBounds(String.valueOf(glyphCharacter), graphics);

            // get the width of this glyph
            float charWidth = (float) bounds.getWidth() + 8;
            float charHeight = (float) bounds.getHeight();

            // make sure our character will not exceed our bounds
            if (x + charHeight >= 512) {
                x = 0;
                y += charHeight + 1;
            }

            // add our character data
            Glyph data = new Glyph(x, y, charWidth, charHeight, glyphCharacter);
            glyphs[glyph] = data;

            // draw glyph to the bitmap
            graphics.drawString(String.valueOf(data.getC()), data.getX() + 2, y + metrics.getAscent());

            // add our current width to not overlap other characters in the bitmap
            x += charWidth;
        }

        try {
            File file = Paths.get("").resolve("image.png").toFile();
            System.out.println(file.getAbsolutePath());
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new DynamicTexture(image);
    }

    public Font getFont() {
        return font;
    }

    public Glyph[] getGlyphs() {
        return glyphs;
    }

    public DynamicTexture getTexture() {
        return texture;
    }
}
