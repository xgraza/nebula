package cope.nebula.client.feature.module.world;

import com.google.common.collect.Lists;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeModContainer;

import java.util.ArrayList;
import java.util.List;

public class Wallhack extends Module {
    public static Wallhack INSTANCE;

    public static final List<Block> WHITELIST = new ArrayList<>();
    public static final List<Block> DEFAULT_BLOCKS = Lists.newArrayList(

            // Normal blocks we'd maybe like to see
            Blocks.OBSIDIAN,
            Blocks.BEDROCK,
            Blocks.PORTAL,
            Blocks.END_PORTAL,
            Blocks.END_PORTAL_FRAME,
            Blocks.COMMAND_BLOCK,
            Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK,
            Blocks.MOB_SPAWNER,
            Blocks.BEACON,
            Blocks.BED,

            // Ores
            Blocks.DIAMOND_ORE,
            Blocks.COAL_ORE,
            Blocks.EMERALD_ORE,
            Blocks.GOLD_ORE,
            Blocks.IRON_ORE,
            Blocks.LAPIS_ORE,
            Blocks.LIT_REDSTONE_ORE,
            Blocks.QUARTZ_ORE,
            Blocks.REDSTONE_ORE,

            // Blocks that can be created from ores
            Blocks.DIAMOND_BLOCK,
            Blocks.COAL_BLOCK,
            Blocks.EMERALD_BLOCK,
            Blocks.GOLD_BLOCK,
            Blocks.IRON_BLOCK,
            Blocks.LAPIS_BLOCK,
            Blocks.REDSTONE_BLOCK
    );

    public Wallhack() {
        super("Wallhack", ModuleCategory.RENDER, "Lets you see ores through walls");
        INSTANCE = this;

        // TODO: this will be loaded from the config file
        WHITELIST.addAll(DEFAULT_BLOCKS);
    }

    public static final Value<Integer> opacity = new Value<>("Opacity", 120, 0, 255);
    public static final Value<Boolean> softReload = new Value<>("SoftReload", true);

    private boolean forgeLightPipelineEnabled;
    private boolean markReload = false;

    @Override
    protected void onActivated() {
        forgeLightPipelineEnabled = ForgeModContainer.forgeLightPipelineEnabled;
        ForgeModContainer.forgeLightPipelineEnabled = false;

        if (nullCheck()) {
            markReload = true;
        } else {
            reloadChunks();
        }
    }

    @Override
    protected void onDeactivated() {
        ForgeModContainer.forgeLightPipelineEnabled = forgeLightPipelineEnabled;
        if (!nullCheck()) {
            reloadChunks();
            mc.renderChunksMany = false;
        }
    }

    @Override
    public void onTick() {
        if (markReload) {
            markReload = false;
            reloadChunks();
        }
    }

    /**
     * Reloads the chunks around you
     */
    private void reloadChunks() {
        if (softReload.getValue()) {
            mc.renderChunksMany = true;

            Vec3d pos = mc.player.getPositionVector();
            int dist = mc.gameSettings.renderDistanceChunks * 16;

            mc.renderGlobal.markBlockRangeForRenderUpdate(
                    (int) (pos.x) - dist, (int) (pos.y) - dist, (int) (pos.z) - dist,
                    (int) (pos.x) + dist, (int) (pos.y) + dist, (int) (pos.z) + dist);
        } else {
            mc.renderGlobal.loadRenderers();
        }
    }
}
