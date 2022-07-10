package wtf.nebula.util.rotation

import java.lang.Float.isNaN

open class Rotation(val yaw: Float, val pitch: Float) {
    open val valid = !isNaN(yaw) && !isNaN(pitch)
}

class InvalidRotation : Rotation(Float.NaN, Float.NaN) {
    override val valid: Boolean = false
}