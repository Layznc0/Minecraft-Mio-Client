package pl.syntaxerr.commands

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.configuration.PluginMeta
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.NotNull
import pl.syntaxerr.GuardianX
import pl.syntaxerr.Helpers.Logger

@Suppress("UnstableApiUsage")
class BanCommand(private val plugin: GuardianX, private val pluginMetas: PluginMeta) : BasicCommand {

    private var config = plugin.getConfig()
    private var debugMode = config.getBoolean("debug")
    private val logger = Logger(pluginMetas.name, pluginMetas.version, pluginMetas.name, debugMode)

    override fun execute(@NotNull stack: CommandSourceStack, @NotNull args: Array<String>) {
        if (args.isNotEmpty()) {
            if (stack.sender.hasPermission("GuardianX.ban")) {
                if (args.size < 2) {
                    stack.sender.sendRichMessage("<red>Poprawne użycie to: <yellow>/ban <player> (czas) <powód>")
                } else {
                    val player = args[0]
                    val gtime = if (args.size > 2) args[1] else null
                    val reason = if (args.size > 2) args.slice(2 until args.size).joinToString(" ") else args[1]

                    // Tutaj możesz zaimplementować logikę banowania gracza
                    stack.sender.sendRichMessage("Zbanowałeś "+ player + " za " + reason + " na czas " + formatTime(gtime))
                    val message = Component.text("Gracz "+ player + " został zbanowany za " + reason + " na czas " + formatTime(gtime))
                    plugin.server.broadcast(message)
                    logger.info("Gracz "+ player + " został zbanowany za " + reason + " na czas " + formatTime(gtime))
                }
            } else {
                stack.sender.sendRichMessage("<red>Nie masz uprawnień do tej komendy.</red>")
            }
        } else {
            stack.sender.sendRichMessage("<green>Wpisz </green><gold>/ban help</gold> <green>aby sprawdzić dostępne komendy</green>")
        }
    }

    fun parseTime(time: String): Long {
        val amount = time.substring(0, time.length - 1).toLong()
        val unit = time.last()

        return when (unit) {
            's' -> amount
            'm' -> amount * 60
            'h' -> amount * 60 * 60
            'd' -> amount * 60 * 60 * 24
            else -> 0
        }
    }

    private fun formatTime(time: String?): String {
        if (time == null) return "nieokreślony"
        val amount = time.substring(0, time.length - 1)
        val unit = time.last()

        return when (unit) {
            's' -> "$amount sekund"
            'm' -> "$amount minut"
            'h' -> "$amount godzin"
            'd' -> "$amount dni"
            else -> "nieokreślony"
        }
    }
}
