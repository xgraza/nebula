package wtf.nebula.asm.hooks

import wtf.nebula.client.Nebula
import wtf.nebula.client.event.TickEvent

object MinecraftHook {
    fun runTick() {
        Nebula.BUS.post(TickEvent())
    }
}