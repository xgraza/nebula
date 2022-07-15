package wtf.nebula.client.event.client

import me.bush.eventbuskotlin.Event
import wtf.nebula.client.feature.module.Module

class ModuleToggledEvent(val module: Module) : Event() {
    override val cancellable: Boolean = false
}