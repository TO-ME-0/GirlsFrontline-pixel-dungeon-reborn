# AutoIdentify：普通药水 / 普通卷轴的 class 指定清单

## 判定范围

- 统计目标：代码中**直接写出某个普通药水/普通卷轴的具象类**（`new PotionOfXxx()` 或 `PotionOfXxx.class`）。
- 不统计：`Generator.Category.POTION / SCROLL` 的纯概率随机抽取（仅作为基础池单独列出）。
- 单独归类：测试模式、纯 UI/文案引用，避免它们被误当成 AutoIdentify 判据。
- 异变药水/卷轴（`PotionOfXxx`/`ScrollOfXxx` 位于 `exotic` 包）只作为“普通 ↔ 异变”的等价映射出现，不重复计入普通道具。

## 现状说明

- `ColorItem.guessType()` 目前只有 `Ring` / `RingOfWealth` 在调用，药水/卷轴没有任何 `guessType` 调用点。
- `Potion.name()` / `Scroll.name()` 只有颜色/符文分支，没有 `Ring.name()` 那种 `"? " + 真名` 的猜中显示分支。
- 因此下列位置就是后续接入药水/卷轴 AutoIdentify 的线索来源，按可靠性分级见文末。

---

## 一、身份注册与基础生成池

| 位置 | 指定道具 | 指定原因 |
|---|---|---|
| `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java:389-404` | 12 种普通药水 | 药水颜色/身份注册表，也是药水掉落池；`PotionOfStrength` 权重为 0，因为每章固定配额单独投放 |
| `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java:422-438` | 12 种普通卷轴 | 卷轴符文/身份注册表，也是卷轴掉落池；`ScrollOfUpgrade` 权重为 0，同样走章节配额 |
| `.../items/potions/Potion.java:147-150`、`.../items/scrolls/Scroll.java:103-106` | 上述两份 classes 数组 | `ItemStatusHandler` 按名单顺序给具象类分配颜色/符文 |
| `.../items/Generator.java:350-356` | `Potion.class` / `Scroll.class` | 仅用于背包排序（普通 → 异变 → 法术/酿造），不是具体道具指定 |

## 二、必得 / 章节配额 / 商店

| 位置 | 指定道具 | 指定原因 |
|---|---|---|
| `.../levels/Level.java:231-238` | 力量药水、升级卷轴 | `Dungeon.posNeeded()` / `souNeeded()` 按章节配额保证成长曲线，同时消耗 `LimitedDrops` 计数 |
| `.../levels/rooms/special/ShopRoom.java:271-285` | 治疗药水、鉴定卷轴、祛邪卷轴、魔法测绘卷轴 | 商店固定基础补给：保命、鉴定、解诅咒、探图 |
| `.../levels/rooms/special/ShopRoom.java:274-281` | 经验药水 / 蜕变卷轴（25 层 1/4 概率二选一） | 终局层高价值随机奖励 |
| `.../actors/hero/HeroClass.java:327` | 鉴定卷轴 | 全职业开局统一赠送，降低前期信息成本 |
| `.../actors/hero/HeroClass.java:409-422` | 治疗药水、力量药水、愤怒卷轴 | 战士初始套件：续航 + 力量 + 群体嘲讽 |
| `.../actors/hero/HeroClass.java:424-434` | 充能卷轴、火焰药水、升级卷轴 | 法师初始套件：法杖充能 + 火系输出 + 升级 |
| `.../actors/hero/HeroClass.java:436-451` | 隐身药水、魔法测绘卷轴 | 盗贼初始套件：潜行 + 探图 |
| `.../actors/hero/HeroClass.java:453-462` | 灵视药水、催眠卷轴 | 猎手初始套件：感知 + 控场 |
| `.../actors/hero/HeroClass.java:464-481` | 恐惧卷轴、灵视药水 | 56-1 式初始套件：控制 + 感知 |
| `.../actors/hero/HeroClass.java:483-497` | 灵视药水 | 旧版 56-1 式初始套件 |
| `.../actors/hero/HeroClass.java:499-512` | 治疗药水、祛邪卷轴 | GSH-18 套件：医疗主题 + “疫苗磁盘”剧情 |
| `.../actors/hero/HeroClass.java:514-530` | 加速药水、升级卷轴 | HK416 套件：机动 + 升级 |
| `.../actors/hero/HeroClass.java:531-544` | 经验药水、蜕变卷轴 | 蒲公英套件：成长 + 天赋蜕变 |
| `.../actors/hero/Hero.java:2254-2265` | 治疗药水、力量药水、升级卷轴 | 旧版 56-1 式转职天赋“老兵新生”：复活时按天赋等级给奖励 |
| `.../items/scrolls/ExtractUpgrade.java:50-55` | 升级卷轴 | 提取升级后只退还升级卷轴，不退还其它卷轴 |

