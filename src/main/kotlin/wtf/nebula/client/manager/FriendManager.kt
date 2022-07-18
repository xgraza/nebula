package wtf.nebula.client.manager

import net.minecraft.entity.player.EntityPlayer

class FriendManager {
    val friends = mutableMapOf<String, Friend>()

    fun add(entityPlayer: EntityPlayer) {
        add(entityPlayer.name)
    }

    fun add(name: String) {
        friends.computeIfAbsent(name) { Friend(it, it) }
    }

    fun remove(entityPlayer: EntityPlayer) {
        remove(entityPlayer.name)
    }

    fun remove(name: String) {
        friends -= name
    }

    fun isFriend(entityPlayer: EntityPlayer): Boolean = isFriend(entityPlayer.name)

    fun isFriend(name: String): Boolean = friends[name] != null
}

data class Friend(val name: String, val alias: String)