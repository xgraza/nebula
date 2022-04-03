package cope.nebula.util.world.damage;

import cope.nebula.util.Globals;
import net.minecraft.util.CombatRules;
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
        switch (diff) {
            case 0:
                return 0.0f;

            case 1:
                return Math.min(damage / 2.0f + 1.0f, damage);

            case 3:
                return damage * 3.0f / 2.0f;
        }

        return damage;
    }
}
