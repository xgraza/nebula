package cope.nebula.util.world.damage;

import cope.nebula.util.Globals;
import net.minecraft.world.World;

/**
 * Calculates general damages
 *
 * @author aesthetical
 * @since 3/19/22
 */
public class DamageUtil implements Globals {
    /**
     * Gets the scaled damage based off of the difficulty
     * @param damage
     * @return
     */
    public static float getScaledDamage(float damage) {
        World world = mc.world;
        if (world == null) {
            return damage;
        }

        int diff = world.getDifficulty().getId();
        return damage * (diff * 0.5f);
    }
}
