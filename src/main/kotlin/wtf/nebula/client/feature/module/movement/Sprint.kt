package wtf.nebula.client.feature.module.movement

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class Sprint : Module(ModuleCategory.MOVEMENT, "Makes you automatically sprint") {
    override fun onDeactivated() {
        if (!mc.gameSettings.keyBindSprint.isKeyDown) {
            mc.player.isSprinting = false
        }
    }

    @EventListener
    private val tickListener = listener<TickEvent> {

        if (!mc.player.isSprinting) {

            mc.player.isSprinting = mc.player.movementInput.moveForward > 0.0f &&
                    mc.player.foodStats.foodLevel > 6 &&
                    !mc.player.collidedHorizontally &&
                    !mc.player.isSneaking &&
                    !mc.player.isHandActive
        }
    }

}