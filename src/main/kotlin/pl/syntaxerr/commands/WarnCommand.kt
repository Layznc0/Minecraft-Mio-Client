package pl.syntaxerr.commands

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.configuration.PluginMeta
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.NotNull
import pl.syntaxerr.GuardianX
import pl.syntaxerr.helpers.Logger
import pl.syntaxerr.helpers.MessageHandler
import pl.syntaxerr.helpers.UUIDManager

@Suppress("UnstableApiUsage")
class WarnCommand(private val plugin: GuardianX, pluginMetas: PluginMeta) : BasicCommand {

    private var config = plugin.config
    private var debugMode = config.getBoolean("debug")
    private val logger = Logger(pluginMetas.name, pluginMetas.version, pluginMetas.name, debugMode)
    private val uuidManager = UUIDManager()
    private val messageHandler = MessageHandler(plugin)

    override fun execute(@NotNull stack: CommandSourceStack, @NotNull args: Array<String>) {
        if (args.isNotEmpty()) {
            if (stack.sender.hasPermission("GuardianX.ban")) {
                if (args.size < 2) {
                    stack.sender.sendRichMessage(messageHandler.getMessage("warn", "usage"))
                } else {
                    val player = args[0]
                    val uuid = uuidManager.getUUID(player)
                    if (uuid == null) {
                        stack.sender.sendRichMessage(messageHandler.getMessage("error", "player_not_found", mapOf("player" to player)))
                        return
                    }

                    val gtime = if (args.size > 2) args[1] else null
                    val reason = if (args.size > 2) args.slice(2 until args.size).joinToString(" ") else args[1]

                    val punishmentType = "BAN"
                    val start = System.currentTimeMillis().toString()
                    val end = if (gtime != null) (System.currentTimeMillis() + parseTime(gtime) * 1000).toString() else "nieokreślony"

                    plugin.databaseHandler.addPunishment(player, uuid, reason, stack.sender.name, punishmentType, start, end)
                    plugin.databaseHandler.addPunishmentHistory(player, uuid, reason, stack.sender.name, punishmentType, start, end)

                    stack.sender.sendRichMessage(messageHandler.getMessage("warn", "warn", mapOf("player" to player, "reason" to reason, "time" to formatTime(gtime))))
                    val message = Component.text(messageHandler.getMessage("warn", "warn", mapOf("player" to player, "reason" to reason, "time" to formatTime(gtime))))
                    plugin.server.broadcast(message)
                    logger.info("Player " + player + "(" + uuid + ") has warned for " + reason + " to time " + formatTime(gtime))
                }
            } else {
                stack.sender.sendRichMessage(messageHandler.getMessage("error", "no_permission"))
            }
        } else {
            stack.sender.sendRichMessage(messageHandler.getMessage("warn", "usage"))
        }
    }

    private fun parseTime(time: String): Long {
        val amount = time.substring(0, time.length - 1).toLong()
        val unit = time.last()

        return when (unit) {
            's' -> amount
            'm' -> amount * 60
            'h' -> amount * 60 * 60
            'd' -> amount * 60 * 60 * 24
            else -> 0
        }
    }

    private fun formatTime(time: String?): String {
        if (time == null) return "nieokreślony"
        val amount = time.substring(0, time.length - 1)
        val unit = time.last()

        return when (unit) {
            's' -> "$amount sekund"
            'm' -> "$amount minut"
            'h' -> "$amount godzin"
            'd' -> "$amount dni"
            else -> "nieokreślony"
        }
    }
}
