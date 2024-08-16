package pl.syntaxdevteam.basic

import pl.syntaxdevteam.databases.PunishmentData


class PunishmentManager {

    fun isPunishmentActive(punishment: PunishmentData): Boolean {
        val currentTime = System.currentTimeMillis()
        return punishment.end > currentTime || punishment.end == -1L
    }
}
