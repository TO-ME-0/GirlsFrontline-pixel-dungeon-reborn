# 随机子流重构 SOP（v2 · 定稿）

> **决策**：push/pop 统一改为「子流句柄 + try-with-resources」，全量迁移 27 处；
> 同时把 `java.util.function.BooleanSupplier`（API 24）替换为自写接口。
>
> **权衡已知**：这 27 处里 24 处没有可观测收益（纯结构性统一），唯一可测的功能收益来自 P6（块间解耦）与 EntranceRoom（确定性）。该取舍已经确认接受。

> **当前状态（2026-11-12）**：P0–P6 已全部落地（8 个 commit，见文末「执行记录」），27/27 站点迁移完成，缩进已统一，`:core:compileJava` 通过。
> **P7 未做**：游戏回归、`:android:lintDebug`、查种器双线程验证、种子基线快照。

---

## 0. 决策记录

| # | 结论 | 依据 |
|---|---|---|
| 1 | 采用 `try (Rng.Sub s = Rng.substream(seed)) { … }` | 实测与手写 push/pop + try/finally 等价（交错 9 轮取 min：79.69 / 79.94 / 80.14 ns，差 <1%；轮间噪声 ±14 ns）。见附录 A |
| 2 | 不用 lambda / 三档 Calling API | body 里的 `return`、局部变量赋值、受检异常都被挡住；且 27 个回调类汇到单点调用是 megamorphic（+8~14 ns）。见附录 B |
| 3 | 不启用 `coreLibraryDesugaringEnabled` | +1~2 MB APK、反射看到 `j$` 名、官方支持下限 API 21 > 本项目 minSdk 19。见附录 C |
| 4 | 不保留旧种子 | 版本间种子本就已大幅变化；只要求**本版本内自洽** |
| 5 | 保留查种器的「10 连复查」 | 已明确决定 |
| 6 | `Dungeons.init` 两处悬空 push 不动 | 那是「整栈重置」语义，不是「恰好 pop 一次」 |
| 7 | 不改 `Random.java` 的算法/表示 | 换 long 栈 + SplitMix64 能省 ~17×（见附录 A.4），但属于独立项目，本次不做 |
| 8 | `Rng` 放在 `com.shatteredpixel.shatteredpixeldungeon.utils` | 已定：沿用当前实际位置（不在 `custom/utils`），与 `DungeonSeed`/`GLog` 同包 |

---

## 1. 目标与非目标

### 目标

1. `Random.pushGenerator/popGenerator` 在全项目只剩 **1 个**调用点（`Rng` 内部）。
2. 每个子流的作用域由 `try` 块界定，异常路径自动出栈。
3. 三类种子来源用**不同名字**区分：任务/派生种子、楼层种子、楼层内派生子流。
4. 消除 `minSdk 19` 上的 API 24 依赖。
5. 不改 minSdk、不加依赖、不改 `SPD-classes`。

### 非目标

| 不做 | 原因 |
|---|---|
| 改 `Random.java` 的 RNG 表示 | 见决策 7 |
| 动 `docs/quest4-src/**` | 冻结参考副本 |
| 动 `Dungeons.java:85` / `:112` | 见决策 6 |
| 移除 10 连复查 | 见决策 5 |

---

## 2. 术语与不变式

| 名词 | 含义 |
|---|---|
| **子流** | push 一个种子 → 在独立 RNG 上跑一段 → pop |
| **扰动** | 子流消耗父流的随机数，使父流后续取值整体位移 |

### 工厂与语义（名字不同，禁止混用）

| 工厂 | 用途 | 处数 |
|---|---|---|
| `substream(seed)` / `substream(seed, skip)` | 种子由调用方给出 | 7 |
| `nextSubstream()` | 从当前流取下一个 `Long` 作种子 | 10 |
| `levelStream()` / `levelStream(level)` / `levelStream(depth, branch)` | 楼层种子，body 直接跑在该种子上 | 7 |

### 三条不变式

1. `Sub` 只能出自 `Rng` 工厂，只能出现在 `try` 头部，**不得手动 `close()`**。
2. 级别生成路径上**禁止无种子子流**。
3. 子流内部**禁止 `Random.resetGenerators()`**（清空整栈 → `close()` 下溢）。

---

## 3. 前置检查

