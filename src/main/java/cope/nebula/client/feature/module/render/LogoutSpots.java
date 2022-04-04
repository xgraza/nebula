package cope.nebula.client.feature.module.render;

import cope.nebula.client.events.ServerConnectionEvent;
import cope.nebula.client.events.ServerConnectionEvent.Type;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class LogoutSpots extends Module {
    public LogoutSpots() {
        super("LogoutSpots", ModuleCategory.RENDER, "Shows where people logged out");
    }

    public static final Value<Boolean> nametags = new Value<>("Nametags", true);
    public static final Value<Boolean> notify = new Value<>("Notify", true);

    public static final Set<EntityOtherPlayerMP> spawnedFakes = new LinkedHashSet<>();
    private final Set<Spot> spots = new LinkedHashSet<>(); // TODO: nametags

    @Override
    protected void onDeactivated() {
        spots.clear();

        spawnedFakes.forEach((fake) -> {
            mc.world.removeEntity(fake);
            mc.world.removeEntityDangerously(fake);

            spawnedFakes.remove(fake);
        });

        spawnedFakes.clear();
    }

    @SubscribeEvent
    public void onServerConnection(ServerConnectionEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player == null || player.equals(mc.player) || mc.player.getDistanceSq(player) > 2500.0) {
            return;
        }

        int playerDimension = player.world.provider.getDimension();
        if (playerDimension != mc.player.world.provider.getDimension()) {
            return;
        }

        if (event.getType().equals(Type.LEAVE)) {
            EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(mc.player.world, player.getGameProfile());
            fakePlayer.copyLocationAndAnglesFrom(player);
            fakePlayer.setUniqueId(player.getUniqueID());
            fakePlayer.setGameType(GameType.SURVIVAL);

            mc.world.spawnEntity(fakePlayer);

            spawnedFakes.add(fakePlayer);
            spots.add(new Spot(player.getUniqueID(), playerDimension,
                    player.getPositionVector().add(0.0, player.height + 0.1, 0.0)));

            if (notify.getValue()) {
                sendChatMessage(player.getName() + " has logged out!");
            }
        } else {
            spawnedFakes
                    .stream().filter((f) -> f.getUniqueID().equals(player.getUniqueID()))
                    .findFirst().ifPresent((fake) -> {
                        if (notify.getValue()) {
                            sendChatMessage(fake.getName() + " has logged back in!");
                        }

                        mc.world.removeEntity(fake);
                        mc.world.removeEntityDangerously(fake);

                        spawnedFakes.remove(fake);
                        spots.removeIf((spot) -> spot.getUuid().equals(player.getUniqueID()));
                    });
        }
    }

    private static class Spot {
        private final UUID uuid;
        private final int dimension;
        private final Vec3d vec;

        public Spot(UUID uuid, int dimension, Vec3d vec) {
            this.uuid = uuid;
            this.dimension = dimension;
            this.vec = vec;
        }

        public UUID getUuid() {
            return uuid;
        }

        public int getDimension() {
            return dimension;
        }

        public Vec3d getVec() {
            return vec;
        }
    }
}
