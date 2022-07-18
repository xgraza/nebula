package wtf.nebula.client.feature.module.world

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.block.material.Material
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.Enchantments
import net.minecraft.init.MobEffects
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.render.RenderWorldEvent
import wtf.nebula.client.event.tick.TickEvent
import wtf.nebula.client.event.world.ClickBlockEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.util.Globals
import wtf.nebula.util.render.RenderUtil
import java.awt.Color
import kotlin.math.ceil
import kotlin.math.pow

class PacketMine : Module(ModuleCategory.WORLD, "Mines blocks with packets") {
    val strict by bool("Strict", true)
    val render by bool("Render", true)

    private var blockPos: BlockPos? = null
    private var facing: EnumFacing? = null

    private val timer = wtf.nebula.util.Timer() // fuck you kotlin
    private var destroySpeed = 0.0

    override fun onDeactivated() {
        super.onDeactivated()

        blockPos = null
        facing = null
        destroySpeed = 0.0

        mc.player.connection.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))
    }

    @EventListener
    private val renderWorldListener = listener<RenderWorldEvent> {
        if (blockPos != null && render) {
            val passed = timer.passedTime(destroySpeed.toLong())

            val color = (if (passed) Color(0, 255, 0, 120) else Color(255, 0, 0, 120)).rgb

            RenderUtil.renderBoxEsp(
                AxisAlignedBB(blockPos!!)
                    .offset(
                        -mc.renderManager.viewerPosX,
                        -mc.renderManager.viewerPosY,
                        -mc.renderManager.viewerPosZ),
                color)
        }
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (blockPos != null) {
            val state = mc.world.getBlockState(blockPos!!)
            if (state.material.isReplaceable || mc.world.isAirBlock(blockPos!!)) {
                blockPos = null
                facing = null

                // reset swapping
                mc.player.connection.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))

                destroySpeed = 0.0
            }
        }
    }

    @EventListener
    private val clickBlockListener = listener<ClickBlockEvent>(priority = Int.MAX_VALUE) {
        val state = mc.world.getBlockState(it.blockPos)
        if (state.getBlockHardness(mc.world, it.blockPos) == -1.0f) {
            return@listener
        }

        // prevent other listeners from overriding
        it.cancel()

        if (blockPos != null) {

            if (blockPos == it.blockPos) {

                if (strict && !timer.passedTime(destroySpeed.toLong())) {
                    return@listener
                }

                mc.player.connection.sendPacket(
                    CPacketPlayerDigging(
                        CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos!!, facing!!
                    )
                )

                return@listener
            }

            mc.player.connection.sendPacket(
                CPacketPlayerDigging(
                    CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, blockPos!!, facing!!
                )
            )

            blockPos = null
            facing = null
        }

        val slot = AutoTool.getBestToolSlot(state)
        var stack = Nebula.inventoryManager.getHeldItemStack()

        if (slot != -1 && Nebula.inventoryManager.serverSlot != slot) {
            mc.player.connection.sendPacket(CPacketHeldItemChange(slot))
            stack = mc.player.inventory.getStackInSlot(slot)
        }

        blockPos = it.blockPos
        facing = it.facing

        destroySpeed = getDestroySpeed(blockPos!!, stack) * 1000.0
        timer.resetTime()

        mc.player.connection.sendPacket(CPacketPlayerDigging(
            CPacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos!!, facing!!))

        if (!strict) {
            mc.player.connection.sendPacket(
                CPacketPlayerDigging(
                    CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos!!, facing!!
                )
            )
        }
    }

    companion object : Globals {
        fun getDestroySpeed(blockPos: BlockPos, stack: ItemStack): Double {
            val state = mc.world.getBlockState(blockPos)
            var speedMultiplier: Float = stack.getDestroySpeed(state)

            if (EnchantmentHelper.getEnchantments(stack).containsKey(Enchantments.EFFICIENCY)) {
                val efficiencyLevel: Int = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)
                speedMultiplier += (efficiencyLevel.toDouble().pow(2.0) + 1).toFloat()
            }

            if (mc.player.isPotionActive(MobEffects.HASTE)) {
                speedMultiplier *= (1 + 0.2 * mc.player.getActivePotionEffect(MobEffects.HASTE)!!.amplifier).toFloat()
            }

            if (mc.player.isPotionActive(MobEffects.MINING_FATIGUE)) {
                val miningFatigueLevel = mc.player.getActivePotionEffect(MobEffects.MINING_FATIGUE)!!.amplifier
                speedMultiplier *= when (miningFatigueLevel) {
                    1 -> 0.3f
                    2 -> 0.09f
                    3 -> 0.0027f
                    else -> 0.00081f
                }
            }

            if (mc.player.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(mc.player)
                || !mc.player.onGround
            ) {
                speedMultiplier /= 5.0.toFloat()
            }

            var damage = speedMultiplier / state.getBlockHardness(mc.world, blockPos)

            damage /= if (stack.canHarvestBlock(state)) {
                30f
            } else {
                100f
            }

            if (damage > 1.0f) {
                return 0.0
            }

            val ticks = ceil((1 / damage).toDouble())
            return ticks / 20.0
        }
    }
}