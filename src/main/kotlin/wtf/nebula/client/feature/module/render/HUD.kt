package wtf.nebula.client.feature.module.render

import com.mojang.realmsclient.gui.ChatFormatting
import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiChat
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.player.motion.update.PreMotionUpdate
import wtf.nebula.client.event.render.RenderHUDEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.client.registry.impl.ModuleRegistry
import kotlin.math.sqrt

class HUD : Module(ModuleCategory.RENDER, "Renders an overlay of the vanilla HUD") {
    val watermark by bool("Watermark", true)
    val arraylist by bool("Arraylist", true)
    val coordinates by bool("Coordinates", true)
    val speedCounter by bool("Speed", true)
    val fps by bool("FPS", true)
    val tps by bool("TPS", true)
    val ping by bool("Ping", true)

    private var speed = 0.0

    override fun onDeactivated() {
        super.onDeactivated()
        speed = 0.0
    }

    @EventListener
    private val preMotionUpdate = listener<PreMotionUpdate> {
        val diffX = mc.player.posX - mc.player.lastTickPosX
        val diffZ = mc.player.posZ - mc.player.lastTickPosZ

        // line is from cosmos
        speed = sqrt(diffX * diffX + diffZ * diffZ) / 1000 / (0.05 / 3600) * (50.0f / mc.timer.tickLength)
    }

    @EventListener
    private val renderHUDEvent = listener<RenderHUDEvent> {
        if (watermark) {
            mc.fontRenderer.drawStringWithShadow(WATERMARK, 2.0f, 2.0f, Colors.color())
        }

        if (arraylist) {
            var y = 2.0
            ModuleRegistry.INSTANCE!!.registers
                .filter { mod -> mod.isActive() && mod.drawn }
                .sortedBy { mod -> -mc.fontRenderer.getStringWidth(mod.getInfo()) }
                .forEach { mod ->
                    val width = mc.fontRenderer.getStringWidth(mod.getInfo())
                    mod.animation.max = width.toFloat()
                    mod.animation.update()

                    val x = (it.res.scaledWidth_double - mod.animation.value - 4.0)
                    val height = mc.fontRenderer.FONT_HEIGHT + 2.0

                    mc.fontRenderer.drawStringWithShadow(
                        mod.getInfo(),
                        (x + 2.3).toFloat(),
                        (y + (height / 2.0) - (mc.fontRenderer.FONT_HEIGHT / 2.0)).toFloat(),
                        Colors.color())

                    y += height
                }
        }

        var y = it.res.scaledHeight_double - mc.fontRenderer.FONT_HEIGHT - 2
        if (mc.currentScreen is GuiChat) {
            y -= 13
        }

        if (coordinates) {
            val text = StringBuilder()
                .append(ChatFormatting.GRAY.toString())
                .append("XYZ: ")
                .append(ChatFormatting.RESET.toString())

            text.append(String.format("%.1f", mc.player.posX))
            text.append(", ")

            text.append(String.format("%.1f", mc.player.posY))
            text.append(", ")

            text.append(String.format("%.1f", mc.player.posZ))

            mc.fontRenderer.drawStringWithShadow(text.toString(), 2.0f, y.toFloat(), -1)
        }

        if (speedCounter) {
            val text = ChatFormatting.GRAY.toString() + "Speed: " + ChatFormatting.RESET.toString() + String.format("%.2f", speed)
            mc.fontRenderer.drawStringWithShadow(text, (it.res.scaledWidth_double - mc.fontRenderer.getStringWidth(text) - 2).toFloat(), y.toFloat(), -1)

            y -= (mc.fontRenderer.FONT_HEIGHT + 2)
        }

        if (tps) {
            val text = ChatFormatting.GRAY.toString() + "TPS: " + ChatFormatting.RESET.toString() + String.format("%.1f", Nebula.serverManager.tpsMonitor.getMonitoredValue())
            mc.fontRenderer.drawStringWithShadow(text, (it.res.scaledWidth_double - mc.fontRenderer.getStringWidth(text) - 2).toFloat(), y.toFloat(), -1)

            y -= (mc.fontRenderer.FONT_HEIGHT + 2)
        }

        if (fps) {
            val text = ChatFormatting.GRAY.toString() + "FPS: " + ChatFormatting.RESET.toString() + Minecraft.getDebugFPS()
            mc.fontRenderer.drawStringWithShadow(text, (it.res.scaledWidth_double - mc.fontRenderer.getStringWidth(text) - 2).toFloat(), y.toFloat(), -1)

            y -= (mc.fontRenderer.FONT_HEIGHT + 2)
        }

        if (ping) {
            var ping = 0
            for (info in mc.player.connection.playerInfoMap) {
                if (info == null) {
                    continue
                }

                if (info.gameProfile.name == mc.player.gameProfile.name) {
                    ping = info.responseTime
                }
            }

            val text = ChatFormatting.GRAY.toString() + "Ping: " + ChatFormatting.RESET.toString() + ping
            mc.fontRenderer.drawStringWithShadow(text, (it.res.scaledWidth_double - mc.fontRenderer.getStringWidth(text) - 2).toFloat(), y.toFloat(), -1)

            y -= (mc.fontRenderer.FONT_HEIGHT + 2)
        }
    }

    companion object {
        var WATERMARK = Nebula.FULL
    }
}