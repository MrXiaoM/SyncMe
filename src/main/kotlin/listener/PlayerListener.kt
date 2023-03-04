package top.mrxiaom.syncme.listener

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import top.mrxiaom.syncme.SyncMe

object PlayerListener : Listener {
    fun register() {
        Bukkit.getPluginManager().registerEvents(this, SyncMe)
    }

    @EventHandler
    fun handle(e: AsyncPlayerPreLoginEvent) {

    }
}