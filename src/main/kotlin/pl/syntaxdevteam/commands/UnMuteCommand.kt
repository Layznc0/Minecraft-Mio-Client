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
import pl.syntaxdevteam.helpers.UUIDManager

@Suppress("UnstableApiUsage")
class UnMuteCommand(private val plugin: PunisherX, pluginMetas: PluginMeta) : BasicCommand {
    private var config = plugin.config
    private var debugMode = config.getBoolean("debug")
    private val logger = Logger(pluginMetas, debugMode)
    private val uuidManager = UUIDManager(plugin)
    private val messageHandler = MessageHandler(plugin, pluginMetas)

    override fun execute(@NotNull stack: CommandSourceStack, @NotNull args: Array<String>) {
        if (args.isNotEmpty()) {
            if (stack.sender.hasPermission("punisherx.unmute")) {
                val player = args[0]
                val uuid = uuidManager.getUUID(player)
                if (uuid != null) {
                    val punishments = plugin.databaseHandler.getPunishments(uuid)
                    if (punishments.isNotEmpty()) {
                        punishments.forEach { punishment ->
                            if (punishment.type == "MUTE") {
                                plugin.databaseHandler.removePunishment(uuid, punishment.type)
                            }
                        }
                        stack.sender.sendRichMessage(messageHandler.getMessage("unmute", "unmute", mapOf("player" to player)))
                        val targetPlayer = Bukkit.getPlayer(player)
                        val muteMessage = messageHandler.getMessage("unmute", "unmute_message")
                        val formattedMessage = MiniMessage.miniMessage().deserialize(muteMessage)
                        targetPlayer?.sendMessage(formattedMessage)
                        logger.info("Player $player ($uuid) has been unmuted")
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
            stack.sender.sendRichMessage(messageHandler.getMessage("unmute", "usage_unmute"))
        }
    }
}
