package pl.syntaxerr

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.event.player.AsyncChatEvent
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bstats.bukkit.Metrics
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import pl.syntaxerr.commands.BanCommand
import pl.syntaxerr.commands.GuardianXCommands
import pl.syntaxerr.commands.WarnCommand
import pl.syntaxerr.databases.MySQLDatabaseHandler
import pl.syntaxerr.helpers.*

@Suppress("UnstableApiUsage")
class GuardianX : JavaPlugin(), Listener {
    lateinit var logger: Logger
    private val pluginMetas = this.pluginMeta
    private var config = getConfig()
    private var debugMode = config.getBoolean("debug")
    lateinit var databaseHandler: MySQLDatabaseHandler
    lateinit var messageHandler: MessageHandler
    lateinit var timeHandler: TimeHandler
    lateinit var punishmentManager: PunishmentManager
    private lateinit var pluginPrioritizer: PluginPrioritizer

    override fun onLoad() {
        logger = Logger(pluginMetas, debugMode)
        // Inicjalizacja PluginPrioritizer
        pluginPrioritizer = PluginPrioritizer(this)
    }

    override fun onEnable() {
        saveDefaultConfig()
        server.pluginManager.registerEvents(pluginPrioritizer, this)
        server.pluginManager.registerEvents(PunishmentChecker(this), this)
        logger.info("Registered plugins: ${pluginPrioritizer.registeredPlugins}")

        messageHandler = MessageHandler(this, pluginMetas)
        timeHandler = TimeHandler(this.config.getString("language") ?: "PL")
        punishmentManager = PunishmentManager()
        databaseHandler = MySQLDatabaseHandler(this, this.config)
        databaseHandler.openConnection()
        databaseHandler.createTables()

        val manager: LifecycleEventManager<Plugin> = this.lifecycleManager
        manager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands: Commands = event.registrar()
            commands.register("guardianx", "Komenda pluginu GuardianX. Wpisz /guardianx help aby sprawdzic dostępne komendy", GuardianXCommands(this))
            commands.register("gnx", "Komenda pluginu GuardianX. Wpisz /gnx help aby sprawdzic dostępne komendy", GuardianXCommands(this))
            commands.register("ban", messageHandler.getMessage("ban", "usage"), BanCommand(this, pluginMetas))
            commands.register("warn", messageHandler.getMessage("warn", "usage"), WarnCommand(this, pluginMetas))
        }
        val pluginId = 22860
        Metrics(this, pluginId)

        // Rejestracja pluginu po zarejestrowaniu nasłuchiwacza
        pluginPrioritizer.registerPlugin()

        val highestPriorityPlugin = pluginPrioritizer.registeredPlugins.maxByOrNull { it.second }
        logger.info("Highest priority plugin: $highestPriorityPlugin")

        if (highestPriorityPlugin?.first == name) {
            pluginPrioritizer.displayLogo()
        }
    }

    override fun onDisable() {
        databaseHandler.closeConnection()
        AsyncChatEvent.getHandlerList().unregister(this as Listener)
        AsyncChatEvent.getHandlerList().unregister(this as Plugin)
    }

    fun restartGuardianTask() {
        try {
            super.reloadConfig()
            onEnable()
        } catch (e: Exception) {
            logger.err("Wystąpił błąd podczas przełądowania konfiguracji: " + e.message)
            e.printStackTrace()
        }
    }
}
