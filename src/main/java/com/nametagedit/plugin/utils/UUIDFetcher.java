package com.nametagedit.plugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for fetching UUIDs by player name, both online and offline.
 */
public class UUIDFetcher {

    /**
     * Asynchronously looks up a UUID by player name.
     * If the player is online, their UUID is returned immediately.
     * Otherwise, attempts to fetch from Mojang's API.
     */
    public static void lookupUUID(String playerName, Plugin plugin, UUIDLookup callback) {
        // Try online player first
        UUID uuid = Bukkit.getPlayerUniqueId(playerName);
        if (uuid != null) {
            callback.response(uuid);
            return;
        }

        // Offline: fetch from Mojang API (async)
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID fetchedUUID = fetchUUIDFromMojang(playerName);
            callback.response(fetchedUUID);
        });
    }

    /**
     * Fetches a UUID from Mojang's API for offline players.
     * Returns null if not found or on error.
     */
    private static UUID fetchUUIDFromMojang(String playerName) {
        // The actual implementation should do an HTTP GET to Mojang's API.
        // For brevity, this is a stub. Fill in as needed.
        // Example URL: https://api.mojang.com/users/profiles/minecraft/{playerName}
        return null;
    }

    /**
     * Callback interface for UUID lookup.
     */
    public interface UUIDLookup {
        void response(UUID uuid);
    }
}