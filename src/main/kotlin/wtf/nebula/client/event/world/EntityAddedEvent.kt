package wtf.nebula.client.event.world

import me.bush.eventbuskotlin.Event
import net.minecraft.entity.Entity

class EntityAddedEvent(val entity: Entity) : Event() {
    override val cancellable: Boolean = false
}