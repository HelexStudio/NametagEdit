# NametagEdit

[![Dev Builds](https://img.shields.io/badge/Jenkins-Development%20Builds-lightgrey.svg)](https://ci.nametagedit.com/job/NametagEdit)
[![Discord](https://img.shields.io/discord/893946808949850122?color=7289DA&label=Discord&logo=discord&logoColor=white)](https://discord.gg/fuC9vac3eB)
[![Spigot](https://img.shields.io/badge/Spigot-Project%20Page-yellow.svg)](https://www.spigotmc.org/resources/nametagedit.3836/)
[![Support](https://img.shields.io/badge/Minecraft-1.8%20--%201.20+-green.svg)](https://www.spigotmc.org/resources/nametagedit.3836/)
[![JDK](https://img.shields.io/badge/JDK-17+-blue.svg)](https://adoptium.net/)

NametagEdit is a powerful and efficient Bukkit/Spigot plugin that gives you full control over player nametags. Add unique prefixes and suffixes for individual players, or create permission-based group tags that update automatically.

This project is actively maintained by **[HelexStudio](https://github.com/HelexStudio)**, which is committed to providing ongoing support and ensuring compatibility with future Minecraft updates. **This is not the offical version of the plugin**

*   **Official Spigot Page:** [spigotmc.org](https://www.spigotmc.org/resources/nametagedit.3836/)
*   **Development Builds:** [ci.nametagedit.com](https://ci.nametagedit.com/job/NametagEdit)
*   **Support & Updates on Discord:** [discord.gg/fuC9vac3eB](https://discord.gg/fuC9vac3eB)

---

## Current Features

This version has been rebuilt from the ground up to provide a stable and feature-rich experience on modern servers.

*   **Full In-Game Command Suite:** Manage players, groups, and plugin settings directly from the game.
*   **Individual & Group Tags:** Set custom prefixes/suffixes for players or create permission-based tags for server ranks.
*   **Dynamic Permission Updates:** Hooks directly into **LuckPerms** to update tags instantly when a player's permissions change.
*   **Persistent Flatfile Storage:** All player and group data is saved in easy-to-manage `players.yml` and `groups.yml` files with a clean, quoted format.
*   **Sortable Tab List:** Control the order of players in the tab list using a priority system for both players and groups.
*   **PlaceholderAPI Support:** Use thousands of placeholders from other plugins directly in your prefixes and suffixes.
*   **Hex Color Support:** Use custom hex colors (`&#RRGGBB`) in nametags on Minecraft 1.16 and newer.

---

## Commands and Permissions

NametagEdit uses a granular permission system to give you full control over its features.

| Command                                | Permission                    | Description                                       |
| -------------------------------------- | ----------------------------- | ------------------------------------------------- |
| `/ne help`                             | `nametagedit.use`             | Allows a player to see the command list.          |
| `/ne reload`                           | `nametagedit.reload`          | Allows a user to reload the plugin's data.        |
| `/ne debug`, `/ne longtags`            | `nametagedit.reload`          | Toggles administrative/debug features.            |
| `/ne priority`                         | `nametagedit.priority`        | Allows a user to view SortPriority information.   |
| `/ne player <player> ...` (self)       | `nametagedit.edit.self`       | Allows a player to edit their own prefix/suffix.  |
| `/ne player <player> ...` (others)     | `nametagedit.edit.others`     | Allows a player to edit anyone's prefix/suffix.   |
| `/ne clear <player>` (self)            | `nametagedit.clear.self`      | Allows a player to clear their own prefix/suffix. |
| `/ne clear <player>` (others)          | `nametagedit.clear.others`    | Allows a player to clear anyone's prefix/suffix.  |
| `/ne group ...`                        | `nametagedit.groups`          | Allows usage of all group subcommands.            |

---

## Frequently Asked Questions

#### Q: My nametag is being cut short!

**A:** This is for compatibility with older Minecraft versions. Use the command `/ne longtags` to enable full-length nametags for modern servers (1.13+).

#### Q: Will this allow me to change my skin or actual username?

**A:** No. This plugin uses Minecraft's scoreboard system to display prefixes and suffixes. It does not change a player's actual username or skin.

#### Q: My client crashes with a "Cannot remove from ID#" error. Why?

**A:** This is a conflict with another plugin that also modifies scoreboard teams (e.g., another nametag plugin, some team-based minigames). A player can only be on one scoreboard team at a time. You must choose one plugin to handle nametags.

---

## Upcoming Features (Roadmap)

The following features are planned for future updates to enhance functionality and restore legacy support.

*   **MySQL Database Support:** Implement a robust database storage option for large networks.
*   **Storage Converters:** Add commands to seamlessly migrate data between flatfile and MySQL.
*   **Expanded Plugin Hooks:**
    *   **Permissions:** Add explicit support for PermissionsEx, zPermissions, and EssentialsGroupManager.
    *   **Placeholders:** Restore support for MVdWPlaceholderAPI.
    *   **Compatibility:** Restore hooks for LibsDisguises and Guilds.
*   **API Enhancements:** Further develop the developer API for deeper integration possibilities.
