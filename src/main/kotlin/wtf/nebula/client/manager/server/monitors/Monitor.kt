package wtf.nebula.client.manager.server.monitors

import wtf.nebula.client.feature.Feature

abstract class Monitor<T> : Feature() {
    abstract fun getMonitoredValue(): T
}