## 三、房间固定放置

| 位置 | 指定道具 | 指定原因 |
|---|---|---|
| `.../levels/rooms/special/MagicalFireRoom.java:113` | 冰霜药水 | 熄灭房内魔法火焰 |
| `.../levels/rooms/special/TrapsRoom.java:113` | 浮空药水 | 越过满房陷阱取宝箱 |
| `.../levels/rooms/special/ToxicGasRoom.java:108` | 净化药水 | 清除毒气 |
| `.../levels/rooms/special/StorageRoom.java:58` | 火焰药水 | 烧掉封门的木栅栏 |
| `.../levels/rooms/special/MassGraveRoom.java:55` | 火焰药水 | 处理封门/障碍 |
| `.../levels/rooms/special/SentryRoom.java:167` | 加速药水 | 抢完宝箱后甩开哨戒炮台 |
| `.../levels/rooms/special/PoolRoom.java:90` | 隐身药水 | 避开食人鱼 |
| `.../levels/rooms/special/LibraryRoom.java:56-62` | 第一件固定为鉴定卷轴或祛邪卷轴 | 图书馆主题的“工具书”，先给一件实用卷轴，其余随机 |
| `.../levels/rooms/special/LibraryRoom.java:69-82` | `findPrizeItem(Scroll.class)` → 随机卷轴 | 优先消耗关卡预生成的卷轴奖励，没有才随机 |
| `.../levels/rooms/special/LaboratoryRoom.java:126-139` | `findPrizeItem(Potion.class)` → 随机药水/石头 | 实验室主题，优先药水 |
| `.../levels/rooms/special/CrystalPathRoom.java:155-167` | 经验药水 / 蜕变卷轴 | 四选一奖励位；异晶簇饰品可概率升级为神启药水/超维卷轴 |
| `.../levels/rooms/special/CrystalPathRoom.java:285-300` | 同上 | 另一处四选一奖励位，逻辑一致 |

## 四、固定权重池（秘密房间）

| 位置 | 指定道具 | 指定原因 |
|---|---|---|
| `.../levels/rooms/secret/SecretLibraryRoom.java:59-72` | 除 `ScrollOfUpgrade` 外的 11 种普通卷轴 | 图书馆主题池；权重按价值递增（鉴定 1、祛邪 2、蜕变 6），升级卷轴被排除以免秘密房破坏成长节奏 |
| `.../levels/rooms/secret/SecretLaboratoryRoom.java:53-66` | 除 `PotionOfStrength` 外的 11 种普通药水 | 实验室主题池；排除力量药水，经验药水权重最高（6） |

## 五、怪物掉落与排除规则

