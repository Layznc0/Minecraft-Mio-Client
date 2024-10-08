package pl.syntaxdevteam.helpers

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.papermc.paper.plugin.configuration.PluginMeta
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

@Suppress("UnstableApiUsage")
class UpdateChecker(private val pluginMeta: PluginMeta, private val logger: Logger, private val config: FileConfiguration) {

    private val hangarApiUrl = "https://hangar.papermc.io/api/v1/projects/${pluginMeta.name}/versions"
    private val pluginUrl = "https://hangar.papermc.io/SyntaxDevTeam/${pluginMeta.name}"

    fun checkForUpdates() {
        if (!config.getBoolean("checkForUpdates", true)) {
            logger.debug("Update check is disabled in the config.")
            return
        }

        runBlocking {
            val client = HttpClient()
            val response: HttpResponse = client.get(hangarApiUrl)
            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()
                val parser = JSONParser()
                val jsonObject = parser.parse(responseBody) as JSONObject
                val versions = jsonObject["result"] as JSONArray
                val latestVersion = versions.firstOrNull() as? JSONObject
                if (latestVersion != null && isNewerVersion(latestVersion["name"] as String, pluginMeta.version)) {
                    notifyUpdate(latestVersion)
                }
            } else {
                logger.warning("Failed to check for updates: ${response.status}")
            }
            client.close()
        }
    }

    private fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
        val latestParts = latestVersion.split("-", limit = 2)
        val currentParts = currentVersion.split("-", limit = 2)

        val latestBase = latestParts[0].split(".")
        val currentBase = currentParts[0].split(".")

        for (i in latestBase.indices) {
            val latestPart = latestBase.getOrNull(i)?.toIntOrNull() ?: 0
            val currentPart = currentBase.getOrNull(i)?.toIntOrNull() ?: 0

            if (latestPart > currentPart) {
                return true
            } else if (latestPart < currentPart) {
                return false
            }
        }

        if (latestParts.size > 1 && currentParts.size > 1) {
            return latestParts[1] > currentParts[1]
        }

        return latestParts.size > currentParts.size
    }

    private fun notifyUpdate(version: JSONObject) {
        val versionName = version["name"] as String
        val channel = (version["channel"] as JSONObject)["name"] as String
        val message = when (channel) {
            "Release" -> "<green>New release version <bold>$versionName</bold> is available on <click:open_url:'$pluginUrl'>Hangar</click>!"
            "Snapshot" -> "<yellow>New snapshot version <bold>$versionName</bold> is available on <click:open_url:'$pluginUrl'>Hangar</click>!"
            else -> "<blue>New version <bold>$versionName</bold> is available on <click:open_url:'$pluginUrl'>Hangar</click>!"
        }
        val component = MiniMessage.miniMessage().deserialize(message)
        logger.success("New release version $versionName is available on $pluginUrl")
        notifyAdmins(component)
    }

    private fun notifyAdmins(message: Component) {
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("PunisherX.update.notify")) {
                player.sendMessage(message)
            }
        }
    }
}
