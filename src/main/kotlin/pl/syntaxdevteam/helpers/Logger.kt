package pl.syntaxdevteam.helpers

import io.papermc.paper.plugin.configuration.PluginMeta
import org.bukkit.Bukkit
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer


@Suppress("UnstableApiUsage")
class Logger(pluginMetas: PluginMeta, private val debugMode: Boolean) {
    private val plName = pluginMetas.name

    private fun clear(s: String?) {
        Bukkit.getConsoleSender().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(s!!))
    }

    fun success(s: String) {
        Bukkit.getConsoleSender().sendMessage(Component.text("[$plName] ").color(NamedTextColor.GREEN).append(Component.text(s).color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD)))
    }

    fun info(s: String) {
        Bukkit.getConsoleSender().sendMessage(Component.text("[$plName] $s"))
    }

    fun warning(s: String) {
        Bukkit.getConsoleSender().sendMessage(Component.text("[$plName] ").color(NamedTextColor.GOLD).append(Component.text(s).color(NamedTextColor.GOLD)))
    }

    fun err(s: String) {
        Bukkit.getConsoleSender().sendMessage(Component.text("[$plName] ").color(NamedTextColor.RED).append(Component.text(s).color(NamedTextColor.RED)))
    }

    fun severe(s: String) {
        Bukkit.getConsoleSender().sendMessage(Component.text("[$plName] ").color(NamedTextColor.RED).append(Component.text(s).color(NamedTextColor.RED).decorate(TextDecoration.BOLD)))
    }

    fun log(s: String) {
        Bukkit.getConsoleSender().sendMessage(Component.text("[$plName] $s"))
    }

    fun debug(s: String) {
        if (debugMode) {
            Bukkit.getConsoleSender().sendMessage(Component.text("[$plName] [DEBUG] ").color(NamedTextColor.YELLOW).append(Component.text(s).color(NamedTextColor.YELLOW).decorate(
                TextDecoration.BOLD)))
        }
    }

    fun pluginStart(pluginsByPriority: List<String>) {
        clear("")
        clear("&9-------------------------------------------------------------------------------")
        clear("")
        clear("&9    _____             _             _____          _______                   ")
        clear("&9   / ____|           | |           |  __ \\        |__   __|                  ")
        clear("&9  | (___  _   _ _ __ | |_ __ ___  _| |  | | _____   _| | ___  __ _ _ __ ___  ")
        clear("&9   \\___ \\| | | | '_ \\| __/ _` \\ \\/ / |  | |/ _ \\ \\ / / |/ _ \\/ _` | '_ ` _ \\ ")
        clear("&9   ____) | |_| | | | | || (_| |>  <| |__| |  __/\\ V /| |  __/ (_| | | | | | |")
        clear("&9  |_____/ \\__, |_| |_|\\__\\__,_/_/\\_\\_____/ \\___| \\_/ |_|\\___|\\__,_|_| |_| |_|")
        clear("&9           __/ |                                                             ")
        clear("&9          |___/                                                              ")
        clear("&9                                                                             ")
        clear("&9    ... is proud to present and enable:")
        clear("&9                   &f * &f&l${plName}")
        for (plugin in pluginsByPriority) {
            clear("&9                   &f * $plugin")
        }
        clear("&9                 utilizing all the optimizations of your server engine!         ")
        clear("")
        clear("&a    Join our Discord! &9&lhttps://discord.gg/Zk6mxv7eMh")
        clear("")
        clear("&9-------------------------------------------------------------------------------")
        clear("")
    }
}