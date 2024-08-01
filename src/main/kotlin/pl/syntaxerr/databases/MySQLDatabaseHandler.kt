package pl.syntaxerr.databases

import pl.syntaxerr.Helpers.Logger
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException
import org.bukkit.configuration.file.FileConfiguration

class MySQLDatabaseHandler(config: FileConfiguration, private val logger: Logger) {
    private var connection: Connection? = null
    private val url: String = "jdbc:mysql://${config.getString("database.sql.host")}:${config.getString("database.sql.port")}/${config.getString("database.sql.dbname")}"
    private val user: String = config.getString("database.sql.username") ?: ""
    private val password: String = config.getString("database.sql.password") ?: ""

    fun openConnection() {
        try {
            connection = DriverManager.getConnection(url, user, password)
            logger.success("Connection to the database established.")
        } catch (e: SQLException) {
            logger.err("Failed to establish connection to the database. ${e.message}")
        }
    }

    fun isConnected(): Boolean {
        return connection != null && !connection!!.isClosed
    }

    fun getConnection(): Connection? {
        return connection
    }

    fun createTables() {
        if (isConnected()) {
            try {
                val statement = connection!!.createStatement()
                val createPunishmentsTable = """
                CREATE TABLE IF NOT EXISTS `punishments` (
                  `id` int(11) NOT NULL AUTO_INCREMENT,
                  `name` varchar(32) DEFAULT NULL,
                  `uuid` varchar(35) DEFAULT NULL,
                  `reason` varchar(255) DEFAULT NULL,
                  `operator` varchar(16) DEFAULT NULL,
                  `punishmentType` varchar(16) DEFAULT NULL,
                  `start` mediumtext DEFAULT NULL,
                  `end` mediumtext DEFAULT NULL,
                  PRIMARY KEY (`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """.trimIndent()

                val createPunishmentHistoryTable = """
                CREATE TABLE IF NOT EXISTS `punishmenthistory` (
                  `id` int(11) NOT NULL AUTO_INCREMENT,
                  `name` varchar(32) DEFAULT NULL,
                  `uuid` varchar(35) DEFAULT NULL,
                  `reason` varchar(255) DEFAULT NULL,
                  `operator` varchar(16) DEFAULT NULL,
                  `punishmentType` varchar(16) DEFAULT NULL,
                  `start` mediumtext DEFAULT NULL,
                  `end` mediumtext DEFAULT NULL,
                  PRIMARY KEY (`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """.trimIndent()

                statement.executeUpdate(createPunishmentsTable)
                statement.executeUpdate(createPunishmentHistoryTable)
                logger.debug("Tables `punishments` and `punishmenthistory` created or already exist.")
            } catch (e: SQLException) {
                logger.err("Failed to create tables. ${e.message}")
            }
        } else {
            logger.warning("Not connected to the database.")
        }
    }

    fun addPunishment(name: String, uuid: String, reason: String, operator: String, punishmentType: String, start: String, end: String) {
        if (!isConnected()) {
            openConnection()
        }

        if (isConnected()) {
            val query = """
            INSERT INTO `punishments` (name, uuid, reason, operator, punishmentType, start, end)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

            try {
                val preparedStatement: PreparedStatement = connection!!.prepareStatement(query)
                preparedStatement.setString(1, name)
                preparedStatement.setString(2, uuid)
                preparedStatement.setString(3, reason)
                preparedStatement.setString(4, operator)
                preparedStatement.setString(5, punishmentType)
                preparedStatement.setString(6, start)
                preparedStatement.setString(7, end)
                preparedStatement.executeUpdate()
                logger.debug("Punishment for player $name added to the database.")
            } catch (e: SQLException) {
                logger.err("Failed to add punishment for player $name. ${e.message}")
            }
        } else {
            logger.warning("Failed to reconnect to the database.")
        }
    }

    fun addPunishmentHistory(name: String, uuid: String, reason: String, operator: String, punishmentType: String, start: String, end: String) {
        if (!isConnected()) {
            openConnection()
        }

        if (isConnected()) {
            val query = """
            INSERT INTO `punishmenthistory` (name, uuid, reason, operator, punishmentType, start, end)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

            try {
                val preparedStatement: PreparedStatement = connection!!.prepareStatement(query)
                preparedStatement.setString(1, name)
                preparedStatement.setString(2, uuid)
                preparedStatement.setString(3, reason)
                preparedStatement.setString(4, operator)
                preparedStatement.setString(5, punishmentType)
                preparedStatement.setString(6, start)
                preparedStatement.setString(7, end)
                preparedStatement.executeUpdate()
                logger.debug("Punishment history for player $name added to the database.")
            } catch (e: SQLException) {
                logger.err("Failed to add punishment history for player $name. ${e.message}")
            }
        } else {
            logger.warning("Failed to reconnect to the database.")
        }
    }

    fun removePunishment(name: String, uuid: String, punishmentType: String) {
        if (!isConnected()) {
            openConnection()
        }

        if (isConnected()) {
            val query = """
            DELETE FROM `punishments` 
            WHERE `name` = ? AND `uuid` = ? AND `punishmentType` = ?
        """.trimIndent()

            try {
                val preparedStatement: PreparedStatement = connection!!.prepareStatement(query)
                preparedStatement.setString(1, name)
                preparedStatement.setString(2, uuid)
                preparedStatement.setString(3, punishmentType)
                val rowsAffected = preparedStatement.executeUpdate()
                if (rowsAffected > 0) {
                    logger.debug("Punishment of type $punishmentType for player $name removed from the database.")
                } else {
                    logger.warning("No punishment of type $punishmentType found for player $name.")
                }
            } catch (e: SQLException) {
                logger.err("Failed to remove punishment of type $punishmentType for player $name. ${e.message}")
            }
        } else {
            logger.warning("Failed to reconnect to the database.")
        }
    }


    fun closeConnection() {
        try {
            connection?.close()
            logger.info("Connection to the database closed.")
        } catch (e: SQLException) {
            logger.err("Failed to close the connection to the database. ${e.message}")
        }
    }
}
