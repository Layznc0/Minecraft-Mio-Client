package pl.syntaxerr.helpers

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.concurrent.ConcurrentHashMap

class IpCache : Listener {
    private val ipCache = ConcurrentHashMap<String, String>()

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val ip = player.address?.address?.hostAddress
        if (ip != null) {
            ipCache[player.uniqueId.toString()] = ip
        }
    }

    fun getIp(uuid: String): String? {
        return ipCache[uuid]
    }
}
