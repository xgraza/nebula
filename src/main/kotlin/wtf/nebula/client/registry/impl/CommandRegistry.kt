package wtf.nebula.client.registry.impl

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.CommandDispatcher
import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import net.minecraft.network.play.client.CPacketChatMessage
import wtf.nebula.client.event.packet.PacketSendEvent
import wtf.nebula.client.feature.command.Command
import wtf.nebula.client.feature.command.impl.FakePlayer
import wtf.nebula.client.feature.command.impl.Friend
import wtf.nebula.client.feature.command.impl.Toggle
import wtf.nebula.client.registry.Registry

class CommandRegistry : Registry<Command>() {
    val dispatcher = CommandDispatcher<Any>()
    val commandsByAlias = mutableMapOf<String, Command>()

    var prefix = ","

    override fun load() {
        dispatcher.setConsumer { context, _, result ->
            runCatching {
                val name = context.input
                    .substring(prefix.length)
                    .trim()
                    .split(" ")[0]

                val command = registers.find {
                    it.name.equals(name, ignoreCase = true) } ?: return@setConsumer

                when (result) {
                    SINGLE_SUCCESS -> return@setConsumer
                    INVALID_USAGE ->
                        printChatMessage(hashCode(),
                            "Invalid usage. Usage: $prefix[${command.aliases.joinToString { "|" }}] ${command.usage}")
                    else ->
                        logger.info("Command returned unknown result $result (${context.input}) - $command")
                }
            }
        }

        loadMember(FakePlayer())
        loadMember(Friend())
        loadMember(Toggle())
    }

    override fun unload() {
        // TODO
    }

    override fun loadMember(member: Command) {
        registers += member
        registerMap[member::class.java] = member

        member.aliases.forEach {
            commandsByAlias[it] = member

            val builder = Command.build(it)
            member.build(builder)

            dispatcher.register(builder)
        }
    }

    @EventListener
    private val packetSendListener = listener<PacketSendEvent> {
        if (it.packet is CPacketChatMessage) {
            val msg = it.packet.message
            if (msg.startsWith(prefix)) {
                it.cancel()

                try {
                    dispatcher.execute(dispatcher.parse(msg.substring(prefix.length), SINGLE_SUCCESS))
                } catch (e: Exception) {
                    e.message?.let { it1 -> printChatMessage(it1) }
                }
            }
        }
    }

    companion object {
        const val UNSUCCESSFUL_EXECUTION = 2
        const val INVALID_USAGE = 3
    }
}