package wtf.nebula.client.feature.module.combat

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.server.SPacketDestroyEntities
import net.minecraft.network.play.server.SPacketExplosion
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.network.play.server.SPacketSpawnObject
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.packet.PacketReceiveEvent
import wtf.nebula.client.event.packet.PacketSendEvent
import wtf.nebula.client.event.render.RenderWorldEvent
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.util.Globals
import wtf.nebula.util.Timer
import wtf.nebula.util.combat.ExplosionUtil
import wtf.nebula.util.ext.canPlaceCrystal
import wtf.nebula.util.ext.getSphere
import wtf.nebula.util.inventory.InventoryRegion
import wtf.nebula.util.inventory.InventoryUtil
import wtf.nebula.util.render.RenderUtil
import wtf.nebula.util.rotation.AngleUtil

class AutoCrystal : Module(ModuleCategory.COMBAT, "Automatically places and breaks end crystals") {
    val timing by setting("Timing", Timing.SEQUENTIAL)
    val rotate by setting("Rotate", Rotate.NORMAL)

    val place by bool("Place", true)
    val placeSpeed by double("Place Speed", 20.0, 1.0..20.0)
    val placeRange by double("Place Range", 4.5, 1.0..6.0)
    val placeWallRange by double("Place Wall Range", 3.0, 1.0..6.0)
    val strictDirection by bool("Strict Direction", false)
    val protocol by setting("Protocol", Protocol.NATIVE)
    val armorBreaker by bool("Armor Breaker", true)

    val attack by bool("Attack", true)
    val attackSpeed by double("Attack Speed", 20.0, 1.0..20.0)
    val attackRange by double("Attack Range", 4.5, 1.0..6.0)
    val attackWallRange by double("Attack Wall Range", 3.0, 1.0..6.0)
    val ticksExisted by int("Ticks Existed", 0, 0..10)
    val inhibit by bool("Inhibit", true)
    val await by bool("Await", true)

    val minDamage by float("Minimum Damage", 6.0f, 0.0f..20.0f)
    val maxLocal by float("Maximum Local", 12.0f, 2.0f..20.0f)
    val safety by bool("Safety", true)
    val lethalMultiplier by float("Lethal Multiplier", 1.5f, 1.0f..3.0f)

    val swap by setting("Swap", Swap.NORMAL)
    val swapDelay by int("Swap Delay", 6, 0..20)

    private val attackTimes = DoubleArray(20)
    private var attackIndex = 0
    private var crystalSpawned = false

    private var crystalsPlaced = 0

    private val placeTimer = Timer()
    private val attackTimer = Timer()
    private val crystalsPerSec = Timer()

    private var lookupThread: AutoCrystalLookup? = null

    private var hand = EnumHand.MAIN_HAND

    override fun getDisplayInfo(): String? {
        val stringBuilder = StringBuilder()

        val secs = (crystalsPerSec.timePassedMs.toDouble() / 1000.0)
        var crystals = 0
        if (secs >= 1.0) {
            crystals = (crystalsPlaced / secs).toInt()
        }

        stringBuilder.append(crystals).append(", ")

        var averageAttackTime = 0.0
        for (attackTime in attackTimes) {
            if (attackTime > 0.0) {
                averageAttackTime += attackTime
            }
        }

        if (averageAttackTime > 0.0) {
            averageAttackTime /= 20.0
        }

        stringBuilder.append(String.format("%.1f", averageAttackTime))

        return stringBuilder.toString()
    }

    override fun onActivated() {
        super.onActivated()

        lookupThread = AutoCrystalLookup(this)
        lookupThread!!.start()
    }

    override fun onDeactivated() {
        super.onDeactivated()

        crystalsPerSec.resetTime()
        lookupThread!!.interrupt()
        lookupThread = null

        crystalsPlaced = 0
    }

    @EventListener
    private val renderWorldListener = listener<RenderWorldEvent> {
        if (lookupThread != null && lookupThread!!.isAlive && !lookupThread!!.isInterrupted) {

            val placePos = lookupThread!!.targetPlacePos ?: return@listener

            RenderUtil.renderNormalBox(AxisAlignedBB(placePos)
                .offset(-mc.renderManager.viewerPosX, -mc.renderManager.viewerPosY, -mc.renderManager.viewerPosZ))
        }
    }

