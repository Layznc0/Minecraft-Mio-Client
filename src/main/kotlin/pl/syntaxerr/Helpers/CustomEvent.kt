package pl.syntaxerr.helpers

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class CustomEvent(val pluginName: String, val priority: Int) : Event() {

    companion object {
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }
    }

    override fun getHandlers(): HandlerList {
        return getHandlerList()
    }
}
