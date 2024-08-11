package pl.syntaxerr.databases

data class PunishmentData(val uuid: String, val type: String, val reason: String, val start: Long, val end: Long)