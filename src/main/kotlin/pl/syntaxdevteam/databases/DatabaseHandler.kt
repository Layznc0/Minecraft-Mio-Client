package pl.syntaxdevteam.databases

interface DatabaseHandler {
    fun openConnection()
    fun closeConnection()
    fun createTables()
    fun addPunishment(name: String, uuid: String, reason: String, operator: String, punishmentType: String, start: Long, end: Long)
    fun addPunishmentHistory(name: String, uuid: String, reason: String, operator: String, punishmentType: String, start: Long, end: Long)
    fun removePunishment(uuidOrIp: String, punishmentType: String, removeAll: Boolean = false)
    fun getPunishments(uuid: String): List<PunishmentData>
    fun getPunishmentsByIP(ip: String): List<PunishmentData>
    fun getActiveWarnCount(uuid: String): Int
}
