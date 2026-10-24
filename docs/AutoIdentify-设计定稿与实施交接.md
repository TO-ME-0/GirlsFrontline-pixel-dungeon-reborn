# AutoIdentify 设计定稿与实施交接

- 状态：设计定稿，尚未修改任何代码
- 日期：2026-10-24
- 输入清单：`docs/AutoIdentify-普通药水与卷轴class指定清单.md`
- 适用范围：普通 Potion / Scroll 的自动鉴定知识系统

---

## 0. 一句话结论

知识全部存入 `Notes.knowledge`，按三种粒度建模：**实例/标签级 `ItemClue`**、**楼层池级 `FloorClue`**、**跨层配额 **（仍用 `FloorClue`，携带多个 `levelId`）。线索在生成点登记，按来源各自的"看见"事件激活；玩家主动点击"尝试猜测"时，才对标签 × 类做约束求解。

---

## 1. 需求与最终约束

用户原始需求：

1. 在满足条件时获得自动鉴定信息，且信息能参与序列化 / 反序列化。
2. 场景包括但不限于：解谜房必刷药剂、力量药水前两层必定有且仅有一瓶、怪物掉落、商店、特殊房间必刷。
3. 结合 `docs/AutoIdentify-普通药水与卷轴class指定清单.md` 实现。

后续修正（按时间顺序，均为最终约束）：

1. 知识可跨层。
2. 知识总是记录该楼层捡到的道具；玩家选择尝试猜测时，以当前所捡到的道具、知识进行猜测。
3. 猜测分为正面猜测与排除猜测。
4. 猜测知识保存到类（`Notes`），不保存到物品实例；实例级保存会在遗失、合并时丢失。
5. **激活条件是"看见"，不是"拾取"。** 拾取只记账，不决定知识是否生效。
6. **解谜房的道具是生成在房间外的**：`level.addItemToSpawn(...)` 进入本层物品池，由 `RegularLevel.createItems()` 随机落点。房间保证的是"本层池子里加入了一个 X"，而不是"某瓶的具体颜色"。
7. 三种来源的激活路径各自独立：
   - 解谜房 → 看见房间时获取（楼层池事实）。
   - 怪物指定 / 排除掉落 → 知识跟随实例产生（实例级事实）。
   - 商店固定刷出 → 看见货架时获取（实例级事实）。

---

## 2. 最终知识模型

### 2.1 三种作用域

| 作用域 | 类 | 内容 | 来源 | 激活 | 参与求解的方式 |
|---|---|---|---|---|---|
| 实例/标签级 | `ItemClue` | `label + mode + classes` | 怪物掉落、商店货架、图书馆书架、水晶路奖励位 | 该堆 `heap.seen` 翻转（或 drop 时已在视野） | 直接约束该标签的候选类 |
| 楼层池级 | `FloorClue` | `levelIds + category + classes + count + 房间范围`，**无 label** | 解谜房 `addItemToSpawn`、力量/升级配额 | 房间内部格进入 FOV / 配额规则已知 | 对 `Seen(F)` 做存在性约束 |
| 类结果 | `ClassGuess` | 类 + 可能标签集 + 状态 + 依据 | 求解结果 | - | 展示与存档 |

### 2.2 三种激活路径

**解谜房：看见房间**

- `Room.paint` 里 `level.addItemToSpawn(item)` 之后，登记 `FloorClue`，把房间内部范围一起写入。
- 在 `Level.updateFieldOfView` 的 `c == Dungeon.hero()` 分支里，对当前层未激活的 `FloorClue` 检查：房间内部格（`left+1..right-1`、`top+1..bottom-1`）是否有任一格在 `fieldOfView` 中。有则 `active = true`。
- 不看 `heap.seen`，因为奖励根本不在房间里。

**怪物：看见实例**

- `Mob.createLoot()` 返回真实实例的那一刻登记 `ItemClue`，标签从该实例读取（`Potion.color` / `Scroll.rune`）。
- 若 `Level.drop` 时该格已在 `heroFOV` 内，立即激活；否则等 `heap.seen` 翻转。
- `createLoot` 的 `Generator.Category` 分支是随机池，不登记。

