package wtf.nebula.util.world

import net.minecraft.block.Block
import net.minecraft.init.Blocks

inline val SNEAK_BLOCKS: List<Block>
    get() = listOf(
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.ENDER_CHEST,
        Blocks.CRAFTING_TABLE,
        Blocks.FURNACE,
        Blocks.LIT_FURNACE,
        Blocks.BEACON,
        Blocks.ANVIL,
        Blocks.DISPENSER,
        Blocks.DROPPER,
        Blocks.DAYLIGHT_DETECTOR,
        Blocks.DAYLIGHT_DETECTOR_INVERTED,
        Blocks.HOPPER
    )

object BlockUtil {
}