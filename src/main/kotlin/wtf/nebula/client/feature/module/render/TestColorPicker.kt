package wtf.nebula.client.feature.module.render

import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import java.awt.Color

class TestColorPicker : Module(ModuleCategory.RENDER, "lol") {
    val color by color("Color", Color(255, 255, 255, 120))
}