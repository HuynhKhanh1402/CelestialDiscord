package dev.khanh.plugin.celestialdiscord.webhook

import com.google.gson.JsonObject
import dev.khanh.plugin.celestialdiscord.CelestialDiscordPlugin
import dev.khanh.plugin.celestialdiscord.util.LuckPermsUtil
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.net.HttpURLConnection
import java.net.URL

class WebhookManager (val plugin: CelestialDiscordPlugin) {
    private val url: String
    private val includeLuckPermsData: Boolean
    private val enabledEvents = mutableSetOf<Event>()
    private val luckPermsDataKeys: List<String>

    init {
        val config = plugin.config

        url = config.getString("web-hook.url")
            ?: throw IllegalArgumentException("Missing required config: web-hook.url")

        includeLuckPermsData = config.getBoolean("web-hook.luckperms-data.included", false)

        luckPermsDataKeys = config.getStringList("web-hook.luckperms-data.metadata-keys")

        val eventsSection = config.getConfigurationSection("web-hook.events")
            ?: throw IllegalArgumentException("Missing required config section: web-hook.events")

            enabledEvents.apply {
            addAll(Event.entries.filter { event -> eventsSection.getBoolean(event.key, false) })
        }
    }

    @Suppress("DEPRECATION")
    fun handlePlayerChatEvent(player: Player, event: AsyncPlayerChatEvent) {
        val data = generatePlayerData(player, Event.CHAT)
        data.addProperty("content", ChatColor.stripColor(event.message))
        sendRequest(data.toString())
    }

    @Suppress("DEPRECATION")
    fun handlePlayerJoinEvent(player: Player, event: PlayerJoinEvent) {
        val data = generatePlayerData(player, Event.JOIN)
        data.addProperty("content", ChatColor.stripColor(event.joinMessage) ?: "")
        sendRequest(data.toString())
    }

    @Suppress("DEPRECATION")
    fun handlePlayerQuitEvent(player: Player, event: PlayerQuitEvent) {
        val data = generatePlayerData(player, Event.LEAVE)
        data.addProperty("content", ChatColor.stripColor(event.quitMessage) ?: "")
        sendRequest(data.toString())
    }

    @Suppress
    fun handlePlayerAdvancementDoneEvent(player: Player, event: PlayerAdvancementDoneEvent) {
        val data = generatePlayerData(player, Event.LEAVE)
        data.addProperty("advancement", event.advancement.key.key)

        event.message()?.let {
            val message = LegacyComponentSerializer.legacySection().serialize(it)
            data.addProperty("content", ChatColor.stripColor(message))
        }

        sendRequest(data.toString())
    }


    private fun generatePlayerData(player: Player, event: Event): JsonObject {
        val json = JsonObject()

        json.addProperty("serverId", plugin.config.getString("server-id", ""))

        json.addProperty("eventType", event.name)

        json.addProperty("playerName", player.name)
        json.addProperty("uuid", player.uniqueId.toString())

        if (includeLuckPermsData) {
            val luckpermsDataJson = JsonObject()
            luckpermsDataJson.addProperty("primary-group", LuckPermsUtil.getPrimaryGroup(player))
            luckpermsDataJson.add("metaData", LuckPermsUtil.getPlayerMetaData(player, luckPermsDataKeys))
            json.add("luckperms", luckpermsDataJson)
        }

        return json
    }

    private fun sendRequest(jsonBody: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 5000
                readTimeout = 5000
            }

            connection.outputStream.use {
                it.write(jsonBody.toByteArray(Charsets.UTF_8))
                it.flush()
            }

            return connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            throw RuntimeException("An error occurred while sending webhook", e)
        } finally {
            connection.disconnect()
        }
    }

}