# 多线程 `cur()` 泄露收敛 — 会话交接

> 目的：让后续会话无需回溯原始对话，即可明确全部工作内容、约束、待办与上下文。

---

## 1. 总目标

把"多线程查种"各上下文类的 `cur()` 向外泄露收敛，最终让持有者类的 `cur()`（及状态字段）对外私有化，外部一律通过**静态门面**访问。统一采用 **PathFinder 范式**：业务状态与逻辑密集下沉到一个 `private static final class State` 内部类，外层只保留线程上下文骨架（`MAIN`/`SEARCH_CTX`/`cur()`/`enterSearchContext()`/`exitSearchContext()`）+ 纯转发静态门面。

- `Dungeons.cur()`：**保持 public**，仅允许 `Dungeon` 内部（及 SeedFinder 子系统）作为合法调用方。
- `Dungeon.cur()`：阶段三私有化。
- `PathFinder.cur()`：已私有化。
- 各子系统（Generator/SpecialRoom/SecretRoom/NPC Quest）的 `cur()`：类内私有。

---

## 2. 核心范式（模板）

```java
public class Xxx {

    //业务状态 + 逻辑全部下沉于此内部类
    private static final class State {
        //专属于该类、本应线程隔离的每局状态字段

        //所有业务方法（原 static 方法）收为实例方法，直接读写 this 字段
        void doSomething() { ... }
    }

    private static final State MAIN = new State();
    private static final ThreadLocal<State> SEARCH_CTX = new ThreadLocal<>();

    private static State cur() {
        State s = SEARCH_CTX.get();
        return (s != null) ? s : MAIN;
    }

    public static void enterSearchContext() { SEARCH_CTX.set(new State()); }
    public static void exitSearchContext()  { SEARCH_CTX.remove(); }

    //静态门面：仅做线程路由，不持有逻辑、不直取字段
    public static void doSomething() { cur().doSomething(); }
}
```

要点：
- 存档读写（store/restore）方法始终走 `MAIN`（主线程），不放 `cur()`。
- 外层门面只 `cur().xxx()` / `MAIN.xxx()`，禁止在外层 `cur().字段` 直取。

---

## 3. 已完成改动

### 3.1 PathFinder（SPD-classes 模块）
文件：`SPD-classes/src/main/java/com/watabou/utils/PathFinder.java`
- `cur()` 已私有化。
- 全部工作代码（方向位掩码 `DIR_BITS/DIRLR_BITS/DIRLR_OPP_BITS`、字段、`find/getStep/getStepBack/buildDistanceMap/direction/buildEscapeDistanceMap`）下沉到 `private static final class State`。
- 外层只留上下文骨架 + 静态门面（`setMapSize/distance/NEIGHBOURS*/CIRCLE*/find/getStep/getStepBack/buildDistanceMap/direction`）。
- 实例 `setMapSize` 更名 `initMapSize`（避免与静态门面同名冲突）。
- 全项目 11 处 `PathFinder.cur().setMapSize(...)` → `PathFinder.setMapSize(...)`。

