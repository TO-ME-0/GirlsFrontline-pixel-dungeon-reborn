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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.SksStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
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
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    //[0]=用户选的第一把（工匠槽），[1]=第二把（宝库槽）；读取方向由每线程的 swapped 决定
    private final WantedTarget[] wands = new WantedTarget[2];
    WantedTarget imp;
    //每线程的读取方向：false=[0] 是 wand1，true=对调。查种线程各自独立，无共享写
    private final ThreadLocal<Boolean> swapped = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };
    //命中种子解析出的方向：结果页据此决定是否交换静态 wand1/wand2
    public boolean seedWandSwapped;

    public WantedTarget wand1() {
        return wands[swapped.get() ? 1 : 0];
    }

    public WantedTarget wand2() {
        return wands[swapped.get() ? 0 : 1];
    }

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
                if (item == SeedFindScene.wand1) {
                    wands[0] = w;
                    putReward(w, item, index);
                }
                else if (item == SeedFindScene.wand2) {
                    wands[1] = w;
                    putReward(w, item, index);
                }
                else if (item == SeedFindScene.impReward) {
                    imp = w;
                    putReward(w, item, index);
                }
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
    //登记一个奖励槽：具体物品的可选类就是自身；Holder 用 fillMap 展开成它代表的整类子类。
    //这里会多跑一次 fillMap（构造函数里已为 matchIndex 跑过一次），只发生在构造期，
    //结果直接挂到目标对象上，热路径只剩下一次 Set.contains
    private void putReward(WantedTarget w, Item template, int index) {
        w.rewardIndex = index;
        if (template instanceof Holder) {
            HashMap<Class<? extends Item>, ArrayList<Integer>> expanded = new HashMap<>();
            ((Holder) template).fillMap(expanded, index);
            w.rewardClasses = expanded.keySet();
        } else {
            w.rewardClasses = Collections.singleton(template.getClass());
        }
    }
    // 多线程分段查种：随机起始种子后，把 [start, TOTAL_SEEDS) 区间均分为 threadCount 段，
    // 每个 worker 独立进入查种上下文扫自己那段，首个命中即广播停止所有线程。
    public final String findSeed(final int threadCount) {
        //每次查找重新发布方向，避免上一次的交换结果残留到结果页
        seedWandSwapped = false;
        if (threadCount <= 1) {
            String result = "NONE";
            SeedFinding = true;
            running = true;
            workerCount = 1;
            workerSeeds = newSeedArray(1);

            //种子域 [0, TOTAL_SEEDS) 环形扫描：随机起点 + 取模，种子永不越界
            final long start = Random.Long(DungeonSeed.TOTAL_SEEDS);

            //单线程同样必须进入查种上下文：Dungeons.cur().isSearch 决定遗物注入、
            //20 层商店、任务 complete 的副作用，缺失会让单线程与多线程结果不一致
            Dungeon.enterSearchContext();
            try {
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
                        //把本线程解析出的法杖方向交给结果页
                        seedWandSwapped = swapped.get();
                        result = logSeedItems(currentSeed);
                        break;
                    }

                    if (Thread.currentThread().isInterrupted()) {
                        running = false;
                        break;
                    }
                }
            } finally {
                swapped.remove();
                Dungeon.exitSearchContext();
                SeedFinding = false;
            }
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
                                    seedWandSwapped = swapped.get();
                                }
                                break;
                            }
                            if (Thread.currentThread().isInterrupted()) break;
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        workerErrors[workerId] = "异常 " + t + stackBrief(t.getStackTrace(), 6);
                    } finally {
                        swapped.remove();
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
        if (hit < 0) return "NONE";
        //命中种子的整局重放同样必须在查种上下文内完成
        Dungeon.enterSearchContext();
        try {
            return logSeedItems(hit);
        } finally {
            Dungeon.exitSearchContext();
        }
    }

    // 多线程查种的命中种子（CAS，-1 表示未命中）
    private static final AtomicLong foundSeed = new AtomicLong(-1);

    private final int need;
    public boolean wandmakerTryMatch(WantedTarget target) {
        //集合与任务对象各取一次：循环里只做 Set.contains
        Set<Class<? extends Item>> cls = target.rewardClasses;
        Wandmaker.Quest q = Wandmaker.Quest.cur();
        //先判类别再判等级：matches() 会按附魔/铭文强转，跨类别直接短路更安全也更快
        return (cls.contains(q.wand1.getClass()) && target.matches(q.wand1))
                || (cls.contains(q.wand2.getClass()) && target.matches(q.wand2));
    }
    public boolean impTryMatch(WantedTarget target) {
        //宝库无论如何只能带出一件，final 以外房间的装备与 6 件宝库奖励同属一个槽位候选：
        //完整扫描时这些装备会作为金库层的堆叠物被 tryMatch 命中，提前剪枝必须同步放宽，否则会出假阴性
        //预生成量按各档最高请求次数取（必然多于实际落地量），剪枝允许这部分假阳性
        Set<Class<? extends Item>> cls = target.rewardClasses;
        Imp.Quest q = Imp.Quest.cur();
        for (Item item : q.rewardOptions)
            if (cls.contains(item.getClass()) && target.matches(item)) {
                GLog.p(String.valueOf(Dungeon.cur().seed));
                return true;
            }
        for (ArrayList<Item> tier : q.vaultEquipOptions)
            for (Item item : tier)
                if (cls.contains(item.getClass()) && target.matches(item)) {
                    GLog.p(String.valueOf(Dungeon.cur().seed));
                    return true;
                }
        return false;
    }

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
        //每颗种子都从用户顺序（未交换）开始推演：剪枝直接用 wands[0]/wands[1]（等价方向=false），
        //方向确定后只写一次 ThreadLocal，省掉“先置 false、再按需置 true”的额外写
        WantedTarget wand = null, reward = null;
        if (wands[0] != null) {
            boolean sw;
            //固定只能查自身
            if (imp != null) {
                if (!wandmakerTryMatch(wands[0]))
                    return false;
                sw = false;
            }
            //可对调
            else if (wands[1] != null) {
                boolean ww1 = wandmakerTryMatch(wands[0]), ww2 = wandmakerTryMatch(wands[1]),
                        wi1 = impTryMatch(wands[0]),       wi2 = impTryMatch(wands[1]);
                //至少存在一条对角线以获取奖励
                if (!(ww1 && wi2 || ww2 && wi1))
                    return false;
                //主对角线为false，则交换为副对角线
                sw = !wi2;
            }
            //可尝试占用宝库任务奖励
            else {
                if (!wandmakerTryMatch(wands[0])
                        && floor > 16 && !impTryMatch(wands[0]))
                    return false;
                //单法杖后置给宝库槽（wands[1] 为空，交换方向即等价于 wand1=null、wand2=此杖）
                sw = true;
            }
            swapped.set(sw);
            //方向已定，缓存到局部变量：热循环内不再反复读 ThreadLocal
            wand = wand1();
            reward = wand2();
        }
        if (imp != null && !impTryMatch(imp))
            return false;
        if (reward == null)
            reward = imp;
        boolean[] itemsFound = new boolean[wantedArr.length];
        int foundCount = 0;
        int n = need;
        boolean ghostSeen = false, impSeen = false, wandmakerSeen = false;

        int depth = 1;
        int branch = 0;
        while (depth <= floor) {
            Level l = Dungeons.cur().newLevel(depth, branch);
            if (branch == 1) {
                branch = 0;
                depth++;
                if (!impSeen && Imp.Quest.cur().spawned) {
                    impSeen = true;
                    if (reward != null) {
                        for (Heap h : l.heaps.values())
                            for (Item item : h.items)
                                if (tryMatch(reward, item, itemsFound)) {
                                    if (++foundCount == n)
                                        return true;
                                    else
                                        continue;
                                }
                        return false;
                    }
                    ArrayList<Item> rewards = new ArrayList<>();
                    for (Heap h : l.heaps.values())
                        for (Item item : h.items)
                            rewards.add(item);
                    //避免优先匹配低等级物品。
                    Collections.sort(rewards, itemComparator);
                    for (Item i : rewards)
                        if (tryMatch(i, itemsFound)) {
                            if (++foundCount == n)
                                return true;
                            else
                                continue;
                        }
                    //宝库匹配逻辑，由于宝库物品都是heap，但只能带走一件，所以要单独写
                    continue;
                }
            }
            else if (Imp.Quest.cur().spawned && !impSeen) {
                branch = 1;
                impSeen = true;
            }
            else
                depth++;

            if (depth % 5 == 0) {
                if (depth == 5 && trinket != null)
                    Reflection.newInstance(trinket).upgrade(3).collect();
                if (depth == 25)
                    branch = 1;
                else if (depth != 20)
                    depth++;
            }
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
                else if (m.getClass() == SksStatue.class) {
                    if (tryMatch(((SksStatue) m).weapon(), itemsFound) && ++foundCount == n)
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
            if (!wandmakerSeen && Wandmaker.Quest.cur().spawned) {
                wandmakerSeen = true;
                Item wa = Wandmaker.Quest.cur().wand1;
                Item wb = Wandmaker.Quest.cur().wand2;
                if (wand != null) {
                    if ((tryMatch(wand, wa, itemsFound) || tryMatch(wand, wb, itemsFound)) && ++foundCount == n)
                        return true;
                }
                else if ((tryMatch(wa, itemsFound) || tryMatch(wb, itemsFound)) && ++foundCount == n)
                        return true;
            }
        }
        return false;
    }
    private static final Comparator<Item> itemComparator = new Comparator<Item>() {
        @Override
        public int compare( Item lhs, Item rhs ) {
            return rhs.trueLevel() - lhs.trueLevel();
        }
    };
    private boolean tryMatch(Item item, boolean[] itemsFound) {
        //实际查找的最热路径：wanted == testing，直接按自身类查表，免去 Holder 判断
        int[] candidates = matchIndex.get(item.getClass());
        if (candidates == null) return false;
        for (int idx : candidates) {
            //只查找这个类所能在的位置
            if (!itemsFound[idx] && wantedArr[idx].matches(item)) {
                itemsFound[idx] = true;
                return true;
            }
        }
        return false;
    }
    //总览页专用：只针对 wantedIndex 这一个目标判断。
    //类门禁交给展开表——用 testing 的真实类查 matchIndex（Holder 已被 fillMap 展开成具体子类），
    //候选里出现 wantedIndex 才说明该目标接受这个类；再叠加 matches() 的等级/附魔过滤。
    //这样不会像旧的“按 wanted 类查表 + matches 不校验类”那样被异类高等级物品误命中
    public boolean tryMatchAt(int wantedIndex, Item testing, boolean[] itemsFound) {
        if (itemsFound[wantedIndex]) return false;
        int[] candidates = matchIndex.get(testing.getClass());
        if (candidates == null) return false;
        for (int c : candidates) {
            if (c == wantedIndex) {
                if (!wantedArr[wantedIndex].matches(testing)) return false;
                itemsFound[wantedIndex] = true;
                return true;
            }
        }
        return false;
    }
    public boolean tryMatch(WantedTarget target, Item testing, boolean[] itemsFound) {
        int idx = target.rewardIndex;
        //任务奖励是混合类型（神器/戒指/武器/护甲/法杖），必须同类别才可比：
        //先查预生成的类别集合（Holder 已展开），既防 matches() 强转 CCE，也防跨类按等级误判
        if (!itemsFound[idx] && target.rewardClasses.contains(testing.getClass())
                && target.matches(testing)) {
            itemsFound[idx] = true;
            return true;
        }
        return false;
    }
    private ArrayList<Heap> getMobDrops(Level l) {
        ArrayList<Heap> heaps = new ArrayList<>();
        for (Mob m : l.mobs) {
            if (m.getClass() == SksStatue.class) {
                Heap h = new Heap();
                h.items = new LinkedList<>();
                h.items.add(((SksStatue) m).weapon().identify());
                h.type = Type.HEAP;
                heaps.add(h);
            } else if (m.getClass() == ArmoredStatue.class) {
                Heap h = new Heap();
                h.items = new LinkedList<>();
                h.items.add(((ArmoredStatue) m).armor().identify());
                h.items.add(((ArmoredStatue) m).weapon().identify());
                h.type = Type.HEAP;
                heaps.add(h);
            } else if (m instanceof Mimic) {
                Heap h = new Heap();
                h.items = new LinkedList<>();
                for (Item item : ((Mimic) m).items)
                    h.items.add(item.identify());
                if (m.getClass() == GoldenMimic.class) h.type = Type.GOLDEN_MIMIC;
                else if (m.getClass() == CrystalMimic.class) h.type = Type.CRYSTAL_MIMIC;
                else h.type = Type.MIMIC;
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
        int branch = 0;
        boolean ghostSeen = false, wandmakerSeen = false, smithSeen = false, impSeen = false;
        SeedFinding = true;
        while (depth <= floor) {
            Level l = Dungeons.cur().newLevel(depth, branch);
            FloorData fd = new FloorData(depth,
                    branch > 0 ? depth + "/" + branch : String.valueOf(depth));
            if (branch == 1) {
                branch = 0;
                depth++;
            }
            else if (Blacksmith.Quest.cur().spawned && !Blacksmith.Quest.cur().completed) {
                branch = 1;
                Blacksmith.Quest.cur().completed = true;
            }
            else if (Imp.Quest.cur().spawned && !Imp.Quest.cur().completed) {
                branch = 1;
                Imp.Quest.cur().completed = true;
            }
            else if (depth == 25)
                branch = 1;
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

            // 地面物品
            for (Heap h : l.heaps.valueList())
                for (Item item : h.items)
                    fd.heapItems.add(new HeapItem(item, h));

            // 怪物掉落
            for (Heap h : getMobDrops(l))
                for (Item item : h.items)
                    fd.heapItems.add(new HeapItem(item, h));

            // 鬼魂任务奖励（奖励自 init 已预生成，以 spawned 判定出现层；complete 后置空保证只收一次）
            if (Ghost.Quest.cur().spawned && !ghostSeen) {
                ghostSeen = true;
                ArrayList<Item> rewards = new ArrayList<>();
                rewards.add(Ghost.Quest.cur().armor);
                rewards.add(Ghost.Quest.cur().weapon);
                Ghost.Quest.cur().complete();
                fd.ghostRewards = rewards;
            }
            // 工匠任务奖励（type 在 complete 前捕获）
            if (Wandmaker.Quest.cur().spawned && !wandmakerSeen) {
                wandmakerSeen = true;
                ArrayList<Item> rewards = new ArrayList<>();
                rewards.add(Wandmaker.Quest.cur().wand1);
                rewards.add(Wandmaker.Quest.cur().wand2);
                fd.wandmakerType = Wandmaker.Quest.cur().type();
                Wandmaker.Quest.cur().complete();
                fd.wandmakerRewards = rewards;
            }
            // 铁匠 2000 积分开炉奖励（仅 log 展示，不参与 testSeed 匹配；不置空故用 smithSeen 防重）
            if (Blacksmith.Quest.cur().spawned && !smithSeen) {
                smithSeen = true;
                fd.smithRewards = Blacksmith.Quest.cur().smithRewards;
            }
            // 小恶魔任务奖励（新版：rewardOptions、vaultEquipOptions 预生成列表）
            if (Imp.Quest.cur().spawned && !impSeen) {
                impSeen = true;
                fd.impRewards = new ArrayList<>(Imp.Quest.cur().rewardOptions);
                for (ArrayList<Item> list : Imp.Quest.cur().vaultEquipOptions)
                    fd.impRewards.addAll(list);
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

            if (fd.smithRewards != null)
                for (Item i : fd.smithRewards) i.identify();
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
            if (fd.smithRewards != null) {
                addTextQuest("[ " + Messages.get(SeedFinder.class, "blacksmith_smith_reward") + " ]", fd.smithRewards, builder);
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

    public boolean ghostSelect;
    public boolean wandmakerSelect;
    public boolean impSelect;
    // 单层数据载体：Phase 1 收集、Phase 2 identify、Phase 3 展示
    public static final class FloorData {
        final int depth;
        final String title;   // 楼层显示名：普通层 "25"、子层 "25/1"
        String text;          // Phase 3 生成的本层完整文本（含楼层头）
        final ArrayList<HeapItem> heapItems = new ArrayList<>();
        ArrayList<Item> ghostRewards = null;
        ArrayList<Item> wandmakerRewards = null;
        int wandmakerType = 0;
        ArrayList<Item> impRewards = null;
        ArrayList<Item> smithRewards = null;

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