```powershell
git status                       # 必须干净
git rev-parse HEAD               # 记录基线
.\gradlew.bat :core:compileJava  # 必须绿
git switch -c refactor/rng-substream
Test-Path core\src\main\java\com\shatteredpixel\shatteredpixeldungeon\utils\Rng.java   # 期望 False（开工前尚未创建）
```

- [ ] 基线编译通过
- [ ] 基线 commit 记入文末「执行记录」
- [ ] 分支已建

---

## 4. 阶段总览

一阶段一 commit，编译通过才进下一阶段。

| 阶段 | 内容 | 改动点 | 风险 | 状态 |
|---|---|---|---|---|
| **P0** | 新增 `Rng.java`（零调用点） | +1 文件 | 无 | ✅ `aa105b7fa` |
| **P1** | `BooleanSupplier` → 自写 `BoolQuery` | 1 文件 | 低 | ✅ `1cad98b09` |
| **P2** | 8 处 `Random.Long()` → `nextSubstream()` | 8 文件 | 低 | ✅ `a2b65faa4` |
| **P2'** | `Generator` 两处（null 资源合并） | 1 文件 | 中 | ✅ `8fe920ec1` |
| **P3** | 任务子种子 4 + 饰品 2 + 地形贴图 1 | 7 文件 | 中（要删旧 finally） | ✅ `81cc5b10d` |
| **P4** | `Dungeon.seedForDepth` | 1 文件 | 中 | ✅ `0d8355ef8` |
| **P5** | 7 处楼层种子 → `levelStream` | 7 文件 | 中 | ✅ `3deaeab2d` |
| **P6** | `RegularLevel.createItems` 块间独立 | 1 文件 | 高（唯一语义变化） | ✅ `3623c8dca` |
| **P6.5** | 缩进重排（3 处平铺 body） | 3 文件 | 无 | ✅ 已做 |
| **P7** | 全量验证 + 种子基线 | — | — | ❌ 待做 |

P2–P4 互相独立可乱序；P5 依赖 P0；P6 依赖 P5。

> **P6.5 说明**：迁移时刻意只动首尾两行，body 保持原缩进，导致 3 处 `try` 与 body 齐平。现已统一：
> `Level.create`（`Level.java:229–341`，body +1 tab）、`EntranceRoom.paint`（`:73–99`）、`Blacksmith.generateRewards`（`:445–490`）。
> 其余 24 处的 body 缩进在迁移时已经正确，无需处理。

---

## 5. 逐阶段作业指导

### P0 — 新增 `Rng.java`

新建 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/utils/Rng.java`，内容见 §7.1（已在真实 classpath 上 `--release 8` 编译通过，`d8 --min-api 19` dex 通过）。

**验收**

```powershell
.\gradlew.bat :core:compileJava
```

**commit**：`feat(rng): 新增随机子流句柄 Rng（零调用点）`

---

### P1 — `BooleanSupplier` → 自写接口

唯一调用点 `core/.../windows/WndWarehouse.java`：

```java
// 删除：import java.util.function.BooleanSupplier;

private interface BoolQuery { boolean test(); }   // 替代 API 24 的 BooleanSupplier

private final BoolQuery unlocked;   // :80
private final BoolQuery enabled;    // :81

SpawnToggleItem(Item item, BoolQuery unlocked, BoolQuery enabled, Runnable toggle) { … }   // :84

