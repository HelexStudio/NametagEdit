package com.nametagedit.plugin;

import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.storage.AbstractConfig;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import java.util.*;

public class NametagHandler {

    private final NametagEdit plugin;
    private AbstractConfig storage;
    private final Map<String, GroupData> groupDataMap = new LinkedHashMap<>();
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private boolean debug = false;
    private boolean longTagsEnabled = false;
    private final NametagManager nametagManager;
    public static boolean DISABLE_PUSH_ALL_TAGS = false;
    private final boolean placeholderApiEnabled;

    public NametagHandler(NametagEdit plugin) {
        this.plugin = plugin;
        this.nametagManager = new NametagManager();
        this.placeholderApiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (this.placeholderApiEnabled) {
            plugin.getLogger().info("Successfully hooked into PlaceholderAPI.");
        }
    }

    public NametagEdit getPlugin() { return plugin; }
    public AbstractConfig getStorage() { return storage; }
    public void setStorage(AbstractConfig storage) { this.storage = storage; }
    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }
    public boolean isLongTagsEnabled() { return longTagsEnabled; }
    public void setLongTagsEnabled(boolean enabled) { this.longTagsEnabled = enabled; applyTags(); }
    public NametagManager getNametagManager() { return nametagManager; }

    public Collection<GroupData> getGroupData() { return groupDataMap.values(); }
    public GroupData getGroupData(String groupName) { return groupDataMap.get(groupName); }

    public void assignGroupData(List<GroupData> groups) {
        groupDataMap.clear();
        groups.sort(Comparator.comparingInt(GroupData::getSortPriority));
        for (GroupData group : groups) groupDataMap.put(group.getGroupName(), group);
    }

    public void clearMemoryData() {
        playerDataMap.clear();
        groupDataMap.clear();
    }

    public void assignData(List<GroupData> groups, Map<UUID, PlayerData> players) {
        assignGroupData(groups);
        playerDataMap.clear();
        if (players != null) {
            playerDataMap.putAll(players);
        }
    }

    public void storePlayerData(UUID uuid, PlayerData data) { playerDataMap.put(uuid, data); }
    public PlayerData getPlayerData(Player player) { return playerDataMap.get(player.getUniqueId()); }

    public void setPlayerPriority(String playerName, int priority) {
        Player p = Bukkit.getPlayerExact(playerName);
        if (p == null) return;
        PlayerData data = playerDataMap.get(p.getUniqueId());
        if (data == null) {
            data = new PlayerData(p.getName(), p.getUniqueId(), "", "", priority);
        } else {
            data.setSortPriority(priority);
        }
        playerDataMap.put(p.getUniqueId(), data);
        applyTagToPlayer(p, false);
    }

    private String translateColors(String input) {
        return input == null ? "" : ChatColor.translateAlternateColorCodes('&', input);
    }

    private ChatColor getLastColor(String text) {
        return ChatColor.getLastColors(text).isEmpty() ? ChatColor.WHITE : ChatColor.getByChar(ChatColor.getLastColors(text).substring(1));
    }

    private boolean hasGroupPermission(Player player, String node) {
        if (node == null || node.isEmpty()) return false;
        if (plugin.getLuckPerms() != null) {
            User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                return user.getCachedData().getPermissionData().checkPermission(node).asBoolean();
            }
        }
        return player.hasPermission(node);
    }

    private GroupData getHighestPriorityGroupForPlayer(Player player) {
        for (GroupData group : groupDataMap.values()) {
            if (hasGroupPermission(player, group.getPermission())) {
                return group;
            }
        }
        return null;
    }

    public void applyTagToPlayer(Player player, boolean loggedIn) {
        String prefix = "";
        String suffix = "";
        int tabPriority = 99;

        PlayerData pdata = playerDataMap.get(player.getUniqueId());
        if (pdata != null && (!isEmpty(pdata.getPrefix()) || !isEmpty(pdata.getSuffix()))) {
            prefix = pdata.getPrefix();
            suffix = pdata.getSuffix();
            tabPriority = pdata.getSortPriority();
        } else {
            GroupData matchedGroup = getHighestPriorityGroupForPlayer(player);
            if (matchedGroup != null) {
                prefix = matchedGroup.getPrefix();
                suffix = matchedGroup.getSuffix();
                tabPriority = matchedGroup.getSortPriority();
            }
        }

        if (placeholderApiEnabled) {
            prefix = PlaceholderAPI.setPlaceholders(player, prefix);
            suffix = PlaceholderAPI.setPlaceholders(player, suffix);
        }

        prefix = translateColors(prefix);
        suffix = translateColors(suffix);
        ChatColor teamColor = getLastColor(prefix);

        String teamName = getTeamName(tabPriority, player);
        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            for (Team oldTeam : scoreboard.getTeams()) {
                if (oldTeam.getName().endsWith("_" + player.getName())) {
                    oldTeam.removeEntry(player.getName());
                }
            }
            team = scoreboard.registerNewTeam(teamName);
        }

        String finalPrefix = prefix;
        String finalSuffix = suffix;

        if (!longTagsEnabled) {
            if (finalPrefix.length() > 16) finalPrefix = finalPrefix.substring(0, 16);
            if (finalSuffix.length() > 16) finalSuffix = finalSuffix.substring(0, 16);
        }

        team.setPrefix(finalPrefix);
        team.setSuffix(finalSuffix);
        try { team.setColor(teamColor); } catch (Throwable ignored) {}

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }

        player.setDisplayName(prefix + player.getName() + suffix);
    }

    public void applyTags() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyTagToPlayer(player, false);
        }
    }

    public void clearPlayerValue(Player player, String type) {
        PlayerData data = playerDataMap.get(player.getUniqueId());
        if (data == null) return;
        if (type == null || type.equalsIgnoreCase("prefix")) {
            data.setPrefix("");
        }
        if (type == null || type.equalsIgnoreCase("suffix")) {
            data.setSuffix("");
        }
        playerDataMap.put(player.getUniqueId(), data);
        applyTagToPlayer(player, false);
    }

    public void save(String playerName, String prefix, String suffix) {
        Player p = Bukkit.getPlayerExact(playerName);
        if (p == null) return;
        PlayerData data = playerDataMap.get(p.getUniqueId());
        if (data == null) {
            data = new PlayerData(p.getName(), p.getUniqueId(), "", "", 99);
        }
        if (prefix != null) data.setPrefix(prefix);
        if (suffix != null) data.setSuffix(suffix);
        playerDataMap.put(p.getUniqueId(), data);
        applyTagToPlayer(p, false);
    }

    public void clear(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getEntryTeam(player.getName());
        if (team != null) {
            team.removeEntry(player.getName());
        }
        player.setDisplayName(player.getName());
    }

    public void hideNametag(Player player) {
        clear(player);
    }

    public void showNametag(Player player) {
        applyTagToPlayer(player, false);
    }

    private String getTeamName(int sortPriority, Player player) {
        String base = String.format("%03d_%s", sortPriority, player.getName());
        return base.length() > 16 ? base.substring(0, 16) : base;
    }

    private boolean isEmpty(String s) { return s == null || s.isEmpty(); }
}