package dev.khanh.plugin.celestialdiscord.file

import dev.khanh.plugin.celestialdiscord.CelestialDiscordPlugin

class ConfigFile(plugin: CelestialDiscordPlugin) {
    var serverId: String

    init {
        plugin.saveDefaultConfig()

        val config = plugin.config

        serverId = config.getString("server-id", "")!!
    }
}