if (!unlocked.test()) { … }   // :95, :112
if (enabled.test()) { … }     // :99, :119
```

方法引用 `SPDSettings::p90Unlocked` 无需改动。

**验收**

```powershell
.\gradlew.bat :core:compileJava
Select-String -Recurse -Include *.java -Path core -Pattern 'java\.util\.function'   # 期望无匹配
```

**commit**：`refactor(compat): BooleanSupplier 换为自写 BoolQuery，去掉 API 24 依赖`

---

### P2 — `Random.Long()` 派生子流（本批 8 处 + RegularLevel 2 处并入 P6）

改法（**只动首尾两行**）：

```java
// 前                                  // 后
Random.pushGenerator(Random.Long());   try (Rng.Sub s = Rng.nextSubstream()) {
    …body…                                …body…
Random.popGenerator();                 }
```

| # | 文件 | 行（push / pop） | 备注 |
|---|---|---|---|
| 1 | `items/weapon/Weapon.java` | 525 / 538 | `return this;` 可留在 try 内 |
| 2 | `items/armor/Armor.java` | 915 / 928 | 同上 |
| 3 | `items/weapon/missiles/MissileWeapon.java` | 419 / 431 | 同上 |
| 4 | `levels/rooms/special/ShopRoom.java` | 425 / 427 | 只有一行 shuffle |
| 5 | `levels/MiningLevel.java` | 171 / 181 | `createItems()` |
| 6 | `levels/VaultLevel.java` | 670 / 679 | 外层有 `if (DARKNESS)` |
| 7 | `actors/mobs/npcs/VaultMirror.java` | 67 / 87 | body 是 switch |
| 8 | `levels/RegularLevel.java` | 525 / 553 | ebony（P6 会重排） |
| 9 | `levels/RegularLevel.java` | 556 / 566 | spyglass（P6 会重排） |
| 10 | `levels/rooms/standard/EntranceRoom.java` | 72 / 98 | **无种子 → 有种子**，见下 |

**EntranceRoom 特别注意**：原来是无种子 `pushGenerator()`（不可复现）。改 `nextSubstream()` 后，`Random.Long()` 在 try 头部**无条件**消耗，所以「玩家是否读过书页」不会再让楼层后续取值位移。

```java
try (Rng.Sub s = Rng.nextSubstream()) {
	if (Dungeon.depth() == 1 && !Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_INTRO)) { … }
	if (Dungeon.depth() == 2 && !Document.ADVENTURERS_GUIDE.isPageFound(Document.GUIDE_SEARCHING)) { … }
}
```

**commit**：`refactor(rng): 8 处物品/关卡随机块改用 nextSubstream`（实际提交，RegularLevel 的 2 处并入 P6）

---

### P2' — `Generator` 两处

原来「有牌堆就 push / 无牌堆就不 push」导致同一段逻辑写两遍（`:839`/`:848`）。用 **null 资源**合并：

```java
default:
	boolean decked = cat.defaultProbs != null && seed(cat) != null;
	int i;
	try (Rng.Sub s = decked ? Rng.substream(seed(cat), dropped(cat)) : null) {
		i = Random.chances(probs(cat));
		if (i == -1) { reset(cat); i = Random.chances(probs(cat)); }
	}
	if (decked) setDropped(cat, dropped(cat) + 1);   // ⚠ 必须在 pop 之后
	…
```

`randomArtifact`（`:935–947`）同理，带出 `i`、`j`：

```java
boolean decked = cat.defaultProbs != null && seed(cat) != null;
int i, j;
try (Rng.Sub s = decked ? Rng.substream(seed(cat), dropped(cat)) : null) {
	i = Random.chances(probs(cat));
	j = Random.chances(probs(cat));
}
if (decked) setDropped(cat, dropped(cat) + 1);
```

**commit**：`refactor(rng): Generator 两处牌堆子流合并，消除重复分支`

---

### P3 — 任务子种子 4 + 饰品 2 + 地形贴图 1

#### 3.1 四个 NPC 任务（**必须先删旧 `try/finally`，否则双 pop**）

| # | 文件 | 行 | 种子 |
|---|---|---|---|
| 1 | `actors/mobs/npcs/Ghost.java` | 364 / 407（finally 406） | `Dungeon.seed() ^ 0x47484F5354L` |
| 2 | `actors/mobs/npcs/Imp.java` | 371 / 446（finally 445） | `Dungeon.seed() ^ 0x494D5034L` |
| 3 | `actors/mobs/npcs/Wandmaker.java` | 331 / 343（finally 342） | `Dungeon.seed() ^ 0x57414E44L` |
| 4 | `actors/mobs/npcs/Blacksmith.java` | 444 / 491（finally 490） | `Dungeon.seed() ^ 0x534D495448L` |

```java
// 前
Random.pushGenerator( Dungeon.seed() ^ 0x47484F5354L );
try { …body… } finally { Random.popGenerator(); }

