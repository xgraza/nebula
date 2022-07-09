package wtf.nebula.asm.transform.api.util

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

object ASMUtil {
    fun toClassNode(clazz: ByteArray): ClassNode? {
        val reader = ClassReader(clazz)
        val classNode = ClassNode()
        reader.accept(classNode, 0)
        return classNode
    }

    fun getMethod(classNode: ClassNode, name: String, desc: String): MethodNode? {
        for (methodNode in classNode.methods) {
            if (methodNode.name == name && methodNode.desc == desc) {
                return methodNode
            }
        }

        return null
    }

    fun toTransformed(classNode: ClassNode): ByteArray? {
        val writer = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        classNode.accept(writer)
        return writer.toByteArray()
    }
}