package wtf.nebula.client.registry.impl

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import org.lwjgl.input.Keyboard
import wtf.nebula.client.event.KeyInputEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.movement.Sprint
import wtf.nebula.client.registry.Registry

class ModuleRegistry : Registry<Module>() {
    override fun load() {
        loadMember(Sprint())

        logger.info("Loaded ${registers.size} modules")
    }

    override fun unload() {
        //TODO
    }

    override fun loadMember(member: Module) {
        registerMap[member::class.java] = member
        registers += member
    }

    @EventListener
    private val keyInputListener = listener<KeyInputEvent> {
        if (mc.currentScreen == null && it.keyCode != Keyboard.KEY_NONE && !it.state) {
            registers.forEach { mod ->
                if (mod.bind == it.keyCode) {
                    mod.toggled = !mod.toggled
                }
            }
        }
    }
}