| 位置 | 指定道具 | 指定原因 |
|---|---|---|
| `.../actors/mobs/Elemental.java:203-215` | 火焰元素 → 火焰药水（1/8） | 属性主题对应 |
| `.../actors/mobs/Elemental.java:281-292` | 冰霜元素 → 冰霜药水（1/8） | 属性主题对应 |
| `.../actors/mobs/Elemental.java:309-318` | 闪电元素 → 充能卷轴（1/4） | 电系主题对应 |
| `.../actors/mobs/Elemental.java:354-376` | 混沌元素 → 蜕变卷轴（必掉，数量随 `mobRan`） | 混沌/变异主题，必掉 |
| `.../actors/mobs/Acidic.java:31-58` | 经验药水（必掉） | 酸性变异蝎的高价值奖励 |
| `.../actors/mobs/DemonSpawner.java:60` | 治疗药水（必掉） | 恶魔孵化器固定补给 |
| `.../actors/mobs/Bat.java:46` | 治疗药水（1/6） | 吸血类敌人的续航主题掉落 |
| `.../actors/mobs/Swarm.java:56` | 治疗药水（1/6） | 同上，虫群续航主题 |
| `.../actors/mobs/Necromancer.java:55` | 治疗药水（0.2） | 治疗型单位主题掉落 |
| `.../actors/mobs/GolyatFactory.java:57` | 治疗药水（0.2） | 后勤/工厂型单位主题掉落 |
| `.../actors/mobs/Aegis.java:51` | 治疗药水（1/3） | 重装单位的高概率补给 |
| `.../actors/mobs/JaegerLock.java:134-162` | 治疗药水（限次，`WARLOCK_HP`） | 沿用术士固定血瓶机制；随机池剔除治疗并归还概率 |
| `.../actors/mobs/Guard.java:110-138` | 治疗药水（限次，`GUARD_HP`） | 同上，限次固定治疗 + 随机池剔除治疗 |
| `.../actors/mobs/Cyclops.java:83-104` | 随机普通药水，排除治疗 | 防止治疗药水被刷 |
| `.../actors/mobs/Archer.java:85-93` | 随机普通药水，排除治疗/力量 | 防止刷保命与力量成长 |
| `.../actors/mobs/Scorpio.java:105-113` | 随机普通药水，排除治疗/力量 | 同上 |
| `.../actors/mobs/Succubus.java:166-174` | 随机普通卷轴，排除鉴定/升级 | 防止刷信息与升级资源 |
| `.../actors/mobs/SpectralNecromancer.java:59-70` | 祛邪卷轴（额外必掉） | 亡灵/诅咒主题 |
| `.../actors/mobs/SpinnerCat.java:47-64` | 蜕变卷轴（1/10）、超维卷轴（1/20） | 彩蛋型稀有掉落 |

## 六、配方 / 等价映射

| 位置 | 指定道具 | 指定原因 |
|---|---|---|
| `.../items/potions/Potion.java:479-493` | 12 种种子 → 12 种普通药水 | 炼金配方固定映射（如阳草→治疗、腐苔→毒气） |
| `.../items/scrolls/Scroll.java:321-335` | 12 种普通卷轴 → 12 种符石 | 卷轴转符石配方，功能一一对应（鉴定→直觉、升级→附魔等） |
| `.../items/bombs/Bomb.java:339-357` | 8 组药水/卷轴 → 炸弹 | 效果同源配方：冰→霜冻炸弹、火→火焰炸弹、隐身→闪光弹、治疗→再生炸弹等 |
| `.../items/potions/Potion.java:108-136` | `mustThrowPots` / `canThrowPots` | 指定“只能砸/可砸”的具象类，决定默认动作与误饮确认框 |
| `.../items/potions/exotic/ExoticPotion.java:55-89` | 普通药水 ↔ 异变药水 | 转化/识别联动：知道一侧即可推出另一侧 |
| `.../items/scrolls/exotic/ExoticScroll.java:51-85` | 普通卷轴 ↔ 异变卷轴 | 同上 |

## 七、非玩法引用（不应作为 AutoIdentify 判据）

以下位置只是图标、文案、数值估算、测试工具，**不代表局面里真的会出现该道具**：

