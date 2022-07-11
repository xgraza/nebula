package wtf.nebula.util.render

import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11.*
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
}