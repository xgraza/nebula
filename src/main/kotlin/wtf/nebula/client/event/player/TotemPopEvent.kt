package wtf.nebula.client.event.player

import me.bush.eventbuskotlin.Event
import net.minecraft.entity.player.EntityPlayer

class TotemPopEvent(val entityPlayer: EntityPlayer) : Event() {
    override val cancellable: Boolean = false
}