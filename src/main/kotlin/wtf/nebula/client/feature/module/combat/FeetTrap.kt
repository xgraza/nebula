package wtf.nebula.client.feature.module.combat

import io.netty.util.internal.ConcurrentSet
import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.SPacketBlockChange
import net.minecraft.network.play.server.SPacketSpawnObject
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.packet.PacketReceiveEvent
import wtf.nebula.client.event.render.RenderWorldEvent
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.util.Globals
import wtf.nebula.util.Timer
import wtf.nebula.util.ext.getHitVecForPlacement
import wtf.nebula.util.ext.isReplaceable
import wtf.nebula.util.inventory.InventoryRegion
import wtf.nebula.util.render.RenderUtil
import wtf.nebula.util.rotation.AngleUtil
import wtf.nebula.util.rotation.Bone
import wtf.nebula.util.world.SNEAK_BLOCKS
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.round

class FeetTrap : Module(ModuleCategory.COMBAT, "Traps your feet to prevent from die") {
    val timing by setting("Timing", Timing.SEQUENTIAL)
    val blocksPerTick by int("Blocks", 4, 1..6)
    val delay by int("Delay", 2, 0..20)
    val rotate by bool("Rotate", true)
    val swing by bool("Swing", true)
    val offGroundDisable by bool("Off Ground Disable", true)
    val render by bool("Render", false)

    private var searchThread: SurroundSearchThread? = null

    private val timer = Timer()
    private var blocks = 0

    private var startY = 0.0

    private var nextTickPlacements = CopyOnWriteArrayList<BlockPos>()

    override fun onActivated() {
        super.onActivated()

        blocks = 0
        startY = mc.player.posY

        searchThread = SurroundSearchThread(this)
        searchThread!!.start()
    }

