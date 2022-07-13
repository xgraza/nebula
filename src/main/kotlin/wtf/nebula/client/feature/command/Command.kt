package wtf.nebula.client.feature.command

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import wtf.nebula.client.feature.Feature

abstract class Command(val aliases: Array<String>, val usage: String, val description: String) : Feature() {
    abstract fun build(builder: LiteralArgumentBuilder<Any>)

    protected fun send(message: String): Int {
        printChatMessage(hashCode(), message)
        return SINGLE_SUCCESS
    }

    companion object {
        fun build(alias: String): LiteralArgumentBuilder<Any> {
            return LiteralArgumentBuilder.literal<Any>(alias)
        }
    }
}