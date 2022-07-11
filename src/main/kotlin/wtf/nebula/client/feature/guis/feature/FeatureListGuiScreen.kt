package wtf.nebula.client.feature.guis.feature

import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Mouse
import wtf.nebula.client.feature.module.render.ClickGUI
import wtf.nebula.client.registry.impl.ModuleRegistry

class FeatureListGuiScreen : GuiScreen() {
    private val categories = mutableListOf<CategoryComponent>()

    init {
        var x = 6.0

        for ((category, modules) in ModuleRegistry.INSTANCE!!.moduleByCategory) {
            val component = CategoryComponent(category.displayName, modules)

            component.x = x
            component.y = 26.0
            component.width = 115.0
            component.height = 15.0

            x += component.width + 5.0

            categories += component
        }
    }

    override fun drawScreen(p_73863_1_: Int, p_73863_2_: Int, p_73863_3_: Float) {
        drawDefaultBackground()
        categories.forEach { it.render(p_73863_1_, p_73863_2_, p_73863_3_) }

        val scroll = Mouse.getDWheel()
        if (scroll != 0) {
            categories.forEach { it.mouseScroll(p_73863_1_, p_73863_2_, scroll) }
        }
    }

    override fun mouseClicked(p_73864_1_: Int, p_73864_2_: Int, p_73864_3_: Int) {
        super.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_)
        categories.forEach { it.mouseClick(p_73864_1_, p_73864_2_, p_73864_3_) }
    }

    override fun mouseReleased(p_146286_1_: Int, p_146286_2_: Int, p_146286_3_: Int) {
        super.mouseReleased(p_146286_1_, p_146286_2_, p_146286_3_)
        categories.forEach { it.mouseRelease(p_146286_1_, p_146286_2_, p_146286_3_) }
    }

    override fun keyTyped(p_73869_1_: Char, p_73869_2_: Int) {
        if (!pauseKeyPresses) {
            super.keyTyped(p_73869_1_, p_73869_2_)
        }

        categories.forEach { it.keyTyped(p_73869_1_, p_73869_2_) }
    }

    override fun onGuiClosed() {
        super.onGuiClosed()

        pauseKeyPresses = false
        ModuleRegistry.INSTANCE!!.registerMap[ClickGUI::class.java]!!.toggled = false
    }

    companion object {
        var pauseKeyPresses = false

        var INSTANCE: FeatureListGuiScreen? = null
            get() {
                if (field == null) {
                    field = FeatureListGuiScreen()
                }

                return field
            }
    }
}