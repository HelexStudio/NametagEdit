package com.nametagedit.plugin.metrics;

import com.nametagedit.plugin.NametagHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;

public class NametagListener implements Listener {

    private final NametagHandler handler;

    public NametagListener(NametagHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        handler.getStorage().load(player, true);
        handler.applyTagToPlayer(player, true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        handler.clear(player);
    }
}