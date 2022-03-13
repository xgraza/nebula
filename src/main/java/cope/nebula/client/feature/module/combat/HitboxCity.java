package cope.nebula.client.feature.module.combat;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import net.minecraft.network.play.client.CPacketPlayer.Position;

public class HitboxCity extends Module {
    public HitboxCity() {
        super("HitboxCity", ModuleCategory.COMBAT, "Glitches your hitbox into a block to city someone");
    }

    @Override
    public void onTick() {
        if (mc.player.collidedHorizontally) {
            double rad = Math.toRadians(mc.player.rotationYaw);

            double x = mc.player.posX + (0.056 * -Math.sin(rad));
            double z = mc.player.posZ + (0.056 * Math.cos(rad));

            mc.player.connection.sendPacket(new Position(x, mc.player.posY, z, true));
            mc.player.setPosition(x, mc.player.posY, z);
        }
    }
}
