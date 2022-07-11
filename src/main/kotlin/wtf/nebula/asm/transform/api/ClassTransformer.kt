package wtf.nebula.asm.transform.api

import jdk.internal.org.objectweb.asm.Type
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode

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

    fun invokeStatic(clazz: Class<*>, name: String, desc: String): MethodInsnNode {
        return MethodInsnNode(
            Opcodes.INVOKESTATIC,
            Type.getInternalName(clazz),
            name,
            desc,
            false
        )
    }
}