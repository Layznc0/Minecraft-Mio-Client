package pl.syntaxdevteam.commands

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner
import org.jetbrains.annotations.NotNull
import pl.syntaxdevteam.PunisherX

@Suppress("UnstableApiUsage", "DEPRECATION")
class PunishesXCommands(private val plugin: PunisherX) : BasicCommand {

    override fun execute(@NotNull stack: CommandSourceStack, @NotNull args: Array<String>) {
        val pluginMeta = (plugin as LifecycleEventOwner).pluginMeta
        val pdf = plugin.description
        if (args.isNotEmpty()) {
            when {
                args[0].equals("help", ignoreCase = true) -> {
                    if (stack.sender.hasPermission("punisherx.help")) {
                        stack.sender.sendRichMessage("\n<gray>#############################################\n#\n#  <gold>Available commands for " + pluginMeta.name + ":\n" +
                                "<gray>#\n" +
                                "<gray>#  <gold>/punisherx help <gray>- <white>Displays this prompt. \n" +
                                "<gray>#  <gold>/punisherx version <gray>- <white>Shows plugin info. \n" +
                                "<gray>#  <gold>/punisherx reload <gray>- <white>Reloads the configuration file\n" +
                                "<gray>#  <gold>/ban <player> (time) <reason> <gray>- <white>Bans a player \n" +
                                "<gray>#  <gold>/banip <player/ip> (time) <reason> <gray>- <white>Bans a player's IP\n" +
                                "<gray>#  <gold>/unban <player/ip> <gray>- <white>Unbans a player\n" +
                                "<gray>#  <gold>/warn <player> (time) <reason> <gray>- <white>Warns a player\n" +
                                "<gray>#  <gold>/unwarn <player> <gray>- <white>Removes a player's warning\n" +
                                "<gray>#  <gold>/mute <player> (time) <reason> <gray>- <white>Mutes a player\n" +
                                "<gray>#  <gold>/unmute <player> <gray>- <white>Unmutes a player\n" +
                                "<gray>#\n" +
                                "<gray>#\n#############################################")
                    } else {
                        stack.sender.sendRichMessage("<red>You do not have permission to use this command.</red>")
                    }
                }
                args[0].equals("version", ignoreCase = true) -> {
                    if (stack.sender.hasPermission("punisherx.version")) {
                        stack.sender.sendRichMessage("\n<gray>#############################################\n#\n#   <gold>→ <bold>" + pluginMeta.name + "</bold> ←\n<gray>#   <white>Author: <bold><gold>" + pdf.authors + "</gold></bold>\n<gray>#   <white>Website: <bold><gold><click:open_url:'" + pdf.website + "'>"  + pdf.website + "</click></gold></bold>\n<gray>#   <white>Version: <bold><gold>" + pluginMeta.version + "</gold></bold><gray>\n#\n#############################################")
                    } else {
                        stack.sender.sendRichMessage("<red>You do not have permission to use this command.</red>")
                    }
                }
                args[0].equals("reload", ignoreCase = true) -> {
                    if (stack.sender.hasPermission("punisherx.reload")) {
                        plugin.restartGuardianTask()
                        stack.sender.sendRichMessage("<green>The configuration file has been reloaded.</green>")
                    } else {
                        stack.sender.sendRichMessage("<red>You do not have permission to use this command.</red>")
                    }
                }
            }
        } else {
            stack.sender.sendRichMessage("<green>Type </green><gold>/punisherx help</gold> <green>to see available commands</green>")
        }
    }
    override fun suggest(@NotNull stack: CommandSourceStack, @NotNull args: Array<String>): List<String> {
        return when (args.size) {
            1 -> listOf("help", "version", "reload")
            else -> emptyList()
        }
    }
}
