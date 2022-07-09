package wtf.nebula.client.registry

import org.apache.logging.log4j.LogManager
import wtf.nebula.client.feature.Feature

abstract class Registry<T> : Feature() {
    protected val logger = LogManager.getLogger(name)

    val registerMap = mutableMapOf<Class<*>, T>()
    val registers = mutableListOf<T>()

    abstract fun load()
    abstract fun unload()

    abstract fun loadMember(member: T)
}