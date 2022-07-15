package wtf.nebula.client.registry.impl

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import org.lwjgl.input.Keyboard
import wtf.nebula.client.event.input.KeyInputEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.client.feature.module.combat.*
import wtf.nebula.client.feature.module.miscellaneous.*
import wtf.nebula.client.feature.module.movement.*
import wtf.nebula.client.feature.module.render.*
import wtf.nebula.client.feature.module.world.*
import wtf.nebula.client.registry.Registry

class ModuleRegistry : Registry<Module>() {
    val moduleByCategory = mutableMapOf<ModuleCategory, Set<Module>>()

    init {
        INSTANCE = this
    }

    override fun load() {
        loadMember(Aura())
        loadMember(AutoTotem())
        loadMember(BowRelease())
        loadMember(Criticals())
        loadMember(FastProjectile())
        loadMember(SelfFill())

        loadMember(AntiAim())
        loadMember(NoFML())
        loadMember(NoRotate())
        loadMember(Notifications())
        loadMember(PingSpoof())
        loadMember(Refill())
        loadMember(ResetVL())
        loadMember(Respawn())
        loadMember(XCarry())

        loadMember(FastFall())
        loadMember(LongJump())
        loadMember(NoSlow())
        loadMember(PacketFlight())
        loadMember(Phase())
        loadMember(Speed())
        loadMember(Sprint())
        loadMember(Step())
        loadMember(TickShift())
        loadMember(Velocity())

        loadMember(ClickGUI())
        loadMember(Colors())
        loadMember(Fullbright())
        loadMember(HUD())
        loadMember(NoRender())

        loadMember(Avoid())
        loadMember(BlockFly())
        loadMember(FastPlace())
        loadMember(Timer())
        loadMember(Yaw())

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