package pl.syntaxdevteam.commands

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.configuration.PluginMeta
import org.jetbrains.annotations.NotNull
import pl.syntaxdevteam.PunisherX
import pl.syntaxdevteam.helpers.Logger
import pl.syntaxdevteam.helpers.MessageHandler
import pl.syntaxdevteam.helpers.UUIDManager

@Suppress("UnstableApiUsage")
class UnWarnCommand(private val plugin: PunisherX, pluginMetas: PluginMeta) : BasicCommand {
    private var config = plugin.config
    private var debugMode = config.getBoolean("debug")
    private val logger = Logger(pluginMetas, debugMode)
    private val uuidManager = UUIDManager(plugin)
    private val messageHandler = MessageHandler(plugin, pluginMetas)

    override fun execute(@NotNull stack: CommandSourceStack, @NotNull args: Array<String>) {
        if (args.isNotEmpty()) {
            if (stack.sender.hasPermission("punisherx.unwarn")) {
                val player = args[0]
                val uuid = uuidManager.getUUID(player)
                if (uuid != null) {
                    val punishments = plugin.databaseHandler.getPunishments(uuid)
                    if (punishments.isNotEmpty()) {
                        punishments.forEach { punishment ->
                            if (punishment.type == "WARN") {
                                plugin.databaseHandler.removePunishment(uuid, punishment.type)
                            }
                        }
                        stack.sender.sendRichMessage(messageHandler.getMessage("unwarn", "unwarn", mapOf("player" to player)))
                        logger.info("Player $player ($uuid) has been unwarned")
                    } else {
                        stack.sender.sendRichMessage(messageHandler.getMessage("error", "player_not_found", mapOf("player" to player)))
                    }
                } else {
                    stack.sender.sendRichMessage(messageHandler.getMessage("error", "player_not_found", mapOf("player" to player)))
                }
            } else {
                stack.sender.sendRichMessage(messageHandler.getMessage("error", "no_permission"))
            }
        } else {
            stack.sender.sendRichMessage(messageHandler.getMessage("unwarn", "usage"))
        }
    }
}
