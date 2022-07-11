package wtf.nebula.client.feature.module.render

import org.lwjgl.input.Keyboard
import wtf.nebula.client.feature.guis.feature.FeatureListGuiScreen
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class ClickGUI : Module(ModuleCategory.RENDER, "Opens a navigable GUI to configure modules") {
    init {
        bind = Keyboard.KEY_RSHIFT
    }

    override fun onActivated() {
        mc.displayGuiScreen(FeatureListGuiScreen.INSTANCE)
    }
}