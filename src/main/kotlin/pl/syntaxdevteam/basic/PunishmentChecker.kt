package pl.syntaxdevteam.basic

import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import pl.syntaxdevteam.PunisherX

class PunishmentChecker(private val plugin: PunisherX) : Listener {

    @EventHandler
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        plugin.logger.debug("Sprawdzanie kary dla gracza: ${event.name}")

        val uuid = event.uniqueId.toString()
        val ip = event.address.hostAddress

        val punishment = plugin.databaseHandler.getPunishment(uuid) ?: plugin.databaseHandler.getPunishmentByIP(ip)
        if (punishment != null) {
            if (plugin.punishmentManager.isPunishmentActive(punishment)) {
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
                plugin.logger.debug("Gracz ${event.name} został wyrzucony z powodu: $reason")
            } else {
                plugin.databaseHandler.removePunishment(uuid, punishment.type)
                plugin.logger.debug("Kara dla UUID: $uuid wygasła i została usunięta")
            }
        }
    }
}
