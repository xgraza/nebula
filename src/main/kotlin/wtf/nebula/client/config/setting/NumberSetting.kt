package wtf.nebula.client.config.setting

class NumberSetting<T : Number>(
    name: String,
    value: T,
    val min: T,
    val max: T
) : Setting<T>(name, value)