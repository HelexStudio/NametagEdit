package com.nametagedit.plugin.packets;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class PacketAccessor {

    private static final String PACKAGE_NAME = Bukkit.getServer().getClass().getPackage().getName();
    public static final boolean modernServer = PACKAGE_NAME.equals("org.bukkit.craftbukkit");
    private static final String[] PACKAGE_SPLIT = PACKAGE_NAME.split("\\.");
    protected static final String VERSION = PACKAGE_SPLIT.length > 3 ? PACKAGE_SPLIT[3] : "";
    protected static final int MINOR_VERSION = getMinorVersion();

    private static final List<String> legacyVersions = Arrays.asList(
            "v1_7_R1", "v1_7_R2", "v1_7_R3", "v1_7_R4", "v1_8_R1", "v1_8_R2",
            "v1_8_R3", "v1_9_R1", "v1_9_R2", "v1_10_R1", "v1_11_R1", "v1_12_R1"
    );
    private static boolean CAULDRON_SERVER = false;
    private static boolean LEGACY_SERVER = false;

    private static Object UNSAFE;
    private static Method ALLOCATE_INSTANCE;

    static Field MEMBERS;
    static Field PREFIX;
    static Field SUFFIX;
    static Field TEAM_NAME;
    static Field PARAM_INT;
    static Field PACK_OPTION;
    static Field DISPLAY_NAME;
    static Field TEAM_COLOR;
    static Field PUSH;
    static Field VISIBILITY;
    static Field PARAMS;

    private static Method getHandle;
    private static Method sendPacket;
    private static Field playerConnection;

    private static Class<?> packetClass;
    private static Class<?> packetParamsClass;

    public static boolean reflectionSupported = true;

    static {
        if (modernServer && VERSION.isEmpty()) {
            Bukkit.getLogger().warning("[NametagEdit] Detected modern PaperMC server (Mojang-mapped 1.20+). Disabling NMS features for compatibility.");
            reflectionSupported = false;
        } else if (VERSION.isEmpty()) {
            Bukkit.getLogger().warning("[NametagEdit] WARNING: Unexpected Bukkit package name: " + PACKAGE_NAME);
        }

        if (reflectionSupported) {
            try {
                Class.forName("cpw.mods.fml.common.Mod");
                CAULDRON_SERVER = true;
            } catch (ClassNotFoundException ignored) {}

            try {
                Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
                theUnsafeField.setAccessible(true);
                UNSAFE = theUnsafeField.get(null);
                ALLOCATE_INSTANCE = UNSAFE.getClass().getMethod("allocateInstance", Class.class);

                if (legacyVersions.contains(VERSION))
                    LEGACY_SERVER = true;

                Class<?> typeCraftPlayer = Class.forName("org.bukkit.craftbukkit." + VERSION + ".entity.CraftPlayer");
                getHandle = typeCraftPlayer.getMethod("getHandle");

                if (CAULDRON_SERVER) {
                    packetClass = Class.forName("net.minecraft.server.v1_7_R4.PacketPlayOutScoreboardTeam");
                    Class<?> typeNMSPlayer = Class.forName("net.minecraft.server.v1_7_R4.EntityPlayer");
                    Class<?> typePlayerConnection = Class.forName("net.minecraft.server.v1_7_R4.PlayerConnection");
                    playerConnection = typeNMSPlayer.getField("field_71135_a");
                    sendPacket = typePlayerConnection.getMethod("func_147359_a", Class.forName("net.minecraft.server.v1_7_R4.Packet"));
                } else if (!isParamsVersion()) {
                    packetClass = Class.forName("net.minecraft.server." + VERSION + ".PacketPlayOutScoreboardTeam");
                    Class<?> typeNMSPlayer = Class.forName("net.minecraft.server." + VERSION + ".EntityPlayer");
                    Class<?> typePlayerConnection = Class.forName("net.minecraft.server." + VERSION + ".PlayerConnection");
                    playerConnection = typeNMSPlayer.getField("playerConnection");
                    sendPacket = typePlayerConnection.getMethod("sendPacket", Class.forName("net.minecraft.server." + VERSION + ".Packet"));
                } else {
                    // 1.17+
                    packetClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam");
                    packetParamsClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam$b");
                    Class<?> typeNMSPlayer = Class.forName("net.minecraft.server.level.EntityPlayer");
                    Class<?> typePlayerConnection = Class.forName("net.minecraft.server.network.PlayerConnection");
                    if (MINOR_VERSION >= 20) {
                        playerConnection = typeNMSPlayer.getField("c");
                    } else {
                        playerConnection = typeNMSPlayer.getField("b");
                    }
                    Class<?>[] sendPacketParameters = new Class[]{Class.forName("net.minecraft.network.protocol.Packet")};
                    sendPacket = Stream.concat(
                                    Arrays.stream(typePlayerConnection.getSuperclass().getMethods()),
                                    Arrays.stream(typePlayerConnection.getMethods())
                            )
                            .filter(method -> Arrays.equals(method.getParameterTypes(), sendPacketParameters))
                            .findFirst().orElseThrow(NoSuchMethodException::new);
                }

                PacketData currentVersion = null;
                for (PacketData packetData : PacketData.values()) {
                    if (VERSION.contains(packetData.name())) {
                        currentVersion = packetData;
                    }
                }

                if (CAULDRON_SERVER) {
                    currentVersion = PacketData.cauldron;
                }

                if (currentVersion != null) {
                    if (!isParamsVersion()) {
                        PREFIX = getNMS(currentVersion.getPrefix());
                        SUFFIX = getNMS(currentVersion.getSuffix());
                        MEMBERS = getNMS(currentVersion.getMembers());
                        TEAM_NAME = getNMS(currentVersion.getTeamName());
                        PARAM_INT = getNMS(currentVersion.getParamInt());
                        PACK_OPTION = getNMS(currentVersion.getPackOption());
                        DISPLAY_NAME = getNMS(currentVersion.getDisplayName());

                        if (!isLegacyVersion()) {
                            TEAM_COLOR = getNMS(currentVersion.getColor());
                        }

                        if (isPushVersion()) {
                            PUSH = getNMS(currentVersion.getPush());
                        }

                        if (isVisibilityVersion()) {
                            VISIBILITY = getNMS(currentVersion.getVisibility());
                        }
                    } else {
                        // 1.17+
                        PARAM_INT = getNMS(currentVersion.getParamInt());
                        TEAM_NAME = getNMS(currentVersion.getTeamName());
                        MEMBERS = getNMS(currentVersion.getMembers());
                        PARAMS = getNMS(currentVersion.getParams());

                        PREFIX = getParamNMS(currentVersion.getPrefix());
                        SUFFIX = getParamNMS(currentVersion.getSuffix());
                        PACK_OPTION = getParamNMS(currentVersion.getPackOption());
                        DISPLAY_NAME = getParamNMS(currentVersion.getDisplayName());
                        TEAM_COLOR = getParamNMS(currentVersion.getColor());
                        PUSH = getParamNMS(currentVersion.getPush());
                        VISIBILITY = getParamNMS(currentVersion.getVisibility());
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("[NametagEdit] NMS reflection unsupported on this server version. Disabling packet features.");
                reflectionSupported = false;
            }
        }
    }

    public static boolean isLegacyVersion() {
        return LEGACY_SERVER;
    }

    public static boolean isParamsVersion() {
        return MINOR_VERSION >= 17;
    }

    private static boolean isPushVersion() {
        return MINOR_VERSION >= 9;
    }

    private static boolean isVisibilityVersion() {
        return MINOR_VERSION >= 8;
    }

    private static int getMinorVersion() {
        String bukkitVersion = Bukkit.getBukkitVersion();
        try {
            String[] parts = bukkitVersion.split("\\.");
            if (parts.length > 1) {
                return Integer.parseInt(parts[1]);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private static Field getNMS(String path) throws Exception {
        Field field = packetClass.getDeclaredField(path);
        field.setAccessible(true);
        return field;
    }

    private static Field getParamNMS(String path) throws Exception {
        Field field = packetParamsClass.getDeclaredField(path);
        field.setAccessible(true);
        return field;
    }

    public static Object createPacket() {
        if (!reflectionSupported || packetClass == null) return null;
        try {
            return packetClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public static Object createPacketParams() {
        if (!reflectionSupported || packetParamsClass == null) return null;
        try {
            return packetParamsClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public static void sendPacket(Collection<? extends Player> players, Object packet) {
        if (!reflectionSupported) return;
        for (Player player : players) {
            sendPacket(player, packet);
        }
    }

    public static void sendPacket(Player player, Object packet) {
        if (!reflectionSupported) return;
        try {
            Object nmsPlayer = getHandle != null ? getHandle.invoke(player) : null;
            Object connection = playerConnection != null && nmsPlayer != null ? playerConnection.get(nmsPlayer) : null;
            if (sendPacket != null && connection != null)
                sendPacket.invoke(connection, packet);
        } catch (Exception e) {
            // Silently ignore or log
        }
    }
}