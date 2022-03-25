package cope.nebula.util.world.entity;

import cope.nebula.util.Globals;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Calculates things on entities
 *
 * @author aesthetical
 * @since 3/11/22
 */
public class EntityUtil implements Globals {
    /**
     * The killer bunny id type
     */
    public static final int KILLER_BUNNY_ID = 99;

    /**
     * Gets the health of a living entity
     * @param entityLivingBase The entity
     * @return the total health including the absorption amount
     */
    public static float getHealth(EntityLivingBase entityLivingBase) {
        return entityLivingBase.getHealth() + entityLivingBase.getAbsorptionAmount();
    }

    /**
     * If the entity is a hostile mob
     * @param entity The entity
     * @return if the entity is hostile
     */
    public static boolean isHostile(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            return false;
        }

        return !isPassive(entity) ||
                entity.isCreatureType(EnumCreatureType.MONSTER, false) ||
                entity instanceof EntitySlime;
    }

    /**
     * Checks if the entity is passive
     * @param entity The entity
     * @return if the entity is passive
     */
    public static boolean isPassive(EntityLivingBase entity) {
        if (entity instanceof EntityWolf) {
            EntityWolf wolf = (EntityWolf) entity;
            if (wolf.getRevengeTarget() != null && wolf.getRevengeTarget().equals(mc.player)) {
                return false;
            }
        }

        // check for the killer bunny
        if (entity instanceof EntityRabbit) {
            EntityRabbit bunny = (EntityRabbit) entity;
            if (bunny.getRabbitType() == KILLER_BUNNY_ID &&
                    bunny.getRevengeTarget() != null && bunny.getRevengeTarget().equals(mc.player)) {
                return false;
            }
        }

        return entity.isCreatureType(EnumCreatureType.AMBIENT, false) ||
                entity.isCreatureType(EnumCreatureType.CREATURE, false) ||
                entity.isCreatureType(EnumCreatureType.WATER_CREATURE, false);
    }
}
