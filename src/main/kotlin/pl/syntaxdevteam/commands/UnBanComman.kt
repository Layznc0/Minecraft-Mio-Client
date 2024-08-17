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
                val playerOrIpOrUUID = args[0]

                // Sprawdzenie, czy podano IP
                if (playerOrIpOrUUID.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+"))) {
                    val punishmentType = "BANIP"
                    val punishment = plugin.databaseHandler.getPunishmentByIP(playerOrIpOrUUID)
                    if (punishment != null) {
                        plugin.databaseHandler.removePunishment(playerOrIpOrUUID, punishmentType)
                        stack.sender.sendRichMessage(messageHandler.getMessage("ban", "unban", mapOf("player" to playerOrIpOrUUID)))
                        val message = Component.text(messageHandler.getMessage("ban", "unban", mapOf("player" to playerOrIpOrUUID)))
                        plugin.server.broadcast(message)
                        logger.info("IP $playerOrIpOrUUID has been unbanned")
                    } else {
                        stack.sender.sendRichMessage(messageHandler.getMessage("error", "player_not_found", mapOf("player" to playerOrIpOrUUID)))
                    }
                } else {
                    // Zakładamy, że to nick gracza
                    val uuid = uuidManager.getUUID(playerOrIpOrUUID)
                    logger.debug("UUID dla gracza $playerOrIpOrUUID: [$uuid]")
                    if (uuid != null) {
                        val punishmentType = "BAN"
                        val punishment = plugin.databaseHandler.getPunishment(uuid)
                        logger.debug("Kara dla UUID: [$punishment]")
                        if (punishment != null) {
                            plugin.databaseHandler.removePunishment(uuid, punishmentType)
                            stack.sender.sendRichMessage(messageHandler.getMessage("ban", "unban", mapOf("player" to playerOrIpOrUUID)))
                            val message = Component.text(messageHandler.getMessage("ban", "unban", mapOf("player" to playerOrIpOrUUID)))
                            plugin.server.broadcast(message)
                            logger.info("Player $playerOrIpOrUUID ($uuid) has been unbanned")
                        } else {
                            // Sprawdzanie kary BANIP po IP gracza
                            val ip = plugin.playerIPManager.getPlayerIPByName(playerOrIpOrUUID)
                            logger.debug("Przypisane IP dla gracza $playerOrIpOrUUID: [$ip]")
                            if (ip != null) {
                                val punishType = "BANIP"
                                val punishmentByIP = plugin.databaseHandler.getPunishmentByIP(ip)
                                logger.debug("Kara dla IP: [$punishmentByIP]")
                                if (punishmentByIP != null) {
                                    plugin.databaseHandler.removePunishment(ip, punishType)
                                    stack.sender.sendRichMessage(messageHandler.getMessage("ban", "unban", mapOf("player" to playerOrIpOrUUID)))
                                    val message = Component.text(messageHandler.getMessage("ban", "unban", mapOf("player" to playerOrIpOrUUID)))
                                    plugin.server.broadcast(message)
                                    logger.info("Player $playerOrIpOrUUID (IP: $ip) has been unbanned")
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