// 后（try/finally 整体删除）
try (Rng.Sub s = Rng.substream( Dungeon.seed() ^ 0x47484F5354L )) { …body… }
```

⚠️ **漏删 `finally` = 双 pop** → 父流少一层，后续取值全错。这是本阶段唯一严重陷阱。

#### 3.2 两个饰品（`+1` 偏移不能丢）

```java
// MossyClump.java:77 / TrapMechanism.java:89
try (Rng.Sub s = Rng.substream(Dungeon.seedCurLevel() + 1)) { … }
```

#### 3.3 地形贴图

```java
// DungeonTileSheet.java:445-454
public static void setupVariance(int size, long seed){
	try (Rng.Sub s = Rng.substream(seed)) {
		tileVariance = new byte[size];
		for (int i = 0; i < tileVariance.length; i++) tileVariance[i] = (byte) Random.Int(100);
	}
}
```

**commit**：`refactor(rng): 任务子种子/饰品/地形贴图改用 Rng.substream()`

---

### P4 — `Dungeon.seedForDepth`

```java
// Dungeon.java:493-504
public static long seedForDepth(int depth, int branch){
	try (Rng.Sub s = Rng.substream(seed(), depth + 30 * branch)) {
		return Random.Long();
	}
}
```

跳格规则从此只在这一行。热点（查种时 3–5 次/层），但改动最小。

**commit**：`refactor(rng): seedForDepth 改用子流，跳格规则单点化`

---

### P5 — 7 处楼层种子

| # | 文件 | 行 | 工厂 |
|---|---|---|---|
| 1 | `levels/Level.java` | 228 / 341 | `Rng.levelStream(levelDepth, levelBranch)`（用**入参**） |
| 2 | `levels/RegularLevel.java` | 410 / 522 | `Rng.levelStream(levelDepth, branch)`（用**字段**） |
| 3 | `actors/mobs/YogDzewa.java` | 114 / 129 | `Rng.levelStream()` |
| 4 | `scenes/GameScene.java` | 690 / 694 | `Rng.levelStream()` |
| 5 | `levels/CityBossLevel.java` | 368 / 373 | `Rng.levelStream(this)` |
| 6 | `levels/rooms/standard/NoelShopRoom.java` | 86 / 89 | `Rng.levelStream(level)` |
| 7 | `levels/rooms/special/SacrificeRoom.java` | 72 / 89 | `Rng.levelStream(level)`（body 内有 early return，TWR 合法） |

`Level.create`：

```java
public void create(int levelDepth, int levelBranch){
	this.levelDepth = levelDepth;
	this.branch    = levelBranch;
	creating = true;

	try (Rng.Sub lv = Rng.levelStream(levelDepth, levelBranch)) {
		…原 230–340 全部逻辑…
		createItems();
	}
	// 原 341 的 pop 由 TWR 取代
	creating = false;

	for (Integer pos : triggeredPlant.keySet()) …
}
```

⚠️ `creating = false` 保持 try **之后**（与原顺序一致）。
⚠️ 后三处必须用 `Level` 重载，不能用无参版（`Dungeon.depth()` 与 `level` 自身深度可能不一致）。

**commit**：`refactor(rng): 7 处楼层种子改用 Rng.levelStream()`

---

### P6 — `RegularLevel.createItems` 块间解耦（唯一语义变化）

**现状**：

```
push(seedToCreate)
   410–522  大块：直接跑在楼层种子上
pop
push(Random.Long())      ← 位置 = 大块消耗完后的下一格
   525–553  ebony
pop
push(Random.Long())
   556–566  spyglass
