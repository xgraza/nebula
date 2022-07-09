package wtf.nebula.asm.transform.api

open class ClassTransformer {
    lateinit var hookClass: String
    var remap: Boolean = true

    init {
        if (javaClass.isAnnotationPresent(ClassInjection::class.java)) {
            val injection = javaClass.getAnnotation(ClassInjection::class.java)

            hookClass = injection.value
            remap = injection.remap
        }
    }
}