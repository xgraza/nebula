package wtf.nebula.client.feature.guis.feature

import wtf.nebula.client.config.setting.BindSetting
import wtf.nebula.client.config.setting.NumberSetting
import wtf.nebula.client.config.setting.Setting
import wtf.nebula.client.feature.guis.common.Component
import wtf.nebula.client.feature.guis.feature.setting.BindComponent
import wtf.nebula.client.feature.guis.feature.setting.BooleanComponent
import wtf.nebula.client.feature.guis.feature.setting.EnumComponent
import wtf.nebula.client.feature.guis.feature.setting.NumberComponent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.render.Colors
import wtf.nebula.util.render.RenderUtil
import java.awt.Color

class ModuleComponent(val module: Module) : Component(module.name) {
    init {
        module.settings.forEach {
            if (it is BindSetting) {
                children += BindComponent(it)
            }

            else if (it is NumberSetting) {
                children += NumberComponent(it as NumberSetting<Number>)
            }

            else {
                if (it.value is Boolean) {
                    children += BooleanComponent(it as Setting<Boolean>)
                }

                else if (it.value is Enum<*>) {
                    children += EnumComponent(it as Setting<Enum<*>>)
                }
            }
        }
    }

    private var expanded = false

    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        // Color(191, 52, 52, 140).rgb
        RenderUtil.rect(x, y, width, height, if (module.toggled) Colors.color() else Color(41, 41, 41).rgb)

        val textY = (y + (height / 2.0) - (mc.fontRenderer.FONT_HEIGHT / 2.0)).toFloat()
        mc.fontRenderer.drawStringWithShadow(name, (x + 2.3).toFloat(), textY, -1)

        val indicator = if (expanded) "-" else "+"
        mc.fontRenderer.drawStringWithShadow(indicator, (x + width - (mc.fontRenderer.getStringWidth(indicator) + 4.0)).toFloat(), textY, -1)

        if (expanded) {
            height = 16.0 + children.sumOf { it.height + 1.0 } + 1.0

            var posY = y + 16.0
            children.forEach {
                it.x = x + 2.0
                it.y = posY
                it.width = width - 4.0
                it.height = 15.0

                it.render(mouseX, mouseY, partialTicks)

                posY += it.height + 1.0
            }
        } else height = 15.0
    }

    override fun mouseClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (isMouseWithinBounds(mouseX, mouseY, x, y, width, 15.0)) {

            playClickSound()
            if (mouseButton == 0) {
                module.toggled = !module.toggled
            }

            else if (mouseButton == 1) {
                expanded = !expanded

                if (!expanded) {
                    children.filterIsInstance<BindComponent>().forEach { it.listening = false }
                }
            }

            return
        }

        children.forEach { it.mouseClick(mouseX, mouseY, mouseButton) }
    }

    override fun mouseScroll(mouseX: Int, mouseY: Int, scroll: Int) {

    }

    override fun mouseRelease(mouseX: Int, mouseY: Int, state: Int) {
        if (expanded) {
            children.forEach { it.mouseRelease(mouseX, mouseY, state) }
        }
    }

    override fun keyTyped(keyTyped: Char, keyCode: Int) {
        if (expanded) {
            children.forEach { it.keyTyped(keyTyped, keyCode) }
        }
    }
}