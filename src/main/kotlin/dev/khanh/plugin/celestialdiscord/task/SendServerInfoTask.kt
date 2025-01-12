package dev.khanh.plugin.celestialdiscord.task

import dev.khanh.plugin.celestialdiscord.CelestialDiscordPlugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class SendServerInfoTask(plugin: CelestialDiscordPlugin): Runnable {
    val enabled: Boolean = plugin.config.getBoolean("server-info.enabled")
    val url: String = plugin.config.getString("server-info.url")
        ?: throw IllegalArgumentException("server-info.url must be specified")
    val interval: Long = plugin.config.getLong("server-info.interval-in-tick", 1200).takeIf { it > 0 }
        ?: throw IllegalArgumentException("server-info.interval-in-tick must be greater than 0")

    val decimalFormat = DecimalFormat(
        "0.00",
        DecimalFormatSymbols.getInstance(Locale.forLanguageTag("en"))
    )

    init {
        if (enabled) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 0, interval)
            plugin.logger.info("Registered sent server info task (interval = $interval)")
        }
    }

    override fun run() {
        val data = mapOf(
            "maxPlayers" to Bukkit.getMaxPlayers().toString(),
            "onlinePlayers" to Bukkit.getOnlinePlayers().size.toString(),
            "maxMemory" to (Runtime.getRuntime().maxMemory() / 1024 / 1024).toString(),
            "usedMemory" to (Runtime.getRuntime().totalMemory() / 1024 / 1024).toString(),
            "TPS" to getTPS(),
            "currentTPS" to  decimalFormat.format(Bukkit.getServer().tps[0]),
            "averagePing" to decimalFormat.format(getAveragePing())
        )

        postToURL(data)
    }

    private fun postToURL(data: Map<String, String>) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 5000
            readTimeout = 5000
            setRequestProperty("Content-Type", "application/json")
        }

        try {
            val jsonData = data.entries.joinToString(",") { "\"${it.key}\": \"${it.value}\"" }
            val jsonPayload = "{ $jsonData }"

            connection.outputStream.use { it.write(jsonPayload.toByteArray(Charsets.UTF_8)) }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                println("Error: ${connection.responseCode}")
            }
        } catch (e: Exception) {
            throw RuntimeException("An error occurred while sending server data", e)
        } finally {
            connection.disconnect()
        }
    }


    private fun getTPS(): String {
        return try {
            val tps = Bukkit.getServer().tps
            tps.joinToString(", ") { decimalFormat.format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            "N/A"
        }
    }


    fun getAveragePing(): Double {
        val players: Collection<Player> = Bukkit.getOnlinePlayers()
        if (players.isEmpty()) {
            return 0.0
        }

        val totalPing = players.sumOf { it.ping }
        return totalPing / players.size.toDouble()
    }
}