**商店 / 图书馆 / 水晶路：看见货架或道具堆**

- 这些道具用 `level.drop(item, pos)` 放在房间内，登记 `ItemClue`。
- 激活同样是 `heap.seen` 翻转。

`AutoIdentify.onSeen(item)` 只做两件事：

1. 把标签记入 `FloorSeen(Dungeon.levelId, category)`。
2. 激活同类别、同标签的休眠 `ItemClue`。

它**不负责** `FloorClue` 的激活。

### 2.3 求解器

输入：类别（POTION / SCROLL）、当前要猜的标签、全部激活线索。

1. 由 `ItemStatusHandler` 构造未知类集合 `U` 与可用标签集合 `L`（`label ↔ class` 双射，已鉴定类占用自己的标签）。
2. 应用所有激活 `ItemClue`：
   - `SINGLE(label → c)`：`cand[label] = {c}`。
   - `ANY_OF(label ∈ G)`：`cand[label] ∩= G`；对 `c ∉ G` 移除该 label。
   - `NOT(label ∉ S)`：`cand[label] −= S`。
3. 单例传播：某标签只剩一个候选 → 锁定；从其他标签的候选里删除该类。
4. 应用 `FloorClue` 的存在性约束：
   - `S_F = FloorSeen(levelId, category)`，即该层曾看见过的标签。
   - 约束：`∃ label ∈ S_F，使 class(label) ∈ clue.classes`。
   - 仅在 `poolComplete(F) == true` 时启用。
   - 若 `S_F` 中只有一个标签的候选仍与 `clue.classes` 相交 → 对它收窄；单类时即为正面锁定。
5. 完美匹配校验（Kuhn，12×12）：对每条候选边，强制该边后检查剩余图是否仍有完美匹配；无匹配则该边不成立。
6. 匹配校验必须同时检查每个启用的 `FloorClue` 至少被满足一次。
7. 输出：
   - 目标标签候选唯一 → 正面猜测。
   - 否则输出已排除的类、仍可能的标签、各条依据。

结果落盘：`ClassGuess.status = POSITIVE`，并把类加进 `Dungeon.guessType`（运行时展示缓存）。

### 2.4 为什么 `FloorClue` 不能存 label

看见房间不等于看见颜色。若在生成时把真实标签写进楼层线索，玩家一进房间就能反推出颜色，等于直接送答案。因此楼层线索只保存"池子里有 X 类"，把标签留给 `FloorSeen` 和后续观察。

### 2.5 `poolComplete` 的定义

- 当前层所有 `Heap.seen == true` 时置为 `true`，单调锁存，离开本层后仍保留。
- 这是 `FloorClue` 参与求解的必要条件：房间只保证"池里有"，不保证"你看见的那瓶就是它"。
- 若本层有未探索区域、未发现的密室堆，则保持 `false`，楼层线索只作展示，不产生标签结论。这是保守策略，不会猜错。

---

## 3. 数据结构

计划新增 `journal/IdentifyKnowledge.java`：

