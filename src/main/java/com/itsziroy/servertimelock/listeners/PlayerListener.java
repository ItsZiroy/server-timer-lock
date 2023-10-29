package com.itsziroy.servertimelock.listeners;

import com.itsziroy.bukkitredis.events.player.PlayerEvent;
import com.itsziroy.servertimelock.Permission;
import com.itsziroy.servertimelock.ServerTimeLock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerListener implements Listener {

    private final ServerTimeLock plugin;

    public PlayerListener(ServerTimeLock plugin) {
        this.plugin = plugin;
    }
    @EventHandler()
    public void onPreJoin(PlayerLoginEvent event) {
        if(plugin.isLocked() && !event.getPlayer().hasPermission(Permission.BYPASS_SERVER_CLOSE)) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "The Server is currently offline.");
        }
    }

    @EventHandler()
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(plugin.isLocked()) {
            event.getPlayer().sendMessage("The Server is currently offline. You are bypassing the closing times.");
        }
    }

    public void onPlayerByPassClosed(PlayerEvent<?> event) {
        if(plugin.isLocked()) {
            event.setCancelled(true);
        }
    }
}
