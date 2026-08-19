/*
 * FakePlayers - NMS 反射工具 - 通过反射构造 ClientboundPlayerInfoUpdatePacket/RemovePacket，把假人塞进 Tab 列表（适配 Paper 1.21.8 包路径）
 * Copyright (c) 2026 TinyAII  ·  MIT License（见仓库根 LICENSE）
 *
 * 反编译恢复：源码随开发服清理丢失，本源码由已发布 jar（v1.0.0）经 CFR 0.152 反编译恢复后做
 *             开源清理（还原中文/补类头/LICENSE），逻辑与原始版一致。本插件因用 NmsUtil 反射
 *             net.minecraft.network.protocol.game 包内类构造发包，适配 Paper 1.21.8。
 */
package com.mcadmin.fakeplayers;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.entity.Player;

public final class NmsUtil {
    private static final String PKG_PACKET = "net.minecraft.network.protocol.game.";
    private static final String INFO_UPDATE = "net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket";
    private static final String INFO_REMOVE = "net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket";
    private static final String ENTRY = "net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry";
    private static final String ACTION = "net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action";
    private static final String GAME_TYPE = "net.minecraft.world.level.GameType";
    private static final String CHAT_SESSION_DATA = "net.minecraft.network.chat.RemoteChatSession$Data";
    private static final String COMPONENT = "net.minecraft.network.chat.Component";
    private static final String PACKET = "net.minecraft.network.protocol.Packet";
    private static Class<?> infoUpdateClass;
    private static Class<?> infoRemoveClass;
    private static Class<?> entryClass;
    private static Class<?> actionClass;
    private static Class<?> gameTypeClass;
    private static Constructor<?> updateCtor;
    private static Constructor<?> entryCtor;
    private static Constructor<?> removeCtor;
    private static Object survivalGameType;
    private static Object latencyAction;
    private static final String[] INIT_ACTION_NAMES;
    private static Object[] initActions;
    private static Method getHandleMethod;
    private static Field connectionField;
    private static Method sendMethod;

    private NmsUtil() {
    }

