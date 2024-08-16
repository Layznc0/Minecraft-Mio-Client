package pl.syntaxdevteam.commands

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.configuration.PluginMeta
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.NotNull
import pl.syntaxdevteam.PunisherX
import pl.syntaxdevteam.helpers.Logger
import pl.syntaxdevteam.helpers.MessageHandler
import pl.syntaxdevteam.helpers.UUIDManager

@Suppress("UnstableApiUsage")
class UnBanCommand(private val plugin: PunisherX, pluginMetas: PluginMeta) : BasicCommand {
    private var config = plugin.config
    private var debugMode = config.getBoolean("debug")
    private val logger = Logger(pluginMetas, debugMode)
    private val uuidManager = UUIDManager(plugin)
    private val messageHandler = MessageHandler(plugin, pluginMetas)

    override fun execute(@NotNull stack: CommandSourceStack, @NotNull args: Array<String>) {
        if (args.isNotEmpty()) {
            if (stack.sender.hasPermission("punisherx.unban")) {
                val player = args[0]
                val uuid = uuidManager.getUUID(player)
                if (uuid == null) {
                    stack.sender.sendRichMessage(messageHandler.getMessage("error", "player_not_found", mapOf("player" to player)))
                    return
                }
                val punishmentType = "BAN"
                plugin.databaseHandler.removePunishment(uuid, punishmentType)
                stack.sender.sendRichMessage(messageHandler.getMessage("ban", "unban", mapOf("player" to player)))
                val message = Component.text(messageHandler.getMessage("ban", "unban", mapOf("player" to player)))
                plugin.server.broadcast(message)
                logger.info("Player $player ($uuid) has unbanned")
            } else {
            stack.sender.sendRichMessage(messageHandler.getMessage("error", "no_permission"))
        }
        } else {
            stack.sender.sendRichMessage(messageHandler.getMessage("ban", "usage_unban"))
        }
    }
}