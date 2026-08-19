/*
 * FakePlayers - 假人插件主类 - Tab 假人列表塞假人，随机上下线 + 孤独搭话 + MOTD 虚高 + ping 跳动 + 防重名
 * Copyright (c) 2026 TinyAII  ·  MIT License（见仓库根 LICENSE）
 *
 * 反编译恢复：源码随开发服清理丢失，本源码由已发布 jar（v1.0.0）经 CFR 0.152 反编译恢复后做
 *             开源清理（还原中文/补类头/LICENSE），逻辑与原始版一致。本插件因用 NmsUtil 反射
 *             net.minecraft.network.protocol.game 包内类构造发包，适配 Paper 1.21.8。
 */
package com.mcadmin.fakeplayers;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mcadmin.fakeplayers.NameGenerator;
import com.mcadmin.fakeplayers.NmsUtil;
import com.mcadmin.fakeplayers.SkinPool;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class FakePlayersPlugin
extends JavaPlugin
implements Listener {
    private boolean enabled = true;
    private int count = 20;
    private int pingMin = 20;
    private int pingMax = 150;
    private boolean useSkin = true;
    private boolean motdBoost = true;
    private int motdExtra = 10;
    private boolean motdShowNames = true;
    private boolean pingJitter = true;
    private int pingJitterInterval = 10;
    private boolean simulateJoin = false;
    private int simulateIntervalMin = 30;
    private int simulateIntervalMax = 120;
    private int simulateOutMin = 1;
    private int simulateOutMax = 2;
    private int simulateInMin = 1;
    private int simulateInMax = 2;
    private int onlineMin = 15;
    private boolean joinMessage = true;
    private boolean avoidRealNames = true;
    private boolean lonelyChat = true;
    private int lonelyChatDelayMin = 60;
    private int lonelyChatDelayMax = 180;
    private int lonelyChatCooldownDays = 1;
    private final List<String> lonelyChatMessages = new ArrayList<String>();
    private final List<String> customNames = new ArrayList<String>();
    private final List<FakePlayer> onlineFakes = new CopyOnWriteArrayList<FakePlayer>();
    private final List<FakePlayer> offlineFakes = new ArrayList<FakePlayer>();
    private final Random rnd = new Random();
    private BukkitTask pingTask;
    private BukkitTask simulateTask;
    private File cooldownFile;
    private YamlConfiguration cooldownConfig;

    public void onEnable() {
        this.saveDefaultConfig();
        this.loadConfig();
        this.loadCooldownData();
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.getCommand("fakeplayer").setExecutor((CommandExecutor)this);
        this.bootstrap();
        String banner = " _____ _                _    ___ ___\n|_   _(_)_ __  _   _   / \\  |_ _|_ _|\n  | | | | '_ \\| | | | / _ \\  | | | |\n  | | | | | | | |_| |/ ___ \\ | | | |\n  |_| |_|_| |_|\\__, /_/   \\_\\___|___|\n               |___/\n";
        banner.lines().forEach(line -> this.getLogger().info((String)line));
        this.getLogger().info("FakePlayers 假人插件 v" + this.getDescription().getVersion() + " - TinyAII 出品");
        this.getLogger().info("假人池 " + this.count + " 个，在线 " + this.onlineFakes.size() + " 个（Tab 假人 / MOTD 虚高 / 随机上下线）");
    }

    public void onDisable() {
        this.cancelTasks();
        this.bringAllOffline();
    }

    private void loadConfig() {
        this.reloadConfig();
        this.customNames.clear();
        this.enabled = this.getConfig().getBoolean("fake-players.enabled", true);
        this.count = this.getConfig().getInt("fake-players.count", 20);
        this.pingMin = Math.max(5, this.getConfig().getInt("fake-players.ping-min", 20));
        this.pingMax = Math.max(this.pingMin, this.getConfig().getInt("fake-players.ping-max", 120));
        this.useSkin = this.getConfig().getBoolean("fake-players.use-skin", true);
        this.motdBoost = this.getConfig().getBoolean("fake-players.motd-boost", true);
        this.motdExtra = Math.max(0, this.getConfig().getInt("fake-players.motd-extra", 10));
        this.motdShowNames = this.getConfig().getBoolean("fake-players.motd-show-names", true);
        this.pingJitter = this.getConfig().getBoolean("fake-players.ping-jitter", true);
        this.pingJitterInterval = Math.max(1, this.getConfig().getInt("fake-players.ping-jitter-interval", 10));
        this.simulateJoin = this.getConfig().getBoolean("fake-players.simulate-join", false);
        this.simulateIntervalMin = Math.max(1, this.getConfig().getInt("fake-players.simulate-interval-min", 30));
        this.simulateIntervalMax = Math.max(this.simulateIntervalMin, this.getConfig().getInt("fake-players.simulate-interval-max", 120));
        this.simulateOutMin = Math.max(1, this.getConfig().getInt("fake-players.simulate-out-min", 1));
        this.simulateOutMax = Math.max(this.simulateOutMin, this.getConfig().getInt("fake-players.simulate-out-max", 2));
        this.simulateInMin = Math.max(1, this.getConfig().getInt("fake-players.simulate-in-min", 1));
        this.simulateInMax = Math.max(this.simulateInMin, this.getConfig().getInt("fake-players.simulate-in-max", 2));
        this.onlineMin = Math.max(0, Math.min(this.getConfig().getInt("fake-players.online-min", 15), Math.max(0, this.count - 1)));
        this.joinMessage = this.getConfig().getBoolean("fake-players.join-message", true);
        this.avoidRealNames = this.getConfig().getBoolean("fake-players.avoid-real-names", true);
        this.lonelyChat = this.getConfig().getBoolean("fake-players.lonely-chat", true);
        this.lonelyChatDelayMin = Math.max(10, this.getConfig().getInt("fake-players.lonely-chat-delay-min", 60));
        this.lonelyChatDelayMax = Math.max(this.lonelyChatDelayMin, this.getConfig().getInt("fake-players.lonely-chat-delay-max", 180));
        this.lonelyChatCooldownDays = Math.max(1, this.getConfig().getInt("fake-players.lonely-chat-cooldown-days", 1));
        this.lonelyChatMessages.clear();
        this.lonelyChatMessages.addAll(this.getConfig().getStringList("fake-players.lonely-chat-messages"));
        if (this.lonelyChatMessages.isEmpty()) {
            this.lonelyChatMessages.addAll(List.of("你在哪？？？？？？？", "你好，你能借我3个钻石吗？", "这服好冷清啊", "这里钓鱼还不错哎", "这里钓鱼真的不错，哈哈哈", "找本书先", "哪里有村庄？？？"));
        }
        if (this.getConfig().isConfigurationSection("fake-players.custom-names")) {
            for (String key : this.getConfig().getConfigurationSection("fake-players.custom-names").getKeys(false)) {
                String raw = this.getConfig().getString("fake-players.custom-names." + key);
                String clean = NameGenerator.sanitize(raw);
                if (clean == null) continue;
                this.customNames.add(clean);
            }
        }
    }

    private int randomPing() {
        return this.pingMin + this.rnd.nextInt(this.pingMax - this.pingMin + 1);
    }

    private List<FakePlayer> generateFakes() {
        String[] skin;
        ArrayList<FakePlayer> list = new ArrayList<FakePlayer>();
        HashSet<String> used = new HashSet<String>();
        if (this.avoidRealNames) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                used.add(p.getName().toLowerCase());
            }
        }
        ArrayList skinPool = new ArrayList();
        if (this.useSkin) {
            Collections.addAll(skinPool, SkinPool.SKINS);
            Collections.shuffle(skinPool, this.rnd);
        }
        int[] skinCursor = new int[]{0};
        for (String name : this.customNames) {
            String[] stringArray;
            if (list.size() >= this.count) break;
            if (!used.add(name.toLowerCase())) continue;
            if (skinCursor[0] < skinPool.size()) {
                int n = skinCursor[0];
                skinCursor[0] = n + 1;
                stringArray = (String[])skinPool.get(n);
            } else {
                stringArray = null;
            }
            skin = stringArray;
            list.add(new FakePlayer(name, skin, this.randomPing(), list.size()));
        }
        int guard = 0;
        while (list.size() < this.count && guard++ < this.count * 50) {
            String[] stringArray;
            String name;
            name = NameGenerator.random();
            if (!used.add(name.toLowerCase())) continue;
            if (skinCursor[0] < skinPool.size()) {
                int n = skinCursor[0];
                skinCursor[0] = n + 1;
                stringArray = (String[])skinPool.get(n);
            } else {
                stringArray = null;
            }
            skin = stringArray;
            list.add(new FakePlayer(name, skin, this.randomPing(), list.size()));
        }
        return list;
    }

    private void bootstrap() {
        this.cancelTasks();
        this.bringAllOffline();
        List<FakePlayer> pool = this.generateFakes();
        this.onlineFakes.addAll(pool);
        if (!this.enabled || this.onlineFakes.isEmpty()) {
            return;
        }
        List<Object> entries = this.entriesOf(this.onlineFakes);
        this.sendToAllPlayers(NmsUtil.buildAddPacket(entries));
        this.scheduleTasks();
    }

    private void cancelTasks() {
        if (this.pingTask != null) {
            this.pingTask.cancel();
            this.pingTask = null;
        }
        if (this.simulateTask != null) {
            this.simulateTask.cancel();
            this.simulateTask = null;
        }
    }

    private void scheduleTasks() {
        if (this.pingJitter) {
            long ticks = (long)this.pingJitterInterval * 20L;
            this.pingTask = Bukkit.getScheduler().runTaskTimer((Plugin)this, this::refreshPing, ticks, ticks);
        }
        if (this.simulateJoin) {
            this.scheduleSimulate();
        }
    }

    private void scheduleSimulate() {
        if (!this.simulateJoin) {
            return;
        }
        long delayTicks = (long)this.randomInRange(this.simulateIntervalMin, this.simulateIntervalMax) * 20L;
        this.simulateTask = Bukkit.getScheduler().runTaskLater((Plugin)this, () -> {
            this.simulateTick();
            this.scheduleSimulate();
        }, delayTicks);
    }

    private int randomInRange(int min, int max) {
        if (max <= min) {
            return min;
        }
        return min + this.rnd.nextInt(max - min + 1);
    }

    private void takeOffline(List<FakePlayer> gone) {
        if (gone.isEmpty()) {
            return;
        }
        if (this.joinMessage) {
            for (FakePlayer f : gone) {
                Bukkit.broadcastMessage((String)(String.valueOf(ChatColor.YELLOW) + f.name + "离开了游戏"));
            }
        }
        this.sendToAllPlayers(NmsUtil.buildRemovePacket(this.uuidsOf(gone)));
        this.onlineFakes.removeAll(gone);
        this.offlineFakes.addAll(gone);
    }

    private void bringOnline(List<FakePlayer> back) {
        if (back.isEmpty()) {
            return;
        }
        if (this.joinMessage) {
            for (FakePlayer f : back) {
                Bukkit.broadcastMessage((String)(String.valueOf(ChatColor.YELLOW) + f.name + "加入了游戏"));
            }
        }
        this.sendToAllPlayers(NmsUtil.buildAddPacket(this.entriesOf(back)));
        this.offlineFakes.removeAll(back);
        this.onlineFakes.addAll(back);
    }

    private void bringAllOffline() {
        if (this.onlineFakes.isEmpty()) {
            return;
        }
        this.sendToAllPlayers(NmsUtil.buildRemovePacket(this.uuidsOf(this.onlineFakes)));
        this.onlineFakes.clear();
        this.offlineFakes.clear();
    }

    private void refreshPing() {
        if (!this.enabled || this.onlineFakes.isEmpty()) {
            return;
        }
        ArrayList<Object> entries = new ArrayList<Object>(this.onlineFakes.size());
        for (FakePlayer f : this.onlineFakes) {
            f.latency = this.randomPing();
            entries.add(NmsUtil.buildEntry(f.uuid, f.name, f.skin, f.latency, f.listOrder));
        }
        this.sendToAllPlayers(NmsUtil.buildLatencyPacket(entries));
    }

    private void simulateTick() {
        int maxUp;
        int maxDown;
        if (!this.enabled) {
            return;
        }
        if (this.onlineFakes.size() > this.onlineMin && (maxDown = Math.min(this.simulateOutMax, this.onlineFakes.size() - this.onlineMin)) >= this.simulateOutMin) {
            int k = this.randomInRange(this.simulateOutMin, maxDown);
            this.takeOffline(this.pickRandom(this.onlineFakes, k));
        }
        if (!this.offlineFakes.isEmpty() && (maxUp = Math.min(this.simulateInMax, this.offlineFakes.size())) >= this.simulateInMin) {
            int j = this.randomInRange(this.simulateInMin, maxUp);
            this.bringOnline(this.pickRandom(this.offlineFakes, j));
        }
    }

    private List<FakePlayer> pickRandom(List<FakePlayer> src, int k) {
        ArrayList<FakePlayer> copy = new ArrayList<FakePlayer>(src);
        Collections.shuffle(copy, this.rnd);
        return new ArrayList<FakePlayer>(copy.subList(0, Math.min(k, copy.size())));
    }

    private List<Object> entriesOf(List<FakePlayer> fakes) {
        ArrayList<Object> entries = new ArrayList<Object>(fakes.size());
        for (FakePlayer f : fakes) {
            entries.add(NmsUtil.buildEntry(f.uuid, f.name, f.skin, f.latency, f.listOrder));
        }
        return entries;
    }

    private List<UUID> uuidsOf(List<FakePlayer> fakes) {
        ArrayList<UUID> uuids = new ArrayList<UUID>(fakes.size());
        for (FakePlayer f : fakes) {
            uuids.add(f.uuid);
        }
        return uuids;
    }

    private void sendToAllPlayers(Object packet) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                NmsUtil.sendPacket(p, packet);
            }
            catch (Exception e) {
                this.getLogger().warning("发包给 " + p.getName() + " 失败：" + e.getMessage());
            }
        }
    }

    private void sendAddTo(Player p) {
        if (!this.enabled || this.onlineFakes.isEmpty()) {
            return;
        }
        NmsUtil.sendPacket(p, NmsUtil.buildAddPacket(this.entriesOf(this.onlineFakes)));
    }

    private void loadCooldownData() {
        this.cooldownFile = new File(this.getDataFolder(), "data.yml");
        this.cooldownConfig = YamlConfiguration.loadConfiguration((File)this.cooldownFile);
    }

    private void scheduleLonelyChat(Player p) {
        long delayTicks = (long)this.randomInRange(this.lonelyChatDelayMin, this.lonelyChatDelayMax) * 20L;
        Bukkit.getScheduler().runTaskLater((Plugin)this, () -> this.checkLonelyChat(p), delayTicks);
    }

    private void checkLonelyChat(Player p) {
        if (!this.lonelyChat || !this.enabled) {
            return;
        }
        if (!p.isOnline()) {
            return;
        }
        if (Bukkit.getOnlinePlayers().size() != 1) {
            return;
        }
        if (this.lonelyChatMessages.isEmpty() || this.onlineFakes.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        long last = this.cooldownConfig.getLong(p.getUniqueId().toString(), 0L);
        long cooldownMs = (long)this.lonelyChatCooldownDays * 24L * 3600000L;
        if (last > 0L && now - last < cooldownMs) {
            return;
        }
        FakePlayer fake = this.onlineFakes.get(this.rnd.nextInt(this.onlineFakes.size()));
        String msg = this.lonelyChatMessages.get(this.rnd.nextInt(this.lonelyChatMessages.size()));
        Bukkit.broadcastMessage((String)("<" + fake.name + "> " + msg));
        this.cooldownConfig.set(p.getUniqueId().toString(), (Object)now);
        try {
            this.cooldownConfig.save(this.cooldownFile);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskLater((Plugin)this, () -> this.sendAddTo(p), 10L);
        if (this.lonelyChat) {
            this.scheduleLonelyChat(p);
        }
    }

    @EventHandler
    public void onServerListPing(PaperServerListPingEvent e) {
        if (!this.motdBoost || !this.enabled || this.onlineFakes.isEmpty()) {
            return;
        }
        int real = Bukkit.getOnlinePlayers().size();
        int total = real + this.onlineFakes.size();
        e.setNumPlayers(total);
        e.setMaxPlayers(Math.max(e.getMaxPlayers(), total + this.motdExtra));
        if (this.motdShowNames) {
            List sample = e.getPlayerSample();
            int n = 0;
            for (FakePlayer f : this.onlineFakes) {
                if (n >= 20) break;
                PlayerProfile prof = Bukkit.createProfile((UUID)f.uuid, (String)f.name);
                if (f.skin != null && f.skin.length >= 2) {
                    String sig = f.skin.length >= 3 ? f.skin[2] : null;
                    prof.setProperty(new ProfileProperty("textures", f.skin[1], sig));
                }
                sample.add(prof);
                ++n;
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("fakeplayers.admin")) {
            sender.sendMessage(String.valueOf(ChatColor.RED) + "你没有权限使用此命令。");
            return true;
        }
        if (args.length == 0) {
            this.showHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "数量": 
            case "count": {
                this.cmdCount(sender, args);
                break;
            }
            case "开": 
            case "on": {
                this.cmdOn(sender);
                break;
            }
            case "关": 
            case "off": {
                this.cmdOff(sender);
                break;
            }
            case "重载": 
            case "reload": {
                this.cmdReload(sender);
                break;
            }
            case "列表": 
            case "list": {
                this.cmdList(sender);
                break;
            }
            default: {
                this.showHelp(sender);
            }
        }
        return true;
    }

    private void showHelp(CommandSender s) {
        s.sendMessage(String.valueOf(ChatColor.GOLD) + "===== 假人插件命令 =====");
        s.sendMessage(String.valueOf(ChatColor.YELLOW) + "/假人 数量 <N>  " + String.valueOf(ChatColor.WHITE) + "设置并立即刷新假人池数量");
        s.sendMessage(String.valueOf(ChatColor.YELLOW) + "/假人 开        " + String.valueOf(ChatColor.WHITE) + "开启假人（重新上线）");
        s.sendMessage(String.valueOf(ChatColor.YELLOW) + "/假人 关        " + String.valueOf(ChatColor.WHITE) + "关闭假人（全部移除）");
        s.sendMessage(String.valueOf(ChatColor.YELLOW) + "/假人 重载      " + String.valueOf(ChatColor.WHITE) + "重载 config.yml 并刷新");
        s.sendMessage(String.valueOf(ChatColor.YELLOW) + "/假人 列表      " + String.valueOf(ChatColor.WHITE) + "查看当前在线假人名单");
    }

    private void cmdCount(CommandSender s, String[] args) {
        int n;
        if (args.length < 2) {
            s.sendMessage(String.valueOf(ChatColor.RED) + "用法：/假人 数量 <N>");
            return;
        }
        try {
            n = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException ex) {
            s.sendMessage(String.valueOf(ChatColor.RED) + "数量必须是数字。");
            return;
        }
        if (n < 0 || n > 200) {
            s.sendMessage(String.valueOf(ChatColor.RED) + "数量范围 0-200（0=全部移除）。");
            return;
        }
        this.count = n;
        this.getConfig().set("fake-players.count", (Object)n);
        this.saveConfig();
        this.onlineMin = Math.max(0, Math.min(this.onlineMin, Math.max(0, this.count - 1)));
        this.bootstrap();
        s.sendMessage(String.valueOf(ChatColor.GREEN) + "已设置假人池 " + n + "，当前在线 " + this.onlineFakes.size() + "。");
    }

    private void cmdOn(CommandSender s) {
        this.enabled = true;
        this.getConfig().set("fake-players.enabled", (Object)true);
        this.saveConfig();
        this.bootstrap();
        s.sendMessage(String.valueOf(ChatColor.GREEN) + "假人已开启，当前在线 " + this.onlineFakes.size() + " 个。");
    }

    private void cmdOff(CommandSender s) {
        this.enabled = false;
        this.getConfig().set("fake-players.enabled", (Object)false);
        this.saveConfig();
        this.cancelTasks();
        this.bringAllOffline();
        s.sendMessage(String.valueOf(ChatColor.GREEN) + "假人已全部移除。");
    }

    private void cmdReload(CommandSender s) {
        this.loadConfig();
        this.bootstrap();
        s.sendMessage(String.valueOf(ChatColor.GREEN) + "配置已重载，当前在线 " + this.onlineFakes.size() + " 个假人。");
    }

    private void cmdList(CommandSender s) {
        if (this.onlineFakes.isEmpty()) {
            s.sendMessage(String.valueOf(ChatColor.YELLOW) + "当前无在线假人。");
            return;
        }
        s.sendMessage(String.valueOf(ChatColor.GOLD) + "===== 在线假人（" + this.onlineFakes.size() + " 个）=====");
        for (FakePlayer f : this.onlineFakes) {
            s.sendMessage(String.valueOf(ChatColor.WHITE) + "- " + f.name + String.valueOf(ChatColor.GRAY) + "  (ping " + f.latency + "ms)");
        }
    }

    private static final class FakePlayer {
        final String name;
        final UUID uuid;
        final String[] skin;
        final int listOrder;
        int latency;

        FakePlayer(String name, String[] skin, int latency, int listOrder) {
            this.name = name;
            this.skin = skin;
            this.latency = latency;
            this.listOrder = listOrder;
            UUID skinUuid = skin != null && skin.length >= 2 ? NmsUtil.extractProfileId(skin[1]) : null;
            this.uuid = skinUuid != null ? skinUuid : NmsUtil.offlineUuid(name);
        }
    }
}

