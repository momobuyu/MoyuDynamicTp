package com.moyu.dynamicTp.listener;

import com.moyu.dynamicTp.service.TeleportService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerCacheListener implements Listener {

    private final TeleportService teleportService;

    public PlayerCacheListener(TeleportService teleportService) {
        this.teleportService = teleportService;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        teleportService.clearPlayerCache(e.getPlayer().getUniqueId());
    }
}
