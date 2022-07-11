package wtf.nebula.client.feature.module.movement

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import wtf.nebula.client.config.setting.Setting
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class Sprint : Module(ModuleCategory.MOVEMENT, "Makes you automatically sprint") {
    val mode by setting("Mode", Mode.LEGIT)

    override fun getDisplayInfo(): String? {
        return Setting.formatEnum(mode)
    }

    override fun onDeactivated() {
        super.onDeactivated()
        if (!mc.gameSettings.keyBindSprint.isKeyDown) {
            mc.player.isSprinting = false
        }
    }

    @EventListener
    private val tickListener = listener<TickEvent> {

        if (!mc.player.isSprinting) {

            mc.player.isSprinting = when (mode) {
                Mode.LEGIT -> mc.player.movementInput.moveForward > 0.0f &&
                        mc.player.foodStats.foodLevel > 6 &&
                        !mc.player.collidedHorizontally &&
                        !mc.player.isSneaking &&
                        !mc.player.isHandActive
                Mode.NORMAL -> mc.player.movementInput.moveForward > 0.0f && mc.player.foodStats.foodLevel > 6
                Mode.RAGE -> true

                // TODO: backwards sprint should go 20km/h rather than 15km/h
                Mode.OMNI -> mc.player.movementInput.moveForward != 0.0f &&
                        mc.player.foodStats.foodLevel > 6

            }
        }
    }

    enum class Mode {
        LEGIT, NORMAL, RAGE, OMNI
    }
}