package wtf.nebula.client.feature.guis.common

import org.lwjgl.input.Mouse

abstract class DraggableComponent(name: String) : Component(name) {
    private var dragX = 0.0
    private var dragY = 0.0

    var dragging = false

    fun setPositionWithDrag(mouseX: Int, mouseY: Int) {
        if (dragging) {
            x = mouseX + dragX
            y = mouseY + dragY
        }
    }

    fun updateDragging(mouseX: Int, mouseY: Int) {
        if (dragging) {
            dragX = x - mouseX
            dragY = y - mouseY
        }
    }
}