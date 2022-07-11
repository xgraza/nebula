package wtf.nebula.client.feature.module.render

import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.util.render.ColorUtil

class Colors : Module(ModuleCategory.RENDER, "Colors... wow") {
    init {
        instance = this
    }

    val hue by float("Hue", 0.0f, 0.0f..346.0f)
    val brightness by float("Brightness", 1.0f, 0.0f..1.0f)
    val saturation by float("Saturation", 1.0f, 0.0f..1.0f)
    val rainbow by bool("Rainbow", false)

    companion object {
        lateinit var instance: Colors

        fun color(): Int {
            if (instance.rainbow) {
                return ColorUtil.rainbow(0.0, instance.saturation, instance.brightness)
            }

            return ColorUtil.toHSB(instance.hue / 360.0f, instance.saturation, instance.brightness)
        }
    }
}