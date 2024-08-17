package pl.syntaxdevteam.players

import pl.syntaxdevteam.PunisherX
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.io.File
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.text.Charsets.UTF_8

class PlayerIPManager(private val plugin: PunisherX) : Listener {

    private val cacheFile = File(plugin.dataFolder, "cache")
    private val secretKey: Key = generateKey()

    init {
        if (!cacheFile.exists()) {
            cacheFile.parentFile.mkdirs()
            cacheFile.createNewFile()
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val playerName = player.name
        val playerUUID = player.uniqueId.toString()
        val playerIP = player.address?.address?.hostAddress

        if (playerIP != null) {
            if (!isPlayerInfoExists(playerName, playerUUID, playerIP)) {
                savePlayerInfo(playerName, playerUUID, playerIP)
                plugin.logger.debug("Zapisano informacje o graczu -> playerName: $playerName, playerUUID: $playerUUID, playerIP: $playerIP")
            } else {
                plugin.logger.debug("Informacje o graczu już istnieją -> playerName: $playerName, playerUUID: $playerUUID, playerIP: $playerIP")
            }
        }
    }

    private fun isPlayerInfoExists(playerName: String, playerUUID: String, playerIP: String): Boolean {
        return cacheFile.readLines().any { line ->
            val decryptedData = decrypt(line)
            val (name, uuid, ip) = decryptedData.split(",")
            name == playerName && uuid == playerUUID && ip == playerIP
        }
    }

    private fun savePlayerInfo(playerName: String, playerUUID: String, playerIP: String) {
        val encryptedData = encrypt("$playerName,$playerUUID,$playerIP")
        cacheFile.appendText("$encryptedData\n")
        plugin.logger.debug("Zapisano zaszyfrowano dane -> playerName: $playerName, playerUUID: $playerUUID, playerIP: $playerIP")
    }

    fun getPlayerIPByName(playerName: String): String? {
        plugin.logger.debug("Pobieranie IP dla gracza: $playerName")
        val ip = searchCache { it[0] == playerName }
        plugin.logger.debug("Znalezione IP dla gracza $playerName: $ip")
        return ip
    }


    fun getPlayerIPByUUID(playerUUID: String): String? {
        return searchCache { it[1] == playerUUID }
    }

    fun getPlayerNamesByIP(playerIP: String): List<String> {
        return searchCacheMultiple { it[2] == playerIP }
    }

    private fun searchCache(predicate: (List<String>) -> Boolean): String? {
        plugin.logger.debug("Przeszukiwanie cache")
        val lines = cacheFile.readLines()
        plugin.logger.debug("Liczba linii w cache: ${lines.size}")
        for (line in lines) {
            val decryptedLine = decrypt(line)
            plugin.logger.debug("Odszyfrowana linia: $decryptedLine")
            val parts = decryptedLine.split(",")
            plugin.logger.debug("Podzielone części: $parts")
            if (predicate(parts.map { it.lowercase() })) {
                plugin.logger.debug("Znaleziono dopasowanie: ${parts[2]}")
                return parts[2]
            }
        }
        plugin.logger.debug("Nie znaleziono dopasowania w cache")
        return null
    }




    private fun searchCacheMultiple(predicate: (List<String>) -> Boolean): List<String> {
        return cacheFile.readLines().map { decrypt(it).split(",") }.filter(predicate).map { it[0] }
    }

    private fun generateKey(): Key {
        val keyString = "M424PmX84WlDDXLb" // Stały klucz szyfrowania (16 znaków dla AES-128)
        return SecretKeySpec(keyString.toByteArray(UTF_8), "AES")
    }

    private fun encrypt(data: String): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher.doFinal(data.toByteArray(UTF_8)).joinToString("") { "%02x".format(it) }
    }

    private fun decrypt(data: String): String {
        val bytes = data.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return String(cipher.doFinal(bytes), UTF_8)
    }
}
