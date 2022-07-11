package wtf.nebula.client.feature.module.combat

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.init.Items
import net.minecraft.item.ItemBow
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.packet.PacketSendEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import kotlin.math.cos
import kotlin.math.sin

class FastProjectile : Module(ModuleCategory.COMBAT, "Makes projectiles go further") {
    val boost by double("Boost", 10.0, 1.0..100.0)
    val sprint by bool("Sprint", true)

    // items
    val bows by bool("Bows", true)
    val pearls by bool("Pearls", true)
    val snowballs by bool("Snowballs", true)
    val eggs by bool("Eggs", true)

    @EventListener
    private val packetSendEvent = listener<PacketSendEvent> {
        if (it.packet is CPacketPlayerDigging) {
            val packet = it.packet
            if (packet.action == CPacketPlayerDigging.Action.RELEASE_USE_ITEM
                && Nebula.inventoryManager.getHeldItemStack().item is ItemBow
                && bows) {

                doFunny()
            }
        }

        else if (it.packet is CPacketPlayerTryUseItem) {
            val held = Nebula.inventoryManager.getHeldItemStack()
            if ((held.item is ItemBow && pearls)
                || (held.item == Items.SNOWBALL && snowballs)
                || (held.item == Items.EGG && eggs)) {

                doFunny()
            }
        }
    }

    private fun doFunny() {
        if (sprint) {
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING))
        }

        val sin = -sin(mc.player.rotationYaw)
        val cos = cos(mc.player.rotationYaw)

        if (mc.player.ticksExisted % 2 == 0) {
            mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX + (sin * boost), mc.player.posY, mc.player.posZ - (cos * boost), false))
            mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX - (sin * boost), mc.player.posY, mc.player.posZ + (cos * boost), false))
        }

        else {
            mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX - (sin * boost), mc.player.posY, mc.player.posZ + (cos * boost), false))
            mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX + (sin * boost), mc.player.posY, mc.player.posZ - (cos * boost), false))
        }
    }
}