```java
public class IdentifyKnowledge implements Bundlable {

    public enum Category { POTION, SCROLL }
    public enum Mode { SINGLE, ANY_OF, NOT }
    public enum Status { UNKNOWN, PARTIAL, POSITIVE }
    public enum Origin { ROOM, RUN, MOB, SHOP }

    // 实例/标签级线索
    public static class ItemClue implements Bundlable {
        public int levelId;
        public Category category;
        public String label;
        public Mode mode;
        public Class<? extends Item>[] classes;
        public boolean active;
        public Origin origin;
        public String cause;
    }

    // 楼层池级线索（无 label）
    public static class FloorClue implements Bundlable {
        public int[] levelIds;             // 解谜房只有一个；跨层配额有多个
        public Category category;
        public Class<? extends Item>[] classes;
        public int minCount = 1;
        public int maxCount = -1;          // -1 表示不限
        public int left = -1, top = -1, right = -1, bottom = -1; // 房间内部范围；-1 表示全层
        public boolean active;
        public boolean poolComplete;
        public Origin origin;
        public String cause;
    }

    // 每层曾看见的标签
    public static class FloorSeen implements Bundlable {
        public int levelId;
        public LinkedHashSet<String> potions = new LinkedHashSet<>();
        public LinkedHashSet<String> scrolls = new LinkedHashSet<>();
    }

    // 类级猜测结果
    public static class ClassGuess implements Bundlable {
        public Class<? extends Item> itemClass;
        public LinkedHashSet<String> possibleLabels;
        public Status status;
        public String cause;
        public LinkedHashSet<Integer> evidenceFloors;
    }

    public ArrayList<ItemClue> itemClues = new ArrayList<>();
    public ArrayList<FloorClue> floorClues = new ArrayList<>();
    public ArrayList<FloorSeen> floors = new ArrayList<>();
    public ArrayList<ClassGuess> classGuesses = new ArrayList<>();
    public LinkedHashSet<Class<? extends Item>> vetoed = new LinkedHashSet<>();

    // 方法：recordSeen / addItemClue / addFloorClue / hasSeen /
    //       solve / applyResult / clearCategory / restoreGuessCache /
    //       storeInBundle / restoreFromBundle
}
```

派生数据不入档：标签候选集、未知类集合每次都从 `ItemStatusHandler` 现算，避免与 `known()` 失同步。

---

## 4. 文件改动清单

### 4.1 基础设施

| 文件 | 改动 |
|---|---|
| `items/ColorItem.java` | 增加 `guessKey()`（默认 `getClass()`）与 `guessLabel()`（默认 `null`）；`guessType` / `isGuess` 改用 `guessKey()` |
| `items/potions/Potion.java` | `guessLabel()` 返回 `color`；`name()` 增加 `showGuess()` 分支；`info()` 显示依据；`setAction()` 与有害判定加入 `showGuess()`；`setIgnore()` 分支处理已猜/未鉴定/重掷；`initColors()` 加重掷钩子；新增 static 访问器 `unknownClasses()` / `labelOf(Class)` / `isKnownClass()` / `contains(Potion)` |
| `items/scrolls/Scroll.java` | 与 Potion 对称；`guessLabel()` 返回 `rune`；`initLabels()` 加重掷钩子 |
| `items/potions/exotic/ExoticPotion.java` | 覆盖 `guessKey()` → `exoToReg.get(getClass())` |
| `items/scrolls/exotic/ExoticScroll.java` | 同上 |
| `items/rings/Ring.java` | 基本不改；`guessKey()` 默认自身类，现有 `name()` 逻辑保留 |
| `journal/Notes.java` | 增加 `public static IdentifyKnowledge knowledge`、`KNOWLEDGE = "knowledge"`；`reset()` 新建；`storeInBundle` / `restoreFromBundle` 读写；旧档缺键时新建空库 |
| `items/AutoIdentify.java`（新增） | 门面：`tag` / `tagSingle` / `tagFloor` / `onSeen(Item)` / `onRoomSeen(Level)` / `guess(Item)` / `canGuess(Item)` / `onLabelsRegenerated(Category)` / `restoreGuessCache()` / `isSupported(Item)` / `categoryOf` / `labelOf` |

### 4.2 拾取与观察钩子

| 文件 | 位置 | 改动 |
|---|---|---|
| `levels/Level.java` | `updateFieldOfView` 末尾，hero 分支（约 L1467-1471） | `heap.seen` 翻转时调 `AutoIdentify.onSeen(heap.peek())`；同处检查未激活的 `FloorClue` 是否看见房间内部格 |
| `levels/Level.java` | `drop(...)` 末尾（约 L983-991） | 若 `Dungeon.level == this && heroFOV[cell]`，调 `AutoIdentify.onSeen(heap.peek())` |
| `actors/hero/Hero.java` | `actPickUp()`（约 L1148-1176） | `doPickUp` 成功后调 `AutoIdentify.onPickUp(item)`，只记账 |
| `windows/WndTradeItem.java` | `buy(Heap)`（约 L270-285）、偷窃成功分支（约 L149-170） | `doPickUp` 成功后调 `AutoIdentify.onPickUp(item)` |

