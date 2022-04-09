package cope.nebula.client.feature.module.combat;

import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.internal.timing.Stopwatch;
import cope.nebula.util.internal.timing.TimeFormat;
import cope.nebula.util.world.BlockUtil;
import cope.nebula.util.world.entity.CrystalUtil;
import cope.nebula.util.world.entity.player.inventory.InventorySpace;
import cope.nebula.util.world.entity.player.inventory.InventoryUtil;
import cope.nebula.util.world.entity.player.inventory.SwapType;
import cope.nebula.util.world.entity.player.rotation.AngleUtil;
import cope.nebula.util.world.entity.player.rotation.Bone;
import cope.nebula.util.world.entity.player.rotation.Rotation;
import cope.nebula.util.world.entity.player.rotation.RotationType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FeetTrap extends Module {
    public FeetTrap() {
        super("FeetTrap", ModuleCategory.COMBAT, "Traps your feet with obsidian");
    }

    public static final Value<Boolean> attack = new Value<>("Attack", true);
    public static final Value<Boolean> extend = new Value<>("Extend", true);

    public static final Value<Integer> delay = new Value<>("Delay", 1, 0, 5);
    public static final Value<Integer> blocks = new Value<>("Blocks", 3, 1, 5);

    public static final Value<SwapType> swap = new Value<>("Swap", SwapType.CLIENT);
    public static final Value<RotationType> rotate = new Value<>("Rotate", RotationType.SERVER);

    public static final Value<Boolean> groundDisable = new Value<>("GroundDisable", true);

    private final Queue<BlockPos> queue = new ConcurrentLinkedQueue<>();
    private final Set<BlockPos> blockCache = new HashSet<>();
    private final Stopwatch stopwatch = new Stopwatch();

    private int oldSlot = -1;
    private EnumHand hand = EnumHand.MAIN_HAND;

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.INCOMING)) {
            if (event.getPacket() instanceof SPacketBlockChange) {
                SPacketBlockChange packet = event.getPacket();
                if (blockCache.contains(packet.getBlockPosition()) && packet.getBlockState().getMaterial().isReplaceable()) {
                    placeBlock(packet.getBlockPosition());
                }
            }
        }
    }

    @Override
    public void onTick() {
        int slot = InventoryUtil.getSlot(InventorySpace.HOTBAR,
                (stack) -> !stack.isEmpty() &&
                        InventoryUtil.isBlock(stack) &&
                        ((ItemBlock) stack.getItem()).getBlock().equals(Blocks.OBSIDIAN));

        if (slot == -1) {
            disable();
            return;
        }

        if (groundDisable.getValue() && !mc.player.onGround) {
            disable();
            return;
        }

        hand = slot == InventoryUtil.OFFHAND_SLOT ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;

        if (queue.isEmpty()) {
            Set<BlockPos> positions = getFeetTrapPositions();
            if (!positions.isEmpty()) {
                queue.addAll(positions);

                blockCache.clear();
                blockCache.addAll(positions);

                return;
            }

            swapBack();
            return;
        }

        if (stopwatch.hasElapsed(delay.getValue(), true, TimeFormat.TICKS)) {
            if (!InventoryUtil.isHolding(Blocks.OBSIDIAN)) {
                oldSlot = mc.player.inventory.currentItem;
                getNebula().getHotbarManager().sendSlotChange(slot, swap.getValue());
            }

            for (int i = 0; i < blocks.getValue(); ++i) {
                BlockPos pos = queue.poll();
                if (pos == null) {
                    break;
                }

                placeBlock(pos);
            }
        }
    }

    private void swapBack() {
        if (oldSlot != -1) {
            getNebula().getHotbarManager().sendSlotChange(oldSlot, swap.getValue());
            oldSlot = -1;
        }
    }

    private void placeBlock(BlockPos pos) {
        if (attack.getValue()) {
            for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {

                // if its an end crystal and it is not dead
                if (entity instanceof EntityEnderCrystal && !entity.isDead) {
                    // if we are within 4 blocks of the crystal
                    double range = 16.0; // 4.0
                    if (!mc.player.canEntityBeSeen(entity)) {
                        range = 12.25; // 3.5
                    }

                    if (mc.player.getDistanceSq(entity) < range) {
                        Rotation rotation = AngleUtil.toEntity(entity, Bone.HEAD);
                        if (rotation.isValid()) {
                            rotation = rotation.setType(rotate.getValue());
                            getNebula().getRotationManager().setRotation(rotation);
                        }

                        CrystalUtil.explode(entity.getEntityId(), EnumHand.MAIN_HAND, true);
                    }
                }
            }
        }

        getNebula().getInteractionManager().placeBlock(pos, hand, rotate.getValue(), true,true);
    }

    private Set<BlockPos> getFeetTrapPositions() {
        BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
        Set<BlockPos> positions = new HashSet<>();

        for (EnumFacing facing : EnumFacing.values()) {
            if (facing.equals(EnumFacing.UP)) {
                continue;
            }

            BlockPos neighbor = pos.offset(facing);

            // if there is a player there or whatever
            if (BlockUtil.hasIntersectingBoundingBoxes(neighbor)) {
                for (EnumFacing dir : EnumFacing.values()) {
                    if (dir.equals(EnumFacing.UP)) {
                        continue;
                    }

                    BlockPos extend = neighbor.offset(dir);
                    if (BlockUtil.isReplaceable(neighbor) && !BlockUtil.hasIntersectingBoundingBoxes(neighbor)) {
                        positions.add(extend);
                    }
                }
            } else {
                if (BlockUtil.isReplaceable(neighbor)) {
                    positions.add(neighbor);
                }
            }
        }

        return positions;
    }
}
