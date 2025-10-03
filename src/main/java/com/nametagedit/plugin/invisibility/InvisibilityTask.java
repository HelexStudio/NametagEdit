package com.nametagedit.plugin.invisibility;

import com.nametagedit.plugin.NametagHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class InvisibilityTask extends BukkitRunnable {

    private final NametagHandler handler;

    // Inject your NametagHandler from your plugin main class
    public InvisibilityTask(NametagHandler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) {
            return;
        }

        players.forEach(player -> {
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                handler.hideNametag(player);
            } else {
                handler.showNametag(player);
            }
        });
    }
}