注意：`Item.collect(Bag)` 会在背包整理时重复调用，不能作为拾取钩子。

### 4.3 线索登记点

| 来源 | 文件 | 方式 | 线索 |
|---|---|---|---|
| 力量药水 | `levels/Level.java` `create()`（约 L235-242） | `addItemToSpawn` | `FloorClue(origin=RUN, levelIds={1,2}, Strength, min=1, max=1)`，实际以真实配额规则为准 |
| 升级卷轴 | 同上 `souNeeded()` | `addItemToSpawn` | 按实际配额规则登记 |
| 火焰房 | `levels/rooms/special/MagicalFireRoom.java` L113 | `addItemToSpawn(new PotionOfFrost())` | `FloorClue(ROOM, Frost, 1)` |
| 毒气房 | `ToxicGasRoom.java` L108 | `addItemToSpawn(new PotionOfPurity())` | `FloorClue(ROOM, Purity, 1)` |
| 哨卫房 | `SentryRoom.java` L168 | `addItemToSpawn(new PotionOfHaste())` | `FloorClue(ROOM, Haste, 1)` |
| 水池房 | `PoolRoom.java` L90 | `addItemToSpawn(new PotionOfInvisibility())` | `FloorClue(ROOM, Invisibility, 1)` |
| 陷阱房 | `TrapsRoom.java` L113 | `addItemToSpawn(new PotionOfLevitation())` | `FloorClue(ROOM, Levitation, 1)` |
| 墓穴房 | `MassGraveRoom.java` L55 | `addItemToSpawn(new PotionOfLiquidFlame())` | `FloorClue(ROOM, LiquidFlame, 1)` |
| 仓库房 | `StorageRoom.java` L58 | `addItemToSpawn(new PotionOfLiquidFlame())` | `FloorClue(ROOM, LiquidFlame, 1)` |
| 图书馆 | `LibraryRoom.java` L58-61 | `level.drop(item, pos)` 在房间内 | `ItemClue(ANY_OF {Identify, RemoveCurse})` |
| 水晶路 | `CrystalPathRoom.java` L154-166 | `level.drop(item, pos)` 在房间内 | 经验药水（含异变）→ `SINGLE PotionOfExperience`；蜕变卷轴（含异变）→ `SINGLE ScrollOfTransmutation`；石头不挂 |
| 商店 | `ShopRoom.java` L159 / L173 | `level.drop(item, cell).type = FOR_SALE` | 固定库存逐件 `SINGLE`：Healing / Identify / RemoveCurse / MagicMapping，25 层另有 Experience 或 Transmutation |
| 怪物通用 | `actors/mobs/Mob.java` `createLoot()` L894-919 | 返回实例处 | `Category` 不挂；`Class` / `Item` → `SINGLE`；异变提前 return 也要挂 |
| 酸性蝎 | `Acidic.java` L57-59 | `new PotionOfExperience()` | `SINGLE Experience` |
| 弓箭手 | `Archer.java` L85-93 | 随机药水排除 | `NOT {Healing, Strength}` |
| 独眼巨人 | `Cyclops.java` L84+ | 随机药水排除 Healing | `NOT {Healing}` |
| 魅魔 | `Succubus.java` L167-174 | 随机卷轴排除 | `NOT {Identify, Upgrade}` |
| 守卫 | `Guard.java` L110-138 | 1/3 固定 Healing，否则随机排除 Healing | 固定分支 `SINGLE Healing`；随机分支 `NOT {Healing}` |
| 猎手 | 待确认 | 清单归入排除治疗组 | `NOT {Healing}` |

`Generator.Category` 的普通随机掉落、商店里的随机补给、实验室等随机池，一律不登记。

### 4.4 UI

