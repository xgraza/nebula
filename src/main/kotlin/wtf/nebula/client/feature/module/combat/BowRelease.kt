package wtf.nebula.client.feature.module.combat

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.item.ItemBow
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class BowRelease : Module(ModuleCategory.COMBAT, "releases ur bow for you lazy fuck") {
    val ticks by int("Ticks", 3, 3..20)
    val tpsSync by bool("TPS Sync", false)

    override fun getDisplayInfo(): String? {
        return ticks.toString()
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (mc.player.isHandActive && mc.player.activeItemStack.item is ItemBow) {
            if (mc.player.itemInUseMaxCount >= ticks) {
                mc.player.stopActiveHand()
                mc.player.connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            }
        }
    }
}