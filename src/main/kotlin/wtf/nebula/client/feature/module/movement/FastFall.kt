package wtf.nebula.client.feature.module.movement

import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class FastFall : Module(ModuleCategory.MOVEMENT, "makes u fall fast - weirdo") {
    val mode by setting("Mode", Mode.MOTION)
    val height by double("Height", 2.0, 1.0..5.0)
    val speed by double("Speed", 1.0, 1.0..5.0)

    enum class Mode {
        MOTION, TIMER, STRICT
    }
}