pop
```

**问题**：后两块的子种子位置 = 「大块消耗了多少格」。往大块加一行 `Random.Float()`，全服种子集体位移。

**目标**：

```java
@Override
protected void createItems() {
	try (Rng.Sub lv = Rng.levelStream(levelDepth, branch)) {
		createItemsCore();                                              // 原 410–522
		try (Rng.Sub s1 = Rng.nextSubstream()) { ebonyMimic(); }        // 原 525–553
		try (Rng.Sub s2 = Rng.nextSubstream()) { extraSpyglassLoot(); } // 原 556–566
	}
}
```

**重构步骤（必须按序）**

1. 410–522 整段剪切到新私有方法 `createItemsCore()`，**不加不减**；
2. 525–553 → `ebonyMimic()`；
3. 556–566 → `extraSpyglassLoot()`；
4. 新 `createItems()` 只留上面 6 行；
5. 检查三个新方法是否引用原方法局部变量 → 必须全是字段或参数。

⚠️ **Java 禁止局部变量遮蔽**：嵌套 resource 必须 `s1`/`s2`（同名编译失败）。
⚠️ 本阶段是唯一行为变化，commit message 加 `!`。

**commit**：`refactor(rng)!: RegularLevel.createItems 三块改为独立子流（块间不再共享消耗位置）`

---

### P7 — 全量验证与收尾

见 §8，并把文末「执行记录」补全。

---

## 6. 全量改动清单（29 处）

| # | 文件:行 | 现状 | 目标 | 阶段 |
|---|---|---|---|---|
| 1 | `Dungeon.java:494–502` | `seed()` + 跳 `depth+30b` + 取 1 | `substream(seed(), depth+30*branch)` | P4 |
| 2 | `Generator.java:839–849` | `seed(cat)` + 跳 `dropped(cat)` | null 资源合并 | P2' |
| 3 | `Generator.java:939–945` | 同上 | 同上 | P2' |
| 4 | `RegularLevel.java:410–522` | 楼层种子 | `levelStream` | P5 |
| 5 | `RegularLevel.java:525–553` | `Random.Long()` | `nextSubstream()` | P2 → P6 |
| 6 | `RegularLevel.java:556–566` | `Random.Long()` | `nextSubstream()` | P2 → P6 |
| 7 | `Level.java:228–341` | 楼层种子 | `levelStream(levelDepth, levelBranch)` | P5 |
| 8 | `YogDzewa.java:114–129` | 楼层种子 | `levelStream()` | P5 |
| 9 | `GameScene.java:690–694` | 楼层种子 | `levelStream()` | P5 |
| 10 | `CityBossLevel.java:368–373` | 楼层种子(this) | `levelStream(this)` | P5 |
| 11 | `NoelShopRoom.java:86–89` | 楼层种子(level) | `levelStream(level)` | P5 |
| 12 | `SacrificeRoom.java:72–89` | 楼层种子(level) | `levelStream(level)` | P5 |
| 13 | `Weapon.java:525–538` | `Random.Long()` | `nextSubstream()` | P2 |
| 14 | `Armor.java:915–928` | `Random.Long()` | `nextSubstream()` | P2 |
| 15 | `MissileWeapon.java:419–431` | `Random.Long()` | `nextSubstream()` | P2 |
| 16 | `ShopRoom.java:425–427` | `Random.Long()` | `nextSubstream()` | P2 |
| 17 | `MiningLevel.java:171–181` | `Random.Long()` | `nextSubstream()` | P2 |
| 18 | `VaultLevel.java:670–679` | `Random.Long()` | `nextSubstream()` | P2 |
| 19 | `VaultMirror.java:67–87` | `Random.Long()` | `nextSubstream()` | P2 |
| 20 | `EntranceRoom.java:72–98` | 无种子 | `nextSubstream()` | P2 |
| 21 | `Ghost.java:364–407` | 任务子种子 + try/finally | `substream(...)`，删 finally | P3 |
| 22 | `Imp.java:371–446` | 同上 | 同上 | P3 |
| 23 | `Wandmaker.java:331–343` | 同上 | 同上 | P3 |
| 24 | `Blacksmith.java:444–491` | 同上 | 同上 | P3 |
| 25 | `MossyClump.java:77–88` | `seedCurLevel()+1` | `substream(seedCurLevel()+1)` | P3 |
| 26 | `TrapMechanism.java:89–100` | `seedCurLevel()+1` | 同上 | P3 |
| 27 | `DungeonTileSheet.java:446–453` | 显式 `seed` | `substream(seed)` | P3 |
| 28 | `Dungeons.java:85` | 悬空 push | **不动** | — |
| 29 | `Dungeons.java:112` | 悬空 push | **不动** | — |

---

## 7. 复用代码

### 7.1 `Rng.java` 全文

```java
package com.shatteredpixel.shatteredpixeldungeon.utils;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.Random;

