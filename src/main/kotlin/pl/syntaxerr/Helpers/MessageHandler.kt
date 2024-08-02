package pl.syntaxerr.helpers

import io.papermc.paper.plugin.configuration.PluginMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

@Suppress("UnstableApiUsage")
class MessageHandler(private val plugin: JavaPlugin, pluginMetas: PluginMeta) {
    private val language = plugin.config.getString("language") ?: "PL"
    private var messages: FileConfiguration
    private var config = plugin.config
    private var debugMode = config.getBoolean("debug")
    private val logger = Logger(pluginMetas.name, pluginMetas.version, pluginMetas.name, debugMode)

    init {
        copyDefaultMessages()
        messages = loadMessages()
    }

    private fun copyDefaultMessages() {
        val messageFile = File(plugin.dataFolder, "lang/messages_${language.lowercase()}.yml")
        if (!messageFile.exists()) {
            messageFile.parentFile.mkdirs()
            plugin.saveResource("lang/messages_${language.lowercase()}.yml", false)
        }
    }

    private fun loadMessages(): FileConfiguration {
        val langFile = File(plugin.dataFolder, "lang/messages_${language.lowercase()}.yml")
        return YamlConfiguration.loadConfiguration(langFile)
    }

    fun reloadMessages() {
        messages = loadMessages()
    }

    fun getMessage(category: String, key: String, placeholders: Map<String, String> = emptyMap()): String {
        val message = messages.getString("$category.$key") ?: run {
            logger.err("There was an error loading the message $key from category $category")
            "Message not found. Check console..."
        }
        return placeholders.entries.fold(message) { acc, entry ->
            acc.replace("{${entry.key}}", entry.value)
        }
    }

    fun getComplexMessage(category: String, key: String, placeholders: Map<String, String> = emptyMap()): List<Component> {
        val messageList = messages.getStringList("$category.$key")
        if (messageList.isEmpty()) {
            logger.err("There was an error loading the message list $key from category $category")
            return listOf(Component.text("Message list not found. Check console..."))
        }
        return messageList.map { message ->
            val formattedMessage = placeholders.entries.fold(message) { acc, entry ->
                acc.replace("{${entry.key}}", entry.value)
            }
            if (formattedMessage.contains("<")) {
                MiniMessage.miniMessage().deserialize(formattedMessage)
            } else {
                LegacyComponentSerializer.legacyAmpersand().deserialize(formattedMessage)
            }
        }
    }
}
