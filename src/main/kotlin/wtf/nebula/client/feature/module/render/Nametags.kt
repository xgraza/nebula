package wtf.nebula.client.feature.module.render

import com.mojang.realmsclient.gui.ChatFormatting
import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.render.RenderWorldEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.util.render.RenderUtil
import wtf.nebula.util.rotation.AngleUtil

class Nametags : Module(ModuleCategory.RENDER, "custom nametags and whatnot") {
    val scale by double("Scale", 0.2, 0.1..1.3)
    val armor by bool("Armor", true)
    val reversed by bool("Reversed Armor", true)
    val ping by bool("Ping", true)
    val health by bool("Health", true)
    val held by bool("Held", true)
    val enchantments by bool("Enchantments", true)

    @EventListener
    private val renderWorldListener = listener<RenderWorldEvent> {
        mc.renderViewEntity ?: return@listener

        for (entityPlayer in mc.world.playerEntities) {
            val x = AngleUtil.interpolate(entityPlayer.posX, entityPlayer.lastTickPosX) - mc.renderManager.viewerPosX
            val y = AngleUtil.interpolate(entityPlayer.posY, entityPlayer.lastTickPosY) - mc.renderManager.viewerPosY
            val z = AngleUtil.interpolate(entityPlayer.posZ, entityPlayer.lastTickPosZ) - mc.renderManager.viewerPosZ

            renderNametag(entityPlayer, x, y, z)
        }
    }

    private fun renderNametag(entityPlayer: EntityPlayer, x: Double, y: Double, z: Double) {
        val renderX = mc.renderManager.viewerPosX
        val renderY = mc.renderManager.viewerPosY
        val renderZ = mc.renderManager.viewerPosZ

        val distance = mc.renderViewEntity!!.getDistance(x + renderX, y + renderY, z + renderZ)
        val scale = scale * distance.coerceAtLeast(4.0) / 50.0

        GlStateManager.pushMatrix()
        RenderHelper.enableGUIStandardItemLighting()

        GlStateManager.enablePolygonOffset()
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f)

        GlStateManager.disableLighting()

        GlStateManager.translate(x, y + entityPlayer.height + 0.25, z)

        GlStateManager.rotate(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(mc.renderManager.playerViewX, if (mc.gameSettings.thirdPersonView == 2) -1.0f else 1.0f, 0.0f, 0.0f)
        GlStateManager.scale(-scale, -scale, scale)

        GlStateManager.disableDepth()
        GlStateManager.enableBlend()

        val info = getNametagInfo(entityPlayer)

        val width = mc.fontRenderer.getStringWidth(info) / 2.0f
        RenderUtil.rect(
            -(width - 2.0),
            -(mc.fontRenderer.FONT_HEIGHT - 1.0),
            width * 2.0 + 2.0,
            mc.fontRenderer.FONT_HEIGHT + 2.0,
            0x70000000
        )

        mc.fontRenderer.drawStringWithShadow(
            info,
            -(width - 4.0).toInt().toFloat(),
            -(mc.fontRenderer.FONT_HEIGHT - 2).toFloat(),
            -1
        )

        var xOffset = -24 / 2 * entityPlayer.inventory.armorInventory.size

        if (held) {

            if (!entityPlayer.heldItemMainhand.isEmpty) {
                renderItem(entityPlayer.heldItemMainhand, xOffset)
            }

            xOffset += 16
        }

        if (armor) {
            val armor = entityPlayer.inventory.armorInventory

            for (armorPiece in armor) {
                if (!armorPiece.isEmpty) {
                    renderItem(armorPiece, xOffset)
                    xOffset += 16
                }
            }
        }

        if (held) {
            if (!entityPlayer.heldItemOffhand.isEmpty) {
                renderItem(entityPlayer.heldItemOffhand, xOffset)
            }

            xOffset += 16
        }

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);

        GlStateManager.enableLighting();
        GlStateManager.enableBlend();

        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.disablePolygonOffset();

        GlStateManager.popMatrix();
    }

    private fun renderItem(stack: ItemStack, x: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.depthMask(true)

        GlStateManager.clear(256)

        RenderHelper.enableStandardItemLighting()

        mc.renderItem.zLevel = -150.0f

        GlStateManager.disableAlpha()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
        GlStateManager.disableCull()

        mc.renderItem.renderItemAndEffectIntoGUI(stack, x, -26)
        mc.renderItem.renderItemOverlays(mc.fontRenderer, stack, x, -26)

        mc.renderItem.zLevel = 0.0f

        RenderHelper.disableStandardItemLighting()

        GlStateManager.enableCull()
        GlStateManager.enableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }

    private fun getNametagInfo(entityPlayer: EntityPlayer): String {
        val stringBuilder = StringBuilder()

        if (ping) {
            var latency = 0

            try {
                for (info in mc.player.connection.playerInfoMap) {
                    info ?: continue

                    if (info.gameProfile.name == entityPlayer.gameProfile.name) {
                        latency = info.responseTime
                        break
                    }
                }
            } catch (ignored: Exception) {

            }

            stringBuilder.append(latency.toString()).append("ms").append(" ")
        }

        val friend = Nebula.friendManager.friends[entityPlayer.name]
        if (friend != null) {
            stringBuilder.append(ChatFormatting.AQUA)
        }

        else if (entityPlayer.isSneaking) {
            stringBuilder.append(ChatFormatting.GOLD)
        }

        if (friend != null) {
            stringBuilder.append(friend.alias)
        }

        else {
            stringBuilder.append(entityPlayer.name)
        }

        stringBuilder.append(ChatFormatting.RESET)

        if (health) {
            stringBuilder.append(" ")

            val health: Float = entityPlayer.health + entityPlayer.absorptionAmount

            if (health >= 20.0f) {
                stringBuilder.append(ChatFormatting.GREEN)
            } else if (health < 12.0f) {
                stringBuilder.append(ChatFormatting.RED)
            } else {
                stringBuilder.append(ChatFormatting.GREEN)
            }

            stringBuilder.append(String.format("%.1f", health))
        }

        return stringBuilder.toString()
    }
}