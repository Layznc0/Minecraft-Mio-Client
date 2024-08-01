package pl.syntaxerr.commands
import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner
import org.jetbrains.annotations.NotNull
import pl.syntaxerr.GuardianX

@Suppress("UnstableApiUsage", "DEPRECATION")
class GuardianXCommands(private val plugin: GuardianX) : BasicCommand {

    override fun execute(@NotNull stack: CommandSourceStack, @NotNull args: Array<String>) {
        val pluginMeta = (plugin as LifecycleEventOwner).pluginMeta
        val pdf = plugin.description
        if (args.isNotEmpty()) {
            when {
                args[0].equals("help", ignoreCase = true) -> {
                    if (stack.sender.hasPermission("GuardianX.help")) {
                        stack.sender.sendRichMessage("<gray>#######################################\n#\n#  <gold>Dostępne komendy dla " + pluginMeta.name + ":\n<gray>#\n#  <gold>/guardianx help <gray>- <white>Wyświetla ten monit.\n<gray>#  <gold>/guardianx version <gray>- <white>Pokazuje info pluginu. \n<gray>#  <gold>/guardianx reload <gray>- <white>Przeładowuje plik konfiguracyjny\n<gray>#\n#######################################")
                    } else {
                        stack.sender.sendRichMessage("<red>Nie masz uprawnień do tej komendy.</red>")
                    }
                }
                args[0].equals("version", ignoreCase = true) -> {
                    if (stack.sender.hasPermission("GuardianX.version")) {
                        stack.sender.sendRichMessage("<gray>#######################################\n#\n#   <gold>→ <bold>" + pluginMeta.name + "</bold> ←\n<gray>#   <white>Autor: <bold><gold>" + pdf.authors + "</gold></bold>\n<gray>#   <white>WWW: <bold><gold><click:open_url:'" + pdf.website + "'>"  + pdf.website + "</click></gold></bold>\n<gray>#   <white>Wersja: <bold><gold>" + pluginMeta.version + "</gold></bold><gray>\n#\n#######################################")
                    } else {
                        stack.sender.sendRichMessage("<red>Nie masz uprawnień do tej komendy.</red>")
                    }
                }
                args[0].equals("reload", ignoreCase = true) -> {
                    if (stack.sender.hasPermission("GuardianX.reload")) {
                        plugin.restartGuardianTask()
                        stack.sender.sendRichMessage("<green>Plik konfiguracyjny został przeładowany.</green>")
                    } else {
                        stack.sender.sendRichMessage("<red>Nie masz uprawnień do tej komendy.</red>")
                    }
                }
            }
        } else {
            stack.sender.sendRichMessage("<green>Wpisz </green><gold>/guardianx help</gold> <green>aby sprawdzić dostępne komendy</green>")
        }
    }
}
