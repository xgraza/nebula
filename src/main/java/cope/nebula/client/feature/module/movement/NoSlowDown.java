package cope.nebula.client.feature.module.movement;

import cope.nebula.client.events.KeyDownEvent;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class NoSlowDown extends Module {
    public NoSlowDown() {
        super("NoSlowDown", ModuleCategory.MOVEMENT, "Stops things from slowing you down");
    }

    public static final Value<Boolean> items = new Value<>("Items", true);
    public static final Value<Boolean> itemStrict = new Value<>(items, "Strict", false);

    public static final Value<Boolean> inventory = new Value<>("Inventory", true);
    public static final Value<Boolean> strictInventory = new Value<>(inventory, "Strict", false);
    public static final Value<Boolean> rotate = new Value<>(inventory, "Rotate", true);

    private boolean windowClick = false;
    private boolean sneakState = false;

    @Override
    protected void onDeactivated() {
        if (sneakState || windowClick) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
            sneakState = false;
        }

        windowClick = false;
    }

    @Override
    public void onTick() {
        if (inventory.getValue() && mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) {
            mc.currentScreen.allowUserInput = true;

            // EntityRenderer#updateCameraAndRender
            if (rotate.getValue()) {
                mc.mouseHelper.mouseXYChange();

                float f = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
                float f1 = f * f * f * 8.0f;

                float yaw = mc.mouseHelper.deltaX * f1;
                float pitch = mc.mouseHelper.deltaY * f1;

                int i = mc.gameSettings.invertMouse ? 1 : -1;

                mc.player.turn(yaw, pitch * i);
            }

            KeyBinding[] binds = { mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindLeft };
            for (KeyBinding binding : binds) {
                KeyBinding.setKeyBindState(binding.getKeyCode(), Keyboard.isKeyDown(binding.getKeyCode()));
            }
        }

        // this only works while strafing (or sprint jumping)
        if (itemStrict.getValue() && mc.player.isHandActive()) {
            if (mc.player.isHandActive()) {
                if (!sneakState) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
                    sneakState = true;
                }
            } else {
                if (sneakState) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
                    sneakState = false;
                }
            }
        }
    }

    @SubscribeEvent
    public void onMovementInputUpdate(InputUpdateEvent event) {
        if (mc.player.isHandActive()) {
            event.getMovementInput().moveForward *= 5.0f;
            event.getMovementInput().moveStrafe *= 5.0f;
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.OUTGOING)) {
            if (event.getPacket() instanceof CPacketClickWindow) {
                if (!strictInventory.getValue() && mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) {
                    return;
                }

                // we cannot sprint or we will flag NCP
                if (mc.player.isSprinting()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SPRINTING));
                }

                // we sneak to represent a slowdown
                if (!sneakState) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
                    sneakState = true;
                }

                windowClick = true;
            } else if (event.getPacket() instanceof CPacketEntityAction) {
                // if we try to stop sneaking or try to start sprinting, we do now allow these packets to be sent
                if ((sneakState || windowClick) && mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) {
                    CPacketEntityAction packet = (CPacketEntityAction) event.getPacket();
                    if (packet.getAction().equals(Action.START_SPRINTING) || packet.getAction().equals(Action.STOP_SNEAKING)) {
                        event.setCanceled(true);
                    }
                }
            }
        } else if (event.getDirection().equals(Direction.INCOMING)) {

            // this is when the server accepts our window click
            if (event.getPacket() instanceof SPacketConfirmTransaction && windowClick) {
                if (sneakState) {
                    sneakState = false;
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
                }

                windowClick = false;
            }
        }
    }

    @SubscribeEvent
    public void onKeyDown(KeyDownEvent event) {
        if (inventory.getValue() && mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) {
            event.setCanceled(true);
        }
    }
}
