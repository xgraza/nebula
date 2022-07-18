package wtf.nebula.client.feature.module.world

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.block.state.IBlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.Enchantments
import net.minecraft.item.ItemTool
import wtf.nebula.client.event.world.ClickBlockEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.util.Globals
import kotlin.math.pow

class AutoTool : Module(ModuleCategory.WORLD, "Finds the best tool to use against a block") {

    @EventListener
    private val clickBlockListener = listener<ClickBlockEvent> {
        val state = mc.world.getBlockState(it.blockPos)

        if (state.getBlockHardness(mc.world, it.blockPos) == -1.0f) {
            return@listener
        }

        val slot = getBestToolSlot(state)
        if (slot == -1) {
            return@listener
        }

        if (mc.player.inventory.currentItem != slot) {
            mc.player.inventory.currentItem = slot
        }
    }

    companion object : Globals {
        fun getBestToolSlot(state: IBlockState): Int {
            var bestSlot = -1
            var bestSpeed = 0.0f

            for (i in 0 until 9) {
                val stack = mc.player.inventory.getStackInSlot(i) ?: continue
                if (stack.item is ItemTool || stack.canHarvestBlock(state)) {
                    var destroySpeed = (stack.item as ItemTool).getDestroySpeed(stack, state)

                    val effModifier = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)
                    if (effModifier > 0) {
                        destroySpeed += effModifier.toDouble().pow(2).toFloat() + 1.0f
                    }

                    if (destroySpeed > bestSpeed) {
                        bestSpeed = destroySpeed
                        bestSlot = i
                    }
                }
            }

            return bestSlot
        }
    }
}