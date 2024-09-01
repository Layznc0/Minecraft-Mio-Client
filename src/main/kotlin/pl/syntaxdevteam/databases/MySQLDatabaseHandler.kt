package pl.syntaxdevteam.databases

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.ResultSet
import org.bukkit.configuration.file.FileConfiguration
import pl.syntaxdevteam.PunisherX

class MySQLDatabaseHandler(private val plugin: PunisherX, config: FileConfiguration) : DatabaseHandler {
    private var connection: Connection? = null
    private val url: String = "jdbc:mysql://${config.getString("database.sql.host")}:${config.getString("database.sql.port")}/${config.getString("database.sql.dbname")}"
    private val user: String = config.getString("database.sql.username") ?: ""
    private val password: String = config.getString("database.sql.password") ?: ""

    override fun openConnection() {
        try {
            connection = DriverManager.getConnection(url, user, password)
            plugin.logger.debug("Connection to the MySQL database established.")
        } catch (e: SQLException) {
            plugin.logger.err("Failed to establish connection to the MySQL database. ${e.message}")
        }
    }

    private fun isConnected(): Boolean {
        return connection != null && !connection!!.isClosed
    }

    override fun createTables() {
        if (isConnected()) {
            try {
                val statement = connection!!.createStatement()
                val createPunishmentsTable = """
                CREATE TABLE IF NOT EXISTS `punishments` (
                  `id` int(11) NOT NULL AUTO_INCREMENT,
                  `name` varchar(32) DEFAULT NULL,
                  `uuid` varchar(36) DEFAULT NULL,
                  `reason` varchar(255) DEFAULT NULL,
                  `operator` varchar(16) DEFAULT NULL,
                  `punishmentType` varchar(16) DEFAULT NULL,
                  `start` bigint(20) DEFAULT NULL,
                  `end` varchar(32) DEFAULT NULL,
                  PRIMARY KEY (`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """.trimIndent()

                val createPunishmentHistoryTable = """
                CREATE TABLE IF NOT EXISTS `punishmenthistory` (
                  `id` int(11) NOT NULL AUTO_INCREMENT,
                  `name` varchar(32) DEFAULT NULL,
                  `uuid` varchar(36) DEFAULT NULL,
                  `reason` varchar(255) DEFAULT NULL,
                  `operator` varchar(16) DEFAULT NULL,
                  `punishmentType` varchar(16) DEFAULT NULL,
                  `start` bigint(20) DEFAULT NULL,
                  `end` varchar(32) DEFAULT NULL,
                  PRIMARY KEY (`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """.trimIndent()

                statement.executeUpdate(createPunishmentsTable)
                statement.executeUpdate(createPunishmentHistoryTable)
                plugin.logger.debug("Tables `punishments` and `punishmenthistory` created or already exist.")
            } catch (e: SQLException) {
                plugin.logger.err("Failed to create tables. ${e.message}")
            }
        } else {
            plugin.logger.warning("Not connected to the database.")
        }
    }

