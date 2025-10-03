package com.nametagedit.plugin.packets;

import org.bukkit.Bukkit;

public class VersionChecker {

    /**
     * Returns true if the server supports hex colors (MC 1.16+).
     */
    public static boolean canHex() {
        try {
            String version = Bukkit.getBukkitVersion(); // e.g., "1.21.0-R0.1-SNAPSHOT"
            String[] split = version.split("-")[0].split("\\.");
            int major = Integer.parseInt(split[0]);
            int minor = split.length > 1 ? Integer.parseInt(split[1]) : 0;
            return (major > 1 || (major == 1 && minor >= 16));
        } catch (Exception e) {
            // If unexpected format, default to true (safe)
            return true;
        }
    }
}