### 3.2 Generator
文件：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java`
- 6 个生成字段迁入本地 `State` 内部类（字段名恢复多线程化前旧名）：
  `usingFirstDeck / categoryProbs / catProbs / catSeeds / catDropped / catUsing2ndProbs`。
- `State()` 构造器内从 `Category` 静态模板克隆概率数组（原 `Dungeons.reset()` 逻辑迁入）。
- `Category` 的 8 个访问器（`probs/setProbs/using2ndProbs/setUsing2ndProbs/seed/setSeed/dropped/setDropped`）路由 `Generator.cur().xxx`。
- `fullReset/generalReset/random/storeInBundle/restoreFromBundle` 每方法首行 `State s = cur()` 取一次。
- 移除 `import Dungeons`，新增 `import LinkedHashMap`。

### 3.3 SpecialRoom（已彻底下沉）
文件：`core/.../levels/rooms/special/SpecialRoom.java`
- 字段 + 全部业务方法下沉 `State` 内部类：
  `initForRun/initForFloor/useType/resetPitRoom/createRoom/restoreRoomsFromBundle/storeRoomsInBundle`。
- 外层只留 6 个转发门面：
  `initForRun/initForFloor/resetPitRoom/createRoom`（走 `cur()`）、
  `restoreRoomsFromBundle/storeRoomsInBundle`（走 `MAIN`）。
- 原 4 个字段门面 `runSpecials()/floorSpecials()/pitNeededDepth()/setPitNeededDepth()` 已删除。

### 3.4 SecretRoom（字段已迁，逻辑未彻底下沉 —— 见待办 4.1）
文件：`core/.../levels/rooms/secret/SecretRoom.java`
- 字段迁入 `State` 内部类：`runSecrets/regionSecretsThisRun/regionSecretsThisRandom`。
- 未彻底：`initForRun/restoreRoomsFromBundle/storeRoomsInBundle` 仍在外层用 `cur()/MAIN`；`runSecrets()/regionSecretsThisRun()/regionSecretsThisRandom()` 字段门面仍存在。

### 3.5 四个 NPC Quest
文件：`core/.../actors/mobs/npcs/{Ghost,Wandmaker,Blacksmith,Imp}.java`
- 各自 `Quest.cur()/main()` 从 `Dungeons.cur().xxxQuest / Dungeons.MAIN().xxxQuest` 改为本地 `MAIN + SEARCH_CTX` 线程隔离。
- 各 `Quest` 新增 `enterSearchContext()/exitSearchContext()`。
- 移除无效 `import Dungeons`。

### 3.6 isSearch 门面
- `Dungeon` 新增 `public static boolean isSearch() { return cur().isSearch; }`。
- 7 处收敛：`CityBossLevel`、`Plant`、`Bones`、`Wandmaker`、`Imp`、`Ghost`、`Blacksmith` 均由 `Dungeons.cur().isSearch` / `Dungeon.cur().isSearch` 改为 `Dungeon.isSearch()`。

### 3.7 Dungeons.java 瘦身
文件：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Dungeons.java`
- 删除已迁移字段（10 个）：
  `genUsingFirstDeck/genCategoryProbs/genCatProbs/genCatSeeds/genCatDropped/genCatUsing2ndProbs`、
  `ghostQuest/wandmakerQuest/blacksmithQuest/impQuest`、
  `spRunSpecials/spFloorSpecials/spPitNeededDepth`、
  `secRunSecrets/secRegionSecretsThisRun/secRegionSecretsThisRandom`。
- 删除无用 import：`Room/ArrayList/HashMap/LinkedHashMap`。
- `reset()` 现只剩 `depth = 0; branch = 0;`。
- 保留字段：`hero/level/depth/branch/seed/mobsToChampion/limitedDrops/isSearch`。

### 3.8 Dungeon.enterSearchContext / exitSearchContext
文件：`core/.../Dungeon.java`
- enter 顺序（exit 严格逆序）：
  `Dungeons → Random → PathFinder → Generator → SpecialRoom → SecretRoom → Ghost.Quest → Wandmaker.Quest → Blacksmith.Quest → Imp.Quest`。

---

## 4. 待办

### 4.1 SecretRoom 下沉整合（仿 SpecialRoom）
把 `initForRun/restoreRoomsFromBundle/storeRoomsInBundle` 下沉进 `State` 实例方法，外层改转发门面；删除 3 个字段门面 `runSecrets()/regionSecretsThisRun()/regionSecretsThisRandom()`。注意 `restore/store` 走 `MAIN`，`initForRun` 走 `cur()`。

### 4.2 阶段三：Dungeon.cur() 私有化（大头，约 250 处）
现 `Dungeon.cur()` 为 public，透传 `Dungeons.cur()`。需私有化，并新增门面按成员收敛：

| 成员 | 规模 | 门面方案 |
|---|---|---|
| `.depth` | ~180 处 | 已有 `curDepth()`；新增 `setDepth(int)` 覆盖 `depth = x` 及 `depth++/--` |
| `.seed` | ~15 处 | 新增 `seed()`/`setSeed(long)` |
| `.branch` | 7 处 | 新增 `branch()`/`setBranch(int)` |
| `.hero` | 8 处赋值 | 已有 `hero()`（读）；新增 `setHero(Hero)`/`clearHero()` |
| `.level` | 1 处（Plant:91） | 新增 `curLevel()` |
| `.mobsToChampion` | 3 处 | 新增 `mobsToChampion()`/`setMobsToChampion(int)` |

