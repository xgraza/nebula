package wtf.nebula.util

import java.util.*

object MathUtil {
    private var RNG = Random()

    fun random(range: ClosedRange<Float>): Float {
        return range.start + RNG.nextFloat() * (range.endInclusive - range.start)
    }
}