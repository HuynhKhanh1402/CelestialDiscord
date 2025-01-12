package dev.khanh.plugin.celestialdiscord.webhook

enum class Event(val key: String) {
    CHAT("chat"),
    JOIN("join"),
    LEAVE("leave"),
    ADVANCEMENT("advancement"),
}