| 文件 | 改动 |
|---|---|
| `windows/WndInfoItem.java` | `layoutFields` 支持 0~2 个按钮；仅对"未鉴定 + `AutoIdentify.canGuess(item)` + `Dungeon.hero.belongings.contains(item)`"显示猜测按钮；图标使用已确认存在的 `Icons.INFO` |
| 结果窗口 | 正面 → 刷新窗口；排除 / 证据不足 → `WndTitledMessage` 展示已排除类与仍可能的标签 |
| `Potion.java` / `Scroll.java` | `name()`：`showGuess()` 时返回 `"? " + super.name()`；`info()` 显示真实描述 + 依据楼层与来源 |
| `setIgnore()` | 已猜未鉴定 → 撤销 POSITIVE 并加入 `vetoed`；真未鉴定 → 走 `initColors()/initLabels()` 并 `onLabelsRegenerated` 清空该类知识；已真鉴定 → 原逻辑 |

---

## 5. 存档与兼容

- `Notes.storeInBundle` 新增 `bundle.put(KNOWLEDGE, knowledge)`；`restoreFromBundle` 读取，缺键则 `new IdentifyKnowledge()`。
- `Dungeon.guessType` 继续使用原键 `Guess_Type`，类型不变，只作展示缓存。
- `Notes.restoreFromBundle` 之后调用 `AutoIdentify.restoreGuessCache()`，把 `classGuesses` 中 POSITIVE 的类补回 `Dungeon.guessType`。
- `Bundle` 的 `Class[]` 反序列化可能含 `null`，全部过滤。
- 标签是字符串（`color` / `rune`），不是 int。
- `Potion.color` / `Scroll.rune` 在构造函数 `reset()` 中赋值；`handler` 在 `Dungeons.init()` 中初始化（约 L153-155），早于任何 `Level.create`，所以生成点可以直接读标签。
- **生成期必须用 `level.levelId` 或显式传入的 `levelId`**，不能用 `Dungeon.levelId`：`Dungeon.levelId` 只在 `switchLevel` 时切换（Dungeon.java 约 L531）。
- `Heap.seen` 本身已序列化，读档不会重复触发，也不会丢激活状态。

---

## 6. 验证清单

1. 前两层力量药水：第二层结束时确认"恰好一瓶"的配额事实成立，且不泄露具体楼层。
2. 火焰房：进入房间后 `FloorClue` 激活；药剂在别处被看见后，标签进入 `FloorSeen`。
3. 火焰房 + 全层探索：`poolComplete=true`；在其它标签被排除后，能正面锁定冰霜。
4. 野兽房奖励未探索完：`poolComplete=false`，不产生标签结论。
5. 商店：看见货架即激活 `ItemClue`；未购买也能在之后猜同一标签。
6. 图书馆：`ANY_OF` 不压成单类；完美匹配后仍可能保留两个可能。
7. 弓箭手：掉落药水的标签被排除治疗/力量；再叠加其他线索后收缩。
8. 酸性蝎：必掉经验药水，标签正面锁定。
9. 背包合并 / 丢地 / 卖店：知识不变。
10. 跨层：换层后 `FloorSeen` 与线索仍在；回到原层 `poolComplete` 保持。
11. 存读档：结论、候选、激活状态、`guessType` 缓存一致；旧档缺 `knowledge` 键不报错。
12. `debugBook` 触发 `setIgnore` 重掷：该类所有知识清空，不残留错误猜测。
13. 关闭 `isAutoIdentify()`：不显示 `?` 名字、不出按钮，但知识照常积累。

---

## 7. 未决问题与风险

1. `Hunter` 类路径/类名未确认（`actors/mobs/` 下未找到），实施前先定位。
2. `FloorClue` 是否需要私有 `truthLabel` 做有效性校验（`poolComplete && Seen(F).contains(truthLabel)`）。它只用于判断是否启用约束，不参与候选、不展示；不加以接受极小的"保底道具看见前被销毁"风险。
3. 魔法地图是否算"看见房间"。当前设计只看 `heroFOV`；若要支持，把 `mapped` 也作为或条件。
4. `poolComplete` 目前要求本层所有 `Heap.seen`。密室未发现、未开箱会长期阻塞该条件；是否放宽为"仅本层公开区域的堆"需要确认。
5. `heap.seen` 的赋值使用包含心视/塔罗视野的 `fieldOfView`，可能让玩家隔墙"看见"物品。当前接受游戏的可见性定义。
6. 猜测按钮只对背包内物品显示，还是地上的堆也可猜。当前按用户原话"当前所捡到的道具"，只做背包内。
7. 楼层池事实是否要进入日志页展示（"本层已知：冰霜药水（火焰房）"）。当前只作为求解约束与物品信息页的依据文本。
8. `Heap.isSimilar` 的合并粒度需再确认一次（同标签合并即可，不影响知识）。

