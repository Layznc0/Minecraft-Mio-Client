package pl.syntaxerr.helpers

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import pl.syntaxerr.GuardianX
import net.kyori.adventure.text.Component
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class PunishmentChecker(private val plugin: GuardianX) : Listener {

    @EventHandler
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val uuid = event.uniqueId.toString()

        val punishment = plugin.databaseHandler.getPunishment(uuid)
        if (punishment != null) {
            val endTime = punishment.end
            val duration = if (endTime == -1L) "permanent" else plugin.timeHandler.formatTime(((endTime - System.currentTimeMillis()) / 1000).toString())
            if (endTime > System.currentTimeMillis() || endTime == -1L) {
                val reason = punishment.reason
                val kickMessages = plugin.messageHandler.getComplexMessage("ban", "kick_message", mapOf("reason" to reason, "time" to duration))
                val kickMessage = Component.text()
                kickMessages.forEach { line ->
                    kickMessage.append(line)
                    kickMessage.append(Component.newline())
                }
                event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_BANNED
                event.kickMessage(kickMessage.build())
            }
        }
    }
}
