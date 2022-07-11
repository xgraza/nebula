package wtf.nebula.client.event.player.motion.update

class PreMotionUpdate(
    var x: Double,
    var y: Double,
    var z: Double,
    var yaw: Float,
    var pitch: Float,
    var onGround: Boolean
) : MotionUpdateBase(Era.PRE)