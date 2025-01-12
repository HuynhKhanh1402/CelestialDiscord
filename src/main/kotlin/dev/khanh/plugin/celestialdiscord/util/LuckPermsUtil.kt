package dev.khanh.plugin.celestialdiscord.util

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.user.User
import org.bukkit.entity.Player

class LuckPermsUtil {
    companion object {

        fun getUser(player: Player): User {
            val luckPerms = LuckPermsProvider.get()
            val user = luckPerms.userManager.getUser(player.uniqueId)
                ?: throw IllegalArgumentException("Can not retrieve user data of player ${player.name}")
            return user
        }

        fun getPrimaryGroup(player: Player): String? {
            val user = getUser(player)
            return user.primaryGroup
        }

        fun getPlayerMetaData(player: Player, metadataKeys: List<String>): JsonObject {
            val user = getUser(player)

            val json = JsonObject()

            val metadata = user.cachedData.metaData

            json.addProperty("prefix", metadata.prefix)
            json.addProperty("suffix", metadata.suffix)
            val prefixes = JsonArray().apply {
                metadata.prefixes.values.forEach {
                    add(it)
                }
            }
            json.add("prefixes", prefixes)

            val suffixes = JsonArray().apply {
                metadata.suffixes.values.forEach {
                    add(it)
                }
            }
            json.add("suffixes", suffixes)

            metadataKeys.forEach {
                json.addProperty(it, user.cachedData.metaData.getMetaValue(it) ?: "null")
            }

            return json
        }
    }
}