/**
 * 随机子流。
 *
 * 一个子流 = push 种子 -> 在独立 RNG 上跑一段 -> pop。用法固定为：
 *
 *     try (Rng.Sub s = Rng.substream(seed)) { …原逻辑原样放这里… }
 *
 * 语义分类（名字不同，避免误用）：
 *   substream(seed[, skip])  种子由调用方给出（任务子种子 / 牌堆种子 / seedForDepth 跳数）
 *   nextSubstream()          从当前流取下一个 Long 作种子：楼层内第 k 段独立子流
 *   levelStream(…)           楼层种子，body 直接跑在该种子上（种子回放）
 *
 * 约束：
 *  1. Sub 只能出自本类工厂，且只能出现在 try-with-resources 头部；不得手动 close()；
 *  2. 级别生成路径上禁止使用无种子子流（会让同一颗种子每次求值结果不同）；
 *  3. 子流内部不得调用 Random.resetGenerators()（会清空整栈，使 close() 下溢）；
 *  4. 依赖 java.lang.AutoCloseable（API 19，与 minSdk 完全对齐，零余量）；
 *     若将来下调 minSdk，改为 implements java.io.Closeable（API 1）即可，用法不变；
 *  5. Dungeons.init 的两处 push 是「整栈重置」语义，不属于本模型，保持手写。
 */
public final class Rng {

	private Rng(){}

	/** 已压栈的子流句柄；close() = pop。无状态，全局复用一个实例（零分配）。 */
	public static final class Sub implements AutoCloseable {
		Sub(){}
		@Override public void close(){
			Random.popGenerator();
		}
	}

	private static final Sub HANDLE = new Sub();

	/** 以 seed 开一个子流。 */
	public static Sub substream(long seed){
		Random.pushGenerator(seed);
		return HANDLE;
	}

	/** 以 seed 开子流，并先空转 skip 个 Long（牌堆重放 / seedForDepth 的跳数）。 */
	public static Sub substream(long seed, int skip){
		Random.pushGenerator(seed);
		for (int i = 0; i < skip; i++) Random.Long();
		return HANDLE;
	}

	/** 从当前流取下一个 Long 作种子：楼层内的第 k 段。 */
	public static Sub nextSubstream(){
		return substream(Random.Long());
	}

	// ---------------------------------------------------------------- 楼层

	public static Sub levelStream(){
		return levelStream(Dungeon.depth(), Dungeon.branch());
	}

	public static Sub levelStream(Level level){
		return levelStream(level.levelDepth, level.branch);
	}

	public static Sub levelStream(int depth, int branch){
		return substream(Dungeon.seedForDepth(depth, branch));
	}
}
```

### 7.2 四种标准改法

```java
// (a) 从当前流派生
try (Rng.Sub s = Rng.nextSubstream()) { … }

// (b) 显式种子
try (Rng.Sub s = Rng.substream( Dungeon.seed() ^ 0x47484F5354L )) { … }

// (c) 楼层种子
try (Rng.Sub s = Rng.levelStream(levelDepth, branch)) { … }

// (d) 条件性子流（资源可为 null，body 只写一份）
try (Rng.Sub s = decked ? Rng.substream(seed(cat), dropped(cat)) : null) { … }
```

---

## 8. 验证清单

### 8.1 每阶段

```powershell
.\gradlew.bat :core:compileJava
```

### 8.2 P0 之后（一次性）

```powershell
# 输出到 build/ 下（已被 .gitignore 忽略，不会出现在改动区）
New-Item -ItemType Directory -Force core\build\tmp\verify\classes, core\build\tmp\verify\dex | Out-Null

javac --release 8 -cp "core\build\classes\java\main;SPD-classes\build\classes\java\main" `
      -d core\build\tmp\verify\classes core\src\main\java\com\shatteredpixel\shatteredpixeldungeon\utils\Rng.java

$d8 = "F:\aa\SDK\build-tools\34.0.0\lib\d8.jar"
java -cp $d8 com.android.tools.r8.D8 --min-api 19 `
     --lib "F:\aa\SDK\platforms\android-34\android.jar" `
     --classpath "core\build\classes\java\main" --classpath "SPD-classes\build\classes\java\main" `
     --output core\build\tmp\verify\dex "core\build\classes\java\main\com\shatteredpixel\shatteredpixeldungeon\utils\Rng.class" `
     "core\build\classes\java\main\com\shatteredpixel\shatteredpixeldungeon\utils\Rng`$Sub.class"
