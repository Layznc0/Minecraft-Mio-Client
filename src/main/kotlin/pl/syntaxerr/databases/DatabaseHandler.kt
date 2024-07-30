package pl.syntaxerr.databases

import java.sql.Connection
import java.sql.SQLException
import java.time.Instant
import java.time.LocalDate


interface DatabaseHandler {
    @Throws(SQLException::class, ClassNotFoundException::class)
    fun openConnection()

    val connection: Connection?

    val isConnected: Boolean

    fun createTable()

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun incrementSessionId(playerUUID: String?): Int

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun openConnectionAndCreateTable()

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun savePlayerJoinTime(playerName: String?, playerUUID: String?, joinTime: Instant?, currentDate: LocalDate?)

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun savePlayerQuitTime(playerUUID: String?, quitTime: Long)

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun getPlayerDailyReward(playerUUID: String?): Double

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun addNewDailyRewardEntry(playerUUID: String?, dailyLimit: Double)

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun updateRemainingReward(playerUUID: String?, reward: Double)

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun getSessionTime(playerUUID: String?, quitTime: Long): Long

    fun handleDailyReward(playerUUID: String?, dailyLimit: Double, reward: Double)

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun checkIfEntryExists(playerUUID: String?): Boolean

    fun closeConnection()
}


