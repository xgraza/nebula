package wtf.nebula.client.event.world

import me.bush.eventbuskotlin.Event
import net.minecraft.entity.Entity

class EntityRemovedEvent(val entity: Entity) : Event() {
    override val cancellable: Boolean = false
}