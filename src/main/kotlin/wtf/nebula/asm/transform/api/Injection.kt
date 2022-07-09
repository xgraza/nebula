package wtf.nebula.asm.transform.api

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Injection(
    val name: String,
    val descriptor: String = "()V"
)