---

## 8. 实施顺序

1. **阶段 A：基础设施**
   - `ColorItem` / `Potion` / `Scroll` / `ExoticPotion` / `ExoticScroll`
   - `IdentifyKnowledge` / `Notes` / `AutoIdentify`
   - `Level.updateFieldOfView` / `Level.drop` / `Hero.actPickUp` / `WndTradeItem` 钩子
   - 编译验证
2. **阶段 B：求解器**
   - 标签候选、单例传播、`ItemClue` 约束、`FloorClue` 存在性约束、完美匹配
   - 用构造数据做离线自测
3. **阶段 C：线索登记**
   - C1：解谜房 `FloorClue` + 配额
   - C2：商店 / 图书馆 / 水晶路 `ItemClue`
   - C3：怪物 `createLoot` 与各子类
   - 每批编译
4. **阶段 D：UI**
   - `WndInfoItem` 猜测按钮与结果窗口
   - `name()` / `info()` / `setAction()` / `setIgnore()`
5. **阶段 E：存档与验证**
   - 旧档兼容、`guessType` 回填、重掷清空
   - 跑第 6 节验证清单

---

## 9. 设计演进记录（避免重走弯路）

| 版本 | 方案 | 结论 |
|---|---|---|
| v1 | 在 `Item` 上挂 `identifyScope` + 实例级 Fact | 否决：物品遗失、合并即丢知识 |
| v2 | 所有线索在拾取时激活 | 否决：用户明确"知识在看见时获取"，拾取只记账 |
| v3 | 解谜房奖励当作房间里的堆，`heap.seen` 激活标签线索 | 否决：奖励经 `addItemToSpawn` 散落全层，不在房间内，且会泄露颜色 |
| v4 | 实例级 `ItemClue` + 楼层池级 `FloorClue`（无 label）+ 三种来源各自激活 | **定稿** |

已确认、不要重复验证的代码事实：

- `Potion.color` / `Scroll.rune` 在构造时由 `reset()` 赋值，`handler` 在开局已初始化。
- `Level.create(levelDepth, levelId)` 在 `build()` 前设置 `this.levelId`；此时 `Dungeon.levelId` 仍是上一层。
- `RegularLevel.createItems()` 把 `itemsToSpawn` 投到全层随机格。
- `ShopRoom` / `LibraryRoom` / `CrystalPathRoom` 用 `level.drop` 把道具放在房间内。
- `Heap.drop` 会把可堆叠同标签物品合并；`peek()` 是当前渲染在格子上的那件。
- `Heap.seen` 已序列化；`Level.updateFieldOfView` 的 hero 分支是看见事件的真值点。
- `Dungeon.posNeeded()`：按 5 层一组、每两层一瓶，前两层合起来恰好一瓶。
- `Notes.findCustomRecord` 会把异变类映射回普通类。
- `Icons.INFO` 存在；`Belongings.contains(Item)` 存在。

---

## 10. 交接提示

- 未得到用户明确"开始实施"前，不修改任何代码。
- 实施时先读 `docs/AutoIdentify-普通药水与卷轴class指定清单.md`，再按第 8 节顺序推进。
- 全程遵守 Java 1.8 语法约束：不使用 lambda、Stream、`var`、`List.of()`、文本块、record、switch 表达式。
- 物品描述只写玩家可感知的行为与依据，不写代码级实现细节。
- 每批改动后编译一次，避免跨批次错误累积。
