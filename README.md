# FakePlayers 假人插件

> 撑排面专用：让服务器「看起来」人气旺。零依赖，纯发包，不吃服务器性能。

服务器人少冷清？装上这个插件，Tab 列表自动出现一批「假玩家」——随机英文 ID、真实皮肤、假 Ping，服务器列表在线人数虚高，甚至在你独自一人时，还会有「假人」主动找你搭话。

---

## 功能

| 功能 | 说明 |
| --- | --- |
| 🧑‍🤝‍🧑 Tab 列表假人 | 随机英文 ID + 内置 23 个真实玩家皮肤 + 假 Ping（绿格随机） |
| 📡 MOTD 在线人数虚高 | 服务器列表显示「真实玩家 + 假人」，悬停还能看到假人名字和皮肤头像 |
| 🔄 假人随机上下线 | 低频随机进出（默认 30 分钟~2 小时一轮），聊天框显示「XXX加入了游戏/离开了游戏」 |
| 📶 假 Ping 动态跳动 | 假人延迟自动上下浮动，更像真人 |
| 💬 孤独玩家搭话 | 当你独自在线玩了一会，随机一个假人找你搭话（默认 20 分钟~1 小时触发，1 天冷却） |
| 🛡️ 防重名 | 假人名字自动避开在线真实玩家 |

**核心特点：零依赖、无 ProtocolLib 前置，纯发包实现，假人不占实体、不吃内存、不进世界。**

---

## 安装

1. 下载 `fakeplayers-1.0.0.jar`
2. 放入服务器 `plugins/` 目录
3. 重启服务器（或 `/reload`）

启动后控制台出现 TinyAII 像素字横幅即加载成功。

## 命令（仅 OP）

| 命令 | 说明 |
| --- | --- |
| `/假人 数量 <N>` | 设置假人池数量并刷新（0~200） |
| `/假人 开` / `/假人 关` | 开启 / 关闭假人 |
| `/假人 重载` | 重载 config.yml |
| `/假人 列表` | 查看当前在线假人 |

## 配置

配置文件 `plugins/FakePlayers/config.yml`，全部可调：

```yaml
fake-players:
  enabled: true          # 插件总开关
  count: 2               # 假人池总数
  use-skin: true         # 真实皮肤 / 史蒂夫
  ping-min: 20           # 假 Ping 下限（毫秒）
  ping-max: 150          # 假 Ping 上限
  custom-names:          # 自定义假人名字（编号随便写）
    "1": "zhangsan"
    "2": "lisi"

  motd-boost: true       # MOTD 在线人数虚高
  motd-extra: 10         # 最大人数额外空位
  motd-show-names: true  # 悬停显示假人名单

  ping-jitter: true      # 假 Ping 跳动
  ping-jitter-interval: 10

  simulate-join: true    # 假人随机上下线
  simulate-interval-min: 1800   # 进出间隔下限（秒）
  simulate-interval-max: 7200   # 进出间隔上限（秒）
  simulate-out-min: 1    # 每轮下线 1~2 个
  simulate-out-max: 2
  simulate-in-min: 1     # 每轮上线 1~2 个
  simulate-in-max: 2
  online-min: 15         # 在线假人下限
  join-message: true     # 进出服聊天提示

  avoid-real-names: true # 防重名

  lonely-chat: true      # 孤独玩家搭话
  lonely-chat-delay-min: 1200  # 搭话延迟下限（秒）
  lonely-chat-delay-max: 3800  # 搭话延迟上限（秒）
  lonely-chat-cooldown-days: 1 # 冷却天数
  lonely-chat-messages:        # 搭话台词
    - "你在哪？？？？？？？"
    - "你好，你能借我3个钻石吗？"
    - "这服好冷清啊"
```

## 兼容

- **Paper 1.21.8**（及其下游 Purpur / Leaves 1.21.8）
- Java 17+
- ⚠️ 使用了 Paper 专有 API + 锁版 NMS 反射，**不支持 Spigot / 其他 MC 版本**

## 技术亮点

- 零依赖、无 ProtocolLib 前置
- 纯发包（PlayerInfo 包）实现，假人不进世界、不占实体
- 假人皮肤 UUID 对齐 Mojang 签名，皮肤能正常渲染

---

# FakePlayers (English)

Make your server *look* popular. Zero dependencies, pure packet-based, almost no performance cost.

## Features

- Fake players in Tab list (random English IDs + 23 real skins + fake ping)
- MOTD online count inflation + hover player list
- Randomized fake join/quit (low frequency) with join/leave chat messages
- Fake ping jitter
- "Lonely player" chat: a fake player sends you a random message when you're alone
- Avoid name collision with real players

## Install

Drop `fakeplayers-1.0.0.jar` into `plugins/`, restart.

## Commands (OP only)

`/假人 数量 <N>` · `/假人 开|关` · `/假人 重载` · `/假人 列表`

## Compatibility

- Paper 1.21.8 (and forks: Purpur / Leaves 1.21.8)
- Java 17+
- ⚠️ Paper-only API + version-locked NMS reflection. Not for Spigot / other MC versions.

## Author

TinyAII · 免费开源 · 零依赖