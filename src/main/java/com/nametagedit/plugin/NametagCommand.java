package com.nametagedit.plugin;

import com.nametagedit.plugin.api.data.GroupData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NametagCommand implements CommandExecutor {

    private final NametagHandler handler;

    public NametagCommand(NametagHandler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            if (!sender.hasPermission("nametagedit.use")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload":
                if (!sender.hasPermission("nametagedit.reload")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return true;
                }
                handler.getStorage().reload();
                sender.sendMessage("§eNametagEdit §7» §aReloaded configuration and nametags.");
                return true;
            case "debug":
                if (!sender.hasPermission("nametagedit.reload")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return true;
                }
                handler.setDebug(!handler.isDebug());
                sender.sendMessage("§eNametagEdit §7» §fDebug mode: §a" + handler.isDebug());
                return true;
            case "longtags":
                if (!sender.hasPermission("nametagedit.reload")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return true;
                }
                handler.setLongTagsEnabled(!handler.isLongTagsEnabled());
                sender.sendMessage("§eNametagEdit §7» §fLong nametags toggled: §a" + handler.isLongTagsEnabled());
                return true;
            case "teams":
                if (!sender.hasPermission("nametagedit.reload")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return true;
                }
                if (args.length > 1 && args[1].equalsIgnoreCase("clear")) {
                    clearEmptyTeams();
                    sender.sendMessage("§eNametagEdit §7» §aCleared empty teams.");
                } else {
                    sender.sendMessage("§cUsage: /ne teams clear");
                }
                return true;
            case "priority":
                if (!sender.hasPermission("nametagedit.priority")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return true;
                }
                sender.sendMessage("§eNametagEdit §7» §fUse '/ne player/group <name> priority <number>' to set sort priority.");
                return true;
            case "player":
                handlePlayerCommand(sender, args);
                return true;
            case "group":
                if (!sender.hasPermission("nametagedit.groups")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to manage groups.");
                    return true;
                }
                handleGroupCommand(sender, args);
                return true;
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§eNametagEdit §7» §fCommands:");
        sender.sendMessage("§7/ne help §8- Show this help menu");
        sender.sendMessage("§7/ne debug §8- Toggle debug mode");
        sender.sendMessage("§7/ne reload §8- Reload configuration and nametags");
        sender.sendMessage("§7/ne longtags §8- Toggle long nametag support in tablist");
        sender.sendMessage("§7/ne priority §8- Info about SortPriority usage");
        sender.sendMessage("§7/ne teams clear §8- Clear empty teams (debug)");
        sender.sendMessage("§7/ne player <player> clear §8- Clear prefix/suffix for player");
        sender.sendMessage("§7/ne player <player> prefix <value> §8- Set prefix for player");
        sender.sendMessage("§7/ne player <player> suffix <value> §8- Set suffix for player");
        sender.sendMessage("§7/ne player <player> priority <number> §8- Set priority for player");
        sender.sendMessage("§7/ne group list §8- List all loaded groups");
        sender.sendMessage("§7/ne group order <group1> <group2> ... §8- Set group ordering");
        sender.sendMessage("§7/ne group add <group> §8- Create a new group");
        sender.sendMessage("§7/ne group remove <group> §8- Remove a group");
        sender.sendMessage("§7/ne group <group> perm <permission> §8- Set group permission");
        sender.sendMessage("§7/ne group <group> prefix <value> §8- Set group prefix");
        sender.sendMessage("§7/ne group <group> suffix <value> §8- Set group suffix");
        sender.sendMessage("§7/ne group <group> priority <number> §8- Set group priority");
        sender.sendMessage("§7/ne group <group> clear <prefix/suffix> §8- Clear group prefix or suffix");
        sender.sendMessage("§7/ne group <group> info §8- Show all details of a group");
    }

    private void clearEmptyTeams() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getScoreboard().getTeams().stream()
                    .filter(team -> team.getEntries().isEmpty())
                    .forEach(Team::unregister);
        }
    }

    private void handlePlayerCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /ne player <player> <prefix|suffix|priority|clear> [value]");
            return;
        }
        String playerName = args[1];
        String action = args[2].toLowerCase();

        boolean isSelf = (sender instanceof Player) && ((Player) sender).getName().equalsIgnoreCase(playerName);
        boolean canEdit = isSelf ? sender.hasPermission("nametagedit.edit.self") : sender.hasPermission("nametagedit.edit.others");
        boolean canClear = isSelf ? sender.hasPermission("nametagedit.clear.self") : sender.hasPermission("nametagedit.clear.others");

        Player player = Bukkit.getPlayerExact(playerName);

        switch (action) {
            case "clear":
                if (!canClear) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to clear nametags.");
                    return;
                }
                if (player == null) {
                    sender.sendMessage("§cPlayer not found: " + playerName);
                    return;
                }
                handler.clearPlayerValue(player, null);
                handler.getStorage().clearPlayer(playerName);
                sender.sendMessage("§aCleared nametag for " + playerName);
                break;
            case "prefix":
                if (!canEdit) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to edit nametags.");
                    return;
                }
                if (args.length < 4) {
                    sender.sendMessage("§cUsage: /ne player <player> prefix <value>");
                    return;
                }
                if (player == null) {
                    sender.sendMessage("§cPlayer not found: " + playerName);
                    return;
                }
                String prefix = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                handler.save(playerName, prefix, null);
                handler.getStorage().savePlayer(playerName, prefix, null);
                sender.sendMessage("§aSet prefix for " + playerName + " to: " + ChatColor.translateAlternateColorCodes('&', prefix));
                break;
            case "suffix":
                if (!canEdit) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to edit nametags.");
                    return;
                }
                if (args.length < 4) {
                    sender.sendMessage("§cUsage: /ne player <player> suffix <value>");
                    return;
                }
                if (player == null) {
                    sender.sendMessage("§cPlayer not found: " + playerName);
                    return;
                }
                String suffix = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                handler.save(playerName, null, suffix);
                handler.getStorage().savePlayer(playerName, null, suffix);
                sender.sendMessage("§aSet suffix for " + playerName + " to: " + ChatColor.translateAlternateColorCodes('&', suffix));
                break;
            case "priority":
                if (!canEdit) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to set priority.");
                    return;
                }
                if (args.length < 4) {
                    sender.sendMessage("§cUsage: /ne player <player> priority <number>");
                    return;
                }
                try {
                    int priority = Integer.parseInt(args[3]);
                    handler.getStorage().savePriority(true, playerName, priority);
                    handler.setPlayerPriority(playerName, priority);
                    sender.sendMessage("§aSet priority for " + playerName + " to: " + priority);
                } catch (NumberFormatException ex) {
                    sender.sendMessage("§cPriority must be a number.");
                }
                break;
            default:
                sender.sendMessage("§cUnknown player action. Use prefix, suffix, priority, or clear.");
                break;
        }
    }

    private void handleGroupCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /ne group <sub-command> [args]");
            return;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "list":
                sender.sendMessage("§e--- NametagEdit Groups ---");
                for (GroupData group : handler.getGroupData()) {
                    sender.sendMessage("§7- §f" + group.getGroupName() + " §7(Priority: " + group.getSortPriority() + ")");
                }
                break;
            case "add":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /ne group add <groupName>");
                    return;
                }
                handler.getStorage().addGroup(new GroupData(args[2], "", "", "nte." + args[2], null, 99));
                sender.sendMessage("§aGroup '" + args[2] + "' created. Reload to apply changes.");
                break;
            case "remove":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /ne group remove <groupName>");
                    return;
                }
                handler.getStorage().delete(new GroupData(args[2], null, null, null, null, 0));
                sender.sendMessage("§aGroup '" + args[2] + "' removed. Reload to apply changes.");
                break;
            case "order":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /ne group order <group1> <group2> ...");
                    return;
                }
                List<String> order = new ArrayList<>(Arrays.asList(args).subList(2, args.length));
                handler.getStorage().orderGroups(sender, order);
                sender.sendMessage("§aGroup order saved. Reload to apply changes.");
                break;
            default:
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /ne group <groupName> <action> [value]");
                    return;
                }
                String groupName = args[1];
                String groupAction = args[2].toLowerCase();
                GroupData group = handler.getGroupData(groupName);
                if (group == null) {
                    sender.sendMessage("§cGroup '" + groupName + "' not found.");
                    return;
                }

                if (groupAction.equals("info")) {
                    sender.sendMessage("§e--- Group Info: " + group.getGroupName() + " ---");
                    sender.sendMessage("§7Prefix: §f" + group.getPrefix());
                    sender.sendMessage("§7Suffix: §f" + group.getSuffix());
                    sender.sendMessage("§7Permission: §f" + group.getPermission());
                    sender.sendMessage("§7Priority: §f" + group.getSortPriority());
                    return;
                }

                if (args.length < 4 && !groupAction.equals("clear")) {
                    sender.sendMessage("§cUsage: /ne group " + groupName + " " + groupAction + " <value>");
                    return;
                }

                String value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

                switch (groupAction) {
                    case "perm":
                        group.setPermission(value);
                        handler.getStorage().save(group);
                        sender.sendMessage("§aSet permission for " + groupName + " to: " + value);
                        break;
                    case "prefix":
                        group.setPrefix(value);
                        handler.getStorage().save(group);
                        sender.sendMessage("§aSet prefix for " + groupName + " to: " + ChatColor.translateAlternateColorCodes('&', value));
                        break;
                    case "suffix":
                        group.setSuffix(value);
                        handler.getStorage().save(group);
                        sender.sendMessage("§aSet suffix for " + groupName + " to: " + ChatColor.translateAlternateColorCodes('&', value));
                        break;
                    case "priority":
                        try {
                            int priority = Integer.parseInt(args[3]);
                            group.setSortPriority(priority);
                            handler.getStorage().save(group);
                            sender.sendMessage("§aSet priority for " + groupName + " to: " + priority);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("§cPriority must be a number.");
                        }
                        break;
                    case "clear":
                        if (args.length < 4 || (!args[3].equalsIgnoreCase("prefix") && !args[3].equalsIgnoreCase("suffix"))) {
                            sender.sendMessage("§cUsage: /ne group " + groupName + " clear <prefix|suffix>");
                            return;
                        }
                        if (args[3].equalsIgnoreCase("prefix")) group.setPrefix("");
                        if (args[3].equalsIgnoreCase("suffix")) group.setSuffix("");
                        handler.getStorage().save(group);
                        sender.sendMessage("§aCleared " + args[3] + " for " + groupName);
                        break;
                    default:
                        sender.sendMessage("§cUnknown group action. Use perm, prefix, suffix, priority, clear, or info.");
                        break;
                }
                sender.sendMessage("§eReload the plugin ('/ne reload') to see changes take effect.");
                break;
        }
    }
}