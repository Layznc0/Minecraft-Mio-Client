package pl.syntaxdevteam.databases

interface DatabaseHandler {
    fun openConnection()
    fun closeConnection()
    fun createTables()
    fun addPunishment(name: String, uuid: String, reason: String, operator: String, punishmentType: String, start: Long, end: Long)
    fun addPunishmentHistory(name: String, uuid: String, reason: String, operator: String, punishmentType: String, start: Long, end: Long)
    fun removePunishment(uuid: String, punishmentType: String)
    fun getPunishment(uuid: String): PunishmentData?
}
