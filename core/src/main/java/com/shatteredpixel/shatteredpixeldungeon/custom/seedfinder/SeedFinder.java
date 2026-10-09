package com.shatteredpixel.shatteredpixeldungeon.custom.seedfinder;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Dungeons;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalMimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GoldenMimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.EnergyCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CeremonialCandle;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public abstract class SeedFinder implements Runnable {

    public static volatile boolean running;
    public static volatile boolean SeedFinding = false;

    // 各 worker 当前扫描的种子：场景线程每 0.25s 轮询显示，-1 表示尚未开始
    private volatile AtomicLongArray workerSeeds = newSeedArray(1);
    private volatile int workerCount = 1;

    private static AtomicLongArray newSeedArray(int n) {
        AtomicLongArray a = new AtomicLongArray(n);
        for (int i = 0; i < n; i++) a.set(i, -1);
        return a;
    }

    public int workerCount() {
        return workerCount;
    }

    public long workerSeed(int index) {
        return workerSeeds.get(index);
    }

    // 诊断：每个 worker 的停滞堆栈 / 异常信息（场景线程每 0.25s 显示）
    private volatile String[] workerErrors = new String[0];
    private volatile long[] workerProgressMs = new long[0];

    public String workerError(int index) {
        String[] errs = workerErrors;
        return (index >= 0 && index < errs.length) ? errs[index] : null;
    }

    private static String stackBrief(StackTraceElement[] st, int max) {
        StringBuilder sb = new StringBuilder();
        int n = Math.min(max, st.length);
        for (int k = 0; k < n; k++) sb.append("\n    ").append(st[k]);
        return sb.toString();
    }

    // logSeedItems 填充：本次生成的结构化楼层数据（供场景构建 Tab 结果窗）
    public final ArrayList<FloorData> lastFloors = new ArrayList<>();
    // logSeedItems 填充：饰品牌堆"洗牌前"的完整开出序列（供总览页显示）
    public final ArrayList<Item> lastTrinketSequence = new ArrayList<>();

    protected WantedTarget[] wantedArr;
    // Class → 目标下标数组：tryMatch 先查 map 取候选目标，跳过无关物品
    private HashMap<Class<? extends Item>, int[]> matchIndex;
    // 预筛下标：循环内直接遍历，避免对全量 wantedArr 逐条 isAssignableFrom
    protected final int floor;
    protected final HeroClass heroClass;
    protected SeedFinder(ArrayList<Item> wanted, int fl, HeroClass cl) {
        wantedItems = wanted;
        buildMatchIndex();
        floor = fl;
        heroClass = cl;
        need = Math.max(1, wantedArr.length - SPDSettings.seedFinderMissing());
    }
    WantedTarget wand;
    WantedTarget ring;
    Class<? extends Trinket> trinket;
    public final ArrayList<Item> wantedItems;

    // 构造时生成目标数组并按 cls 分组下标，供 tryMatch 做 O(1) 跳查
    private void buildMatchIndex() {
        ArrayList<WantedTarget> targets = new ArrayList<>();
        HashMap<Class<? extends Item>, ArrayList<Integer>> temp = new HashMap<>();
        int index = 0;
        for (Item item : wantedItems) {
            for (int i = 0; i < item.quantity(); i++) {
                if (item instanceof Trinket) {
                    trinket = (Class<? extends Trinket>) item.getClass();
                    item = new TrinketCatalyst();
                }
                WantedTarget w = new WantedTarget(item);
                targets.add(w);
                if ((item instanceof Wand || item instanceof Holder.WandHolder) && item.level() >= 3)
                    wand = w;
                else if ((item instanceof Ring || item instanceof Holder.RingHolder) && item.level() >= 3)
                    ring = w;
                if (item instanceof Holder)
                    ((Holder) item).fillMap(temp, index);
                else {
                    ArrayList<Integer> list = temp.get(item.getClass());
                    if (list == null)
                        temp.put(item.getClass(), list = new ArrayList<>());
                    list.add(index);
                }
                index++;
            }
        }
        wantedArr = targets.toArray(new WantedTarget[0]);

        HashMap<Class<? extends Item>, int[]> idx = new HashMap<>();
        for (Map.Entry<Class<? extends Item>, ArrayList<Integer>> e : temp.entrySet()) {
            ArrayList<Integer> list = e.getValue();
            int[] indices = new int[list.size()];
            for (int k = 0; k < list.size(); k++) {
                indices[k] = list.get(k);
            }
            idx.put(e.getKey(), indices);
        }
        matchIndex = idx;
    }
    public void buildLogMatchIndex() {
        HashMap<Class<? extends Item>, ArrayList<Integer>> temp = new HashMap<>();
        int index = 0;
        for (Item item : wantedItems)
            for (int i = 0; i < item.quantity(); i++) {
                if (item instanceof Holder) {
                    ArrayList<Integer> list = temp.get(item.getClass());
                    if (list == null)
                        temp.put(item.getClass(), list = new ArrayList<>());
                    list.add(index);
                }
                index++;
            }
        for (Map.Entry<Class<? extends Item>, ArrayList<Integer>> e : temp.entrySet()) {
            ArrayList<Integer> list = e.getValue();
            int[] indices = new int[list.size()];
            for (int k = 0; k < list.size(); k++) {
                indices[k] = list.get(k);
            }
            matchIndex.put(e.getKey(), indices);
        }
    }
    // 多线程分段查种：随机起始种子后，把 [start, TOTAL_SEEDS) 区间均分为 threadCount 段，
    // 每个 worker 独立进入查种上下文扫自己那段，首个命中即广播停止所有线程。
    public final String findSeed(final int threadCount) {
        if (threadCount <= 1) {
            String result = "NONE";
            SeedFinding = true;
            running = true;
            workerCount = 1;
            workerSeeds = newSeedArray(1);

            //种子域 [0, TOTAL_SEEDS) 环形扫描：随机起点 + 取模，种子永不越界
            final long start = Random.Long(DungeonSeed.TOTAL_SEEDS);

            for (long j = 0; j < DungeonSeed.TOTAL_SEEDS
                    && running && SeedFinding; ++j) {
                long currentSeed = (start + j) % DungeonSeed.TOTAL_SEEDS;
                workerSeeds.set(0, currentSeed);

                // 10 连复查：命中目标必须落在该种子各楼层生成变体的交集内
                boolean confirmed = true;
                for (int r = 0; r < 10; r++) {
                    if (!testSeed(currentSeed)) {
                        confirmed = false;
                        break;
                    }
                }
                if (confirmed) {
                    result = logSeedItems(currentSeed);
                    break;
                }

                if (Thread.currentThread().isInterrupted()) {
                    running = false;
                    break;
                }
            }
            SeedFinding = false;
            return result;
        }

        // ---- 多线程分段 ----
        SeedFinding = true;
        running = true;
        foundSeed.set(-1);
        workerCount = threadCount;
        workerSeeds = newSeedArray(threadCount);
        workerErrors = new String[threadCount];
        workerProgressMs = new long[threadCount];
        long initMs = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) workerProgressMs[i] = initMs;

        //种子域 [0, TOTAL_SEEDS)：环形偏移随机起点，各 worker 均分偏移段
        //currentSeed = (start + j) % total 永在合法域内（原先 seedDigits + i 会越过域上界导致后段 worker 全灭）
        final long total = DungeonSeed.TOTAL_SEEDS;
        final long start = Random.Long(total);
        Thread[] workers = new Thread[threadCount];

        for (int w = 0; w < threadCount; w++) {
            final int workerId = w;
            final long segStart = total * w / threadCount;
            final long segEnd = total * (w + 1) / threadCount;
            workers[w] = new Thread(new Runnable() {
                @Override
                public void run() {
                    Dungeon.enterSearchContext();
                    try {
                        for (long j = segStart; j < segEnd && running && SeedFinding
                                && foundSeed.get() < 0; ++j) {
                            long currentSeed = (start + j) % total;
                            workerSeeds.set(workerId, currentSeed);
                            workerProgressMs[workerId] = System.currentTimeMillis();

                            // 10 连复查
                            boolean confirmed = true;
                            for (int r = 0; r < 10; r++) {
                                if (!testSeed(currentSeed)) {
                                    confirmed = false;
                                    break;
                                }
                            }
                            if (confirmed) {
                                // CAS 保证只有第一个命中的 worker 胜出
                                if (foundSeed.compareAndSet(-1, currentSeed)) {
                                    SeedFinding = false; // 广播停止
                                    running = false;
                                }
                                break;
                            }
                            if (Thread.currentThread().isInterrupted()) break;
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        workerErrors[workerId] = "异常 " + t + stackBrief(t.getStackTrace(), 6);
                    } finally {
                        Dungeon.exitSearchContext();
                    }
                }
            });
            workers[w].start();
        }

        // 诊断看门狗：worker 超过 15 秒无进展时抓取其堆栈，供场景状态文本显示
        Thread watchdog = new Thread(new Runnable() {
            @Override
            public void run() {
                while (SeedFinding && running) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        return;
                    }
                    long tNow = System.currentTimeMillis();
                    for (int i = 0; i < workers.length; i++) {
                        Thread t = workers[i];
                        if (!t.isAlive()) continue;
                        if (tNow - workerProgressMs[i] > 15000) {
                            workerErrors[i] = "停滞超 15 秒，当前堆栈"
                                    + stackBrief(t.getStackTrace(), 10);
                        } else {
                            workerErrors[i] = null;
                        }
                    }
                }
            }
        });
        watchdog.setDaemon(true);
        watchdog.start();

        // 等待所有 worker 结束（命中者或全部扫完/被停）
        for (Thread th : workers) {
            try {
                th.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        SeedFinding = false;
        long hit = foundSeed.get();
        return hit >= 0 ? logSeedItems(hit) : "NONE";
    }

    // 多线程查种的命中种子（CAS，-1 表示未命中）
    private static final AtomicLong foundSeed = new AtomicLong(-1);

    private final int need;
    protected boolean testSeed(long seed) {
        Dungeon.cur().hero = null;
        //worker 的 initHero 读取静态 selectedClass：必须与场景所选职业一致，
        //否则天才/盗贼天赋、初始容器（LimitedDrops）都会按错误的英雄生成
        GamesInProgress.selectedClass = heroClass;
        Dungeons.cur().init(DungeonSeed.convertToCode(seed));
        if (trinket != null) {
            boolean getTrinket = false;
            for (int i = 0; i < TrinketCatalyst.WndTrinket.NUM_TRINKETS; i++)
                if (trinket.isInstance(Generator.random(Generator.Category.TRINKET)))
                    getTrinket = true;
            if (!getTrinket)
                return false;
        }
        boolean[] itemsFound = new boolean[wantedArr.length];
        int foundCount = 0;
        int n = need;
        boolean ghostSeen = false, impSeen = false, wandmakerSeen = false;

        int depth = 1;
        int levelSub = 0;
        while (depth <= floor) {
            Level l = Dungeons.cur().newLevel(depth, levelSub);
            if (depth == 25) {
                if (levelSub == 0)
                    levelSub++;
                else {
                    levelSub = 0;
                    depth++;
                }
            }
            else
                depth++;
            Class<? extends Level> cl = l.getClass();
            if (cl == SewerBossLevel.class && trinket != null)
                Reflection.newInstance(trinket).upgrade(3).collect();
            else if (cl == CityBossLevel.class)
                ((CityBossLevel) l).spawnShop();

            // 地面物品：遇物即匹配，不建中间表、不 identify
            // level()/enchantment/glyph 均为生成时定型的字段/方法，无需 identify 即可读取
            // values() 直接遍历 IntMap 的 Values 迭代器，省去 valueList() 的数组拷贝+List包装
            for (Heap h : l.heaps.values())
                for (Item item : h.items)
                    if (tryMatch(item, itemsFound) && ++foundCount == n)
                        return true;

            // 怪物掉落：直接取物，不包装 Heap
            for (Mob m : l.mobs) {
                if (m.getClass() == ArmoredStatue.class) {
                    if (tryMatch(((ArmoredStatue) m).armor(), itemsFound) && ++foundCount == n)
                        return true;
                    if (tryMatch(((ArmoredStatue) m).weapon(), itemsFound) && ++foundCount == n)
                        return true;
                }
                else if (m.getClass() == Statue.class) {
                    if (tryMatch(((Statue) m).weapon(), itemsFound) && ++foundCount == n)
                        return true;
                }
                else if (m instanceof Mimic) {
                    for (Item item : ((Mimic) m).items)
                        if (tryMatch(item, itemsFound) && ++foundCount == n)
                            return true;
                }
            }
            if (!ghostSeen && Ghost.Quest.cur().spawned) {
                ghostSeen = true;
                if ((tryMatch(Ghost.Quest.cur().armor, itemsFound)
                        || tryMatch(Ghost.Quest.cur().weapon, itemsFound)) && ++foundCount == n)
                    return true;
            }
            if (!wandmakerSeen && Wandmaker.Quest.cur().wand1 != null) {
                wandmakerSeen = true;
                Item w1 = Wandmaker.Quest.cur().wand1;
                Item w2 = Wandmaker.Quest.cur().wand2;
                if (wand != null && !wand.matches(w1) && !wand.matches(w2))
                    return false;
                if ((tryMatch(w1, itemsFound) || tryMatch(w2, itemsFound)) && ++foundCount == n)
                    return true;
            }
            if (!impSeen && Imp.Quest.cur().reward != null) {
                impSeen = true;
                if (ring != null && !ring.matches(Imp.Quest.cur().reward))
                    return false;
                if (tryMatch(Imp.Quest.cur().reward, itemsFound) && ++foundCount == n)
                    return true;
            }

        }
        return false;
    }
    private boolean tryMatch(Item item, boolean[] itemsFound) {
        //用于实际查找，超类没法用getClass查找，所以在建表的时候改为添加具体子类
        return tryMatch(item, item, itemsFound);
    }
    public boolean tryMatch(Item wanted, Item testing, boolean[] itemsFound) {
        //用于总览页以超类查找。
        int[] candidates = matchIndex.get(wanted.getClass());
        //兼容以实际查找中的具体子类查找
        if (candidates == null) return false;
        for (int idx : candidates) {
            //只查找这个类所能在的位置
            if (!itemsFound[idx] && wantedArr[idx].matches(testing)) {
                itemsFound[idx] = true;
                return true;
            }
        }
        return false;
    }
    private ArrayList<Heap> getMobDrops(Level l) {
        ArrayList<Heap> heaps = new ArrayList<>();
        for (Mob m : l.mobs) {
            if (m instanceof Statue && !(m instanceof ArmoredStatue)) {
                Heap h = new Heap();
                h.items = new LinkedList<>();
                h.items.add(((Statue) m).weapon().identify());
                h.type = Type.HEAP;
                heaps.add(h);
            } else if (m instanceof ArmoredStatue) {
                Heap h = new Heap();
                h.items = new LinkedList<>();
                h.items.add(((ArmoredStatue) m).armor().identify());
                h.items.add(((ArmoredStatue) m).weapon().identify());
                h.type = Type.HEAP;
                heaps.add(h);
            } else if (m instanceof Mimic) {
                Heap h = new Heap();
                h.items = new LinkedList<>();
                for (Item item : ((Mimic) m).items) {
                    h.items.add(item.identify());
                }
                if (m instanceof GoldenMimic) {
                    h.type = Type.GOLDEN_MIMIC;
                } else if (m instanceof CrystalMimic) {
                    h.type = Type.CRYSTAL_MIMIC;
                } else {
                    h.type = Type.MIMIC;
                }
                heaps.add(h);
            }
        }
        return heaps;
    }

    protected String logSeedItems(long seed) {
        String seedCode = DungeonSeed.convertToCode(seed);
        SeedFindScene.seedCode = seedCode;
        Dungeon.cur().hero = null;
        GamesInProgress.selectedClass = heroClass;
        Dungeons.cur().init(seedCode);

        // 饰品"洗牌前"完整序列：牌堆由本局 seed 固定、与使用时机无关，
        // 连续调用 classes.length 次即为洗一次牌内的完整开出顺序
        for (int i = 0; i < Generator.Category.TRINKET.classes.length; i++)
            lastTrinketSequence.add(Generator.random(Generator.Category.TRINKET));

        HashSet<Class<? extends Item>> blacklist = new HashSet<>(Arrays.asList(Dewdrop.class, IronKey.class, GoldenKey.class, CrystalKey.class, EnergyCrystal.class, CorpseDust.class, Embers.class, CeremonialCandle.class, Pickaxe.class));

        // Phase 1: 遍历所有楼层，收集物品（不 identify），任务奖励在出现层一次性收取并 complete
        ArrayList<FloorData> floorDataList = lastFloors;
        int depth = 1;
        int levelSub = 0;
        SeedFinding = true;
        while (depth <= floor) {
            Level l = Dungeons.cur().newLevel(depth, levelSub);
            // 楼层显示名：子层记作 "25/1"（分组也依赖这里的 depth，须在推进前捕获）
            int curDepth = depth;
            int curSub = levelSub;
            if (depth == 25) {
                if (levelSub == 0)
                    levelSub++;
                else {
                    levelSub = 0;
                    depth++;
                }
            }
            else
                depth++;

            Class<? extends Level> cl = l.getClass();
            if (cl == SewerBossLevel.class && trinket != null)
                for (int i = 0; i < 4; i++) {
                    if (trinket.isInstance(lastTrinketSequence.get(i)))
                        Reflection.newInstance(trinket).upgrade(3).collect();
                }
            else if (cl == CityBossLevel.class)
                ((CityBossLevel) l).spawnShop();

            FloorData fd = new FloorData(curDepth,
                    curSub > 0 ? curDepth + "/" + curSub : String.valueOf(curDepth));

            // 地面物品
            for (Heap h : l.heaps.valueList())
                for (Item item : h.items)
                    fd.heapItems.add(new HeapItem(item, h));

            // 怪物掉落
            for (Heap h : getMobDrops(l))
                for (Item item : h.items)
                    fd.heapItems.add(new HeapItem(item, h));

            // 鬼魂任务奖励
            if (Ghost.Quest.cur().armor != null) {
                ArrayList<Item> rewards = new ArrayList<>();
                rewards.add(Ghost.Quest.cur().armor);
                rewards.add(Ghost.Quest.cur().weapon);
                Ghost.Quest.cur().complete();
                fd.ghostRewards = rewards;
            }
            // 工匠任务奖励（type 在 complete 前捕获）
            if (Wandmaker.Quest.cur().wand1 != null) {
                ArrayList<Item> rewards = new ArrayList<>();
                rewards.add(Wandmaker.Quest.cur().wand1);
                rewards.add(Wandmaker.Quest.cur().wand2);
                fd.wandmakerType = Wandmaker.Quest.cur().type();
                Wandmaker.Quest.cur().complete();
                fd.wandmakerRewards = rewards;
            }
            // 小恶魔任务奖励
            if (Imp.Quest.cur().reward != null) {
                ArrayList<Item> rewards = new ArrayList<>();
                rewards.add(Imp.Quest.cur().reward);
                Imp.Quest.cur().complete();
                fd.impRewards = rewards;
            }

            floorDataList.add(fd);
        }
        SeedFinding = false;

        // Phase 2: 统一 identify 所有收集到的物品
        for (FloorData fd : floorDataList) {
            for (HeapItem hi : fd.heapItems)
                hi.item.identify();

            if (fd.ghostRewards != null)
                for (Item i : fd.ghostRewards) i.identify();

            if (fd.wandmakerRewards != null)
                for (Item i : fd.wandmakerRewards) i.identify();

            if (fd.impRewards != null)
                for (Item i : fd.impRewards) i.identify();
        }

        // Phase 3: 生成文本（逐层文本同时存入 fd.text，供场景 Tab 结果窗使用）
        StringBuilder result = new StringBuilder(Messages.get(SeedFinder.class, "seed") + seedCode + " (" + seed + ") " + Messages.get(SeedFinder.class, "items") + ":\n\n");
        for (FloorData fd : floorDataList) {
            StringBuilder builder = new StringBuilder();
            builder.append("\n_----- ").append(fd.title).append(" ").append(Messages.get(SeedFinder.class, "floor")).append(" -----_\n\n");
            ArrayList<HeapItem> scrolls = new ArrayList<>();
            ArrayList<HeapItem> potions = new ArrayList<>();
            ArrayList<HeapItem> equipment = new ArrayList<>();
            ArrayList<HeapItem> rings = new ArrayList<>();
            ArrayList<HeapItem> artifacts = new ArrayList<>();
            ArrayList<HeapItem> wands = new ArrayList<>();
            ArrayList<HeapItem> others = new ArrayList<>();
            ArrayList<HeapItem> forSales = new ArrayList<>();

            // 任务奖励（在地面物品之前展示）
            if (fd.ghostRewards != null) {
                this.addTextQuest("[ " + Messages.get(SeedFinder.class, "sad_ghost_reward") + " ]", fd.ghostRewards, builder);
            }
            if (fd.wandmakerRewards != null) {
                builder.append("[ ").append(Messages.get(SeedFinder.class, "wandmaker_need")).append(" ]:\n ");
                switch (fd.wandmakerType) {
                    case 1:
                    default:
                        builder.append(Messages.get(SeedFinder.class, "corpseDust")).append("\n\n");
                        break;
                    case 2:
                        builder.append(Messages.get(SeedFinder.class, "embers")).append("\n\n");
                        break;
                    case 3:
                        builder.append(Messages.get(SeedFinder.class, "rotBerry")).append("\n\n");
                }
                addTextQuest("[ " + Messages.get(SeedFinder.class, "wandmaker_reward") + " ]", fd.wandmakerRewards, builder);
            }
            if (fd.impRewards != null) {
                addTextQuest("[ " + Messages.get(SeedFinder.class, "imp_reward") + " ]", fd.impRewards, builder);
            }

            // 分类地面物品
            int gold = 0;
            for (HeapItem hi : fd.heapItems) {
                Item item = hi.item;
                Heap h = hi.heap;
                if (h.type == Type.FOR_SALE) {
                    forSales.add(hi);
                } else if (!blacklist.contains(item.getClass())) {
                    if (item instanceof Scroll)
                        scrolls.add(hi);
                    else if (item instanceof Potion)
                        potions.add(hi);
                    else if (!(item instanceof MeleeWeapon) && !(item instanceof Armor)) {
                        if (item instanceof Ring)
                            rings.add(hi);
                        else if (item instanceof Artifact)
                            artifacts.add(hi);
                        else if (item instanceof Wand)
                            wands.add(hi);
                        else if (item instanceof Gold)
                            gold += item.quantity();
                        else
                            others.add(hi);
                    } else
                        equipment.add(hi);
                }
            }
            if (gold != 0) {
                Gold goldA = new Gold(gold);
                Heap heapA = new Heap();
                heapA.items = new LinkedList<>();
                heapA.items.add(goldA);
                others.add(new HeapItem(goldA, heapA));
            }
            addTextItems("[ " + Messages.get(SeedFinder.class, "scrolls") + " ]", scrolls, builder);
            addTextItems("[ " + Messages.get(SeedFinder.class, "potions") + " ]", potions, builder);
            addTextItems("[ " + Messages.get(SeedFinder.class, "equipment") + " ]", equipment, builder);
            addTextItems("[ " + Messages.get(SeedFinder.class, "rings") + " ]", rings, builder);
            addTextItems("[ " + Messages.get(SeedFinder.class, "artifacts") + " ]", artifacts, builder);
            addTextItems("[ " + Messages.get(SeedFinder.class, "wands") + " ]", wands, builder);
            addTextItems("[ " + Messages.get(SeedFinder.class, "for_sales") + " ]", forSales, builder);
            addTextItems("[ " + Messages.get(SeedFinder.class, "others") + " ]", others, builder);
            result.append(builder);
            fd.text = builder.toString();
        }
        return result.toString();
    }

    private void addTextItems(String caption, ArrayList<HeapItem> items, StringBuilder builder) {
        if (!items.isEmpty()) {
            builder.append(caption).append(":\n");
            for (HeapItem item : items) {
                Item i = item.item;
                Heap h = item.heap;
                if (!(i instanceof Armor && ((Armor) i).hasCurseGlyph()
                        || i instanceof Weapon && ((Weapon) i).hasCurseEnchant()) && i.cursed)
                    builder.append("- ").append(Messages.get(SeedFinder.class, "cursed")).append(i);
                else
                    builder.append("- ").append(i);
                if (h.type != Type.HEAP) {
                    String heap = h.toString();
                    if (h.type == Type.FOR_SALE)
                        heap = Shopkeeper.sellPrice(h.peek()) + "钻石";
                    builder.append("(").append(heap).append(")");
                } else if (h.room != Heap.Room.NONE) {
                    String room = Heap.RoomName(Type.NONE, h.room);
                    builder.append("(").append(room).append(")");
                }
                builder.append("\n");
            }
            builder.append("\n");
        }
    }

    private void addTextQuest(String caption, ArrayList<Item> items, StringBuilder builder) {
        if (!items.isEmpty()) {
            builder.append(caption).append(":\n");
            for (Item i : items)
                if (i.cursed)
                    builder.append("- ").append(Messages.get(SeedFinder.class, "cursed")).append(i).append("\n");
                else
                    builder.append("- ").append(i).append("\n");
            builder.append("\n");
        }
    }

    // 单层数据载体：Phase 1 收集、Phase 2 identify、Phase 3 展示
    public static final class FloorData {
        final int depth;
        final String title;   // 楼层显示名：普通层 "25"、子层 "25/1"
        String text;          // Phase 3 生成的本层完整文本（含楼层头）
        final ArrayList<HeapItem> heapItems = new ArrayList<>();
        ArrayList<Item> ghostRewards = null;
        boolean ghostSelect;
        ArrayList<Item> wandmakerRewards = null;
        boolean wandmakerSelect;
        int wandmakerType = 0;
        ArrayList<Item> impRewards = null;
        boolean impSelect;

        FloorData(int depth, String title) {
            this.depth = depth;
            this.title = title;
        }
    }

    public static class HeapItem {
        public Item item;
        public Heap heap;

        public HeapItem(Item item, Heap heap) {
            this.item = item;
            this.heap = heap;
        }
    }
}
