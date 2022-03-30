package cope.nebula.client.feature.module.world;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.internal.timing.Stopwatch;
import cope.nebula.util.internal.timing.TimeFormat;
import cope.nebula.util.world.BlockUtil;
import cope.nebula.util.world.entity.player.inventory.InventorySpace;
import cope.nebula.util.world.entity.player.inventory.InventoryUtil;
import cope.nebula.util.world.entity.player.inventory.SwapType;
import cope.nebula.util.world.entity.player.rotation.RotationType;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import static cope.nebula.client.feature.module.world.Timer.setTimerSpeed;

public class Scaffold extends Module {
    public Scaffold() {
        super("Scaffold", ModuleCategory.WORLD, "Places blocks below you");
    }

    public static final Value<Boolean> rotate = new Value<>("Rotate", true);
    public static final Value<Boolean> swing = new Value<>("Swing", true);
    public static final Value<Swap> swap = new Value<>("Swap", Swap.CLIENT);

    public static final Value<Boolean> tower = new Value<>("Tower", true);
    public static final Value<Boolean> stopMotion = new Value<>("StopMotion", false);

    public static final Value<Boolean> timerBoost = new Value<>("TimerBoost", false);
    public static final Value<Boolean> keepY = new Value<>("KeepY", false);

    private final Stopwatch towerStopwatch = new Stopwatch();

    private EnumHand hand = null;
    private int oldSlot = -1;

    private double y;

    @Override
    protected void onActivated() {
        y = mc.player.posY;
    }

    @Override
    protected void onDeactivated() {
        swapBack();
        setTimerSpeed(1.0f);

        y = -1.0;
    }

    @Override
    public void onTick() {
        BlockPos pos = new BlockPos(mc.player.posX, (keepY.getValue() ? y : mc.player.posY) - 1.0, mc.player.posZ);
        if (BlockUtil.isReplaceable(pos)) {
            EnumFacing facing = BlockUtil.getFacing(pos);
            if (facing == null) {
                swapBack();
                return;
            }

            if (!(InventoryUtil.getHeld(EnumHand.MAIN_HAND).getItem() instanceof ItemBlock) ||
                    !(InventoryUtil.getHeld(EnumHand.OFF_HAND).getItem() instanceof ItemBlock)) {

                int slot = InventoryUtil.getSlot(InventorySpace.HOTBAR, (stack) -> {
                    if (!(stack.getItem() instanceof ItemBlock)) {
                        return false;
                    }

                    // TODO blacklist gravity blocks
                    return true;
                });

                if (slot == -1) {
                    swapBack();
                    return;
                }

                hand = slot == InventoryUtil.OFFHAND_SLOT ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                if (hand.equals(EnumHand.MAIN_HAND)) {
                    oldSlot = mc.player.inventory.currentItem;
                    getNebula().getHotbarManager().sendSlotChange(slot, swap.getValue().swapType);
                } else {
                    mc.player.setActiveHand(EnumHand.OFF_HAND);
                }
            }

            if (hand == null) {
                return;
            }

            if (timerBoost.getValue() && mc.player.onGround) {
                setTimerSpeed(1.6f);
            }

            if (stopMotion.getValue()) {
                mc.player.motionX = 0.0;
                mc.player.motionZ = 0.0;

                mc.player.setSprinting(false);
            }

            getNebula().getInteractionManager().placeBlock(pos, hand, rotate.getValue() ? RotationType.SERVER : RotationType.NONE, swing.getValue());

            if (tower.getValue() && mc.gameSettings.keyBindJump.isKeyDown() && facing.equals(EnumFacing.DOWN)) {
                mc.player.motionX *= 0.2;
                mc.player.motionZ *= 0.2;
                mc.player.jump();

                if (towerStopwatch.hasElapsed(1000L, true, TimeFormat.MILLISECONDS)) {
                    mc.player.motionY = -0.28;
                }
            }

            if (!swap.getValue().equals(Swap.KEEP)) {
                swapBack();
            }
        } else {
            setTimerSpeed(1.0f);
        }
    }

    private void swapBack() {
        if (oldSlot != -1) {
            getNebula().getHotbarManager().sendSlotChange(oldSlot, swap.getValue().swapType);
            oldSlot = -1;
        }

        hand = null;
    }

    public enum Swap {
        CLIENT(SwapType.CLIENT),
        SERVER(SwapType.SERVER),
        KEEP(SwapType.CLIENT);

        private final SwapType swapType;

        Swap(SwapType swapType) {
            this.swapType = swapType;
        }
    }
}
