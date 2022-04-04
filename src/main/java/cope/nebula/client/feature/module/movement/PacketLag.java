package cope.nebula.client.feature.module.movement;

import cope.nebula.asm.duck.INetworkManager;
import cope.nebula.client.events.PacketEvent;
import cope.nebula.client.events.PacketEvent.Direction;
import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.internal.timing.Stopwatch;
import cope.nebula.util.internal.timing.TimeFormat;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PacketLag extends Module {
    public PacketLag() {
        super("PacketLag", ModuleCategory.MOVEMENT, "Suspends movement packets");
    }

    public static final Value<Boolean> manual = new Value<>("Manual", false);

    public static final Value<Integer> time = new Value<>("Time", 5, 1, 10);
    public static final Value<Boolean> keepAlive = new Value<>("KeepAlive", false);

    private final Stopwatch stopwatch = new Stopwatch();
    private EntityOtherPlayerMP fakePlayer = null;

    private final Queue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    private boolean sending = false;

    @Override
    public String getDisplayInfo() {
        return String.valueOf(packets.size());
    }

    @Override
    protected void onActivated() {
        if (nullCheck()) {
            disable();
            return;
        }

        fakePlayer = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.inventory.copyInventory(mc.player.inventory);

        mc.world.spawnEntity(fakePlayer);
    }

    @Override
    protected void onDeactivated() {
        if (!nullCheck()) {
            if (fakePlayer != null) {
                mc.world.removeEntity(fakePlayer);
                mc.world.removeEntityDangerously(fakePlayer);
            }

            unsuspendPackets();
        }

        fakePlayer = null;
    }

    @Override
    public void onTick() {
        if (!manual.getValue() && stopwatch.hasElapsed(time.getValue(), true, TimeFormat.SECONDS)) {
            unsuspendPackets();

            if (fakePlayer != null) {
                mc.world.removeEntity(fakePlayer);
                mc.world.removeEntityDangerously(fakePlayer);
            }

            fakePlayer = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
            fakePlayer.copyLocationAndAnglesFrom(mc.player);
            fakePlayer.inventory.copyInventory(mc.player.inventory);

            mc.world.spawnEntity(fakePlayer);
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection().equals(Direction.OUTGOING) && !sending) {
            Packet<?> packet = event.getPacket();

            if (packet instanceof CPacketPlayer ||
                    (keepAlive.getValue() && (packet instanceof CPacketKeepAlive || packet instanceof CPacketConfirmTransaction))) {

                packets.add(packet);
                event.setCanceled(true);
            }
        }
    }

    private void unsuspendPackets() {
        sending = true;
        while (!packets.isEmpty()) {
            Packet<?> packet = packets.poll();
            if (packet != null) {
                ((INetworkManager) mc.player.connection.getNetworkManager()).sendPacketSilent(packet);
            }
        }
        sending = false;
    }
}
