package wtf.nebula.client.feature.guis.feature

import wtf.nebula.client.feature.guis.common.DraggableComponent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.util.render.RenderUtil
import java.awt.Color

class CategoryComponent(name: String, modules: Set<Module>) : DraggableComponent(name) {
    init {
        modules.forEach { children += ModuleComponent(it) }
    }

    private var scrollOffset = 0.0

    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        setPositionWithDrag(mouseX, mouseY)

        RenderUtil.rect(x, y, width, totalHeight(), Color(28, 28, 28).rgb)
        mc.fontRenderer.drawStringWithShadow(name, (x + 2.3).toFloat(), (y + (height / 2.0) - (mc.fontRenderer.FONT_HEIGHT / 2.0)).toFloat(), -1)

        var posY = y + height + 0.5
        children.forEach {
            it.x = x + 2.0
            it.y = posY
            it.width = width - 4.0
            it.height = height - 1.0

            it.render(mouseX, mouseY, partialTicks)

            posY += it.height + 1.0
        }
    }

    override fun mouseClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (isMouseInBounds(mouseX, mouseY) && mouseButton == 0) {

            dragging = true
            updateDragging(mouseX, mouseY)
            playClickSound()

            return
        }

        if (isMouseWithinBounds(mouseX, mouseY, x, y + 16.0, width, compHeight())) {
            dragging = false
            children.forEach { it.mouseClick(mouseX, mouseY, mouseButton) }
        }
    }

    override fun mouseScroll(mouseX: Int, mouseY: Int, scroll: Int) {
        if (isMouseWithinBounds(mouseX, mouseY, x, y + 16.0, width, compHeight())) {
            if (scroll > 0) {
                children.forEach { it.y -= 10.0 }
            }

            else if (scroll < 0) {
                children.forEach { it.y += 10.0 }
            }
        }
    }

    override fun mouseRelease(mouseX: Int, mouseY: Int, state: Int) {
        if (dragging && state == 0) {
            dragging = false
            return
        }

        children.forEach { it.mouseRelease(mouseX, mouseY, state) }
    }

    override fun keyTyped(keyTyped: Char, keyCode: Int) {
        children.forEach { it.keyTyped(keyTyped, keyCode) }
    }

    private fun compHeight(): Double {
        return children.sumOf { it.height + 1.0 } + 1.5
    }

    private fun totalHeight(): Double {
        return height + compHeight()
    }
}