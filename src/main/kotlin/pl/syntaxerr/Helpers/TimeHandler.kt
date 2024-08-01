package pl.syntaxerr.helpers

class TimeHandler(private val language: String) {

    fun parseTime(time: String): Long {
        val amount = time.substring(0, time.length - 1).toLong()
        val unit = time.last()

        return when (unit) {
            's' -> amount
            'm' -> amount * 60
            'h' -> amount * 60 * 60
            'd' -> amount * 60 * 60 * 24
            else -> 0
        }
    }

    fun formatTime(time: String?): String {
        if (time == null) return if (language == "PL") "nieokreślony" else "undefined"
        val amount = time.substring(0, time.length - 1)
        val unit = time.last()

        return when (unit) {
            's' -> "$amount ${if (language == "PL") "sekund" else "seconds"}"
            'm' -> "$amount ${if (language == "PL") "minut" else "minutes"}"
            'h' -> "$amount ${if (language == "PL") "godzin" else "hours"}"
            'd' -> "$amount ${if (language == "PL") "dni" else "days"}"
            else -> if (language == "PL") "nieokreślony" else "undefined"
        }
    }
}
