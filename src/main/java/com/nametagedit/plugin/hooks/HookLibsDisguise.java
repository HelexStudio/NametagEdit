package com.nametagedit.plugin.hooks;

import com.nametagedit.plugin.NametagEdit;
import lombok.AllArgsConstructor;
import me.libraryaddict.disguise.events.DisguiseEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

@AllArgsConstructor
public class HookLibsDisguise implements Listener {

    private final NametagEdit plugin;

    @EventHandler
    public void onDisguiseEvent(final DisguiseEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            // Reset the player's nametag using NametagManager
            plugin.getHandler().getNametagManager().reset(player.getName());

            // Re-apply the nametag after a short delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getHandler().applyTagToPlayer(player, false);
                }
            }.runTaskLater(plugin, 3);
        }
    }
}