package pl.syntaxerr.basic

import pl.syntaxerr.databases.Punishment


class PunishmentManager {

    fun isPunishmentActive(punishment: Punishment): Boolean {
        val currentTime = System.currentTimeMillis()
        return punishment.end > currentTime || punishment.end == -1L
    }
}
