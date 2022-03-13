package cope.nebula.client.feature.module.movement;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.manager.hole.HoleType;
import cope.nebula.client.value.Value;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.atomic.AtomicReference;

public class HoleSnap extends Module {
    public HoleSnap() {
        super("HoleSnap", ModuleCategory.MOVEMENT, "Teleports you into a hole");
    }

    public static final Value<Double> range = new Value<>("Range", 4.0, 1.0, 6.0);
    public static final Value<Boolean> disable = new Value<>("Disable", true);

    @Override
    public void onTick() {
        if (getNebula().getHoleManager().isInHole()) {
            disable();
            return;
        }

        AtomicReference<BlockPos> position = new AtomicReference<>(BlockPos.ORIGIN);

        getNebula().getHoleManager().getHoles(HoleType.SINGLE, HoleType.DOUBLE, HoleType.QUAD)
                .forEach((hole) -> hole.getPositions().forEach((pos) -> {
                    double distance = mc.player.getDistanceSq(pos);
                    if (distance < range.getValue() * range.getValue()) {
                        position.set(pos);
                    }
                }));

        if (position.get().equals(BlockPos.ORIGIN)) {
            return;
        }

        BlockPos pos = position.get();

        double x = Math.floor(pos.getX()) + 0.5;
        double z = Math.floor(pos.getZ()) + 0.5;

        mc.player.connection.sendPacket(new Position(x, pos.getY(), z, mc.player.onGround));
        mc.player.setPosition(x, pos.getY(), z);

        if (disable.getValue()) {
            disable();
            return;
        }
    }
}
