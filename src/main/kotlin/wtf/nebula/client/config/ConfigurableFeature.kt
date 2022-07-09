package wtf.nebula.client.config

import wtf.nebula.client.config.setting.BindSetting
import wtf.nebula.client.config.setting.NumberSetting
import wtf.nebula.client.config.setting.Setting
import wtf.nebula.client.feature.ToggleFeature

open class ConfigurableFeature : ToggleFeature() {
    val settingsByName = mutableMapOf<String, Setting<*>>()
    val settings = mutableListOf<Setting<*>>()

    private fun loadSetting(setting: Setting<*>) {
        settings += setting;
        settingsByName[setting.name] = setting;
    }

    protected fun bind(name: String, bind: Int): BindSetting {
        val setting = BindSetting(name, bind)
        loadSetting(setting)
        return setting
    }

    protected fun bool(name: String, value: Boolean): Setting<Boolean> {
        return setting(name, value)
    }

    protected fun int(name: String, value: Int, range: IntRange): NumberSetting<Int> {
        val setting = NumberSetting(name, value, range.first, range.last)
        loadSetting(setting)
        return setting
    }

    protected fun double(name: String, value: Double, range: ClosedRange<Double>): NumberSetting<Double> {
        val setting = NumberSetting(name, value, range.start, range.endInclusive)
        loadSetting(setting)
        return setting
    }

    protected fun float(name: String, value: Float, range: ClosedRange<Float>): NumberSetting<Float> {
        val setting = NumberSetting(name, value, range.start, range.endInclusive)
        loadSetting(setting)
        return setting
    }

    protected fun <T : Any> setting(name: String, value: T): Setting<T> {
        val setting = Setting(name, value)
        loadSetting(setting)
        return setting
    }
}