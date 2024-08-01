package pl.syntaxerr.helpers

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class MessageHandler(plugin: JavaPlugin) {
    private val language = plugin.config.getString("language") ?: "PL"
    private val messages: FileConfiguration

    init {
        val langFile = File(plugin.dataFolder, "lang/messages_${language.lowercase()}.yml")
        messages = YamlConfiguration.loadConfiguration(langFile)
    }

    fun getMessage(category: String, key: String, placeholders: Map<String, String> = emptyMap()): String {
        val message = messages.getString("$category.$key") ?: "Message not found"
        return placeholders.entries.fold(message) { acc, entry ->
            acc.replace("{${entry.key}}", entry.value)
        }
    }
}
