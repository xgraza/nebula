package wtf.nebula.client.feature.module.render

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.init.MobEffects
import net.minecraft.potion.PotionEffect
import wtf.nebula.client.config.setting.Setting
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class Fullbright : Module(ModuleCategory.RENDER, "Makes the game bright") {
    val mode by setting("Mode", Mode.GAMMA)

    private var gaveNightVision = false
    private var oldGamma = -1.0f

    override fun getDisplayInfo(): String? {
        return Setting.formatEnum(mode)
    }

    override fun onDeactivated() {
        super.onDeactivated()

        if (gaveNightVision) {
            mc.player.removePotionEffect(MobEffects.NIGHT_VISION)
            gaveNightVision = false
        }

        if (oldGamma != -1.0f) {
            mc.gameSettings.gammaSetting = oldGamma
            oldGamma = -1.0f
        }
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        when (mode) {
            Mode.GAMMA -> {
                if (gaveNightVision) {
                    mc.player.removePotionEffect(MobEffects.NIGHT_VISION)
                    gaveNightVision = false
                }

                if (oldGamma == -1.0f) {
                    oldGamma = mc.gameSettings.gammaSetting
                }

                mc.gameSettings.gammaSetting = 100.0f
            }

            Mode.POTION -> {
                if (!gaveNightVision && mc.player.isPotionActive(MobEffects.NIGHT_VISION)) {
                    mc.player.addPotionEffect(PotionEffect(MobEffects.NIGHT_VISION, Int.MAX_VALUE))
                    gaveNightVision = true
                }

                if (oldGamma != -1.0f) {
                    mc.gameSettings.gammaSetting = oldGamma
                    oldGamma = -1.0f
                }
            }
        }
    }

    enum class Mode {
        GAMMA, POTION
    }
}