package pl.syntaxerr.commands

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.configuration.PluginMeta
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.jetbrains.annotations.NotNull
import pl.syntaxerr.PunisherX
import pl.syntaxerr.helpers.Logger
import pl.syntaxerr.helpers.MessageHandler
import pl.syntaxerr.helpers.TimeHandler
import pl.syntaxerr.helpers.IpCache

@Suppress("UnstableApiUsage")
class BanIpCommand(private val plugin: PunisherX, pluginMetas: PluginMeta, private val ipCache: IpCache) : BasicCommand {

    private var config = plugin.config
    private var debugMode = config.getBoolean("debug")
    private val logger = Logger(pluginMetas, debugMode)
    private val messageHandler = MessageHandler(plugin, pluginMetas)
    private val timeHandler = TimeHandler(plugin.config.getString("language") ?: "PL")

    override fun execute(@NotNull stack: CommandSourceStack, @NotNull args: Array<String>) {
        if (args.isNotEmpty()) {
            if (stack.sender.hasPermission("punisherx.banip")) {
                if (args.size < 2) {
                    stack.sender.sendRichMessage(messageHandler.getMessage("ban", "usage"))
                } else {
                    val player = args[0]
                    val ip = ipCache.getIp(player) // Pobieranie IP z cache'u
                    if (ip == null) {
                        stack.sender.sendRichMessage(messageHandler.getMessage("error", "player_ip_not_found", mapOf("player" to player)))
                        return
                    }
                    val gtime = if (args.size > 2) args[1] else null
                    val reason = if (args.size > 2) args.slice(2 until args.size).joinToString(" ") else args[1]
                    val punishmentType = "BANIP"
                    val start = System.currentTimeMillis().toString()
                    val end = if (gtime != null) (System.currentTimeMillis() + timeHandler.parseTime(gtime) * 1000).toString() else if (plugin.config.getString("language") == "PL") "nieokreślony" else "undefined"

                    plugin.databaseHandler.addPunishment(player, ip, reason, stack.sender.name, punishmentType, start, end)
                    plugin.databaseHandler.addPunishmentHistory(player, ip, reason, stack.sender.name, punishmentType, start, end)

                    val targetPlayer = Bukkit.getPlayer(player)
                    if (targetPlayer != null) {
                        val kickMessages = messageHandler.getComplexMessage("banip", "kick_message", mapOf("reason" to reason, "time" to timeHandler.formatTime(gtime)))
                        val kickMessage = Component.text()
                        kickMessages.forEach { line ->
                            kickMessage.append(line)
                            kickMessage.append(Component.newline())
                        }
                        targetPlayer.kick(kickMessage.build())
                    }

                    stack.sender.sendRichMessage(messageHandler.getMessage("banip", "ban", mapOf("player" to player, "reason" to reason, "time" to timeHandler.formatTime(gtime))))
                    val message = Component.text(messageHandler.getMessage("banip", "ban", mapOf("player" to player, "reason" to reason, "time" to timeHandler.formatTime(gtime))))
                    plugin.server.broadcast(message)
                    logger.info("IP: " + ip + " (" + player + ") has banned for " + reason + " to time " + timeHandler.formatTime(gtime))
                }
            } else {
                stack.sender.sendRichMessage(messageHandler.getMessage("error", "no_permission"))
            }
        } else {
            stack.sender.sendRichMessage(messageHandler.getMessage("banip", "usage"))
        }
    }

    override fun suggest(@NotNull stack: CommandSourceStack, @NotNull args: Array<String>): List<String> {
        return when (args.size) {
            1 -> plugin.server.onlinePlayers.map { it.name }
            2 -> listOf("1s", "1m", "1h", "1d")
            3 -> messageHandler.getReasons("banip", "reasons")
            else -> emptyList()
        }
    }
}
