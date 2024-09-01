package pl.syntaxdevteam.commands

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.configuration.PluginMeta
import net.kyori.adventure.text.minimessage.MiniMessage
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
                val playerOrIpOrUUID = args[0]
                if (playerOrIpOrUUID.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+"))) {
                    val punishments = plugin.databaseHandler.getPunishmentsByIP(playerOrIpOrUUID)
                    if (punishments.isNotEmpty()) {
                        punishments.forEach { punishment ->
                            if (punishment.type == "BANIP") {
                                plugin.databaseHandler.removePunishment(playerOrIpOrUUID, punishment.type)
                            }
                        }
                        stack.sender.sendRichMessage(messageHandler.getMessage("unban", "unban", mapOf("player" to playerOrIpOrUUID)))
                        val message = MiniMessage.miniMessage().deserialize(messageHandler.getMessage("unban", "unban", mapOf("player" to playerOrIpOrUUID)))
                        plugin.server.broadcast(message)
                        logger.info("IP $playerOrIpOrUUID has been unbanned")
                    } else {
                        stack.sender.sendRichMessage(messageHandler.getMessage("error", "player_not_found", mapOf("player" to playerOrIpOrUUID)))
                    }
                } else {
                    val uuid = uuidManager.getUUID(playerOrIpOrUUID)
                    logger.debug("UUID for player $playerOrIpOrUUID: [$uuid]")
                    if (uuid != null) {
                        val punishments = plugin.databaseHandler.getPunishments(uuid)
                        if (punishments.isNotEmpty()) {
                            punishments.forEach { punishment ->
                                if (punishment.type == "BAN") {
                                    plugin.databaseHandler.removePunishment(uuid, punishment.type)
                                }
                            }
                            stack.sender.sendRichMessage(messageHandler.getMessage("unban", "unban", mapOf("player" to playerOrIpOrUUID)))
                            val message = MiniMessage.miniMessage().deserialize(messageHandler.getMessage("unban", "unban", mapOf("player" to playerOrIpOrUUID)))
                            plugin.server.broadcast(message)
                            logger.info("Player $playerOrIpOrUUID ($uuid) has been unbanned")
                        } else {
                            val ip = plugin.playerIPManager.getPlayerIPByName(playerOrIpOrUUID)
                            logger.debug("Assigned IP for player $playerOrIpOrUUID: [$ip]")
                            if (ip != null) {
                                val punishmentsByIP = plugin.databaseHandler.getPunishmentsByIP(ip)
                                if (punishmentsByIP.isNotEmpty()) {
                                    punishmentsByIP.forEach { punishment ->
                                        if (punishment.type == "BANIP") {
                                            plugin.databaseHandler.removePunishment(ip, punishment.type)
                                        }
                                    }
                                    stack.sender.sendRichMessage(messageHandler.getMessage("unban", "unban", mapOf("player" to playerOrIpOrUUID)))
                                    val message = MiniMessage.miniMessage().deserialize(messageHandler.getMessage("unban", "unban"))
                                    plugin.server.broadcast(message)
                                    logger.log(messageHandler.getLogMessage("unban", "unban"))
                                } else {
                                    stack.sender.sendRichMessage(messageHandler.getMessage("error", "player_not_found", mapOf("player" to playerOrIpOrUUID)))
                                }
                            } else {
                                stack.sender.sendRichMessage(messageHandler.getMessage("error", "player_not_found", mapOf("player" to playerOrIpOrUUID)))
                            }
                        }
                    } else {
                        stack.sender.sendRichMessage(messageHandler.getMessage("error", "player_not_found", mapOf("player" to playerOrIpOrUUID)))
                    }
                }
            } else {
                stack.sender.sendRichMessage(messageHandler.getMessage("error", "no_permission"))
            }
        } else {
            stack.sender.sendRichMessage(messageHandler.getMessage("ban", "usage_unban"))
        }
    }
}
