package cope.nebula.client.feature.module.movement;

import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.world.entity.player.rotation.Rotation;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoRotate extends Module {
    public NoRotate() {
        super("NoRotate", ModuleCategory.MOVEMENT, "Stops the server from rotating you");
    }

    public static final Value<Boolean> confirm = new Value<>("Confirm", true);

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook && event.getDirection().equals(Direction.INCOMING)) {
            if (confirm.getValue()) {
                Rotation rotation = getNebula().getRotationManager().getFixedRotations();

                mc.player.connection.sendPacket(new PositionRotation(
                        mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ,
                        rotation.getYaw(), rotation.getPitch(), mc.player.onGround));
            }

            event.setCanceled(true);
        }
    }
}
