# 假人插件 FakePlayers

> Tab 假人 + 随机皮肤 + 假 Ping + MOTD 虚高 + 孤独搭话 + 随机上下线 + 防重名，零依赖撑排面。MIT 开源。

小服"撑排面"利器：给 Tab 列表塞假人（随机英文 ID + 内置 23 张皮肤 + 假 Ping），MOTD 在线人数虚高让小服看起来热闹；单人在线时假人随机闲谈台词缓解冷清；假人还会随机上下线配合 join 提示。

- 🕹 **Tab 假人**：往 Tab 列表塞 N 个假人，名字随机英文 ID（可配置自定义 `1:zhangsan` `2:lisi` ...），不与真玩家重名
- 🎨 **随机皮肤**：内置 23 张预设皮肤 textures，每个假人随机分一张，Tab 显示头像（不依赖在线皮肤服务）
- 📡 **假 Ping**：每个假人显示一个 1~999 ms 随机 ping，跳着变化更像真人
- 📈 **MOTD 虚高**：MOTD 在线人数 = 真人 + 假人 + 额外数（可调）；悬停显示在线名单（含假人）
- 💬 **孤独搭话**：服务器只 1 个真人玩家时，每隔一段时间随机一个假人发一句闲谈台词（只算真人）
- 🔄 **随机上下线**：可开关；假人间隔 30 分钟~2 小时随机上下线（1~2 个），并配合 join 提示
- 🛡 **防重名**：假人 ID 与真玩家撞名时自动加后缀
- 🎨 品牌横幅 TinyAII；**MIT 开源**

---

## 安装

1. 下载 `fakeplayers-1.0.0.jar`
2. 放入 `plugins/`，重启
3. `/假人 数量 <N>` 设假人数（OP 权限 `fakeplayers.admin`）

## 命令

别名：`/假人`、`/fp`、`/fakeplayer`、`/jr`

| 命令 | 说明 |
|---|---|
| `/假人 数量 <N>` | 设置假人数量（0=关闭假人） |
| `/假人 开` / `/假人 关` | 开关假人 |
| `/假人 重载` | 重载配置 |
| `/假人 列表` | 列出当前假人 |

## 配置（`plugins/FakePlayers/config.yml`）

```yaml
fake-players:
  count: 2                 # 假人数量
  enabled: true            # 是否启用
  ping:
    min: 1
    max: 999
  name-format:
    random: true           # true=随机英文ID; false=用 custom 列表
    custom:
      "1": "zhangsan"      # "1" 是顺序号（不固定），对应那个假人的名字
      "2": "lisi"
motd:
  extra: 10                # MOTD 在线人数额外加这么多
  online-min: 15           # MOTD 显示在线下限
  hover-list: true         # 悬停显示在线名单（含假人）
lonely-chat:
  enabled: true            # 孤独搭话开关
  delay-min: 1200          # 触发延迟区间下限（秒，20分钟）
  delay-max: 3800           # 上限（约1小时）
  cooldown-days: 1          # 每玩家每天冷却
  lines:
    - "今天也来挖矿吗？"
    - "你听说了吗？"
    - ...（共7条）
simulate-join:
  enabled: true            # 随机上下线开关
  interval-min: 1800       # 间隔区间（秒，30分钟）
  interval-max: 7200       # 上限（2小时）
  per-cycle: 1            # 每轮上下线数量
```

## 实现原理（开源可读）

- `FakePlayersPlugin`：主类，假人列表管理 + scheduler 定时发"假人上下线 / ping 更新 / 孤独搭话"包 + 命令分发
- `NmsUtil`：**反射**构造 `ClientboundPlayerInfoUpdatePacket`（ADD_PLAYER / UPDATE_LATENCY 等 action）与 `ClientboundPlayerInfoRemovePacket`，把假人塞进 Tab 列表——通过反射拿 `net.minecraft.network.protocol.game.*` 内部类，**适配 Paper 1.21.8 包路径**（不同版本包名变化需重编译）
- `SkinPool`：内置 23 个预选皮肤 textures（Base64 注入 GameProfile.properties 供 Tab 显示头像）
- `NameGenerator`：随机英文 ID 或从配置自定义名字（sanitize：只留 `[a-zA-Z0-9_]`、长度 3~16）

## 兼容

- **Paper 1.21.8**（用了反射访问 `net.minecraft.network.protocol.game` 内部类锁版本；不同 MC 版本需重编译）
- Java 21
- 零依赖（authlib 由服务端提供）

## 开源许可

**MIT License** — Copyright (c) 2026 TinyAII。源码见 `src/main/java/com/mcadmin/fakeplayers/`，可自由使用/修改/分发，请保留版权与许可声明。

---

# FakePlayers (English)

Fake players for Tab list with random skins, fake ping, MOTD inflation, lonely-chat, random join/quit. MIT open source, zero deps.

## Features
- N fake players in Tab with random English IDs (or custom names) + 23 built-in skins + fake ping
- MOTD online count inflated (+extra, min shown); hover list shows fake players
- Lonely-chat: single real player → fake player sends a chat line every 20min~1hour
- Random join/quit (1~2 fakes, 30min~2h interval) + join messages
- Anti-name-collision (auto suffix on collision with real players)

## Commands
`/假人 数量 <N>`, `/假人 开|关`, `/假人 重载`, `/假人 列表`. Alias: `/fp`, `/fakeplayer`, `/jr`. Permission: `fakeplayers.admin`.

## Compatibility
**Paper 1.21.8** (reflection into `net.minecraft.network.protocol.game` locks version), Java 21, zero dependencies (authlib provided by server).

## License
**MIT** — Copyright (c) 2026 TinyAII. Source in `src/`. Free to use/modify/distribute; keep the copyright notice.

## Author
TinyAII · MIT 开源 · 零依赖
