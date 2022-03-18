package cope.nebula.client.feature.module.combat;

import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.internal.timing.Stopwatch;
import cope.nebula.util.internal.timing.TimeFormat;
import cope.nebula.util.world.BlockUtil;
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
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FeetTrap extends Module {
    public FeetTrap() {
        super("FeetTrap", ModuleCategory.COMBAT, "Traps your feet with obsidian");
    }

    public static final Value<Double> speed = new Value<>("Speed", 20.0, 1.0, 20.0);
    public static final Value<Boolean> floor = new Value<>("Floor", true);
    public static final Value<Boolean> attack = new Value<>("Attack", true);
    public static final Value<SwapType> swap = new Value<>("Swap", SwapType.CLIENT);
    public static final Value<Boolean> center = new Value<>("Center", false);

    private final Queue<BlockPos> queuedPositions = new ConcurrentLinkedQueue<>();
    private final Set<BlockPos> cached = new HashSet<>();
    private final Stopwatch stopwatch = new Stopwatch();

    private EnumHand hand = EnumHand.MAIN_HAND;
    private int oldSlot = -1;

    @Override
    protected void onDeactivated() {
        queuedPositions.clear();
        cached.clear();

        swapBack();
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.INCOMING) && event.getPacket() instanceof SPacketBlockChange) {
            SPacketBlockChange packet = event.getPacket();

            if (packet.getBlockState().getMaterial().isReplaceable() && cached.contains(packet.getBlockPosition())) {
                queuedPositions.remove(packet.getBlockPosition());
                place(packet.getBlockPosition());
            }
        }
    }

    @Override
    public void onTick() {
        if (!mc.player.onGround) {
            disable();
            return;
        }

        if (queuedPositions.isEmpty()) {
            queuedPositions.addAll(updateCachedPositions());
            if (queuedPositions.isEmpty()) {
                swapBack();
            }
        } else {
            if (stopwatch.getTime(TimeFormat.MILLISECONDS) / 50.0f >= 20.0f - speed.getValue()) {
                BlockPos pos = queuedPositions.poll();
                if (pos == null) {
                    return;
                }

                if (!InventoryUtil.isBlock(InventoryUtil.getHeld(EnumHand.MAIN_HAND)) ||
                        !InventoryUtil.isBlock(InventoryUtil.getHeld(EnumHand.OFF_HAND))) {

                    int slot = InventoryUtil.getSlot(InventorySpace.HOTBAR,
                            (stack) -> InventoryUtil.isBlock(stack) && ((ItemBlock) stack.getItem()).getBlock().equals(Blocks.OBSIDIAN));

                    if (slot == -1) {
                        return;
                    }

                    hand = slot == InventoryUtil.OFFHAND_SLOT ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                    if (hand.equals(EnumHand.MAIN_HAND)) {
                        oldSlot = mc.player.inventory.currentItem;
                        getNebula().getHotbarManager().sendSlotChange(slot, swap.getValue());
                    }

                    mc.player.setActiveHand(hand);
                }

                stopwatch.resetTime();

                if (center.getValue()) {
                    double x = Math.floor(mc.player.posX) + 0.5;
                    double z = Math.floor(mc.player.posZ) + 0.5;

                    double diffX = Math.abs(mc.player.posX - x);
                    double diffZ = Math.abs(mc.player.posZ - z);

                    if (diffX < 0.7 || diffX > 0.3 || diffZ < 0.7 || diffZ > 0.3) {
                        mc.player.setPosition(x, mc.player.posY, z);
                        mc.player.connection.sendPacket(new Position(x, mc.player.getEntityBoundingBox().minY, z, mc.player.onGround));

                        // recalculate
                        cached.clear();
                        updateCachedPositions();

                        return;
                    }
                }

                place(pos);
            }
        }
    }

    private void place(BlockPos pos) {
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityEnderCrystal &&
                    !entity.isDead &&
                    entity.getEntityBoundingBox().intersects(new AxisAlignedBB(pos))) {

                // rotate to crystal
                Rotation rotation = AngleUtil.toEntity(entity, Bone.HEAD).setType(RotationType.SERVER);
                if (rotation.isValid()) {
                    getNebula().getRotationManager().setRotation(rotation);
                }

                mc.player.connection.sendPacket(new CPacketUseEntity(entity));
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
            }
        }

        getNebula().getInteractionManager().placeBlock(pos, hand, RotationType.SERVER, true);
    }

    private Set<BlockPos> updateCachedPositions() {
        BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);

        Set<BlockPos> additions = new HashSet<>();
        Set<BlockPos> needsExtensions = new HashSet<>();

        for (EnumFacing direction : EnumFacing.values()) {
            if (direction.equals(EnumFacing.UP) || (!floor.getValue() && direction.equals(EnumFacing.DOWN))) {
                continue;
            }

            BlockPos neighbor = pos.offset(direction);

            if (BlockUtil.hasIntersectingBoundingBoxes(neighbor)) {
                needsExtensions.add(neighbor);
            } else {
                if (!BlockUtil.isReplaceable(neighbor)) {
                    continue;
                }

                additions.add(neighbor);
            }
        }

        if (!needsExtensions.isEmpty()) {
            for (BlockPos extension : needsExtensions) {
                for (EnumFacing direction : EnumFacing.values()) {
                    if (direction.equals(EnumFacing.UP) || (!floor.getValue() && direction.equals(EnumFacing.DOWN))) {
                        continue;
                    }

                    BlockPos neighbor = extension.offset(direction);

                    if (BlockUtil.isReplaceable(neighbor) && !BlockUtil.hasIntersectingBoundingBoxes(neighbor)) {
                        additions.add(neighbor);
                    }
                }
            }
        }

        return additions;
    }

    private void swapBack() {
        if (oldSlot != -1) {
            getNebula().getHotbarManager().sendSlotChange(oldSlot, swap.getValue());
        }

        oldSlot = -1;
        hand = EnumHand.MAIN_HAND;
    }
}
