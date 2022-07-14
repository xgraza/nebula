package wtf.nebula.util.combat

import net.minecraft.world.EnumDifficulty
import wtf.nebula.util.Globals
import kotlin.math.min

object DamageUtil : Globals {
    fun getDamageMultiplierOnDiff(damage: Float): Float {
        return when (mc.world.difficulty) {
            EnumDifficulty.PEACEFUL -> 0.0f
            EnumDifficulty.EASY -> min(damage / 2.0f + 1.0f, damage)
            EnumDifficulty.HARD -> damage * 3.0f / 2.0f
            else -> damage
        }
    }
}