    public static UUID offlineUuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    public static UUID extractProfileId(String skinValue) {
        if (skinValue == null || skinValue.isEmpty()) {
            return null;
        }
        try {
            byte[] dec = Base64.getDecoder().decode(skinValue);
            String json = new String(dec, StandardCharsets.UTF_8);
            Matcher m = Pattern.compile("\"profileId\"\\s*:\\s*\"([0-9a-fA-F]{32})\"").matcher(json);
            if (m.find()) {
                String hex = m.group(1);
                return UUID.fromString(hex.substring(0, 8) + "-" + hex.substring(8, 12) + "-" + hex.substring(12, 16) + "-" + hex.substring(16, 20) + "-" + hex.substring(20, 32));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    public static GameProfile buildProfile(UUID uuid, String name, String[] skin) {
        GameProfile profile = new GameProfile(uuid, name);
        if (skin != null && skin.length >= 2 && skin[1] != null && !skin[1].isEmpty()) {
            String value = skin[1];
            String signature = skin.length >= 3 ? skin[2] : null;
            profile.getProperties().put("textures", new Property("textures", value, signature));
        }
        return profile;
    }

    public static Object buildEntry(UUID uuid, String name, String[] skin, int latency, int listOrder) {
        try {
            GameProfile profile = NmsUtil.buildProfile(uuid, name, skin);
            return entryCtor.newInstance(uuid, profile, true, latency, survivalGameType, null, true, listOrder, null);
        }
        catch (Exception e) {
            throw new RuntimeException("构造 PlayerInfo Entry 失败", e);
        }
    }

    public static Object buildAddPacket(List<Object> entries) {
        try {
            @SuppressWarnings({"unchecked","rawtypes"}) EnumSet actions = EnumSet.noneOf((Class) actionClass);
            for (Object a : initActions) {
                actions.add(a);
            }
            return updateCtor.newInstance(actions, entries);
        }
        catch (Exception e) {
            throw new RuntimeException("构造 ADD_PLAYER 包失败", e);
        }
    }

    public static Object buildRemovePacket(List<UUID> uuids) {
        try {
            return removeCtor.newInstance(new ArrayList<UUID>(uuids));
        }
        catch (Exception e) {
            throw new RuntimeException("构造 PlayerInfoRemove 包失败", e);
        }
    }

    public static Object buildLatencyPacket(List<Object> entries) {
        try {
            @SuppressWarnings({"unchecked","rawtypes"}) EnumSet actions = EnumSet.noneOf((Class) actionClass);
            actions.add(latencyAction);
            return updateCtor.newInstance(actions, entries);
        }
        catch (Exception e) {
            throw new RuntimeException("构造 UPDATE_LATENCY 包失败", e);
        }
    }

    public static void sendPacket(Player player, Object packet) {
        try {
            Object handle = getHandleMethod.invoke((Object)player, new Object[0]);
            Object connection = connectionField.get(handle);
            sendMethod.invoke(connection, packet);
        }
        catch (Exception e) {
            throw new RuntimeException("发包给 " + player.getName() + " 失败", e);
        }
    }

    static {
        INIT_ACTION_NAMES = new String[]{"ADD_PLAYER", "INITIALIZE_CHAT", "UPDATE_GAME_MODE", "UPDATE_LISTED", "UPDATE_LATENCY", "UPDATE_DISPLAY_NAME", "UPDATE_HAT", "UPDATE_LIST_ORDER"};
        try {
            ClassLoader cl = NmsUtil.class.getClassLoader();
            infoUpdateClass = Class.forName(INFO_UPDATE, false, cl);
            infoRemoveClass = Class.forName(INFO_REMOVE, false, cl);
            entryClass = Class.forName(ENTRY, false, cl);
            actionClass = Class.forName(ACTION, false, cl);
            gameTypeClass = Class.forName(GAME_TYPE, false, cl);
            Class<GameProfile> gameProfileClass = GameProfile.class;
            Class<?> componentClass = Class.forName(COMPONENT, false, cl);
            Class<?> chatSessionDataClass = Class.forName(CHAT_SESSION_DATA, false, cl);
            updateCtor = infoUpdateClass.getConstructor(EnumSet.class, List.class);
            entryCtor = entryClass.getConstructor(UUID.class, gameProfileClass, Boolean.TYPE, Integer.TYPE, gameTypeClass, componentClass, Boolean.TYPE, Integer.TYPE, chatSessionDataClass);
            removeCtor = infoRemoveClass.getConstructor(List.class);
            initActions = new Object[INIT_ACTION_NAMES.length];
            for (int i = 0; i < INIT_ACTION_NAMES.length; ++i) {
                NmsUtil.initActions[i] = Enum.valueOf((Class) actionClass, INIT_ACTION_NAMES[i]);
            }
            latencyAction = Enum.valueOf((Class) actionClass, "UPDATE_LATENCY");
            survivalGameType = gameTypeClass.getField("SURVIVAL").get(null);
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer", false, cl);
            getHandleMethod = craftPlayerClass.getMethod("getHandle", new Class[0]);
            Class<?> serverPlayerClass = getHandleMethod.getReturnType();
            connectionField = serverPlayerClass.getField("connection");
            Class<?> packetClass = Class.forName(PACKET, false, cl);
            Class<?> listenerClass = connectionField.getType();
            Method found = null;
            Class<?> cursor = listenerClass;
            while (cursor != null && found == null) {
                try {
                    found = cursor.getDeclaredMethod("send", packetClass);
                }
                catch (NoSuchMethodException ignored) {
                    cursor = cursor.getSuperclass();
                }
            }
            if (found == null) {
                throw new IllegalStateException("未找到 send(Packet) 方法，Paper 版本不受支持");
            }
            found.setAccessible(true);
            sendMethod = found;
        }
        catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}

