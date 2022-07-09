package wtf.nebula.client.feature.module

import org.lwjgl.input.Keyboard
import wtf.nebula.client.config.ConfigurableFeature

open class Module(val category: ModuleCategory, val description: String) : ConfigurableFeature() {
    var bind by bind("Bind", Keyboard.KEY_NONE)
}