# 期望 exit=0，生成 core\build\tmp\verify\dex\classes.dex
```

### 8.3 P7 全量

- [ ] `:core:compileJava` 绿
- [ ] `:desktop:debug` 能起，连过 5 层
- [ ] `:android:lintDebug` → NewApi 无新增
- [ ] 残留搜索：
  ```powershell
  Select-String -Recurse -Include *.java -Path core,SPD-classes -Pattern 'pushGenerator|popGenerator'
  # 期望：只剩 Dungeons.java 两处 + Rng.java 内部
  Select-String -Recurse -Include *.java -Path core -Pattern 'java\.util\.function'
  # 期望：无匹配（WndBlackMarket 里 2 条解释性注释除外）
  ```
- [ ] 缩进自检：每个 `try (Rng.Sub` 的 body 首行缩进 > try 缩进，闭合 `}` 缩进 == try 缩进
  （27 处已全部通过；`Level.java` 原第 232 行的 12 空格缩进已归一为 4 tab）
- [ ] 功能回归：仓库界面、4 个 NPC 奖励、Boss 层、宝库层、商场层
- [ ] 查种器：单线程 2 分钟 + 四线程 2 分钟，无 `workerErrors`，两边命中结果一致
- [ ] P6 之后重建种子基线：`SeedFinder.logSeedItems` 跑 200 颗固定样本 → `docs/seed-baseline/after-P6.txt`

---

## 9. 回滚

- 每阶段一个 commit → `git revert <hash>` 单阶段回退；
- P0 纯新增，可单独保留；
- P6 是唯一行为变化，出问题优先 revert P6；
- 整体放弃：`git switch main`（分支保留备查）。

---

## 10. 风险表

| 风险 | 触发 | 后果 | 对策 |
|---|---|---|---|
| **双 pop** | P3 加 TWR 但没删旧 `try/finally` | 父流少一层，后续取值错 | 改完 `grep finally <file>` 必须为空 |
| **漏 pop** | 把 `pop` 留在 try 外 | 栈变深、守卫失效 | §8.3 残留搜索必须为空 |
| **`close()` 下溢** | try 内调 `resetGenerators()` | 空栈 NPE | 不变式 3 |
| **同名遮蔽** | P6 嵌套写同名 resource | 编译失败 | 用 `s1`/`s2` |
| **`setDropped` 顺序** | P2' 把它挪进 try | 牌堆计数错，物品分布漂移 | 保持 pop 之后 |
| **`EntranceRoom` 退化** | 用了无种子 | 同种子每次不同 | 用 `nextSubstream()` |
| **`levelStream` 用错楼层** | 后三处用无参重载 | 种子错 | 用 `levelStream(level)` |
| **`Level.create` 时序** | 用 `Dungeon.depth()` 而非入参 | 换层瞬间种子错 | 用 `levelStream(levelDepth, levelBranch)` |
| **种子漂移** | P6 + EntranceRoom | 有意为之 | commit 标 `!`，P7 重建基线 |
| **API 19 零余量** | `AutoCloseable` 恰为 API 19 | 未来降 minSdk 会崩 | 改 `implements Closeable` |

---

## 11. 完成定义（DoD）

1. `Rng` 是全项目唯一调用 `pushGenerator/popGenerator` 的地方（`Dungeons.init` 两处除外，带注释）。
2. 全项目 `java.util.function` 引用数 = 0。
3. `:core:compileJava`、`:android:lintDebug` 均绿。
4. `Rng` 在 `--release 8` 与 `--min-api 19` 下均通过。
5. 游戏内 7 类场景走通（普通层/Boss/宝库/商场/水井/仓库/4 个 NPC 奖励）。
6. 查种器 1 线程、4 线程各 2 分钟无异常且结果一致。
7. P6 之后的种子基线已入库。

---

## 附录 A：性能实测（含噪声说明）

### A.1 三种写法等价（交错 9 轮，body ≈ 60 ns，1M 次/轮）

| 变体 | min | median | max |
|---|---|---|---|
| 无保护 `push/pop` | 79.69 | 85.63 | 89.57 |
| `try/finally` | 79.94 | 84.29 | 93.89 |
| `try (Rng.Sub)` | 80.14 | 85.36 | 86.61 |

min 差 **0.6%**；同一变体轮间波动达 **18%**（79.7~93.9）。→ **三者等价，任何 <5% 的差都不能解释。**

> 更早一次单轮测量显示「无保护 83.67 / try-finally 82.90 / TWR 81.09」，那是单次运行的噪声，不作为结论。

### A.2 小 body 时 try 会显得贵，是内联假象

body 只有一条 `sink += 1` 时：裸写法 29.20，`try/finally` 41.39，工厂+TWR 35.55。
原因：小 body 被内联进异常处理块，打断指令调度；body 一大（真实规模）这个差就消失。

### A.3 每次 open 的 29 ns 全在 `new java.util.Random`

| 测量 | ns |
|---|---|
| `new java.util.Random(seed).nextLong()` | 29.03 |
| `pushGenerator` + `popGenerator`（无 body） | 28.63 |
| 单次 `Random.Long()` 边际（查种上下文） | ~18 |

→ push/pop 本身 ≈ 0，包装写法不影响这个成本。

### A.4 唯一稳健的优化杠杆：换 RNG 表示

| 场景 | 现状 | long 栈 + SplitMix64 | 倍数 |
|---|---|---|---|
| 1 次 open（1 次取值） | 46.62 | 8.78 | 5.3× |
| `seedForDepth`（跳 21 + 取 1） | 427.18 | 31.41 | 13.6× |
| branch=2 边界（跳 81 + 取 1） | 1570.68 | 98.64 | 15.9× |
| 单次取值 | 18.1 | 1.08 | ~17× |

本次不做（决策 7）。若将来要优化查种器速度，这是唯一值得动的地方。

> 测量用的临时基准脚本未入库（已删除）；上表数字来自本机 JDK 17 的一次性测跑，如需复现按「交错 9 轮 + 取 min + 报 max」的方法重建即可。

---

## 附录 B：为什么不用 lambda / 三档 Calling API

| 项 | 后果 |
|---|---|
| `return` 出不来 | `SeedForDepth` 的 long、`Generator` 的下标、`SacrificeRoom` 的 Item 都需要泛型返回值版 + 装箱（实测 +13.6 ns/op） |
| 局部变量必须 effectively final | `GameScene` 的 `reqSecrets`、`Generator` 的 `i` 都要重构 |
| 受检异常不能抛 | 需要包装 |
| megamorphic | 27 个回调类汇到单点 `c.call()`，实测 +8~14 ns（16 类） |
| class 文件 | 匿名类方案每站点 +1 个 `.class`（实测 4 个匿名类 → 4 个文件） |

`try (Rng.Sub)` 无以上任何一条。

---

## 附录 C：为什么不开 coreLibraryDesugaring

| 项 | 代价 |
|---|---|
| APK | +1~2 MB |
| 反射 | `getClass().getName()` 变成 `j$.util.function.Xxx` |
| minSdk | 官方支持下限 API 21；本项目 19 |
| 替代方案 | 3 行自写接口（P1） |

---

## 执行记录

| 项 | 值 |
|---|---|
| 分支 | `refactor/rng-substream` |
| 基线 commit | `7990c9612`（重构前最后一个非本主题提交） |
| P0 | ✅ `aa105b7fa` feat(rng): 新增 Rng 子流工具类 |
| P1 | ✅ `1cad98b09` refactor(compat): WndWarehouse 以 BoolQuery 替代 BooleanSupplier |
| P2 | ✅ `a2b65faa4` refactor(rng): 8 处物品/关卡随机块改用 nextSubstream |
| P2' | ✅ `8fe920ec1` refactor(rng): Generator 牌堆重放改用 substream |
| P3 | ✅ `81cc5b10d` refactor(rng): 任务/饰品/地块纹理改用种子子流 |
| P4 | ✅ `0d8355ef8` refactor(rng): Dungeon.seedForDepth 改用 substream 跳数 |
| P5 | ✅ `3deaeab2d` refactor(rng): 7 处楼层种子改用 levelStream |
| P6 | ✅ `3623c8dca` refactor(rng): RegularLevel.createItems 拆分为独立子流段落 |
| P6.5 缩进重排 | ✅ Level.create / EntranceRoom.paint / Blacksmith.generateRewards |
| P7 | ❌ 待做（游戏回归 / lintDebug / 查种器 / 种子基线） |
| 种子基线快照 | ❌ 待做 |
| `:core:compileJava` | ✅ 通过（含 P6.5 之后复验） |

**待提交**：`Rng` 从 `custom/utils` 移到 `utils` 的改名 + 25 处 import 修正目前只在工作区（`git status` 显示 1 个已暂存改名 + 25 个未暂存改动）。**注意：此时只提交暂存区会得到一个无法编译的提交**，必须先 `git add -A core/` 再提交。
