package wtf.nebula.client.feature.module.world

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.player.motion.update.PostMotionUpdate
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.util.ext.getHitVecForPlacement
import wtf.nebula.util.inventory.InventoryRegion
import wtf.nebula.util.inventory.InventoryUtil
import wtf.nebula.util.rotation.AngleUtil
import wtf.nebula.util.rotation.InvalidRotation
import wtf.nebula.util.rotation.Rotation

class BlockFly : Module(ModuleCategory.WORLD, "Places blocks under you") {
    val rotations by setting("Rotations", Rotations.NORMAL)
    val tower by bool("Tower", true)
    val swing by bool("Swing", true)

    private var previous: PlaceInfo? = null
    private var current: PlaceInfo? = null

    private var lastRotation: Rotation = InvalidRotation()

    private var slot = -1

    override fun onDeactivated() {
        super.onDeactivated()

        if (slot != -1) {
            mc.player.connection.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))
        }

        slot = -1
        previous = null
        current = null
        lastRotation = InvalidRotation()
    }

    @EventListener
    private val postMotionListener = listener<PostMotionUpdate> {
        if (!lastRotation.valid) {
            lastRotation = Rotation(mc.player.rotationYaw, 89.0f)
        }

        else {
            Nebula.rotationManager.rotation = lastRotation
        }

        current = calcNextPlace()
        if (current == null) {
            return@listener
        }

        slot = InventoryUtil.findItem(InventoryRegion.HOTBAR) { it.item is ItemBlock }
        if (slot == -1) {
            if (slot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))
                slot = -1
            }

            return@listener
        }

        else {
            if (slot != Nebula.inventoryManager.serverSlot && slot != InventoryUtil.OFFHAND_SLOT) {
                mc.player.connection.sendPacket(CPacketHeldItemChange(slot))
            }
        }

        when (rotations) {
            Rotations.NONE -> {}

            Rotations.NORMAL -> {
                if (previous != null) {
                    val rot = AngleUtil.toBlock(previous!!.pos, previous!!.direction)
                    if (rot!!.valid) {
                        Nebula.rotationManager.rotation = rot
                        lastRotation = rot
                    }
                }
            }

            Rotations.CURRENT -> {
                val rot = AngleUtil.toBlock(current!!.pos, current!!.direction.opposite)
                if (rot!!.valid) {
                    Nebula.rotationManager.rotation = rot
                    lastRotation = rot
                }
            }
        }

        previous = calcNextPlace()

        if (tower && mc.gameSettings.keyBindJump.isKeyDown) {
            mc.player.jump()
            mc.player.motionX *= 0.6
            mc.player.motionZ *= 0.6

            if (mc.player.ticksExisted % 20 == 0) {
                mc.player.motionY = -0.28
            }
        }

        val result = mc.playerController.processRightClickBlock(
            mc.player,
            mc.world,
            current!!.pos,
            current!!.direction,
            current!!.pos.getHitVecForPlacement(current!!.direction),
            if (slot == InventoryUtil.OFFHAND_SLOT) EnumHand.OFF_HAND else EnumHand.MAIN_HAND
        )

        if (result != EnumActionResult.FAIL) {
            if (swing) {
                mc.player.swingArm(EnumHand.MAIN_HAND)
            }

            else {
                mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
            }
        }
    }

    private fun calcNextPlace(): PlaceInfo? {
        val pos = BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ)

        if (!mc.world.getBlockState(pos).material.isReplaceable) {
            return null
        }

        for (facing in EnumFacing.VALUES) {
            val neighbor = pos.offset(facing)
            if (!mc.world.getBlockState(neighbor).material.isReplaceable) {
                return PlaceInfo(neighbor, facing.opposite)
            }
        }

        for (dir in EnumFacing.HORIZONTALS) {
            val neighbor = pos.offset(dir)
            if (mc.world.getBlockState(neighbor).material.isReplaceable) {
                for (facing in EnumFacing.values()) {
                    val b = neighbor.offset(facing)
                    if (!mc.world.getBlockState(b).material.isReplaceable) {
                        return PlaceInfo(b, facing.opposite)
                    }
                }
            }
        }

        return null
    }

    enum class Rotations {
        NONE, NORMAL, CURRENT
    }
}

data class PlaceInfo(val pos: BlockPos, val direction: EnumFacing)