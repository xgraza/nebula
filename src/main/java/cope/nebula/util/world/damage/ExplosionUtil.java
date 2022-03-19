package cope.nebula.util.world.damage;

import cope.nebula.util.Globals;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;

/**
 * Calculates damage from explosions
 *
 * *Not all my code*
 *
 * @author aesthetical
 * @since 3/19/22
 */
public class ExplosionUtil implements Globals {
    /**
     * Calculates the damage to the target an end crystal explosion will do
     * @param player The target
     * @param vec The position
     * @param ignoreTerrain If to ignore terrain blocks when calculating block density
     * @return the explosion damage to an end crystal to the target player
     */
    public static float calculateCrystalDamage(EntityPlayer player, Vec3d vec, boolean ignoreTerrain) {
        return calculateExplosionDamage(player, vec, ignoreTerrain, 12.0, false, true);
    }

    /**
     * Calculates the amount of damage an entity will take
     * @param player The target player
     * @param vec The position
     * @param ignoreTerrain If to ignore terrain blocks when calculating block density
     * @param powerSq The power of the explosion squared
     * @param causesFire If the explosion causes fire
     * @param damagesTerrain If the explosion damages the terrain
     * @return the explosion damage to the target player
     */
    public static float calculateExplosionDamage(EntityPlayer player, Vec3d vec, boolean ignoreTerrain, double powerSq, boolean causesFire, boolean damagesTerrain) {
//        if (player == null || player.isCreative() || player.isDead) {
//            return 0.0f;
//        }

        double size = player.getDistanceSq(vec.x, vec.y, vec.z) / powerSq;
        double density = mc.world.getBlockDensity(vec, player.getEntityBoundingBox());

        double impact = (1.0 - size) * density;
        float damage = (float) ((impact * impact + impact) / 2.0f * 7.0f * powerSq + 1.0);

        return getBlastReduction(player, DamageUtil.getScaledDamage(damage),
                new Explosion(player.world, player, vec.x, vec.y, vec.z, (float) (powerSq / 2.0), causesFire, damagesTerrain));
    }

    /**
     * Calculates the actual damage taking into account armor, combat rules, and potion effects
     * @param target The target entity
     * @param damage The roughly calculated damage
     * @param explosion The explosion object
     * @return a reduced damage value
     */
    public static float getBlastReduction(EntityPlayer target, float damage, Explosion explosion) {
        DamageSource src = DamageSource.causeExplosionDamage(explosion);
        damage = CombatRules.getDamageAfterAbsorb(damage, target.getTotalArmorValue(), (float) target.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

        int enchantModifier = 0;
        try {
            enchantModifier = EnchantmentHelper.getEnchantmentModifierDamage(target.getArmorInventoryList(), src);
        } catch (NullPointerException ignored) { }

        float eof = MathHelper.clamp(enchantModifier, 0.0f, 20.0f);
        damage *= 1.0f - eof / 25.0f;

        if (target.isPotionActive(MobEffects.RESISTANCE)) {
            damage -= damage / 4.0f;
        }

        return Math.max(0.0f, damage);
    }
}
