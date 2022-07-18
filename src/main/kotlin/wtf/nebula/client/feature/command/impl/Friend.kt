package wtf.nebula.client.feature.command.impl

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import wtf.nebula.client.Nebula
import wtf.nebula.client.feature.command.Command
import wtf.nebula.client.registry.impl.CommandRegistry.Companion.INVALID_USAGE

class Friend : Command(arrayOf("friend", "fren", "f"), "[player name]", "Friends or unfriends a player") {
    override fun build(builder: LiteralArgumentBuilder<Any>) {
        builder.then(RequiredArgumentBuilder.argument<Any?, String?>("friendName", StringArgumentType.word())
            .executes {
                val name = StringArgumentType.getString(it, "friendName")
                    ?: return@executes INVALID_USAGE

                if (Nebula.friendManager.isFriend(name)) {
                    Nebula.friendManager.remove(name)
                    send("Removed that player as a friend")
                }

                else {
                    Nebula.friendManager.add(name)
                    send("Added that player as a friend")
                }

                return@executes SINGLE_SUCCESS
            })
            .executes {
                return@executes INVALID_USAGE
            }
    }
}