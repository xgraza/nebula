package wtf.nebula.client.registry.impl

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import org.lwjgl.input.Keyboard
import wtf.nebula.client.event.input.KeyInputEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.client.feature.module.combat.Aura
import wtf.nebula.client.feature.module.combat.Criticals
import wtf.nebula.client.feature.module.combat.FastProjectile
import wtf.nebula.client.feature.module.miscellaneous.Notifications
import wtf.nebula.client.feature.module.movement.*
import wtf.nebula.client.feature.module.render.ClickGUI
import wtf.nebula.client.feature.module.render.Colors
import wtf.nebula.client.feature.module.render.Fullbright
import wtf.nebula.client.feature.module.render.HUD
import wtf.nebula.client.feature.module.world.Avoid
import wtf.nebula.client.feature.module.world.BlockFly
import wtf.nebula.client.feature.module.world.FastPlace
import wtf.nebula.client.feature.module.world.Timer
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

        loadMember(Notifications())

        loadMember(LongJump())
        loadMember(NoSlow())
        loadMember(Speed())
        loadMember(Sprint())
        loadMember(Step())
        loadMember(TickShift())
        loadMember(Velocity())

        loadMember(ClickGUI())
        loadMember(Colors())
        loadMember(Fullbright())
        loadMember(HUD())

        loadMember(Avoid())
        loadMember(BlockFly())
        loadMember(FastPlace())
        loadMember(Timer())

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

        if (member is HUD || member is Colors || member is Notifications) {
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