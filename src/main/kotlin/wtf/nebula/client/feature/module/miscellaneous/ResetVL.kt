package wtf.nebula.client.feature.module.miscellaneous

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class ResetVL : Module(ModuleCategory.MISCELLANEOUS, "Resets/reduces your violation count to prevent anti-cheat kicks") {
    val count by int("Jumps", 25, 10..200)

    private var jumps = 0
    private var posY = 0.0

    override fun getDisplayInfo(): String? {
        return "$jumps/$count"
    }

    override fun onActivated() {
        super.onActivated()

        posY = mc.player.posY
        jumps = 0
    }

    override fun onDeactivated() {
        super.onDeactivated()
        Nebula.serverManager.speed = 1.0f
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (mc.player.onGround) {
            if (jumps <= count) {
                mc.player.motionY = 0.11
                jumps++
            }
        }

        if (jumps <= count) {
            mc.player.posY = posY
            Nebula.serverManager.speed = 2.25f
        }

        else {
            Nebula.serverManager.speed = 1.0f
            toggled = false
        }
    }
}