package cope.nebula.util.renderer;

import cope.nebula.util.Globals;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

/**
 * Utilities for rendering 3d or 2d things into the game
 *
 * @author aesthetical
 * @since 3/11/22
 *
 * Note: I'm not that good at rendering, so I might have jewed some code so credits will be found in the javadoc
 */
public class RenderUtil implements Globals {
    /**
     * Renders a box
     * @param box The bounding box
     * @param color The color
     */
    public static void renderFilledBox(AxisAlignedBB box, int color) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);

        float alpha = (color >> 24 & 0xff) / 255f;
        float red = (color >> 16 & 0xff) / 255f;
        float green = (color >> 8 & 0xff) / 255f;
        float blue = (color & 0xff) / 255f;

        RenderGlobal.renderFilledBox(box, red, green, blue, alpha);

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    /**
     * Renders an outlined box
     * @param box The bounding box
     * @param color The color
     */
    public static void renderOutlinedBox(AxisAlignedBB box, float lineWidth, int color) {
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        GlStateManager.glLineWidth(lineWidth);

        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

        float alpha = (color >> 24 & 0xff) / 255f;
        float red = (color >> 16 & 0xff) / 255f;
        float green = (color >> 8 & 0xff) / 255f;
        float blue = (color & 0xff) / 255f;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(box.minX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();

        glDisable(GL_LINE_SMOOTH);

        GlStateManager.glLineWidth(1.0f);

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void renderSide(AxisAlignedBB box, EnumFacing facing, int color) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        // TODO this needs to be figured out

        tessellator.draw();
    }

    /**
     * Draws a rectangle
     * @param x The x coordinate
     * @param y The y coordinate
     * @param width The width of the rectangle
     * @param height The height of the rectangle
     * @param color The color of the rectangle
     */
    public static void drawRectangle(double x, double y, double width, double height, int color) {
        float alpha = (color >> 24 & 0xff) / 255f;
        float red = (color >> 16 & 0xff) / 255f;
        float green = (color >> 8 & 0xff) / 255f;
        float blue = (color & 0xff) / 255f;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
        buffer.pos(x, y + height, 0.0).color(red, green, blue, alpha).endVertex();
        buffer.pos(x + width, y + height, 0.0).color(red, green, blue, alpha).endVertex();
        buffer.pos(x + width, y, 0.0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(GL_FLAT);
        GlStateManager.enableTexture2D();
    }

    /**
     * Draws a half rounded rectangle
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param width The width
     * @param height The height
     * @param radius The radius of the rounded corners
     * @param color The color
     */
    public static void drawHalfRoundedRectangle(double x, double y, double width, double height, double radius, int color) {
        glPushAttrib(GL_POINTS);
        glScaled(0.5, 0.5, 0.5);

        x *= 2.0;
        y *= 2.0;

        width = (width * 2.0) + x;
        height = (height * 2.0) + y;

        float alpha = (color >> 24 & 0xff) / 255f;
        float red = (color >> 16 & 0xff) / 255f;
        float green = (color >> 8 & 0xff) / 255f;
        float blue = (color & 0xff) / 255f;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        glColor4f(red, green, blue, alpha);

        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

        glBegin(GL_POLYGON);

        double pi = Math.PI;

        int i;
        for (i = 0; i <= 90; ++i) {
            glVertex2d(x + radius + Math.sin(i * pi / 180.0) * radius * -1.0, y + radius + Math.cos(i * pi / 180.0) * radius * -1.0);
        }

        for (i = 90; i <= 180; ++i) {
            glVertex2d((x + 1.0) + Math.sin(i * pi / 180.0) * -1.0, (height - 1.0) + Math.cos(i * pi / 180.0) * -1.0);
        }

        for (i = 0; i <= 90; ++i) {
            glVertex2d((width - 1.0) + Math.sin(i * pi / 180.0), (height - 1.0) + Math.cos(i * pi / 180.0));
        }

        for (i = 90; i <= 180; ++i) {
            glVertex2d(width - radius + Math.sin(i * pi / 180.0) * radius, y + radius + Math.cos(i * pi / 180.0) * radius);
        }

        GL11.glEnd();

        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableTexture2D();
        glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.disableBlend();

        glScaled(2.0, 2.0, 0.0);
        glPopAttrib();
    }

    /**
     * Creates a hex color from RGBA
     * @param red red
     * @param green green
     * @param blue blue
     * @param alpha alpha (transparency)
     * @return the hex color
     */
    public static int fromRGBA(int red, int green, int blue, int alpha) {
        return (red << 16) + (green << 8) + blue + (alpha << 24);
    }

    /**
     * hacker shit
     * @param x The x coordinate
     * @param y The y coordinate
     * @param w The width of the cutoff
     * @param h The height of the cutoff
     */
    public static void scissor(double x, double y, double w, double h) {
        ScaledResolution res = new ScaledResolution(mc);
        int scaleFactor = res.getScaleFactor();

        glPushAttrib(GL_SCISSOR_BIT);
        glScissor((int) (x * scaleFactor), (int) ((res.getScaledHeight() - h) * scaleFactor), (int) ((w - x) * scaleFactor), (int) ((h - y) * scaleFactor));
        glEnable(GL_SCISSOR_TEST);
    }

    /**
     * Disables GL_SCISSOR_TEST if it is on
     */
    public static void stopScissor() {
        glPopAttrib();
        glDisable(GL_SCISSOR_TEST);
    }
}
