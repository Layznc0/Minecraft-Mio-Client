package pl.syntaxdevteam.helpers

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import pl.syntaxdevteam.PunisherX
import java.util.concurrent.ConcurrentHashMap

class IpCache(private val plugin: PunisherX) : Listener {
    private val ipCache = ConcurrentHashMap<String, String>()

    @EventHandler
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val uuid = event.uniqueId.toString()
        val ip = event.address.hostAddress
        ipCache[uuid] = ip
        plugin.logger.debug("Dodano IP do cache: $uuid -> $ip") // Dodaj logowanie, aby upewnić się, że IP jest dodawane do cache
    }

    fun getIp(uuid: String): String? {
        val ip = ipCache[uuid]
        plugin.logger.debug("Pobrano IP z cache: $uuid -> $ip") // Dodaj logowanie, aby upewnić się, że IP jest pobierane z cache
        return ip
    }
}
