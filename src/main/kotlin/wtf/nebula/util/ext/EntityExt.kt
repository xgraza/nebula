package wtf.nebula.util.ext

import net.minecraft.entity.EntityLivingBase
import wtf.nebula.client.registry.RegistryContainer.mc

fun EntityLivingBase.isOutOfRange(range: Double) =
    mc.player.getDistanceSq(this) > range * range