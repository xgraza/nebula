package wtf.nebula.client.registry

import wtf.nebula.client.Nebula
import wtf.nebula.util.Globals

object RegistryContainer : Globals {
    private val registrars = mutableMapOf<Class<out Registry<*>>, Registry<*>>()

    fun register(registry: Registry<*>) {
        registry.load()
        registrars[registry::class.java] = registry
        Nebula.BUS.subscribe(registry)
    }

    fun <T> get(clazz: Class<Registry<T>>): Registry<T>? {
        return registrars[clazz] as Registry<T> ?: null
    }
}