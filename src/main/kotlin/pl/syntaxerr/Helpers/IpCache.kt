package pl.syntaxerr.helpers

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import java.util.concurrent.ConcurrentHashMap

class IpCache : Listener {
    private val ipCache = ConcurrentHashMap<String, String>()

    @EventHandler
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val uuid = event.uniqueId.toString()
        val ip = event.address.hostAddress
        ipCache[uuid] = ip
    }

    fun getIp(uuid: String): String? {
        return ipCache[uuid]
    }
}