- 图标/文案：`windows/WndStartGame.java:532-534`、`windows/WndJournal.java:442`、`journal/Notes.java:228-230`、`actors/buffs/DandelionOwner/Vector_Fire_Aura.java:145-147`
- 数值估算：`actors/mobs/npcs/DEL.java:245-247`
- 更新日志：`ui/changelist/girlpd/v0_5_X_Changes.java:458-491`
- 测试模式：`custom/testmode/generator/TestPotion.java`、`LazyTest.java`、`debugBook.java`、`LevelTeleporter.java`、`items/bombs/TestBomb.java`
- 免疫/抗性名单：`actors/Char.java:1033`（BOSS 免疫天罚/灵能爆破，不是投放）
- 升级窗口存量检查：`windows/WndUpgrade.java:160`

---

## 八、接入进度（2026-10-25）

已实现：

- **名称显示**：`Potion.name()` / `Scroll.name()` 在 `showGuess()` 为真时返回 `"? 真名"`，与 `Ring.name()` 一致；总开关仍是 `SPDSettings.isAutoIdentify()`，提示文本受 `SPDSettings.showAutoGuessingText()` 控制。
- **投放工具**：`ColorItem.guess(item, cause)` 静态方法，创建道具时顺手标记并可链式书写。
- **已接线的强判据来源**：
  - `Level`：章节配额的力量药水、升级卷轴。
  - `ShopRoom`：治疗药水、鉴定 / 祛邪 / 魔法测绘卷轴；25 层经验药水或蜕变卷轴。
  - 房间固定道具：火焰房冰霜、陷阱房浮空、毒气房净化、仓库房火焰、乱葬岗火焰、哨戒房加速、水池房隐身。
  - 必掉怪：酸性蝎经验药水、恶魔孵化器治疗药水、混沌元素蜕变卷轴、幽灵死灵祛邪卷轴。

触发时机：

- 关卡 / 商店 / 房间固定道具在**生成期**触发（进入该层即生效）。
- 必掉怪在**死亡掉落时**触发。

暂未接入及原因：

- 角色初始套件：原代码已直接 `.identify()`，无需再猜。
- `LibraryRoom` 第一件：鉴定 / 祛邪二选一，无法唯一判定。
- 元素 1/8、1/4，蝙蝠 / 虫群 1/6，Aegis 1/3，死灵 / 工厂 0.2，蜘蛛猫 1/10、1/20，以及秘密房间权重池：属概率判据，不自动触发。
- 种子 / 符石 / 炸弹 / 普通↔异变的等价映射：需要“知一侧推另一侧”的额外逻辑，暂未实现。
- 负向判据（弓箭手、蝎子、魅魔等的排除规则）：只缩小候选，不触发 `guessType`。
- 非玩法引用（图标、文案、数值估算、测试模式、更新日志）：不作为判据。

---

## 可靠性分级（供 AutoIdentify 判定使用）

1. **强判据（可 100% 推断）**
   - 章节必得：力量药水、升级卷轴。
   - 房间固定道具：冰霜 / 浮空 / 净化 / 火焰 / 加速 / 隐身。
   - 商店固定补给、角色初始套件。
   - 必掉怪：酸性蝎（经验）、恶魔孵化器（治疗）、混沌元素（蜕变）、幽灵死灵（祛邪）。

2. **概率判据（只能“倾向”，不能定死）**
   - 元素 1/8、1/4；蝙蝠/虫群 1/6；Aegis 1/3；死灵/工厂 0.2；蜘蛛猫 1/10、1/20。
   - 秘密房间权重池也属于此级。

3. **负向判据（缩小候选，不给结论）**
   - 弓箭手、蝎子：排除治疗 + 力量。
   - 魅魔：排除鉴定 + 升级。
   - 独眼巨人：排除治疗。
   - 守卫/猎手锁：随机池剔除治疗。

4. **等价判据（A 知则 B 知）**
   - 种子 ↔ 药水、卷轴 ↔ 符石、普通 ↔ 异变、药水/卷轴 ↔ 炸弹。

5. **使用限制**
   - 房间固定道具通常和随机奖励混放（如图书馆第 2、3 件仍是随机）。
   - 只有“该容器/该来源恰好只产出一件未知药水/卷轴”时才能安全推断。
   - 其余情况只能作为加权线索，不能直接 `guessType`。
