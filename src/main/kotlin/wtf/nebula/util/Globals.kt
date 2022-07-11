package wtf.nebula.util

import com.mojang.realmsclient.gui.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.util.text.TextComponentString

interface Globals {
    val mc: Minecraft
        get() = Minecraft.getMinecraft()

    private val PREFIX
        get() = ChatFormatting.RED.toString() + "[Nebula] " + ChatFormatting.RESET.toString()

    fun printChatMessage(message: String) {
        mc.ingameGUI.chatGUI.printChatMessage(TextComponentString(PREFIX + message))
    }

    fun printChatMessage(id: Int, message: String) {
        mc.ingameGUI.chatGUI.printChatMessageWithOptionalDeletion(TextComponentString(PREFIX + message), id)
    }

    fun nullCheck(): Boolean {
        return mc.player == null || mc.world == null
    }
}