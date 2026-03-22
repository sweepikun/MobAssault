# MobAssault

通过命令生成怪物攻击玩家的 Minecraft Spigot 插件。

## 功能特性

- 通过命令生成怪物攻击指定玩家或权限组
- 配置文件自定义怪物属性（血量、攻击力、速度等）
- 支持装备附魔武器和护甲
- 多种怪物行为：火球、爆炸、执行命令、脚本
- 控制攻击间隔和攻击精准度
- 支持多种怪物类型（原版怪物 + Boss）

## 命令

| 命令 | 说明 | 权限 |
|------|------|------|
| `/ma spawn <类型> [玩家]` | 生成怪物攻击玩家 | `mobassault.spawn` |
| `/ma spawn <类型> -g <权限组>` | 生成怪物攻击权限组 | `mobassault.spawn` |
| `/ma remove <ID>` | 移除怪物 | `mobassault.remove` |
| `/ma removeall` | 移除所有怪物 | `mobassault.removeall` |
| `/ma list` | 列出活动怪物 | `mobassault.list` |
| `/ma reload` | 重载配置 | `mobassault.reload` |
| `/ma config <类型>` | 查看配置 | `mobassault.config` |

## 快速开始

1. 下载插件放入 `plugins` 文件夹
2. 启动服务器
3. 使用命令生成怪物：
   ```
   /ma spawn zombie Steve
   /ma spawn phantom -g vip
   ```

## 配置示例

### 幻翼自爆配置 (phantom.yml)

```yaml
name: "&b自爆幻翼"
type: PHANTOM

attributes:
  health: 20.0
  damage: 6.0
  speed: 0.35

attack:
  interval: 10
  accuracy: 0.9
  range: 3.0

behaviors:
  explosion:
    enabled: true
    trigger-distance: 5.0  # 距离玩家5格爆炸
    power: 3.0
    fire: true
    break-blocks: false
```

### 火焰傀儡配置 (fire_golem.yml)

```yaml
name: "&6火焰傀儡"
type: IRON_GOLEM

attributes:
  health: 100.0
  damage: 15.0
  speed: 0.25

weapon:
  main-hand:
    material: BLAZE_ROD
    name: "&6烈焰之杖"
    enchantments:
      - type: FIRE_ASPECT
        level: 2

behaviors:
  fireball:
    enabled: true
    interval: 40
    speed: 2.0
    accuracy: 0.9
    type: LARGE
```

## 行为类型

| 行为 | 说明 | 配置项 |
|------|------|--------|
| `fireball` | 发射火球 | interval, speed, accuracy, type |
| `explosion` | 距离触发爆炸 | trigger-distance, power, fire |
| `command` | 执行命令 | commands |
| `kether` | 脚本执行 | script |

## 构建

```bash
./gradlew build
```

构建产物位于 `build/libs/MobAssault-1.0.0.jar`

## 环境要求

- Minecraft 1.17+
- Java 17+
- Spigot/Paper 服务器

## License

MIT
