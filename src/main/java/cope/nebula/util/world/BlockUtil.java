package cope.nebula.util.world;

import com.google.common.collect.Lists;
import cope.nebula.util.Globals;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Common utilities for blocks in the world
 *
 * @author aesthetical
 * @since 3/8/22
 */
public class BlockUtil implements Globals {
    /**
     * A list of all blocks that are resistant to explosions
     */
    public static final List<Block> EXPLOSION_RESISTANT_BLOCKS = Lists.newArrayList(
            Blocks.OBSIDIAN,
            Blocks.BEDROCK,
            Blocks.ANVIL,
            Blocks.BARRIER,
            Blocks.COMMAND_BLOCK,
            Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK
    );

    /**
     * Checks if a block will be removed upon an explosion
     * @param pos The position of the block
     * @return if this block will be removed if in the radius of an explosion
     */
    public static boolean canExplode(BlockPos pos) {
        return !EXPLOSION_RESISTANT_BLOCKS.contains(mc.world.getBlockState(pos).getBlock());
    }

    /**
     * If we can break this block in survival
     * @param pos The position to check
     * @return If we can break this block
     */
    public static boolean canBreak(BlockPos pos) {
        if (mc.player.isCreative()) {
            return true;
        }

        return mc.world.getBlockState(pos).getBlockHardness(mc.world, pos) != -1;
    }

    /**
     * Checks if we can replace this block
     * @param pos The position of the block
     * @return if we can place a block there/enter this space
     */
    public static boolean isReplaceable(BlockPos pos) {
        return mc.world.getBlockState(pos).getMaterial().isReplaceable();
    }

    /**
     * Gets a sphere around an origin point
     * @param origin The point of origin
     * @param radius The radius around the origin point
     * @return a list containing all blocks in a sphere around block position
     */
    public static List<BlockPos> sphere(BlockPos origin, int radius) {
        List<BlockPos> positions = new ArrayList<>();

        for (int x = -radius; x <= radius; ++x) {
            for (int y = -radius; y <= radius; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    BlockPos pos = origin.add(x, y, z);

                    if (mc.player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5) < radius * radius) {
                        positions.add(pos);
                    }
                }
            }
        }

        return positions;
    }
}
