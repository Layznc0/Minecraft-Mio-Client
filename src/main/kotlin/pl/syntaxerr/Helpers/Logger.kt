package pl.syntaxerr.helpers

import io.papermc.paper.plugin.configuration.PluginMeta
import org.bukkit.Bukkit
import org.bukkit.ChatColor

@Suppress("UnstableApiUsage")
class Logger(pluginMetas: PluginMeta, private val debugMode: Boolean) {
    private val plName = pluginMetas.name

    private fun clear(s: String?) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', s!!))
    }

    fun success(s: String) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[$plName] &a&l$s&r"))
    }

    fun info(s: String) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[$plName] $s"))
    }

    fun warning(s: String) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[$plName] &6$s&r"))
    }

    fun err(s: String) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[$plName] &c$s&r"))
    }

    fun severe(s: String) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[$plName] &c&l$s&r"))
    }

    fun log(s: String) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[$plName] $s"))
    }

    fun debug(s: String) {
        if (debugMode) {
            Bukkit.getConsoleSender().sendMessage(
                ChatColor.translateAlternateColorCodes(
                    '&',
                    "[$plName] [DEBUG] &e&l$s"
                )
            )
        }
    }

    fun pluginStart(registeredPlugins: List<Pair<String, Int>>) {
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
        clear("&9    ... is proud to present and enabled:")
        clear("&9             &f * &f&l$plName")
        registeredPlugins.forEach { (name, priority) ->
            if (name != plName) {
                clear("&9             &f * $name")
            }
        }
        clear("&9                   running on Paper and utilizing its optimizations!         ")
        clear("")
        clear("&a    Join our Discord! &9&lhttps://discord.gg/Zk6mxv7eMh")
        clear("")
    }
}
