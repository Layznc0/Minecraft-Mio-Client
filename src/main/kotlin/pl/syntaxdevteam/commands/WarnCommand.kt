package pl.syntaxdevteam.commands

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.configuration.PluginMeta
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.jetbrains.annotations.NotNull
import pl.syntaxdevteam.PunisherX
import pl.syntaxdevteam.helpers.Logger
import pl.syntaxdevteam.helpers.MessageHandler
import pl.syntaxdevteam.helpers.TimeHandler
import pl.syntaxdevteam.helpers.UUIDManager

@Suppress("UnstableApiUsage")
class WarnCommand(private val plugin: PunisherX, pluginMetas: PluginMeta) : BasicCommand {

    private var config = plugin.config
    private var debugMode = config.getBoolean("debug")
    private val logger = Logger(pluginMetas, debugMode)
    private val uuidManager = UUIDManager(plugin)
    private val messageHandler = MessageHandler(plugin, pluginMetas)
    private val timeHandler = TimeHandler(plugin.config.getString("language") ?: "PL")

    override fun execute(@NotNull stack: CommandSourceStack, @NotNull args: Array<String>) {
        if (args.isNotEmpty()) {
            if (stack.sender.hasPermission("punisherx.warn")) {
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
                    val punishmentType = "WARN"
                    val start = System.currentTimeMillis()
                    val end: Long? = if (gtime != null) (System.currentTimeMillis() + timeHandler.parseTime(gtime) * 1000) else null

                    plugin.databaseHandler.addPunishment(player, uuid, reason, stack.sender.name, punishmentType, start, end ?: -1)
                    plugin.databaseHandler.addPunishmentHistory(player, uuid, reason, stack.sender.name, punishmentType, start, end ?: -1)

                    val warnCount = plugin.databaseHandler.getActiveWarnCount(uuid)
                    stack.sender.sendRichMessage(messageHandler.getMessage("warn", "warn", mapOf("player" to player, "reason" to reason, "time" to timeHandler.formatTime(gtime), "warn_no" to warnCount.toString())))
                    val targetPlayer = Bukkit.getPlayer(player)
                    val warnMessage = messageHandler.getMessage("warn", "warn_message", mapOf("reason" to reason, "time" to timeHandler.formatTime(gtime), "warn_no" to warnCount.toString()))
                    val formattedMessage = MiniMessage.miniMessage().deserialize(warnMessage)
                    targetPlayer?.sendMessage(formattedMessage)
                    val broadcastMessage = MiniMessage.miniMessage().deserialize(messageHandler.getMessage("warn", "broadcast", mapOf("player" to player, "reason" to reason, "time" to timeHandler.formatTime(gtime), "warn_no" to warnCount.toString())))
                    plugin.server.broadcast(broadcastMessage)
                    //logger.log(messageHandler.getLogMessage("warn", "broadcast", mapOf("player" to player, "reason" to reason, "time" to timeHandler.formatTime(gtime), "warn_no" to warnCount.toString())))
                    executeWarnAction(player, warnCount)
                }
            } else {
                stack.sender.sendRichMessage(messageHandler.getMessage("error", "no_permission"))
            }
        } else {
            stack.sender.sendRichMessage(messageHandler.getMessage("warn", "usage"))
        }
    }

    override fun suggest(@NotNull stack: CommandSourceStack, @NotNull args: Array<String>): List<String> {
        return when (args.size) {
            1 -> plugin.server.onlinePlayers.map { it.name }
            2 -> generateTimeSuggestions()
            3 -> messageHandler.getReasons("warn", "reasons")
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

    private fun executeWarnAction(player: String, warnCount: Int) {
        val warnActions = plugin.config.getConfigurationSection("WarnActions")?.getKeys(false)
        warnActions?.forEach { key ->
            val warnThreshold = key.toIntOrNull()
            if (warnThreshold != null && warnCount == warnThreshold) {
                val command = plugin.config.getString("WarnActions.$key")
                if (command != null) {
                    val formattedCommand = command.replace("{player}", player).replace("{warn_no}", warnCount.toString())
                    plugin.server.dispatchCommand(plugin.server.consoleSender, formattedCommand)
                    plugin.logger.debug("Executed command for $player: $formattedCommand")
                }
            }
        }
    }

}
