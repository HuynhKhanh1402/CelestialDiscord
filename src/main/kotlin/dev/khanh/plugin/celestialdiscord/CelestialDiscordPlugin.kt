package dev.khanh.plugin.celestialdiscord

import dev.khanh.plugin.celestialdiscord.file.ConfigFile
import dev.khanh.plugin.celestialdiscord.listener.PluginListener
import dev.khanh.plugin.celestialdiscord.task.SendServerInfoTask
import dev.khanh.plugin.celestialdiscord.webhook.WebhookManager
import org.bukkit.plugin.java.JavaPlugin

class CelestialDiscordPlugin : JavaPlugin() {

    lateinit var configFile: ConfigFile
    lateinit var webhookManager: WebhookManager
    lateinit var sendServerInfoTask: SendServerInfoTask

    override fun onEnable() {
        configFile = ConfigFile(this)
        webhookManager = WebhookManager(this)
        sendServerInfoTask = SendServerInfoTask(this)

        server.pluginManager.registerEvents(PluginListener(this), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
