package pl.syntaxerr

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.event.player.AsyncChatEvent
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import pl.syntaxerr.helpers.Logger
import pl.syntaxerr.commands.BanCommand
import pl.syntaxerr.commands.GuardianXCommands
import pl.syntaxerr.databases.MySQLDatabaseHandler

@Suppress("UnstableApiUsage")
class GuardianX : JavaPlugin(), Listener {
    private lateinit var logger: Logger
    private val pluginMetas = this.pluginMeta
    private var config = getConfig()
    private var debugMode = config.getBoolean("debug")
    lateinit var databaseHandler: MySQLDatabaseHandler

    override fun onEnable() {
        saveDefaultConfig()
        logger = Logger(pluginMetas.name, pluginMetas.version, pluginMetas.name, debugMode)

        databaseHandler = MySQLDatabaseHandler(config, logger)
        databaseHandler.openConnection()
        databaseHandler.createTables()

        val manager: LifecycleEventManager<Plugin> = this.lifecycleManager
        manager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands: Commands = event.registrar()
            commands.register("guardianx", "Komenda pluginu GuardianX. Wpisz /guardianx help aby sprawdzic dostępne komendy", GuardianXCommands(this))
            commands.register("gnx", "Komenda pluginu GuardianX. Wpisz /gnx help aby sprawdzic dostępne komendy", GuardianXCommands(this))
            commands.register("ban", "Poprawne użycie to: /ban <player> (czas) <powód>", BanCommand(this, pluginMetas))
        }
        logger.pluginStart()
    }

    override fun onDisable() {
        databaseHandler.closeConnection()
        AsyncChatEvent.getHandlerList().unregister(this as Listener)
        AsyncChatEvent.getHandlerList().unregister(this as Plugin)
    }

    fun restartGuardianTask() {
        try {
            super.reloadConfig()
            databaseHandler = MySQLDatabaseHandler(config, logger)
            databaseHandler.openConnection()
            databaseHandler.createTables()
            AsyncChatEvent.getHandlerList().unregister(this as Plugin)
        } catch (e: Exception) {
            logger.err("Wystąpił błąd podczas przełądowania konfiguracji: " + e.message)
            e.printStackTrace()
        }
    }
}
