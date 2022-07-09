package wtf.nebula.client.event.translate

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent
import org.lwjgl.input.Keyboard
import wtf.nebula.client.Nebula

class ForgeEventListener {
    init {
        Nebula.logger.info("FML event listener registered to send to bush bus")
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onKeyInput(event: KeyInputEvent) {
        Nebula.BUS.post(wtf.nebula.client.event.KeyInputEvent(
            Keyboard.getEventKey(), Keyboard.getEventKeyState()))
    }
}