    override fun onDeactivated() {
        super.onDeactivated()

        mc.player.connection.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))

        nextTickPlacements.clear()

        if (searchThread != null) {
            searchThread!!.interrupt()
            searchThread = null
        }
    }

    override fun getDisplayInfo(): String? {
        var size = nextTickPlacements.size
        if (searchThread != null) {
            size += searchThread!!.blocksNeededToReplace.size
        }

        return size.toString()
    }

    @EventListener
    private val worldRenderListener = listener<RenderWorldEvent> {

        if (render) {
            for (pos in searchThread!!.blocks) {
                RenderUtil.renderBoxEsp(
                    AxisAlignedBB(pos)
                        .offset(
                            -mc.renderManager.viewerPosX,
                            -mc.renderManager.viewerPosY,
                            -mc.renderManager.viewerPosZ
                        ),
                    Color(0, 255, 0, 120).rgb
                )
            }

            for (placement in nextTickPlacements) {
                RenderUtil.renderBoxEsp(
                    AxisAlignedBB(placement)
                        .offset(
                            -mc.renderManager.viewerPosX,
                            -mc.renderManager.viewerPosY,
                            -mc.renderManager.viewerPosZ
                        ),
                    Color(255, 0, 0, 120).rgb
                )
            }

            for (placement in searchThread!!.blocksNeededToReplace) {
                RenderUtil.renderBoxEsp(
                    AxisAlignedBB(placement)
                        .offset(
                            -mc.renderManager.viewerPosX,
                            -mc.renderManager.viewerPosY,
                            -mc.renderManager.viewerPosZ
                        ),
                    Color(255, 0, 0, 120).rgb
                )
            }
        }
    }

    @EventListener
    private val packetReceiveListener = listener<PacketReceiveEvent> {
        if (searchThread != null) {

            when (it.packet) {
                is SPacketSpawnObject -> {
                    if (it.packet.type == 51) {

                        // end crystals have a width & height of 2.0

                        val base = BlockPos(it.packet.x, it.packet.y, it.packet.z)
                        val box = AxisAlignedBB(base, base.add(2.0, 2.0, 2.0))

                        val up = base.up()

                        for (pos in ArrayList(nextTickPlacements + searchThread!!.blocksNeededToReplace)) {

                            if (pos == up && doesBBBlockSurround(box, up)) {
                                breakCrystal(it.packet.entityID)

                                if (timing == Timing.SEQUENTIAL) {
                                    ++blocks
                                }

                                break
                            }
                        }
                    }
                }

                is SPacketBlockChange -> {
                    val pos = it.packet.blockPosition
                    val state = it.packet.blockState

                    if (state != null && pos != null) {

                        if (state.material.isReplaceable) {

                            if (timing == Timing.SEQUENTIAL) {
                                ++blocks
                            }

                            placeBlockAt(pos)
                        }
                    }
                }
            }
        }
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (mc.player.posY > startY && offGroundDisable) {
            toggled = false
            return@listener
        }

        if (!timer.passedTime(delay.toLong() * 50L)) {
            return@listener
        }

        // reset every tick
        blocks = 0

        if (nextTickPlacements.isNotEmpty()) {

            for (blockPos in nextTickPlacements) {
                if (blocks >= blocksPerTick) {
                    timer.resetTime()
                    return@listener
                }

                placeBlockAt(blockPos, true)
                nextTickPlacements.remove(blockPos)

                ++blocks
            }
        }

        if (blocks >= blocksPerTick) {
            timer.resetTime()
            return@listener
        }

        if (searchThread != null && !searchThread!!.isInterrupted && searchThread!!.blocksNeededToReplace.isNotEmpty()) {

            for (blockPos in searchThread!!.blocksNeededToReplace) {
                if (blocks >= blocksPerTick) {
                    timer.resetTime()
                    return@listener
                }

                placeBlockAt(blockPos, true)
                ++blocks
            }
        }

        if (nextTickPlacements.isEmpty() && searchThread!!.blocksNeededToReplace.isEmpty()) {
            mc.player.connection.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))
        }
    }

    private fun placeBlockAt(blockPos: BlockPos, ignoreEntities: Boolean = false) {
        if (blocks >= blocksPerTick) {
            timer.resetTime()
            nextTickPlacements.add(blockPos)
            return
        }

        if (!ignoreEntities) {
            for (entity in ArrayList(mc.world.loadedEntityList)) {
                if (entity !is EntityEnderCrystal || entity.isDead) {
                    continue
                }

                if (doesBBBlockSurround(entity.entityBoundingBox, blockPos)) {

                    // attack this cunt ass crystal
                    if (rotate) {
                        val rotation = AngleUtil.toEntity(entity, Bone.HEAD)
                        if (rotation != null) {
                            Nebula.rotationManager.rotation = rotation
                            mc.player.connection.sendPacket(CPacketPlayer.Rotation(rotation.yaw, rotation.pitch, mc.player.onGround))
                        }
                    }

                    breakCrystal(entity.entityId)

                    if (timing == Timing.SEQUENTIAL) {
                        nextTickPlacements.add(blockPos)
                        return
                    }

                    break
                }
            }
        }

        val slot = Nebula.inventoryManager.getSlot(InventoryRegion.HOTBAR, arrayOf(Blocks.OBSIDIAN, Blocks.ENDER_CHEST))
        if (slot == -1) {
            toggled = false
            return
        }

        if (Nebula.inventoryManager.serverSlot != slot) {
            mc.player.connection.sendPacket(CPacketHeldItemChange(slot))
        }

        for (facing in EnumFacing.values()) {

            val neighbor = blockPos.offset(facing)
            val blockState = mc.world.getBlockState(neighbor)

            if (blockState.material.isReplaceable) {
                continue
            }

            if (rotate) {
                val rotation = AngleUtil.toBlock(neighbor, facing.opposite)
                if (rotation != null) {
                    Nebula.rotationManager.rotation = rotation
                    mc.player.connection.sendPacket(CPacketPlayer.Rotation(rotation.yaw, rotation.pitch, mc.player.onGround))
                }
            }

            val shouldSneak = !mc.player.isSneaking && SNEAK_BLOCKS.contains(blockState.block)
            if (shouldSneak) {
                mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING))
            }

            val hitVec = neighbor.getHitVecForPlacement(facing.opposite)

            val facingX = (hitVec.x - neighbor.x).toFloat()
            val facingY = (hitVec.y - neighbor.y).toFloat()
            val facingZ = (hitVec.z - neighbor.z).toFloat()

            mc.player.connection.sendPacket(CPacketPlayerTryUseItemOnBlock(neighbor, facing.opposite, EnumHand.MAIN_HAND, facingX, facingY, facingZ))

            if (swing) {
                mc.player.swingArm(EnumHand.MAIN_HAND)
            }

            else {
                mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
            }

            if (shouldSneak) {
                mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING))
            }
        }
    }

    private fun breakCrystal(id: Int) {
        val packet = CPacketUseEntity()
        packet.entityId = id
        packet.action = CPacketUseEntity.Action.ATTACK

        mc.player.connection.sendPacket(packet)

        if (swing) {
            mc.player.swingArm(EnumHand.MAIN_HAND)
        } else {
            mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
        }
    }

    private fun doesBBBlockSurround(axisAlignedBB: AxisAlignedBB, blockPos: BlockPos? = null): Boolean {
        if (searchThread == null) {
            return false
        }

        if (blockPos == null) {
            searchThread!!.blocks.forEach {
                if (AxisAlignedBB(it).intersects(axisAlignedBB)) {
                    return true
                }
            }
        }

        else {
            return AxisAlignedBB(blockPos).intersects(axisAlignedBB)
        }

        return false
    }

    fun isEntityBlocking(blockPos: BlockPos): Boolean {
        val bb = AxisAlignedBB(blockPos)

        try {
            for (entity in ArrayList(mc.world.loadedEntityList)) {
                if (entity.isDead || entity is EntityItem || entity is EntityXPOrb) {
                    continue
                }

                if (bb.intersects(entity.entityBoundingBox)) {
                    return true
                }
            }
        } catch (e: Exception) {
            return false
        }

        return false
    }

    enum class Timing {
        VANILLA, SEQUENTIAL
    }
}

