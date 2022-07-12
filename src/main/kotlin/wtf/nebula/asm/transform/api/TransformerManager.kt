package wtf.nebula.asm.transform.api

import net.minecraftforge.fml.common.ObfuscationReflectionHelper
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper
import org.apache.logging.log4j.LogManager
import wtf.nebula.asm.transform.impl.client.MinecraftTransformer
import wtf.nebula.asm.transform.impl.entity.EntityTransformer
import wtf.nebula.asm.transform.impl.entity.player.EntityPlayerSPTransformer
import wtf.nebula.asm.transform.impl.network.NetworkManagerTransformer
import wtf.nebula.asm.transform.impl.render.EntityRendererTransformer
import wtf.nebula.asm.transform.impl.render.RenderPlayerTransformer
import wtf.nebula.asm.transform.impl.render.gui.GuiIngameTransformer
import wtf.nebula.asm.transform.impl.world.WorldTransformer
import java.lang.reflect.Field
import java.util.regex.Pattern

class TransformerManager {
    val logger = LogManager.getLogger("TransformerManager")

    var runtimeObfuscation: Boolean = false
    private val transformers = mutableMapOf<String, ClassTransformer>()

    init {
        add(MinecraftTransformer())
        add(NetworkManagerTransformer())
        add(EntityPlayerSPTransformer())
        add(EntityTransformer())
        add(GuiIngameTransformer())
        add(EntityRendererTransformer())
        add(RenderPlayerTransformer())
        add(WorldTransformer())

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

    fun getField(clazz: Class<*>, name: String): Field? {
        return if (runtimeObfuscation) {
            ObfuscationReflectionHelper.findField(clazz, name)
        } else clazz.getField(name)
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

    companion object {
        private var instance: TransformerManager? = null
        val CLASS_REGEX = Pattern.compile("(L(?:[a-zA-Z]+\\/?)+;)+", Pattern.CASE_INSENSITIVE)
        val PRIMITIVE_REGEX = Pattern.compile("(Z)|(C)|(B)|(S)|(I)|(F)|(D)")

        fun getInstance(): TransformerManager {
            if (instance == null) {
                instance = TransformerManager()
            }

            return instance!!
        }
    }
}