package dev.khanh.plugin.celestialdiscord.listener

import dev.khanh.plugin.celestialdiscord.CelestialDiscordPlugin
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PluginListener(val plugin: CelestialDiscordPlugin): Listener {

    @Suppress("DEPRECATION")
    @EventHandler(priority = EventPriority.MONITOR)
    fun onChat(event: AsyncPlayerChatEvent) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            plugin.webhookManager!!.handlePlayerChatEvent(event.player, event)
        })
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(event: PlayerJoinEvent) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            plugin.webhookManager!!.handlePlayerJoinEvent(event.player, event)
        })
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            plugin.webhookManager!!.handlePlayerQuitEvent(event.player, event)
        })
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onAdvancementDone(event: PlayerAdvancementDoneEvent) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            plugin.webhookManager!!.handlePlayerAdvancementDoneEvent(event.player, event)
        })
    }
}