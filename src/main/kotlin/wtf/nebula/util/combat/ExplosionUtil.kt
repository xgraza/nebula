package wtf.nebula.util.combat

import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.init.MobEffects
import net.minecraft.util.CombatRules
import net.minecraft.util.DamageSource
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Explosion
import wtf.nebula.util.Globals
import kotlin.math.max

object ExplosionUtil : Globals {
    fun calcExplosion(vec: Vec3d, target: EntityLivingBase, explosionSize: Float): Float {
        val doubledExplosionSize = (explosionSize * 2.0f).toDouble()

        val dist = target.getDistance(vec.x, vec.y, vec.z) / doubledExplosionSize
        if (dist > 1.0) {
            return 0.0f
        }

        val v = (1.0 - dist) * target.world.getBlockDensity(vec, target.entityBoundingBox)

        var damage = CombatRules.getDamageAfterAbsorb(
            DamageUtil.getDamageMultiplierOnDiff(
                ((v * v + v) / 2.0 * 7.0 * doubledExplosionSize + 1.0).toFloat()),
            target.totalArmorValue.toFloat(),
            target.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat())

        val damageSource = DamageSource.causeExplosionDamage(
            Explosion(
                target.world, target,
                vec.x, vec.y, vec.z,
                doubledExplosionSize.toFloat(),
                false, true))

        val n = EnchantmentHelper.getEnchantmentModifierDamage(target.armorInventoryList, damageSource)
        if (n > 0) {
            damage = CombatRules.getDamageAfterMagicAbsorb(damage, n.toFloat())
        }

        if (target.isPotionActive(MobEffects.RESISTANCE)) {
            val potionEffect = target.getActivePotionEffect(MobEffects.RESISTANCE)
            if (potionEffect != null) {
                damage = damage * (25.0f - (potionEffect.amplifier + 1) * 5) / 25.0f
            }
        }

        return max(damage, 0.0f)
    }
}