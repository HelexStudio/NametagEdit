package com.nametagedit.plugin.listener;

import com.nametagedit.plugin.NametagHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class NametagListener implements Listener {

    private final NametagHandler handler;

    public NametagListener(NametagHandler handler) {
        this.handler = handler;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(handler.getPlugin(), () -> {
            handler.applyTagToPlayer(player, true);
            if (handler.isDebug()) {
                handler.getPlugin().getLogger().info("[NametagEdit DEBUG] Tag applied for: " + player.getName());
            }
        }, 20L); // Delay to allow other plugins (like LuckPerms) to load permissions
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        handler.clear(player);
        if (handler.isDebug()) {
            handler.getPlugin().getLogger().info("[NametagEdit DEBUG] Tag cleared for: " + player.getName());
        }
    }
}