package wtf.nebula.client.manager.server.monitors.impl

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.network.play.server.SPacketTimeUpdate
import net.minecraft.util.math.MathHelper
import wtf.nebula.client.event.packet.PacketReceiveEvent
import wtf.nebula.client.manager.server.monitors.Monitor

class TPSMonitor : Monitor<Double>() {
    private val ticks = DoubleArray(20)
    private var lastTime = -1L
    private var currentTick = 0

    var responding = true

    @EventListener
    private val packetReceiveListener = listener<PacketReceiveEvent> {
        if (it.packet is SPacketTimeUpdate) {

            if (lastTime != -1L) {
                // the time since our last packet - this ideally should be ~1000L for 20 TPS
                val timeSinceLastPacket = System.currentTimeMillis() - lastTime

                // if the server is responding
                responding = timeSinceLastPacket >= 1500L

                // make sure we are always in bounds of the array
                // this calculation is to see how many ticks it took to respond
                // aka if it took 1s to respond, (20 / (1000 / 1000)) = 20 TPS
                // else, let's say it took 2s to respond (20 / (2000 / 1000)) = 10 TPS
                val tickDiff = (20L / (timeSinceLastPacket.toDouble() / 1000.0))
                ticks[currentTick % ticks.size] = MathHelper.clamp(tickDiff, 0.0, 20.0)

                // update to our next tick
                ++currentTick
            }

            // update our time
            lastTime = System.currentTimeMillis()
        }
    }

    override fun getMonitoredValue(): Double {
        // let's do 5th grade math for average, aka mean
        // mean = sum all elements / amount of elements (the amount of elements not 0)
        // we have to take into account 0's because of division by zero errors

        var ticksNotNull = 0
        var sum = 0.0

        for (tick in ticks) {
            if (tick > 0.0) {
                ++ticksNotNull
                sum += tick
            }
        }

        if (ticksNotNull == 0) {
            return 20.0
        }

        return MathHelper.clamp(sum / ticksNotNull, 0.0, 20.0)
    }

    fun getNotRespondingTime(): Double {
        return if (responding) 0.0 else ((System.currentTimeMillis() - lastTime) / 1000L).toDouble()
    }
}