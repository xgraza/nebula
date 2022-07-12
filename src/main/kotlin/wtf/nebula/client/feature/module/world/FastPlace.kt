package wtf.nebula.client.feature.module.world

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class FastPlace : Module(ModuleCategory.WORLD, "Places things fast and shit") {
    val speed by int("Speed", 4, 1..4)
    val ghostFix by bool("Ghost Fix", true)

    val blocks by bool("Blocks", true)
    val exp by bool("EXP", true)
    val fireworks by bool("Fireworks", true)
    val crystals by bool("Crystals", true)

    @EventListener
    private val tickListener = listener<TickEvent> {
        val holding = mc.player.heldItemMainhand.item
        if (blocks && holding is ItemBlock
            || exp && holding == Items.EXPERIENCE_BOTTLE
            || fireworks && holding == Items.FIREWORKS
            || crystals && holding == Items.END_CRYSTAL) {

            mc.rightClickDelayTimer = 4 - speed
        }

        if (ghostFix) {
            // TODO
        }
    }
}