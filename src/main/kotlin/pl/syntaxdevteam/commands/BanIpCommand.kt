package pl.syntaxdevteam.commands

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.configuration.PluginMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.jetbrains.annotations.NotNull
import pl.syntaxdevteam.PunisherX
import pl.syntaxdevteam.helpers.Logger
import pl.syntaxdevteam.helpers.MessageHandler
import pl.syntaxdevteam.helpers.TimeHandler
import pl.syntaxdevteam.helpers.UUIDManager

@Suppress("UnstableApiUsage")
class BanIpCommand(private val plugin: PunisherX, pluginMetas: PluginMeta) : BasicCommand {

    private var config = plugin.config
    private var debugMode = config.getBoolean("debug")
    private val logger = Logger(pluginMetas, debugMode)
    private val messageHandler = MessageHandler(plugin, pluginMetas)
    private val timeHandler = TimeHandler(plugin.config.getString("language") ?: "PL")

    override fun execute(@NotNull stack: CommandSourceStack, @NotNull args: Array<String>) {
        if (args.isNotEmpty()) {
            if (stack.sender.hasPermission("punisherx.banip")) {
                if (args.size < 2) {
                    stack.sender.sendRichMessage(messageHandler.getMessage("banip", "usage"))
                } else {
                    val playerOrIpOrUUID = args[0]
                    val playerIP = when {
                        playerOrIpOrUUID.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")) -> playerOrIpOrUUID
                        playerOrIpOrUUID.matches(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) -> plugin.playerIPManager.getPlayerIPByUUID(playerOrIpOrUUID)
                        else -> plugin.playerIPManager.getPlayerIPByName(playerOrIpOrUUID.lowercase())
                    }

                    if (playerIP == null) {
                        stack.sender.sendRichMessage(messageHandler.getMessage("banip", "not_found"))
                        return
                    }

                    val gtime = if (args.size > 2) args[1] else null
                    val reason = if (args.size > 2) args.slice(2 until args.size).joinToString(" ") else args[1]
                    val punishmentType = "BANIP"
                    val start = System.currentTimeMillis()
                    val end: Long? = if (gtime != null) (System.currentTimeMillis() + timeHandler.parseTime(gtime) * 1000) else null
                    val endText = if (end == null) timeHandler.formatTime(null) else timeHandler.formatTime((end / 1000).toString())

                    plugin.databaseHandler.addPunishment(playerOrIpOrUUID, playerIP, reason, stack.sender.name, punishmentType, start, end ?: -1)
                    plugin.databaseHandler.addPunishmentHistory(playerOrIpOrUUID, playerIP, reason, stack.sender.name, punishmentType, start, end ?: -1)

                    val targetPlayer = Bukkit.getPlayer(playerOrIpOrUUID)
                    if (targetPlayer != null) {
                        val kickMessages = messageHandler.getComplexMessage("banip", "kick_message", mapOf("reason" to reason, "time" to timeHandler.formatTime(gtime)))
                        val kickMessage = Component.text()
                        kickMessages.forEach { line ->
                            kickMessage.append(line)
                            kickMessage.append(Component.newline())
                        }
                        targetPlayer.kick(kickMessage.build())
                    }

                    stack.sender.sendRichMessage(messageHandler.getMessage("banip", "ban", mapOf("player" to playerOrIpOrUUID, "reason" to reason, "time" to timeHandler.formatTime(gtime))))
                    val message = MiniMessage.miniMessage().deserialize(messageHandler.getMessage("banip", "ban", mapOf("player" to playerOrIpOrUUID, "reason" to reason, "time" to timeHandler.formatTime(gtime))))
                    plugin.server.broadcast(message)
                    logger.info("IP: " + playerIP + " (" + playerOrIpOrUUID + ") has been banned for " + reason + " until " + timeHandler.formatTime(gtime))
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
            2 -> generateTimeSuggestions()
            3 -> messageHandler.getReasons("banip", "reasons")
            else -> emptyList()
        }
    }

    private fun generateTimeSuggestions(): List<String> {
        val units = listOf("s", "m", "h", "d")
        val suggestions = mutableListOf<String>()
        for (i in 1..999) {
            for (unit in units) {
                suggestions.add("$i$unit")
            }
        }
        return suggestions
    }
}