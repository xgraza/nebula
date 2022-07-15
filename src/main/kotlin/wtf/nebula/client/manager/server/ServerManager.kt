package wtf.nebula.client.manager.server

import wtf.nebula.client.Nebula
import wtf.nebula.client.manager.server.monitors.Monitor
import wtf.nebula.client.manager.server.monitors.impl.TPSMonitor
import wtf.nebula.util.Globals

class ServerManager : Globals {
    var speed = 1.0f
        set(value) {
            field = value
            mc.timer.tickLength = 50.0f / value
        }

    private val monitors = mutableMapOf<Class<out Monitor<*>>, Monitor<*>>()

    val tpsMonitor: TPSMonitor

    init {
        tpsMonitor = addMonitor(TPSMonitor()) as TPSMonitor
    }

    private fun addMonitor(monitor: Monitor<*>): Monitor<*> {
        monitors[monitor::class.java] = monitor
        Nebula.BUS.subscribe(monitor)
        return monitor
    }

    fun reset() {
        speed = 1.0f
    }
}