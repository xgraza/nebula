package wtf.nebula.asm.transform.api

import net.minecraftforge.fml.common.ObfuscationReflectionHelper
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper
import org.apache.logging.log4j.LogManager
import wtf.nebula.asm.transform.impl.MinecraftTransformer
import java.lang.reflect.Field
import java.lang.reflect.Method

class TransformerManager {
    var logger = LogManager.getLogger("TransformerManager")

    var runtimeObfuscation: Boolean = false
    private val transformers = mutableMapOf<String, ClassTransformer>()

    init {
        add(MinecraftTransformer())

        logger.info("Loaded ${transformers.size} transformer classes")
    }

    private fun add(classTransformer: ClassTransformer) {
        transformers[classTransformer.hookClass] = classTransformer
    }

    fun findClassTransformer(name: String): ClassTransformer? {
        val s = if (runtimeObfuscation) getClassName(name) ?: return null else name
        return transformers[s]
    }

    fun getClassName(obfName: String): String? {
        return FMLDeobfuscatingRemapper.INSTANCE.unmap(obfName) ?: return null
    }

    fun getMethod(className: String, name: String, desc: String): Method? {
        val s = if (runtimeObfuscation) getClassName(className) else className
        val clazz = Class.forName(s) ?: null

        val params = fromDesc(desc)

        // TODO: get return type etc

        return null
    }

    fun getField(clazz: Class<*>, name: String): Field? {
        return if (runtimeObfuscation) {
            ObfuscationReflectionHelper.findField(clazz, name)
        } else clazz.getField(name)
    }

    private fun fromDesc(desc: String): List<Class<*>> {
        val list = mutableListOf<Class<*>>()

        if (desc.startsWith("()")) {
            return list
        }

        val chars = desc.toCharArray()

        var endingPoint = -1
        for (i in 0..chars.size) {
            if (endingPoint != -1 && endingPoint > i) {
                continue
            }

            endingPoint = -1

            val char = chars[i]
            if (char == 'L') {

                var classDesc = ""
                endingPoint = 0

                for (j in i..desc.length) {
                    val str = desc[j]
                    if (str == ';') {
                        endingPoint = j + 1
                    }

                    if (j == endingPoint) {
                        break
                    }

                    classDesc += str.toString()
                }

                if (endingPoint != 0) {
                    val res = parseDescriptorPart(classDesc) ?: continue
                    list.add(res)
                }
            }

            else if (char == '[') {

            }

            else {
                primitiveType(char)?.let { list.add(it) }
            }
        }

        return list
    }

    private fun primitiveType(char: Char): Class<*>? {
        return when (char) {
            'Z' -> Boolean::class.java
            'C' -> Char::class.java
            'B' -> Byte::class.java
            'S' -> Short::class.java
            'I' -> Int::class.java
            'F' -> Float::class.java
            'D' -> Double::class.java
            else -> null
        }
    }

    private fun parseDescriptorPart(part: String, arrayDepth: Int = 0): Class<*>? {

        // indicates a class
        if (part.startsWith("L")) {
            val endIndex = part.indexOf(';')
            if (endIndex == -1) {
                throw IllegalArgumentException("Class types must end with a semi-colon (;)")
            }

            else {
                var name = part

                val innerClassIndex = part.indexOf('$')
                if (innerClassIndex != -1) {
                    name = part.substring(innerClassIndex, part.lastIndex)
                }

                else {

                    if (!runtimeObfuscation) {
                        name = part
                            .substring(1, endIndex)
                            .replace('/', '.')
                    }
                }

                return Class.forName(name)
            }
        }

        else {
            if (part == "[") {
                return null
            }

            else {
                return primitiveType(part.first())
            }
        }
    }

    companion object {
        private var instance: TransformerManager? = null

        fun getInstance(): TransformerManager {
            if (instance == null) {
                instance = TransformerManager()
            }

            return instance!!
        }
    }
}