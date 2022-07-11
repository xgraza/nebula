package wtf.nebula.client.event.render

import me.bush.eventbuskotlin.Event
import net.minecraft.client.gui.ScaledResolution

class RenderHUDEvent(val res: ScaledResolution) : Event() {
    override val cancellable: Boolean = false
}