class SurroundSearchThread(private val feetTrap: FeetTrap) : Thread(), Globals {
    var blocks = HashSet<BlockPos>()
    var blocksNeededToReplace = ConcurrentSet<BlockPos>()

    override fun run() {
        while (!isInterrupted) {
            val p = BlockPos(mc.player.posX, round(mc.player.posY), mc.player.posZ)
            val lookupBlocks = mutableSetOf<BlockPos>()

            for (facing in EnumFacing.values()) {
                if (facing == EnumFacing.UP) {
                    continue
                }

                val neighbor = p.offset(facing)
                if (feetTrap.isEntityBlocking(neighbor)) {
                    lookupBlocks.add(neighbor)
                }

                else {
                    if (neighbor.isReplaceable()) {
                        blocksNeededToReplace += neighbor
                    }

                    blocks += neighbor
                }
            }

            if (lookupBlocks.isNotEmpty()) {
                for (pos in lookupBlocks) {

                    for (facing in EnumFacing.values()) {
                        if (facing == EnumFacing.UP) {
                            continue
                        }

                        val neighbor = pos.offset(facing)
                        if (neighbor.isReplaceable()) {

                            if (feetTrap.isEntityBlocking(neighbor)) {

                                for (dir in EnumFacing.values()) {
                                    if (dir == EnumFacing.UP) {
                                        continue
                                    }

                                    val otherNeighbor = neighbor.offset(dir)
                                    if (!feetTrap.isEntityBlocking(otherNeighbor)) {
                                        if (otherNeighbor.isReplaceable()) {
                                            blocksNeededToReplace += otherNeighbor
                                        }

                                        blocks += otherNeighbor
                                    }
                                }
                            }

                            else {
                                blocksNeededToReplace += neighbor
                            }
                        }

                        blocks += neighbor
                    }
                }
            }

            blocksNeededToReplace.removeIf { !mc.world.getBlockState(it).material.isReplaceable }
        }
    }
}