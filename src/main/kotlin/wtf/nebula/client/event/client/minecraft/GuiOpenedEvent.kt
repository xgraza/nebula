package wtf.nebula.client.event.client.minecraft

import me.bush.eventbuskotlin.Event
import net.minecraft.client.gui.GuiScreen

class GuiOpenedEvent(val current: GuiScreen?, val opened: GuiScreen?) : Event() {
    override val cancellable: Boolean = false
}