package cope.nebula.util.renderer;

import cope.nebula.util.Globals;
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
        GlStateManager.tryBlendFuncSeparate(771, 770, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();

        float alpha = (color >> 24 & 0xff) / 255f;
        float red = (color >> 16 & 0xff) / 255f;
        float green = (color >> 8 & 0xff) / 255f;
        float blue = (color & 0xff) / 255f;

        RenderGlobal.renderFilledBox(box, red, green, blue, alpha);

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
    }

    /**
     * Renders an outlined box
     * @param box The bounding box
     * @param color The color
     */
    public static void renderOutlinedBox(AxisAlignedBB box, float lineWidth, int color) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(771, 770, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();

        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

        GlStateManager.glLineWidth(lineWidth);

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

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
    }

    public static void renderSide(AxisAlignedBB box, EnumFacing facing, int color) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        // TODO this needs to be figured out

        tessellator.draw();
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
}
