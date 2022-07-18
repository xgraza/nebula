package wtf.nebula.client.feature.module.miscellaneous

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.RayTraceResult
import wtf.nebula.client.Nebula
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory

class MiddleClick : Module(ModuleCategory.MISCELLANEOUS, "Does things upon a middle click") {
    val pearl by bool("Pearl", true)
    val friend by bool("Friend", true)
    val exp by bool("EXP", true)

    @EventListener
    private val middleClickListener = listener<MiddleClick> {
        val result = mc.objectMouseOver ?: return@listener
        when (result.typeOfHit ?: return@listener) {
            RayTraceResult.Type.ENTITY -> {
                if (!friend) {
                    return@listener
                }

                val over = result.entityHit ?: return@listener
                if (over !is EntityPlayer) {
                    return@listener
                }

                if (Nebula.friendManager.isFriend(over)) {
                    Nebula.friendManager.remove(over)
                    printChatMessage("Removed that player as a friend")
                }

                else {
                    Nebula.friendManager.add(over)
                    printChatMessage("Added that player as a friend")
                }
            }

            RayTraceResult.Type.BLOCK -> {
                if (!exp) {
                    return@listener
                }
            }

            RayTraceResult.Type.MISS -> {
                if (!pearl) {
                    return@listener
                }
            }
        }
    }
}