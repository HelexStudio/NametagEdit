package com.nametagedit.plugin.hooks;

import com.nametagedit.plugin.NametagHandler;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class HookLuckPerms implements Listener {

    private final NametagHandler handler;

    public HookLuckPerms(NametagHandler handler) {
        this.handler = handler;
        LuckPerms luckPerms = Bukkit.getServicesManager().load(LuckPerms.class);
        if (luckPerms != null) {
            EventBus eventBus = luckPerms.getEventBus();
            eventBus.subscribe(UserDataRecalculateEvent.class, this::onUserDataRecalculateEvent);
        }
    }

    private void onUserDataRecalculateEvent(UserDataRecalculateEvent event) {
        User user = event.getUser();
        Player player = Bukkit.getPlayer(user.getUniqueId());
        if (player != null) {
            handler.applyTagToPlayer(player, false);
        }
    }
}