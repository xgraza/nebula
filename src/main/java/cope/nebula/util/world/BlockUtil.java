package cope.nebula.util.world;

import com.google.common.collect.Lists;
import cope.nebula.util.Globals;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
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
            Blocks.COMMAND_BLOCK,
            Blocks.BARRIER,
            Blocks.ENCHANTING_TABLE,
            Blocks.END_PORTAL_FRAME,
            Blocks.BEACON,
            Blocks.ANVIL
    );

    /**
     * Represents blocks we need to send a sneak packet with to place a block on
     */
    public static final List<Block> SNEAK_BLOCKS = Lists.newArrayList(
            // normal tile entities
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.ANVIL,
            Blocks.ENDER_CHEST,
            Blocks.FURNACE,
            Blocks.LIT_FURNACE,
            Blocks.BREWING_STAND,
            Blocks.DISPENSER,
            Blocks.DROPPER,
            Blocks.BED,
            Blocks.BEACON,
            Blocks.CRAFTING_TABLE,
            Blocks.HOPPER,

            // redstone shit
            Blocks.LEVER,
            Blocks.STONE_BUTTON,
            Blocks.WOODEN_BUTTON,
            Blocks.POWERED_REPEATER,
            Blocks.UNPOWERED_REPEATER,
            Blocks.POWERED_COMPARATOR,
            Blocks.UNPOWERED_COMPARATOR,

            // doors
            Blocks.BREWING_STAND,
            Blocks.ACACIA_DOOR,
            Blocks.DARK_OAK_DOOR,
            Blocks.BIRCH_DOOR,
            Blocks.JUNGLE_DOOR,
            Blocks.OAK_DOOR,
            Blocks.SPRUCE_DOOR,

            // command blocks
            Blocks.COMMAND_BLOCK,
            Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK,

            // all shulker types
            Blocks.BLACK_SHULKER_BOX,
            Blocks.WHITE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX,
            Blocks.SILVER_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX,
            Blocks.ORANGE_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.RED_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX
    );

    /**
     * Checks if you need to sneak to place a block there to avoid opening a GUI/interacting
     * @param pos The position
     * @return if we should sneak
     */
    public static boolean shouldSneak(BlockPos pos) {
        return SNEAK_BLOCKS.contains(mc.world.getBlockState(pos).getBlock());
    }

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

    /**
     * Gets a facing offset to place a block on
     * @param pos The block position
     * @return a EnumFacing enum constant or null if none found
     */
    public static EnumFacing getFacing(BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(facing);

            if (!isReplaceable(neighbor)) {
                return facing;
            }
        }

        return null;
    }

    public static boolean hasIntersectingBoundingBoxes(BlockPos pos) {
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity == null || entity instanceof EntityItem) {
                continue;
            }

            if (entity.getEntityBoundingBox().intersects(new AxisAlignedBB(pos))) {
                return true;
            }
        }

        return false;
    }
}