替换规则（机械）：
- 读 `.depth` → `Dungeon.curDepth()`；写 `.depth` → `Dungeon.setDepth(...)` / `++/--` 改为 `setDepth(depth±1)`。
- `.seed` 读 → `Dungeon.seed()`；写 → `setSeed(...)`。
- `.hero` 读 → `Dungeon.hero()`；写 → `setHero/clearHero`。
- 门面实现内部均经 `Dungeons.cur()` 路由。

---

## 5. 关键约束（必须遵守）

1. **Java 1.8 及更早**：不用 Lambda、Stream、`var`、`List.of()`、文本块、`record`、switch 表达式等 Java 9+ 特性。
2. **代码注释中文**，与现有风格一致。
3. **玩家友好术语**（如"护盾/燃烧"，非 `Barrier` 等代码词），仅涉及卡牌/玩法文案时。
4. **存档兼容**：各子系统 `store/restore` 必须走 `MAIN`，bundle 键与读写顺序不变；迁移只改"内存存放位置"，不改序列化格式。
5. **`enterSearchContext` 注册完整性**：任何新增 ThreadLocal 子系统，必须在 `Dungeon.enterSearchContext/exitSearchContext` 成对注册（exit 逆序），漏注册会造成 worker 与主世界共享状态（跨线程污染）。
6. **名字冲突**：Java 不允许同类同签名的 static 与实例方法共存，下沉实例方法时若与静态门面同名，需重命名实例方法（参考 PathFinder 的 `initMapSize`）。

---

## 6. 编译验证方法

命令（在项目根 `f:\AAF\GirlsFrontline-pixel-dungeon-reborn`）：

```powershell
.\gradlew.bat :SPD-classes:compileJava --no-daemon   # 改 SPD-classes 后
.\gradlew.bat :core:compileJava --no-daemon          # 改 core 后
```

**内存注意**：本机内存紧张（7.9GB，空闲常仅 ~1GB），默认 `gradle.properties` 的 `-Xmx2048m` 会触发 JVM CodeCache/内存分配失败。编译前临时把 `org.gradle.jvmargs` 改为：

```
-Xmx512m -XX:MaxMetaspaceSize=192m -XX:ReservedCodeCacheSize=64m -Dfile.encoding=UTF-8
```

编译完成后 **必须恢复原值**：
```
-Xmx2048m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
```

若仍失败，先 `Get-Process java` 查找并 `Stop-Process` 清理残留 gradle daemon（**不要**杀掉 IDE 语言服务器：路径含 `extensions\redhat.java` 的 java 进程）。

---

## 7. 风险点（阶段三前必须核实）

1. **`level` 双轨**：`Dungeon.java` 仍有静态 `Dungeon.level`（多处旧代码用），而 `Dungeons` 实例字段 `level` 是另一处。`Plant.java:91` 用实例版 `Dungeon.cur().level`。加 `curLevel()` 门面前，必须确认两处赋值是否始终同步，否则门面会返回错误对象。
2. **depth 写语义**：`InterlevelScene`（约 34 处 depth）含 `depth++/--`、`depth = returnDepth` 等楼层切换核心路径，`setDepth` 替换需逐处核对偏移，不可机械全局替换 `++/--`。

---

## 8. 文件索引

| 文件 | 状态 |
|---|---|
| `SPD-classes/.../watabou/utils/PathFinder.java` | ✅ 完成（State 下沉 + cur 私有化） |
| `core/.../items/Generator.java` | ✅ 完成 |
| `core/.../levels/rooms/special/SpecialRoom.java` | ✅ 完成（逻辑彻底下沉） |
| `core/.../levels/rooms/secret/SecretRoom.java` | ⚠️ 字段已迁，逻辑未下沉 |
| `core/.../actors/mobs/npcs/{Ghost,Wandmaker,Blacksmith,Imp}.java` | ✅ 完成 |
| `core/.../Dungeon.java` | 🔶 部分（isSearch 门面 + 注册完成；阶段三 cur 私有化待做） |
| `core/.../Dungeons.java` | 🔶 字段已瘦身（阶段三后进一步加/改门面） |