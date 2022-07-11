package wtf.nebula.client.config.setting

import java.util.*
import kotlin.reflect.KProperty

open class Setting<T>(val name: String, valueIn: T) {
    var value: T = valueIn
        set(v) {
            field = v
            //Launcher.BUS.post(SettingChangeEvent(this))
        }

    operator fun getValue(thisRef: Any, property: KProperty<*>) = value

    operator fun setValue(thisRef: Any, property: KProperty<*>, v: T) {
        value = v
    }

    companion object {

        // TODO: current, increase, and decrease should be extensions to Enum?
        private fun current(clazz: Enum<*>): Int {
            for (i in clazz.javaClass.enumConstants.indices) {
                val e = (clazz.javaClass.enumConstants as Array<Enum<*>>)[i]
                if (e.toString().equals(clazz.toString(), ignoreCase = true)) {
                    return i
                }
            }
            return -1
        }

        fun increase(clazz: Enum<*>): Enum<*> {
            val index = current(clazz)
            val next = index + 1
            val constants = clazz.javaClass.enumConstants
            return if (next > constants.size - 1) {
                constants[0]
            } else constants[next]
        }

        fun decrease(clazz: Enum<*>): Enum<*> {
            val index = current(clazz)
            val last = index - 1
            val constants = clazz.javaClass.enumConstants
            return if (last < 0) {
                constants[constants.size - 1]
            } else constants[last]
        }

        fun formatEnum(clazz: Enum<*>): String {
            val name = clazz.toString().replace("_".toRegex(), " ")
            return name[0].toString().uppercase(Locale.getDefault()) + name.substring(1).lowercase(Locale.getDefault())
        }
    }
}