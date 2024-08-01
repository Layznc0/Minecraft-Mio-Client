package pl.syntaxerr.helpers

import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

class UUIDManager {
    private val activeUUIDs: MutableMap<String, String> = HashMap()

    fun getUUID(playerName: String): String? {
        val player: Player? = Bukkit.getPlayer(playerName)
        if (player != null) {
            return player.uniqueId.toString()
        }

        val offlinePlayer: OfflinePlayer = Bukkit.getOfflinePlayer(playerName)
        if (offlinePlayer.hasPlayedBefore()) {
            return offlinePlayer.uniqueId.toString()
        }

        val uuid = fetchUUIDFromAPI(playerName)
        return uuid ?: generateOfflineUUID(playerName)
    }

    private fun fetchUUIDFromAPI(playerName: String): String? {
        val url = "https://api.mojang.com/users/profiles/minecraft/$playerName"
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            println("API Response Code: ${connection.responseCode}")

            if (connection.responseCode == 200) {
                val reader = InputStreamReader(connection.inputStream)
                val response = reader.readText()
                reader.close()

                val uuid = parseUUIDFromResponse(response)
                if (uuid != null) {
                    activeUUIDs[playerName.lowercase(Locale.getDefault())] = uuid
                }
                uuid
            } else {
                println("Failed to fetch UUID from API. Response code: ${connection.responseCode}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseUUIDFromResponse(response: String): String? {
        return try {
            val parser = JSONParser()
            val jsonObject = parser.parse(response) as JSONObject
            val rawUUID = jsonObject["id"] as String
            println("Raw UUID from API: $rawUUID")
            rawUUID.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)".toRegex(),
                "$1-$2-$3-$4-$5"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun generateOfflineUUID(playerName: String): String {
        val offlineUUID = UUID.nameUUIDFromBytes("OfflinePlayer:$playerName".toByteArray()).toString()
        println("Generated offline UUID for $playerName: $offlineUUID")
        return offlineUUID
    }
}