    @EventListener
    private val tickListener = listener<TickEvent> {

        if (lookupThread != null && !lookupThread!!.isInterrupted) {

            val slot = Nebula.inventoryManager.getSlot(InventoryRegion.HOTBAR, arrayOf(Items.END_CRYSTAL))
            if (slot == -1) {
                return@listener
            }

            hand = if (slot == InventoryUtil.OFFHAND_SLOT) EnumHand.OFF_HAND else EnumHand.MAIN_HAND

            if (hand == EnumHand.MAIN_HAND && Nebula.inventoryManager.serverSlot != slot) {
                when (swap) {
                    Swap.NORMAL -> mc.player.inventory.currentItem = slot
                    Swap.SILENT -> mc.player.connection.sendPacket(CPacketHeldItemChange(slot))
                    Swap.NONE -> return@listener
                }
            }

            if (attack && attackTimer.timePassedMs / 50.0f >= 20.0f - attackSpeed) {
                if (attackCrystal()) {
                    attackTimer.resetTime()
                }
            }

            if (place && placeTimer.timePassedMs / 50.0f >= 20.0f - placeSpeed) {
                if (placeCrystal(lookupThread!!.targetPlacePos)) {
                    placeTimer.resetTime()
                }

                if (swap == Swap.SILENT) {
                    mc.player.connection.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))
                }
            }
        }
    }

    @EventListener
    private val packetSendListener = listener<PacketSendEvent> {

    }

    @EventListener
    private val packetReceiveListener = listener<PacketReceiveEvent>(priority = Int.MAX_VALUE) {
        when (it.packet) {
            is SPacketExplosion -> {
                val packet = it.packet

                for (entity in ArrayList(mc.world.loadedEntityList)) {
                    if (entity !is EntityEnderCrystal) {
                        continue
                    }

                    val dist = entity.getDistanceSq(packet.x, packet.y, packet.z)
                    if (dist <= packet.strength * packet.strength) {
                        if (entity.entityId == lookupThread!!.targetCrystal) {
                            lookupThread!!.crystalInvalidated()
                        }

                        mc.addScheduledTask {
                            mc.world.removeEntity(entity)
                            mc.world.removeEntityDangerously(entity)
                        }
                    }
                }
            }

            is SPacketSoundEffect -> {
                val packet = it.packet
                if (packet.sound != SoundEvents.ENTITY_GENERIC_EXPLODE) {
                    return@listener
                }

                for (entity in ArrayList(mc.world.loadedEntityList)) {
                    if (entity !is EntityEnderCrystal) {
                        continue
                    }

                    val dist = entity.getDistanceSq(packet.x, packet.y, packet.z)
                    if (dist <= 36.0) { // 6 ^ 2 - 6 being the crystal explosion strength
                        if (entity.entityId == lookupThread!!.targetCrystal) {
                            lookupThread!!.crystalInvalidated()
                        }

                        mc.addScheduledTask {
                            mc.world.removeEntity(entity)
                            mc.world.removeEntityDangerously(entity)
                        }
                    }
                }
            }

            is SPacketDestroyEntities -> {
                val packet = it.packet

                for (id in packet.entityIDs) {
                    val entity = mc.world.getEntityByID(id) ?: continue
                    if (entity is EntityEnderCrystal && entity.entityId == lookupThread!!.targetCrystal) {
                        lookupThread!!.crystalInvalidated()
                    }

                    mc.addScheduledTask {
                        mc.world.removeEntity(entity)
                        mc.world.removeEntityDangerously(entity)
                    }
                }
            }

            is SPacketSpawnObject -> {
                val packet = it.packet

                if (packet.type == 51) { // 51 = end crystal, we only care about end crystal spawns
                    val pos = BlockPos(packet.x, packet.y - 1.0, packet.z)
                    if (pos == lookupThread!!.targetPlacePos) {
                        ++crystalsPlaced

                        if (await) {
                            crystalSpawned = true
                            lookupThread!!.offerCrystal(packet.entityID)

                            if (!inhibit && attack && attackCrystal()) {
                                attackTimer.resetTime()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun placeCrystal(blockPos: BlockPos?): Boolean {
        if (blockPos == null) {
            return false
        }

        var facing = EnumFacing.UP
        var hitVec = Vec3d.ZERO

        if (strictDirection) {

            if (blockPos.y > mc.player.entityBoundingBox.minY + mc.player.getEyeHeight()) {
                val result = mc.world.rayTraceBlocks(
                    mc.player.getPositionEyes(1.0f),
                    Vec3d(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5))

                if (result != null) {

                    if (result.sideHit != null) {
                        facing = result.sideHit
                    }

                    if (result.hitVec != null && !result.hitVec.equals(Vec3d.ZERO)) {
                        hitVec = result.hitVec
                    }
                }
            }
        }

        if (rotate != Rotate.NONE) {
            val rotation = AngleUtil.toBlock(blockPos, facing)
            if (rotation != null) {
                Nebula.rotationManager.rotation = rotation
            }
        }

        mc.player.connection.sendPacket(CPacketPlayerTryUseItemOnBlock(
            blockPos, facing, hand, hitVec.x.toFloat(), hitVec.y.toFloat(), hitVec.z.toFloat()))
        mc.player.swingArm(hand)

        return true
    }

    private fun attackCrystal(): Boolean {
        if (lookupThread!!.isInterrupted || lookupThread!!.targetCrystal == -1) {
            return false
        }

        if (rotate != Rotate.NONE) {
            val pos = lookupThread!!.targetPlacePos
            if (pos != null) {
                val rotation = AngleUtil.toVec(Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5))
                if (rotation != null) {
                    Nebula.rotationManager.rotation = rotation
                }
            }
        }

        val packet = CPacketUseEntity()
        packet.action = CPacketUseEntity.Action.ATTACK
        packet.entityId = lookupThread!!.targetCrystal

        mc.player.connection.sendPacket(packet)
        mc.player.resetCooldown()

        mc.player.swingArm(hand)

        return true
    }

    enum class Timing {
        VANILLA, SEQUENTIAL
    }

    enum class Rotate {
        NONE, NORMAL, LIMIT
    }

    enum class Protocol {
        NATIVE, UPDATED
    }

    enum class Swap {
        NONE, NORMAL, SILENT
    }
}

class AutoCrystalLookup(val autoCrystal: AutoCrystal) : Thread(), Globals {
    var targetPlayer: EntityPlayer? = null
    var targetCrystal = -1

    var targetPlacePos: BlockPos? = null
    var placeDamage = 0.0f

    override fun run() {
        while (!isInterrupted) {
            if (nullCheck()) {
                return
            }

            super.run()

            try {
                if (targetPlayer != null && Nebula.friendManager.isFriend(targetPlayer!!)) {
                    targetPlayer = null
                    targetPlacePos = null
                    return
                }

                findPlacePosition()



                if (!autoCrystal.await) {
                    findAttackCrystal()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun crystalInvalidated() {
        targetCrystal = -1
    }

    fun offerCrystal(id: Int) {
        targetCrystal = id
    }

    private fun findAttackCrystal() {
        if (targetPlayer == null) {
            return
        }

        var crystal: EntityEnderCrystal? = null
        var dmg = autoCrystal.minDamage + 0.5f

        ArrayList(mc.world.loadedEntityList).forEach {
            if (it == null || it.isDead || it !is EntityEnderCrystal) {
                return@forEach
            }

            if (it.ticksExisted < autoCrystal.ticksExisted) {
                return@forEach
            }

            val dist = mc.player.getDistance(it)
            if (dist > autoCrystal.attackRange) {
                return@forEach
            }

            // calc damages

            crystal = it
        }

        if (crystal != null) {
            targetCrystal = crystal!!.entityId
        }
    }

    private fun findPlacePosition() {
        val origin = BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)

        var dmg = autoCrystal.minDamage + 0.5f
        var blockPos: BlockPos? = null

        for (pos in origin.getSphere(autoCrystal.placeRange.toInt())) {
            if (!pos.canPlaceCrystal(autoCrystal.protocol == AutoCrystal.Protocol.UPDATED)) {
                continue
            }

            val vec = Vec3d(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5)

            val selfDmg = damage(mc.player, vec)
            if (selfDmg > mc.player.health || selfDmg >= autoCrystal.maxLocal) {
                continue
            }

            var targetDmg = targetPlayer?.let { damage(it, vec) } ?: 0.0f

            for (entityPlayer in mc.world.playerEntities) {
                if (entityPlayer == mc.player) {
                    continue
                }

                // TODO: make sure we dont pop our friends?
                if (Nebula.friendManager.isFriend(entityPlayer)) {
                    continue
                }

                val playerDmg = damage(entityPlayer, vec)
                if (selfDmg > playerDmg) {
                    continue
                }

                if (targetDmg < playerDmg) {
                    targetDmg = playerDmg
                    targetPlayer = entityPlayer
                }
            }

            if (targetDmg > dmg && (autoCrystal.safety && targetDmg > selfDmg)) {
                dmg = targetDmg
                blockPos = pos
            }
        }

        if (blockPos != null) {
            targetPlacePos = blockPos
            placeDamage = dmg
        }

        else {
            targetPlacePos = null
        }
    }

    private fun damage(entity: EntityLivingBase, vec: Vec3d): Float {
        return ExplosionUtil.calcExplosion(vec, entity, 6.0f) * autoCrystal.lethalMultiplier
    }
}