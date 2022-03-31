package cope.nebula.client.feature.module.world;

import cope.nebula.client.events.ClickBlockEvent;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.internal.timing.Stopwatch;
import cope.nebula.util.renderer.RenderUtil;
import cope.nebula.util.world.BlockUtil;
import cope.nebula.util.world.entity.player.inventory.SwapType;
import cope.nebula.util.world.entity.player.rotation.AngleUtil;
import cope.nebula.util.world.entity.player.rotation.Rotation;
import cope.nebula.util.world.entity.player.rotation.RotationType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class FastBreak extends Module {
    public FastBreak() {
        super("FastBreak", ModuleCategory.WORLD, "Breaks blocks faster");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.PACKET);
    public static final Value<Break> breakMode = new Value<>("Break", Break.AUTOMATIC);
    public static final Value<Boolean> rotate = new Value<>("Rotate", true);
    public static final Value<SwapType> swap = new Value<>("Swap", SwapType.SERVER);

    private final Stopwatch stopwatch = new Stopwatch();

    private BlockPos pos;
    private EnumFacing facing;

    private boolean sent = false;
    private double breakSpeed = 0.0;

    private int swapSlot = -1;
    private int oldSlot = -1;

    @Override
    protected void onDeactivated() {
        pos = null;
        facing = null;

        swapBack();
    }

    @Override
    public void onRender3d() {
        if (pos != null) {
            boolean passed = stopwatch.passedMs((long) (breakSpeed * 1000.0));

            int r = passed ? 0 : 255;
            int g = passed ? 255 : 0;

            int color = new Color(r, g, 0, 120).getRGB();

            AxisAlignedBB box = new AxisAlignedBB(pos).offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

            RenderUtil.renderFilledBox(box, color);
            RenderUtil.renderOutlinedBox(box, 3.5f, color);
        }
    }

    @Override
    public void onTick() {
        if (pos != null) {
            if (BlockUtil.isReplaceable(pos)) {
                pos = null;
                facing = null;

                breakSpeed = 100000000.0;
                sent = false;

                swapBack();

                return;
            }

            if (stopwatch.passedMs((long) (breakSpeed * 1000.0))) {
                if (swapSlot != -1) {
                    oldSlot = mc.player.inventory.currentItem;
                    getNebula().getHotbarManager().sendSlotChange(swapSlot, swap.getValue());
                }

                if (rotate.getValue()) {
                    Rotation rotation = AngleUtil.toBlock(pos, facing);
                    if (rotation.isValid()) {
                        getNebula().getRotationManager().setRotation(rotation.setType(RotationType.SERVER));
                    }
                }

                if (mode.getValue().equals(Mode.PACKET) && breakMode.getValue().equals(Break.STRICT)) {
                    if (!sent) {
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, pos, facing));
                        sent = true;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onClickBlock(ClickBlockEvent event) {

        // check if this block is breakable
        // for example, we cannot break bedrock or command blocks in survival
        IBlockState state = mc.world.getBlockState(event.getPos());
        if (state.getBlockHardness(mc.world, event.getPos()) == -1) {
            return;
        }

        event.setCanceled(true);

        if (pos != null) {
            if (pos.equals(event.getPos())) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, pos, facing));
                return;
            } else {
                // tell the server we're gonna stop breaking this block
                mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.ABORT_DESTROY_BLOCK, pos, facing));

                pos = null;
                facing = null;
            }
        }

        pos = event.getPos();
        facing = event.getFacing();

        if (rotate.getValue()) {
            Rotation rotation = AngleUtil.toBlock(pos, facing);
            if (rotation.isValid()) {
                getNebula().getRotationManager().setRotation(rotation.setType(RotationType.SERVER));
            }
        }

        if (!swap.getValue().equals(SwapType.NONE)) {
            swapSlot = AutoTool.getBestTool(event.getPos());
        }

        breakSpeed = getTimeToBreakBlock(pos, swapSlot);

        if (mode.getValue().equals(Mode.INSTANT)) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, pos, facing));
            mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, pos, facing));
            mc.world.setBlockToAir(pos);
        } else {
            if (swapSlot != -1 && breakMode.getValue().equals(Break.STRICT)) {
                oldSlot = mc.player.inventory.currentItem;
                getNebula().getHotbarManager().sendSlotChange(swapSlot, swap.getValue());
            }

            mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, pos, facing));

            if (breakMode.getValue().equals(Break.AUTOMATIC)) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, pos, facing));
            }

            stopwatch.resetTime();
        }
    }

    /**
     * Gets the time to break a block in seconds
     *
     * Similar to mc.player.getDigSpeed, but it calculates in seconds
     *
     * Calculations based off of https://minecraft.fandom.com/wiki/Breaking
     *
     * @param pos the block position
     * @param slot the slot of the item to switch to
     * @return the time in seconds it'll take to break a block
     */
    public double getTimeToBreakBlock(BlockPos pos, int slot) {
        IBlockState state = mc.world.getBlockState(pos);

        if (slot == -1) {
            slot = getNebula().getHotbarManager().getServerSlot();
        }

        ItemStack held = mc.player.inventory.getStackInSlot(slot);

        float speedMultiplier = held.getDestroySpeed(state);

        if (EnchantmentHelper.getEnchantments(held).containsKey(Enchantments.EFFICIENCY)) {
            int efficiencyLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, held);
            speedMultiplier += Math.pow(efficiencyLevel, 2) + 1;
        }

        if (mc.player.isPotionActive(MobEffects.HASTE)) {
            speedMultiplier *= 1 + (0.2 * mc.player.getActivePotionEffect(MobEffects.HASTE).getAmplifier());
        }

        if (mc.player.isPotionActive(MobEffects.MINING_FATIGUE)) {
            int miningFatigueLevel = mc.player.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier();
            switch (miningFatigueLevel) {
                case 1:
                    speedMultiplier *= 0.3;
                    break;
                case 2:
                    speedMultiplier *= 0.09;
                    break;
                case 3:
                    speedMultiplier *= 0.0027;
                    break;
                default:
                    speedMultiplier *= 0.00081;
                    break;
            }
        }

        if ((mc.player.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(mc.player))
                || !mc.player.onGround) {

            speedMultiplier /= 5.0;
        }

        float damage = speedMultiplier / state.getBlockHardness(mc.world, pos);

        if (held.canHarvestBlock(state)) {
            damage /= 30;
        } else {
            damage /= 100;
        }

        if (damage > 1.0f) {
            return 0.0f;
        }

        double ticks = Math.ceil(1 / damage);
        return ticks / 20.0;
    }

    private void swapBack() {
        if (oldSlot != -1) {
            getNebula().getHotbarManager().sendSlotChange(oldSlot, swap.getValue());

            oldSlot = -1;
            swapSlot = -1;
        }
    }

    public enum Mode {
        PACKET, INSTANT
    }

    public enum Break {
        AUTOMATIC, STRICT
    }
}
