package wtf.nebula.util.ext

import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.world.World
import wtf.nebula.client.registry.RegistryContainer.mc

fun World.closestEntity(range: Double, wallRange: Double): EntityLivingBase? {
    return loadedEntityList.filter {
        var base = it is EntityLivingBase && !it.isDead && !it.equals(mc.player)

        val dist = mc.player.getDistanceSq(it)
        if (dist > range * range || !mc.player.canEntityBeSeen(it) && dist > wallRange * wallRange) {
            base = false
        }

        if (it is EntityArmorStand) {
            return@filter false
        }

        return@filter base
    }.minByOrNull { mc.player.getDistanceSq(it) } as EntityLivingBase?
}