    override fun addPunishment(name: String, uuid: String, reason: String, operator: String, punishmentType: String, start: Long, end: Long) {
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
                preparedStatement.setLong(6, start)
                preparedStatement.setLong(7, end)
                preparedStatement.executeUpdate()
                plugin.logger.debug("Punishment for player $name added to the database.")
            } catch (e: SQLException) {
                plugin.logger.err("Failed to add punishment for player $name. ${e.message}")
            }
        } else {
            plugin.logger.warning("Failed to reconnect to the database.")
        }
    }

    override fun addPunishmentHistory(name: String, uuid: String, reason: String, operator: String, punishmentType: String, start: Long, end: Long) {
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
                preparedStatement.setLong(6, start)
                preparedStatement.setLong(7, end)
                preparedStatement.executeUpdate()
                plugin.logger.debug("Punishment history for player $name added to the database.")
            } catch (e: SQLException) {
                plugin.logger.err("Failed to add punishment history for player $name. ${e.message}")
            }
        } else {
            plugin.logger.warning("Failed to reconnect to the database.")
        }
    }

    override fun closeConnection() {
        try {
            connection?.close()
            plugin.logger.info("Connection to the database closed.")
        } catch (e: SQLException) {
            plugin.logger.err("Failed to close the connection to the database. ${e.message}")
        }
    }

    override fun getPunishments(uuid: String): List<PunishmentData> {
        if (!isConnected()) {
            openConnection()
        }

        val query = "SELECT * FROM punishments WHERE uuid = ?"
        val punishments = mutableListOf<PunishmentData>()
        try {
            val preparedStatement: PreparedStatement = connection!!.prepareStatement(query)
            preparedStatement.setString(1, uuid)
            val resultSet: ResultSet = preparedStatement.executeQuery()
            while (resultSet.next()) {
                val type = resultSet.getString("punishmentType")
                val reason = resultSet.getString("reason")
                val start = resultSet.getLong("start")
                val end = resultSet.getLong("end")
                val punishment = PunishmentData(uuid, type, reason, start, end)
                if (plugin.punishmentManager.isPunishmentActive(punishment)) {
                    punishments.add(punishment)
                } else {
                    removePunishment(uuid, type)
                }
            }
        } catch (e: SQLException) {
            plugin.logger.err("Failed to get punishments for UUID: $uuid. ${e.message}")
        }
        return punishments
    }

    override fun getPunishmentsByIP(ip: String): List<PunishmentData> {
        if (!isConnected()) {
            openConnection()
        }

        val query = "SELECT * FROM punishments WHERE uuid = ?"
        val punishments = mutableListOf<PunishmentData>()
        try {
            val preparedStatement: PreparedStatement = connection!!.prepareStatement(query)
            preparedStatement.setString(1, ip)
            val resultSet: ResultSet = preparedStatement.executeQuery()
            while (resultSet.next()) {
                val type = resultSet.getString("punishmentType")
                val reason = resultSet.getString("reason")
                val start = resultSet.getLong("start")
                val end = resultSet.getLong("end")
                val punishment = PunishmentData(ip, type, reason, start, end)
                if (plugin.punishmentManager.isPunishmentActive(punishment)) {
                    punishments.add(punishment)
                } else {
                    removePunishment(ip, type)
                }
            }
        } catch (e: SQLException) {
            plugin.logger.err("Failed to get punishments for IP: $ip. ${e.message}")
        }
        return punishments
    }


    override fun removePunishment(uuidOrIp: String, punishmentType: String, removeAll: Boolean) {
        if (!isConnected()) {
            openConnection()
        }
        if (isConnected()) {
            val query = if (removeAll) {
                """
            DELETE FROM `punishments` 
            WHERE `uuid` = ? AND `punishmentType` = ?
            """.trimIndent()
            } else {
                """
            DELETE FROM `punishments` 
            WHERE `uuid` = ? AND `punishmentType` = ?
            ORDER BY `start` DESC
            LIMIT 1
            """.trimIndent()
            }
            try {
                val preparedStatement: PreparedStatement = connection!!.prepareStatement(query)
                preparedStatement.setString(1, uuidOrIp)
                preparedStatement.setString(2, punishmentType)
                val rowsAffected = preparedStatement.executeUpdate()
                if (rowsAffected > 0) {
                    plugin.logger.debug("Punishment of type $punishmentType for UUID/IP: $uuidOrIp removed from the database.")
                } else {
                    plugin.logger.warning("No punishment of type $punishmentType found for UUID/IP: $uuidOrIp.")
                }
            } catch (e: SQLException) {
                plugin.logger.err("Failed to remove punishment of type $punishmentType for UUID/IP: $uuidOrIp. ${e.message}")
            }
        } else {
            plugin.logger.warning("Failed to reconnect to the database.")
        }
    }

    override fun getActiveWarnCount(uuid: String): Int {
        if (!isConnected()) {
            openConnection()
        }

        val query = "SELECT * FROM punishments WHERE uuid = ? AND punishmentType = 'WARN'"
        val punishments = mutableListOf<PunishmentData>()
        try {
            val preparedStatement: PreparedStatement = connection!!.prepareStatement(query)
            preparedStatement.setString(1, uuid)
            val resultSet: ResultSet = preparedStatement.executeQuery()
            while (resultSet.next()) {
                val type = resultSet.getString("punishmentType")
                val reason = resultSet.getString("reason")
                val start = resultSet.getLong("start")
                val end = resultSet.getLong("end")
                val punishment = PunishmentData(uuid, type, reason, start, end)
                if (plugin.punishmentManager.isPunishmentActive(punishment)) {
                    punishments.add(punishment)
                }
            }
        } catch (e: SQLException) {
            plugin.logger.err("Failed to get active warn count for UUID: $uuid. ${e.message}")
        }
        return punishments.size
    }
}
