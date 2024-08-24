package pl.syntaxdevteam.basic

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import pl.syntaxdevteam.PunisherX
import java.util.*

class PunishmentChecker(private val plugin: PunisherX) : Listener {

    @EventHandler
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        plugin.logger.debug("Checking punishment for player: ${event.name}")

        val uuid = event.uniqueId.toString()
        val ip = event.address.hostAddress

        val punishment = plugin.databaseHandler.getPunishment(uuid) ?: plugin.databaseHandler.getPunishmentByIP(ip)
        if (punishment != null) {
            if (plugin.punishmentManager.isPunishmentActive(punishment)) {
                if (punishment.type == "BAN" || punishment.type == "BANIP") {
                    val endTime = punishment.end
                    val remainingTime = (endTime - System.currentTimeMillis()) / 1000
                    val duration = if (endTime == -1L) "permanent" else plugin.timeHandler.formatTime(remainingTime.toString())
                    val reason = punishment.reason
                    val kickMessages = when (punishment.type) {
                        "BAN" -> plugin.messageHandler.getComplexMessage("ban", "kick_message", mapOf("reason" to reason, "time" to duration))
                        "BANIP" -> plugin.messageHandler.getComplexMessage("banip", "kick_message", mapOf("reason" to reason, "time" to duration))
                        else -> emptyList()
                    }
                    val kickMessage = Component.text()
                    kickMessages.forEach { line ->
                        kickMessage.append(line)
                        kickMessage.append(Component.newline())
                    }
                    event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_BANNED
                    event.kickMessage(kickMessage.build())
                    plugin.logger.debug("Player ${event.name} was kicked for: $reason")
                }
            } else {
                plugin.databaseHandler.removePunishment(uuid, punishment.type)
                plugin.logger.debug("Punishment for UUID: $uuid has expired and has been removed")
            }
        }
    }


    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player
        val uuid = player.uniqueId.toString()

        val punishment = plugin.databaseHandler.getPunishment(uuid)
        if (punishment != null) {
            if(punishment.type == "MUTE" && plugin.punishmentManager.isPunishmentActive(punishment)) {
                val endTime = punishment.end
                val remainingTime = (endTime - System.currentTimeMillis()) / 1000
                val duration = if (endTime == -1L) "permanent" else plugin.timeHandler.formatTime(remainingTime.toString())
                val reason = punishment.reason
                event.isCancelled = true
                val muteMessage = plugin.messageHandler.getMessage("mute", "mute_info_message", mapOf("reason" to reason, "time" to duration))
                val formattedMessage = MiniMessage.miniMessage().deserialize(muteMessage)
                player.sendMessage(formattedMessage)

            } else {
                plugin.databaseHandler.removePunishment(uuid, punishment.type)
                plugin.logger.debug("Punishment for UUID: $uuid has expired and has been removed")
            }
        }
    }

    @EventHandler
    fun onPlayerCommand(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        val uuid = player.uniqueId.toString()
        val command = event.message.split(" ")[0].lowercase(Locale.getDefault()).removePrefix("/")

        if (plugin.config.getBoolean("mute_pm")) {
            val muteCommands = plugin.config.getStringList("mute_cmd")
            if (muteCommands.contains(command)) {
                val punishment = plugin.databaseHandler.getPunishment(uuid)
                if (punishment != null) {
                    if(punishment.type == "MUTE" && plugin.punishmentManager.isPunishmentActive(punishment)) {
                        val endTime = punishment.end
                        val remainingTime = (endTime - System.currentTimeMillis()) / 1000
                        val duration = if (endTime == -1L) "permanent" else plugin.timeHandler.formatTime(remainingTime.toString())
                        val reason = punishment.reason
                        event.isCancelled = true
                        val muteMessage = plugin.messageHandler.getMessage("mute", "mute_message", mapOf("reason" to reason, "time" to duration))
                        val formattedMessage = MiniMessage.miniMessage().deserialize(muteMessage)
                        player.sendMessage(formattedMessage)

                    } else {
                        plugin.databaseHandler.removePunishment(uuid, punishment.type)
                        plugin.logger.debug("Punishment for UUID: $uuid has expired and has been removed")
                    }
                }
            }
        }
    }
}
