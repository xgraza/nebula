package cope.nebula.client.feature.module.movement;

import cope.nebula.asm.mixins.network.packet.c2s.ICPacketPlayer;
import cope.nebula.client.events.KeyDownEvent;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class NoSlowDown extends Module {
    public NoSlowDown() {
        super("NoSlowDown", ModuleCategory.MOVEMENT, "Stops things from slowing you down");
    }

    public static final Value<Boolean> items = new Value<>("Items", true);
    public static final Value<Bypass> bypass = new Value<>(items, "Bypass", Bypass.NONE);

    public static final Value<Boolean> inventory = new Value<>("Inventory", true);
    public static final Value<Boolean> arrowRotate = new Value<>(inventory, "ArrowRotate", true);
    public static final Value<Boolean> airStrict = new Value<>(inventory, "AirStrict", false);

    private boolean sneakState = false;
    private boolean windowClick = false;

    @Override
    protected void onDeactivated() {
        if (sneakState) {
            applySneakBypass();
            sneakState = false;
        }
    }

    @Override
    public void onTick() {
        if (shouldItemNoSlow() && !windowClick && !sneakState) {
            applySneakBypass();
        }

        if (inventory.getValue() && isInValidGui()) {
            KeyBinding[] binds = { mc.gameSettings.keyBindRight, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack };
            for (KeyBinding bind : binds) {
                KeyBinding.setKeyBindState(bind.getKeyCode(), Keyboard.isKeyDown(bind.getKeyCode()));
            }

            if (arrowRotate.getValue()) {
                if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                    mc.player.rotationYaw -= 4.0f;
                } else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                    mc.player.rotationYaw += 4.0f;
                } else if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                    mc.player.rotationPitch -= 4.0f;
                } else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                    mc.player.rotationPitch += 4.0f;
                }

                if (mc.player.rotationPitch > 90.0f) {
                    mc.player.rotationPitch = 90.0f;
                }

                if (mc.player.rotationPitch < -90.0f) {
                    mc.player.rotationPitch = -90.0f;
                }
            }
        }
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event) {
        if (shouldItemNoSlow()) {
            event.getMovementInput().moveForward *= 5.0f;
            event.getMovementInput().moveStrafe *= 5.0f;
        }
    }

    @SubscribeEvent
    public void onKeyDown(KeyDownEvent event) {
        if (inventory.getValue() && isInValidGui()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.OUTGOING)) {
            if (event.getPacket() instanceof CPacketEntityAction) {
                CPacketEntityAction packet = event.getPacket();

                switch (packet.getAction()) {
                    case START_SNEAKING:
                        sneakState = true;
                        break;

                    case STOP_SNEAKING:
                        sneakState = false;
                        break;
                }
            } else if (event.getPacket() instanceof CPacketPlayer) {
                if (((ICPacketPlayer) event.getPacket()).isMoving() && shouldItemNoSlow()) {
                    if (bypass.getValue().equals(Bypass.NCP)) {
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.ABORT_DESTROY_BLOCK, BlockPos.ORIGIN, EnumFacing.DOWN));
                    } else if (bypass.getValue().equals(Bypass.SWAP)) {
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                    }
                }
            } else if (event.getPacket() instanceof CPacketClickWindow) {
                if (!sneakState && inventory.getValue() && airStrict.getValue()) {
                    windowClick = true;

                    applySneakBypass();

                    // with NCP-Updated, we cannot use items while in inventories, so...
                    if (mc.player.isHandActive()) {
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    }

                    // we also cannot be sprinting, because that'll also flag NCP-Updated
                    if (mc.player.isSprinting()) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                    }
                }
            }
        } else if (event.getDirection().equals(Direction.INCOMING)) {
            if (event.getPacket() instanceof SPacketConfirmTransaction) {
                if (windowClick && inventory.getValue() && airStrict.getValue() && sneakState) {
                    applySneakBypass();
                }
            }
        }
    }

    private void applySneakBypass() {
        if (sneakState) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            sneakState = false;
        } else {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            sneakState = true;
        }
    }

    private boolean shouldItemNoSlow() {
        return items.getValue() && mc.player.isHandActive();
    }

    private boolean isInValidGui() {
        return mc.currentScreen != null &&
                !(mc.currentScreen instanceof GuiChat) &&
                !(mc.currentScreen instanceof GuiRepair) &&
                !(mc.currentScreen instanceof GuiScreenBook);
    }

    public enum Bypass {
        /**
         * Does not use a bypass, just modifies movement
         */
        NONE,

        /**
         * Sends a CPacketPlayerDigging packet while using the item
         */
        NCP,

        /**
         * Sends a sneak packet during the duration of you holding down the eat button
         *
         * Only works while strafing and on NCP-Updated, do not attempt on regular NCP
         */
        SNEAK,

        /**
         * Sends a CPacketHeldItemChange to completely bypass the NoSlow check on NCP-Updated
         *
         * This also works on AAC for 1.8
         *
         * Does not work on 2b2t anymore, but still useful on servers where it still works
         */
        SWAP
    }
}
