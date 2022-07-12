package wtf.nebula.client.feature.module.miscellaneous

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.client.feature.module.combat.Aura
import wtf.nebula.client.registry.impl.ModuleRegistry
import wtf.nebula.util.MathUtil
import wtf.nebula.util.rotation.InvalidRotation
import wtf.nebula.util.rotation.Rotation

class AntiAim : Module(ModuleCategory.MISCELLANEOUS, "wtf") {
    val yaw by setting("Yaw", Yaw.SPIN)
    val pitch by setting("Pitch", Pitch.ROCKER)
    val speed by float("Speed", 1.0f, 1.0f..50.0f)
    val packet by bool("Packet", true)

    private var rotation: Rotation = InvalidRotation()

    override fun onDeactivated() {
        super.onDeactivated()
        rotation = InvalidRotation()
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (!rotation.valid) {
            rotation = Rotation(mc.player.rotationYaw, mc.player.rotationPitch)
        }

        // TODO: more modules when they're added
        if (ModuleRegistry.INSTANCE!!.registerMap[Aura::class.java]!!.isActive()) {
            return@listener
        }

        when (yaw) {
            Yaw.OFF -> {}
            Yaw.SPAZ -> rotation.yaw = MathUtil.random(0.0f..360.0f)
            Yaw.SPIN -> rotation.yaw += speed
            Yaw.REVERSE -> rotation.yaw = -mc.player.rotationYaw
        }

        when (pitch) {
            Pitch.OFF -> {}
            Pitch.SPAZ -> rotation.pitch = MathUtil.random(-90.0f..90.0f)
            Pitch.ROCKER -> {
                rotation.pitch += MathUtil.random(-speed..speed)
                if (rotation.pitch > 90.0f) {
                    rotation.pitch = 90.0f
                }

                else if (rotation.pitch < -90.0f) {
                    rotation.pitch = -90.0f
                }
            }
            Pitch.REVERSE -> rotation.pitch = -mc.player.rotationPitch
        }

        if (packet) {
            Nebula.rotationManager.rotation = rotation
        }

        else {
            mc.player.rotationYaw = rotation.yaw
            mc.player.rotationPitch = rotation.pitch
        }
    }

    enum class Yaw {
        OFF, SPAZ, REVERSE, SPIN
    }

    enum class Pitch {
        OFF, SPAZ, REVERSE, ROCKER
    }
}