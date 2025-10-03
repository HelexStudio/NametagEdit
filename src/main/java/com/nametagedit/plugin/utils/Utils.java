package com.nametagedit.plugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for NametagEdit plugin.
 */
public class Utils {

    public static YamlConfiguration getConfig(File file, String resourceName, Plugin plugin) {
        if (!file.exists()) {
            plugin.saveResource(resourceName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public static YamlConfiguration getConfig(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

    public static List<Player> getOnline() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    public static String deformat(String text) {
        if (text == null) return "";
        return text.replaceAll("§[0-9A-FK-ORa-fk-or]", "").replaceAll("&[0-9A-FK-ORa-fk-or]", "");
    }

    public static String format(String text) {
        if (text == null) return "";
        return text.replaceAll("&([0-9A-Fa-fK-Ok-oRr])", "§$1");
    }

    public static String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}