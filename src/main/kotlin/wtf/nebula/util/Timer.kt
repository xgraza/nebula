package wtf.nebula.util

class Timer {
    private var time = -1L

    fun passedTime(delay: Long, reset: Boolean = false): Boolean {
        val passed = System.nanoTime() - time >= delay * 1000000L
        if (passed && reset) {
            resetTime()
        }

        return passed
    }

    val timePassedMs: Long
        get() = (System.nanoTime() - time) / 1000000L

    fun resetTime(): Timer {
        time = System.nanoTime()
        return this
    }
}