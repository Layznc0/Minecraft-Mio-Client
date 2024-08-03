package pl.syntaxerr.helpers

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import pl.syntaxerr.GuardianX
import net.kyori.adventure.text.Component
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class PunishmentChecker(private val plugin: GuardianX) : Listener {

    @EventHandler
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        plugin.logger.info("Sprawdzanie kary dla gracza: ${event.name}")

        val uuid = event.uniqueId.toString()
        val punishment = plugin.databaseHandler.getPunishment(uuid)
        if (punishment != null) {
            if (plugin.punishmentManager.isPunishmentActive(punishment)) {
                if (punishment.type == "BAN") {
                    val endTime = punishment.end
                    val duration = if (endTime == -1L) "permanent" else plugin.timeHandler.formatTime(((endTime - System.currentTimeMillis()) / 1000).toString())
                    val reason = punishment.reason
                    val kickMessages = plugin.messageHandler.getComplexMessage("ban", "kick_message", mapOf("reason" to reason, "time" to duration))
                    val kickMessage = Component.text()
                    kickMessages.forEach { line ->
                        kickMessage.append(line)
                        kickMessage.append(Component.newline())
                    }
                    event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_BANNED
                    event.kickMessage(kickMessage.build())
                    plugin.logger.info("Gracz ${event.name} został wyrzucony z powodu: $reason")
                }
            } else {
                plugin.databaseHandler.removePunishment("", uuid, punishment.type)
                plugin.logger.info("Kara dla UUID: $uuid wygasła i została usunięta")
            }
        }
    }
}
