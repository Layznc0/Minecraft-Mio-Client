package pl.syntaxerr.helpers

import com.google.gson.Gson
import org.bukkit.plugin.java.JavaPlugin
import java.net.URL

data class PluginInfo(val name: String, val uuid: String, val prior: Int)

@Suppress("UnstableApiUsage")
class PluginManager(private val plugin: JavaPlugin) {

    private val gson = Gson()

    // Metoda pobierająca listę pluginów z zewnętrznego źródła
    fun fetchPluginsFromExternalSource(url: String): List<PluginInfo> {
        val json = URL(url).readText()
        return gson.fromJson(json, Array<PluginInfo>::class.java).toList()
    }

    // Metoda pobierająca listę załadowanych pluginów na serwerze
    fun fetchLoadedPlugins(): List<String> {
        val plugins = mutableListOf<String>()
        for (plugin in plugin.server.pluginManager.plugins) {
            if (plugin.pluginMeta.authors.contains("SyntaxDevTeam")) {
                plugins.add(plugin.name)
            }
        }
        return plugins
    }

    // Metoda porównująca priorytety i zwracająca nazwę pluginu z najwyższym priorytetem
    fun getHighestPriorityPlugin(externalPlugins: List<PluginInfo>, loadedPlugins: List<String>): String? {
        val matchedPlugins = externalPlugins.filter { it.name in loadedPlugins }
        return matchedPlugins.maxByOrNull { it.prior }?.name
    }
}
