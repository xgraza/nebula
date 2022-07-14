package wtf.nebula.util.render

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.AxisAlignedBB
import org.lwjgl.opengl.GL11.*
import wtf.nebula.client.feature.module.render.Colors
import wtf.nebula.util.render.ColorUtil.setColor

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
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)

        val (r, g, b, a) = color.toColor()

        if (filled) {
            RenderGlobal.renderFilledBox(box, r.toFloat(), g.toFloat(), b.toFloat(), a.toFloat())
        }

        if (outlined) {
            RenderGlobal.drawSelectionBoundingBox(box, r.toFloat(), g.toFloat(), b.toFloat(), a.toFloat())
        }

        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.popMatrix()
    }

    fun renderNormalBox(box: AxisAlignedBB) {
        renderBoxEsp(box, ColorUtil.addAlpha(Colors.color(), 120))
    }
}