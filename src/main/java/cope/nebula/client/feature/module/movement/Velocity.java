package cope.nebula.client.feature.module.movement;

import cope.nebula.asm.mixins.network.packet.s2c.ISPacketEntityVelocity;
import cope.nebula.asm.mixins.network.packet.s2c.ISPacketExplosion;
import cope.nebula.client.events.EntityCollisionEvent;
import cope.nebula.client.events.EntityPushedByWaterEvent;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Velocity extends Module {
    public Velocity() {
        super("Velocity", ModuleCategory.MOVEMENT, "Ignores server velocity");
    }

    public static final Value<Float> vertical = new Value<>("Vertical", 0.0f, 0.0f, 100.0f);
    public static final Value<Float> horizontal = new Value<>("Horizontal", 0.0f, 0.0f, 100.0f);

    public static final Value<Boolean> blocks = new Value<>("Blocks", true);
    public static final Value<Boolean> entities = new Value<>("Entities", true);
    public static final Value<Boolean> liquids = new Value<>("Liquids", true);
    public static final Value<Boolean> fishingHooks = new Value<>("FishingHooks", true);

    @Override
    public String getDisplayInfo() {
        return "V:" + vertical.getValue() + "%, H:" + horizontal.getValue() + "%";
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.INCOMING)) {
            if (event.getPacket() instanceof SPacketEntityVelocity) {
                SPacketEntityVelocity packet = event.getPacket();

                ((ISPacketEntityVelocity) packet).setMotionX(packet.getMotionX() * (int) (horizontal.getValue() / 100.0));
                ((ISPacketEntityVelocity) packet).setMotionY(packet.getMotionY() * (int) (vertical.getValue() / 100.0));
                ((ISPacketEntityVelocity) packet).setMotionZ(packet.getMotionZ() * (int) (horizontal.getValue() / 100.0));
            } else if (event.getPacket() instanceof SPacketExplosion) {
                SPacketExplosion packet = event.getPacket();

                ((ISPacketExplosion) packet).setMotionX(packet.getMotionX() * (horizontal.getValue() / 100.0f));
                ((ISPacketExplosion) packet).setMotionY(packet.getMotionY() * (vertical.getValue() / 100.0f));
                ((ISPacketExplosion) packet).setMotionZ(packet.getMotionZ() * (horizontal.getValue() / 100.0f));
            } else if (event.getPacket() instanceof SPacketEntityStatus) {
                SPacketEntityStatus packet = event.getPacket();
                if (packet.getOpCode() == 31 && packet.getEntity(mc.world) instanceof EntityFishHook) {
                    EntityFishHook hook = (EntityFishHook) packet.getEntity(mc.world);
                    if (hook.caughtEntity != null && hook.caughtEntity.equals(mc.player) && fishingHooks.getValue()) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPushOutOfBlocks(PlayerSPPushOutOfBlocksEvent event) {
        if (event.getEntityPlayer().equals(mc.player) && blocks.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityCollision(EntityCollisionEvent event) {
        event.setCanceled(entities.getValue());
    }

    @SubscribeEvent
    public void onEntityPushedByWater(EntityPushedByWaterEvent event) {
        if (event.getPlayer().equals(mc.player) && liquids.getValue()) {
            event.setCanceled(true);
        }
    }
}
