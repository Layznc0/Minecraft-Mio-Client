package pl.syntaxdevteam.commands

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.configuration.PluginMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.jetbrains.annotations.NotNull
import pl.syntaxdevteam.PunisherX
import pl.syntaxdevteam.helpers.MessageHandler
import pl.syntaxdevteam.helpers.TimeHandler
import pl.syntaxdevteam.helpers.UUIDManager

@Suppress("UnstableApiUsage")
class CheckCommand(private val plugin: PunisherX, pluginMetas: PluginMeta) : BasicCommand {

    private val uuidManager = UUIDManager(plugin)
    private val messageHandler = MessageHandler(plugin, pluginMetas)
    private val timeHandler = TimeHandler(plugin.config.getString("language") ?: "PL")

    override fun execute(@NotNull stack: CommandSourceStack, @NotNull args: Array<String>) {
        if (args.isNotEmpty()) {
            if (stack.sender.hasPermission("punisherx.check")) {
                if (args.size < 2) {
                    stack.sender.sendRichMessage(messageHandler.getMessage("check", "usage"))
                } else {
                    val player = args[0]
                    val type = args[1]
                    val uuid = uuidManager.getUUID(player)
                    if (uuid == null) {
                        stack.sender.sendRichMessage(messageHandler.getMessage("error", "player_not_found", mapOf("player" to player)))
                        return
                    }

                    val punishments = plugin.databaseHandler.getPunishments(uuid)
                    val filteredPunishments = when (type.lowercase()) {
                        "all" -> punishments
                        "ban" -> punishments.filter { it.type == "BAN" || it.type == "BANIP" }
                        "mute" -> punishments.filter { it.type == "MUTE" }
                        "warn" -> punishments.filter { it.type == "WARN" }
                        else -> {
                            stack.sender.sendRichMessage(messageHandler.getMessage("check", "invalid_type"))
                            return
                        }
                    }

                    if (filteredPunishments.isEmpty()) {
                        stack.sender.sendRichMessage(messageHandler.getMessage("check", "no_punishments", mapOf("player" to player)))
                    } else {
                        val topHeader = Component.text("----------------------------------").color(NamedTextColor.BLUE)
                        val header = Component.text("    Aktywne kary dla $player:").color(NamedTextColor.BLUE)
                        val tableHeader = Component.text("|   Typ  |  Powód  |  Czas").color(NamedTextColor.GOLD)
                        val br = Component.text(" ").color(NamedTextColor.WHITE)
                        val hr = Component.text("|").color(NamedTextColor.WHITE)
                        stack.sender.sendMessage(br)
                        stack.sender.sendMessage(header)
                        stack.sender.sendMessage(topHeader)
                        stack.sender.sendMessage(tableHeader)
                        stack.sender.sendMessage(hr)

                        filteredPunishments.forEach { punishment ->
                            val endTime = punishment.end
                            val remainingTime = (endTime - System.currentTimeMillis()) / 1000
                            val duration = if (endTime == -1L) "permanent" else timeHandler.formatTime(remainingTime.toString())
                            val reason = punishment.reason
                            val row = Component.text("|   ${punishment.type} | $reason | $duration").color(NamedTextColor.WHITE)
                            stack.sender.sendMessage(row)
                        }
                        stack.sender.sendMessage(topHeader)
                    }
                }
            } else {
                stack.sender.sendRichMessage(messageHandler.getMessage("error", "no_permission"))
            }
        } else {
            stack.sender.sendRichMessage(messageHandler.getMessage("check", "usage"))
        }
    }

    override fun suggest(@NotNull stack: CommandSourceStack, @NotNull args: Array<String>): List<String> {
        return when (args.size) {
            1 -> plugin.server.onlinePlayers.map { it.name }
            2 -> listOf("all", "warn", "mute", "ban")
            else -> emptyList()
        }
    }
}
