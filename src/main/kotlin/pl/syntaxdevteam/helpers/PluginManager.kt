package pl.syntaxdevteam.helpers

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.bukkit.plugin.java.JavaPlugin
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

data class PluginInfo(val name: String, val uuid: String, val prior: Int)

@Suppress("UnstableApiUsage")
class PluginManager(private val plugin: JavaPlugin) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    fun fetchPluginsFromExternalSource(url: String): List<PluginInfo> {
        return runBlocking {
            val response: HttpResponse = client.get(url)
            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()
                val parser = JSONParser()
                val jsonArray = parser.parse(responseBody) as JSONArray
                jsonArray.map { jsonObject ->
                    val json = jsonObject as JSONObject
                    PluginInfo(
                        name = json["name"] as String,
                        uuid = json["uuid"] as String,
                        prior = (json["prior"] as Long).toInt()
                    )
                }
            } else {
                emptyList()
            }
        }
    }

    fun fetchLoadedPlugins(): List<Pair<String, String>> {
        val plugins = mutableListOf<Pair<String, String>>()
        for (plugin in plugin.server.pluginManager.plugins) {
            if (plugin.pluginMeta.authors.contains("SyntaxDevTeam")) {
                plugins.add(Pair(plugin.name, plugin.pluginMeta.version))
            }
        }
        return plugins
    }

    fun getHighestPriorityPlugin(externalPlugins: List<PluginInfo>, loadedPlugins: List<Pair<String, String>>): String? {
        val matchedPlugins = externalPlugins.filter { externalPlugin ->
            loadedPlugins.any { it.first == externalPlugin.name }
        }
        return matchedPlugins.maxByOrNull { it.prior }?.name
    }
}
