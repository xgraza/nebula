package wtf.nebula.client.registry.impl

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import org.lwjgl.input.Keyboard
import wtf.nebula.client.event.KeyInputEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.client.feature.module.combat.Aura
import wtf.nebula.client.feature.module.combat.Criticals
import wtf.nebula.client.feature.module.combat.FastProjectile
import wtf.nebula.client.feature.module.movement.Speed
import wtf.nebula.client.feature.module.movement.Sprint
import wtf.nebula.client.feature.module.render.ClickGUI
import wtf.nebula.client.feature.module.render.Colors
import wtf.nebula.client.feature.module.render.HUD
import wtf.nebula.client.registry.Registry

class ModuleRegistry : Registry<Module>() {
    val moduleByCategory = mutableMapOf<ModuleCategory, Set<Module>>()

    init {
        INSTANCE = this
    }

    override fun load() {
        loadMember(Aura())
        loadMember(Criticals())
        loadMember(FastProjectile())

        loadMember(Speed())
        loadMember(Sprint())

        loadMember(ClickGUI())
        loadMember(Colors())
        loadMember(HUD())

        logger.info("Loaded ${registers.size} modules")
    }

    override fun unload() {
        //TODO
    }

    override fun loadMember(member: Module) {
        registerMap[member::class.java] = member
        registers += member

        val values = (moduleByCategory[member.category] ?: HashSet()).toMutableSet()
        values += member

        moduleByCategory[member.category] = values

        if (member is HUD || member is Colors) {
            member.drawn = false
            member.toggled = true
        }
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

    companion object {
        var INSTANCE: ModuleRegistry? = null
    }
}