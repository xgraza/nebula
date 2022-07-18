package wtf.nebula.util.render

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.AxisAlignedBB
import org.lwjgl.opengl.GL11.*
import wtf.nebula.client.feature.module.render.Colors
import wtf.nebula.util.render.ColorUtil.setColor
import java.awt.Color

object RenderUtil {
    fun rect(x: Double, y: Double, width: Double, height: Double, color: Int) {

        // GL settings
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // set the rectangle color
        setColor(color)

        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        val (r, g, b, a) = color.toColor()

        // draw the rectangle
        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR)

        // add the rectangle vertices
        buffer.pos(x, y, 0.0).color(r, g, b, a).endVertex()
        buffer.pos(x, y + height, 0.0).color(r, g, b, a).endVertex()
        buffer.pos(x + width, y + height, 0.0).color(r, g, b, a).endVertex()
        buffer.pos(x + width, y, 0.0).color(r, g, b, a).endVertex()

        // draw the vertices
        tessellator.draw()

        // GL resets
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
    }

    fun translateNormal() {
        // GlStateManager.translate()
    }

    fun renderBoxEsp(box: AxisAlignedBB, color: Int, filled: Boolean = true, outlined: Boolean = true) {
        GlStateManager.pushMatrix()

        GlStateManager.clear(256)

        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()

        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)

        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);

        val colorArr = ColorUtil.getColor(color)

        if (filled) {
            RenderGlobal.renderFilledBox(box, colorArr[0], colorArr[1], colorArr[2], colorArr[3])
        }

        if (outlined) {
            glEnable(GL_LINE_SMOOTH)
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

            GlStateManager.glLineWidth(1.5f)

            RenderGlobal.drawSelectionBoundingBox(box, colorArr[0], colorArr[1], colorArr[2], colorArr[3])

            GlStateManager.glLineWidth(1.0f)

            glDisable(GL_LINE_SMOOTH)
        }

        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)

        GlStateManager.disableBlend()

        GlStateManager.enableTexture2D()
        GlStateManager.enableLighting()

        GlStateManager.popMatrix()
    }

    fun renderNormalBox(box: AxisAlignedBB) {
        renderBoxEsp(box, ColorUtil.addAlpha(Colors.color(), 120))
    }
}