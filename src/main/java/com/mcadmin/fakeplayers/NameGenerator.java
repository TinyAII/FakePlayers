/*
 * FakePlayers - 假人姓名生成器 - 生成随机英文 ID 或从配置文件读取自定义名字
 * Copyright (c) 2026 TinyAII  ·  MIT License（见仓库根 LICENSE）
 *
 * 反编译恢复：源码随开发服清理丢失，本源码由已发布 jar（v1.0.0）经 CFR 0.152 反编译恢复后做
 *             开源清理（还原中文/补类头/LICENSE），逻辑与原始版一致。本插件因用 NmsUtil 反射
 *             net.minecraft.network.protocol.game 包内类构造发包，适配 Paper 1.21.8。
 */
package com.mcadmin.fakeplayers;

import java.util.Random;

public final class NameGenerator {
    private static final String[] PREFIX = new String[]{"Steve", "Alex", "Notch", "Herobrine", "Dream", "Techno", "Tommy", "Wilbur", "Ranboo", "Tubbo", "Philza", "Grian", "Mumbo", "Scar", "Pearl", "Gem", "Keralis", "Zedaph", "Tango", "Impulse", "Falseness", "Iskall", "Stress", "Cleo", "Etho", "Beef", "Vintage", "Hypno", "Bdouble", "Docm", "Kart", "Jevin", "Rendog", "Cubfan", "Wels", "Xb", "Joe", "Zombie", "Skeleton", "Creeper", "Ender", "Dragon", "Wither", "Blaze", "Ghast", "Slime", "Wolf", "Fox", "Panda", "Piglin", "Villager"};
    private static final String[] SUFFIX = new String[]{"King", "Pro", "Master", "Lord", "Gamer", "Fan", "Lover", "Hunter", "Builder", "Miner", "Farmer", "Knight", "Nomad", "Legend", "Boss", "Warrior", "Mage", "Archer", "Sage", "Hero", "Shadow", "Ghost"};
    private static final Random RANDOM = new Random();

    private NameGenerator() {
    }

    public static String random() {
        for (int attempt = 0; attempt < 100; ++attempt) {
            String p = PREFIX[RANDOM.nextInt(PREFIX.length)];
            int mode = RANDOM.nextInt(4);
            String name = mode == 0 ? p + "_" + SUFFIX[RANDOM.nextInt(SUFFIX.length)] : (mode == 1 ? p + (100 + RANDOM.nextInt(900)) : (mode == 2 ? p + "_" + (10 + RANDOM.nextInt(90)) : p + RANDOM.nextInt(10)));
            if (name.length() > 16) continue;
            return name;
        }
        return "Steve_" + (100 + RANDOM.nextInt(900));
    }

    public static String sanitize(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.replaceAll("[^a-zA-Z0-9_]", "");
        if (s.isEmpty()) {
            return null;
        }
        if (s.length() < 3) {
            s = s + "_" + (100 + RANDOM.nextInt(900));
        }
        if (s.length() > 16) {
            s = s.substring(0, 16);
        }
        return s;
    }
}

