package wtf.nebula.client.feature.module.miscellaneous

import com.mojang.realmsclient.gui.ChatFormatting
import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.ModuleToggledEvent
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class Notifications : Module(ModuleCategory.MISCELLANEOUS, "Notifications and shit") {
    val modules by bool("Modules", true)
    val totemPops by bool("Totem Pops", true)

    override fun onDeactivated() {
        super.onDeactivated()
        Nebula.totemPopManager.sendEvents = false
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        Nebula.totemPopManager.sendEvents = totemPops
    }

    @EventListener
    private val moduleToggledListener = listener<ModuleToggledEvent> {
        if (!modules || nullCheck()) {
            return@listener
        }

        printChatMessage(hashCode(),
            ChatFormatting.GRAY.toString() + it.module.name + " has toggled " + ChatFormatting.RESET.toString() +
            (if (it.module.toggled) ChatFormatting.GREEN.toString() + "on" else ChatFormatting.RED.toString() + "off"))
    }
}