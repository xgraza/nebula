package wtf.nebula.client.feature.module.combat

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.common.gameevent.TickEvent
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.player.motion.update.PreMotionUpdate
import wtf.nebula.client.event.world.EntityRemovedEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.util.ext.closestEntity
import wtf.nebula.util.ext.isOutOfRange
import wtf.nebula.util.rotation.AngleUtil
import wtf.nebula.util.rotation.Bone

class Aura : Module(ModuleCategory.COMBAT, "Attacks entities around you") {
    val range by double("Range", 4.5, 1.0..6.0)
    val walls by double("Walls", 3.5, 1.0..6.0)

    val rotate by bool("Rotate", true)
    val bone by setting("Bone", Bone.HEAD)
    val swing by bool("Swing", true)

    val weapon by setting("Weapon", Weapon.SWORD)

    private var target: EntityLivingBase? = null

    override fun getDisplayInfo(): String? {
        if (target != null) {
            return target!!.name
        }

        return null
    }

    @EventListener
    private val tickListener = listener<TickEvent> {
        if (target != null && rotate) {
            val rotation = AngleUtil.toEntity(target, bone)
            if (rotation != null && rotation.valid) {
                Nebula.rotationManager.rotation = rotation
            }
        }
    }

    @EventListener
    private val preMotionUpdateListener = listener<PreMotionUpdate> {
        if (target == null || target!!.isDead || target!!.isOutOfRange(range)) {
            target = mc.world.closestEntity(range, walls)
        }

        if (target == null) {
            return@listener
        }

        if (weapon != Weapon.ANY) {
            when (weapon) {
                Weapon.SWORD -> {
                    if (mc.player.heldItemMainhand.item !is ItemSword) {
                        return@listener
                    }
                }

                Weapon.AXE -> {
                    if (mc.player.heldItemMainhand.item !is ItemAxe) {
                        return@listener
                    }
                }

                else -> {

                }
            }
        }

        // more of a delay
        if (mc.player.getCooledAttackStrength(1.0f) == 1.0f) {
            mc.playerController.attackEntity(mc.player, target!!)

            if (swing) {
                mc.player.swingArm(EnumHand.MAIN_HAND)
            }

            else {
                mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
            }
        }
    }

    enum class Weapon {
        SWORD, AXE, ANY
    }
}