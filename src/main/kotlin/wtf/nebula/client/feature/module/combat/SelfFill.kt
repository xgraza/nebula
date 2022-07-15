package wtf.nebula.client.feature.module.combat

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.util.ext.getHitVecForPlacement
import wtf.nebula.util.inventory.InventoryRegion
import wtf.nebula.util.inventory.InventoryUtil
import wtf.nebula.util.rotation.Rotation

class SelfFill : Module(ModuleCategory.COMBAT, "cringe") {
    val rotate by bool("Rotate", true)
    val swing by bool("Swing", true)
    val flag by bool("Flag", true)

    private var pos = BlockPos.ORIGIN
    private var placing = false

    override fun onDeactivated() {
        super.onDeactivated()

        pos = BlockPos.ORIGIN
        placing = false
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (pos == BlockPos.ORIGIN) {
            pos = BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)
        }

        if (!mc.world.getBlockState(pos).material.isReplaceable) {
            toggled = false
            return@listener
        }

        var hand = EnumHand.MAIN_HAND

        val slot = Nebula.inventoryManager.getSlot(InventoryRegion.HOTBAR, PLACE_BLOCKS)
        if (slot == -1) {
            toggled = false
            return@listener
        }

        if (rotate) {
            mc.player.connection.sendPacket(CPacketPlayer.Rotation(mc.player.rotationYaw, 90.0f, false))
            Nebula.rotationManager.rotation = Rotation(mc.player.rotationYaw, 90.0f)
        }

        if (slot != InventoryUtil.OFFHAND_SLOT) {
            mc.player.connection.sendPacket(CPacketHeldItemChange(slot))
        }

        else {
            hand = EnumHand.OFF_HAND
        }

        for (offset in FAKE_JUMPS) {
            mc.player.connection.sendPacket(CPacketPlayer.Position(
                mc.player.posX, mc.player.posY + offset, mc.player.posZ, false))
        }

        placing = true

        // TODO: are these facing values right?
        val result = mc.playerController.processRightClickBlock(
            mc.player,
            mc.world,
            pos.offset(EnumFacing.DOWN),
            EnumFacing.UP,
            pos.getHitVecForPlacement(EnumFacing.DOWN),
            hand
        )

        if (result != EnumActionResult.FAIL) {
            if (swing) {
                mc.player.swingArm(hand)
            }

            else {
                mc.player.connection.sendPacket(CPacketAnimation(hand))
            }
        }

        if (flag) {
            mc.player.connection.sendPacket(
                CPacketPlayer.Position(
                    mc.player.posX, mc.player.posY + 1.6, mc.player.posZ, false
                )
            )
        }

        if (hand == EnumHand.MAIN_HAND) {
            mc.player.connection.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))
        }

        placing = false
        toggled = false
    }

    override fun isActive(): Boolean {
        return super.isActive() && placing
    }

    companion object {
        private val FAKE_JUMPS = arrayOf(0.41999998688698, 0.7531999805211997, 1.00133597911214, 1.16610926093821)
        private val PLACE_BLOCKS = arrayOf(Blocks.OBSIDIAN, Blocks.ENDER_CHEST)
    }
}