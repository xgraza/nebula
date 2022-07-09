package wtf.nebula.asm.transform.api

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ClassInjection(
    val value: String,
    val remap: Boolean = true
)
