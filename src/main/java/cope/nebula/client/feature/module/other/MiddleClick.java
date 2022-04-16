package cope.nebula.client.feature.module.other;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.nebula.asm.duck.IEntityPlayer;
import cope.nebula.client.events.MiddleClickEvent;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.world.entity.player.inventory.InventorySpace;
import cope.nebula.util.world.entity.player.inventory.InventoryUtil;
import cope.nebula.util.world.entity.player.inventory.SwapType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MiddleClick extends Module {
    public MiddleClick() {
        super("MiddleClick", ModuleCategory.OTHER, "Does things upon a middle click");
    }

    public static final Value<Boolean> friend = new Value<>("Friend", true);
    public static final Value<Boolean> pearl = new Value<>("Pearl", true);

    @SubscribeEvent
    public void onMiddleClick(MiddleClickEvent event) {
        RayTraceResult result = mc.objectMouseOver;
        if (result == null) {
            return;
        }

        switch (result.typeOfHit) {
            case MISS: {
                if (!pearl.getValue()) {
                    return;
                }

                int slot = InventoryUtil.getSlot(InventorySpace.HOTBAR,
                        (stack) -> !stack.isEmpty() && stack.getItem().equals(Items.ENDER_PEARL));

                if (slot != -1) {
                    event.setCanceled(true);

                    EnumHand hand = slot == InventoryUtil.OFFHAND_SLOT ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                    int oldSlot = -1;

                    if (hand.equals(EnumHand.MAIN_HAND)) {
                        oldSlot = mc.player.inventory.currentItem;
                        getNebula().getHotbarManager().sendSlotChange(slot, SwapType.CLIENT);
                    }

                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(hand));

                    if (oldSlot != -1) {
                        getNebula().getHotbarManager().sendSlotChange(oldSlot, SwapType.CLIENT);
                    }
                }

                break;
            }

            case ENTITY: {
                if (result.entityHit instanceof EntityPlayer && friend.getValue()) {
                    sendChatMessage(
                            (((IEntityPlayer) result.entityHit).friend() ? "Added" : "Removed")
                                    + " " + ChatFormatting.DARK_PURPLE + result.entityHit.getName() + ChatFormatting.RESET + " as a friend.");
                }
                break;
            }
        }
    }
}
