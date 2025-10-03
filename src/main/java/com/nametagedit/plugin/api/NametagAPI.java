package com.nametagedit.plugin.api;

import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.NametagManager;
import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.Nametag;
import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.api.events.NametagEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Modern NametagAPI implementation for PaperMC/Bukkit.
 */
public final class NametagAPI implements INametagApi {

    private final NametagHandler handler;

    public NametagAPI(NametagHandler handler) {
        this.handler = handler;
    }

    private NametagManager getManager() {
        return handler.getNametagManager();
    }

    @Override
    public Nametag getNametag(Player player) {
        PlayerData data = getManager().getPlayerData(player.getUniqueId());
        if (data != null) {
            return new Nametag(data.getPrefix(), data.getSuffix());
        }
        // Optionally, fallback to group data if needed
        return new Nametag("", "");
    }

    @Override
    public void clearNametag(Player player) {
        if (shouldFireEvent(player, NametagEvent.ChangeType.CLEAR)) {
            getManager().reset(player.getName());
        }
    }

    @Override
    public void reloadNametag(Player player) {
        if (shouldFireEvent(player, NametagEvent.ChangeType.RELOAD)) {
            handler.applyTagToPlayer(player, false);
        }
    }

    @Override
    public void clearNametag(String player) {
        getManager().reset(player);
    }

    @Override
    public void setPrefix(Player player, String prefix) {
        setNametag(player, prefix, null);
    }

    @Override
    public void setSuffix(Player player, String suffix) {
        setNametag(player, null, suffix);
    }

    @Override
    public void setPrefix(String player, String prefix) {
        getManager().setNametag(player, prefix, null);
    }

    @Override
    public void setSuffix(String player, String suffix) {
        getManager().setNametag(player, null, suffix);
    }

    @Override
    public void setNametag(Player player, String prefix, String suffix) {
        getManager().setNametag(player.getName(), prefix, suffix);
    }

    @Override
    public void setNametag(String player, String prefix, String suffix) {
        getManager().setNametag(player, prefix, suffix);
    }

    @Override
    public void hideNametag(Player player) {
        getManager().reset(player.getName());
    }

    @Override
    public void hideNametag(String player) {
        getManager().reset(player);
    }

    @Override
    public void showNametag(Player player) {
        handler.applyTagToPlayer(player, false);
    }

    @Override
    public void showNametag(String player) {
        Player p = Bukkit.getPlayerExact(player);
        if (p != null) handler.applyTagToPlayer(p, false);
    }

    @Override
    public List<GroupData> getGroupData() {
        return (List<GroupData>) handler.getGroupData();
    }

    @Override
    public void saveGroupData(GroupData... groupData) {
        // Implement saving as needed (e.g., to config/database/storage)
    }

    @Override
    public void applyTags() {
        handler.applyTags();
    }

    @Override
    public void applyTagToPlayer(Player player, boolean loggedIn) {
        handler.applyTagToPlayer(player, loggedIn);
    }

    @Override
    public void updatePlayerPrefix(String target, String prefix) {
        getManager().setNametag(target, prefix, null);
    }

    @Override
    public void updatePlayerSuffix(String target, String suffix) {
        getManager().setNametag(target, null, suffix);
    }

    @Override
    public void updatePlayerNametag(String target, String prefix, String suffix) {
        getManager().setNametag(target, prefix, suffix);
    }

    private boolean shouldFireEvent(Player player, NametagEvent.ChangeType type) {
        NametagEvent event = new NametagEvent(player.getName(), "", getNametag(player), type);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }
}