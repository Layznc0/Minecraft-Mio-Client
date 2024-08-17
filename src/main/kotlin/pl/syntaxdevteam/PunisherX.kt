package pl.syntaxdevteam

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.event.player.AsyncChatEvent
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import pl.syntaxdevteam.basic.PunishmentChecker
import pl.syntaxdevteam.basic.PunishmentManager
import pl.syntaxdevteam.commands.*
import pl.syntaxdevteam.databases.DatabaseHandler
import pl.syntaxdevteam.databases.MySQLDatabaseHandler
import pl.syntaxdevteam.databases.SQLiteDatabaseHandler
import pl.syntaxdevteam.helpers.*
import pl.syntaxdevteam.players.PlayerIPManager
import java.util.*

@Suppress("UnstableApiUsage")
class PunisherX : JavaPlugin(), Listener {
    lateinit var logger: Logger
    private val pluginMetas = this.pluginMeta
    private var config = getConfig()
    private var debugMode = config.getBoolean("debug")
    private lateinit var pluginManager: PluginManager
    private lateinit var statsCollector: StatsCollector
    lateinit var databaseHandler: DatabaseHandler
    lateinit var messageHandler: MessageHandler
    lateinit var timeHandler: TimeHandler
    lateinit var punishmentManager: PunishmentManager
    lateinit var playerIPManager: PlayerIPManager

    override fun onLoad() {
        logger = Logger(pluginMetas, debugMode)
    }

    override fun onEnable() {
        saveDefaultConfig()
        databaseHandler = when (config.getString("database.type")?.lowercase(Locale.getDefault())) {
            "mysql", "mariadb" -> {
                MySQLDatabaseHandler(this, this.config)
            }
            "sqlite" -> {
                SQLiteDatabaseHandler(this)
            }
            else -> {
                logger.warning("Nieprawidłowy typ bazy danych w konfiguracji. Używanie domyślnej bazy danych SQLite.")
                SQLiteDatabaseHandler(this)
            }
        }
        databaseHandler.openConnection()
        databaseHandler.createTables()
        messageHandler = MessageHandler(this, pluginMetas)
        playerIPManager = PlayerIPManager(this)
        timeHandler = TimeHandler(this.config.getString("language") ?: "PL")
        punishmentManager = PunishmentManager()
        server.pluginManager.registerEvents(PlayerIPManager(this), this)
        val manager: LifecycleEventManager<Plugin> = this.lifecycleManager
        manager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands: Commands = event.registrar()
            commands.register("punisherx", "Komenda pluginu PunisherX. Wpisz /punisherx help aby sprawdzic dostępne komendy", PunishesXCommands(this))
            commands.register("prx", "Komenda pluginu PunisherX. Wpisz /prx help aby sprawdzic dostępne komendy", PunishesXCommands(this))
            commands.register("ban", messageHandler.getMessage("ban", "usage"), BanCommand(this, pluginMetas))
            commands.register("banip", messageHandler.getMessage("banip", "usage"), BanIpCommand(this, pluginMetas))
            commands.register("unban", messageHandler.getMessage("ban", "usage"), UnBanCommand(this, pluginMetas))
            commands.register("warn", messageHandler.getMessage("warn", "usage"), WarnCommand(this, pluginMetas))
        }
        server.pluginManager.registerEvents(PunishmentChecker(this), this)
        pluginManager = PluginManager(this)
        val externalPlugins = pluginManager.fetchPluginsFromExternalSource("https://raw.githubusercontent.com/SyntaxDevTeam/plugins-list/main/plugins.json")
        val loadedPlugins = pluginManager.fetchLoadedPlugins()
        val highestPriorityPlugin = pluginManager.getHighestPriorityPlugin(externalPlugins, loadedPlugins)
        if (highestPriorityPlugin == pluginMeta.name) {
            val syntaxDevTeamPlugins = loadedPlugins.filter { it != pluginMeta.name }
            logger.pluginStart(syntaxDevTeamPlugins)
        }
        statsCollector = StatsCollector(this)
    }

    override fun onDisable() {
        databaseHandler.closeConnection()
        AsyncChatEvent.getHandlerList().unregister(this as Plugin)
    }


    fun restartGuardianTask() {
        try {
            super.reloadConfig()
            messageHandler.reloadMessages()
        } catch (e: Exception) {
            logger.err(messageHandler.getMessage("error", "reload") + e.message)
        }
    }
}
