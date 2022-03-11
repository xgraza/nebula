package cope.nebula.util.world.entity;

import net.minecraft.entity.EntityLivingBase;

/**
 * Calculates things on entities
 *
 * @author aesthetical
 * @since 3/11/22
 */
public class EntityUtil {
    /**
     * Gets the health of a living entity
     * @param entityLivingBase The entity
     * @return the total health including the absorption amount
     */
    public static float getHealth(EntityLivingBase entityLivingBase) {
        return entityLivingBase.getHealth() + entityLivingBase.getAbsorptionAmount();
    }
}
