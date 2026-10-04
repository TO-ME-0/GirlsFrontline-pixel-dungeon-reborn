package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon.LimitedDrops;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.levels.CavesBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CavesLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.DeadEndLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CoffeeRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.Workshop;
import com.shatteredpixel.shatteredpixeldungeon.levels.DeepCaveBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.DeepCaveLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.LastLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.LastShopLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.MiningLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.PrisonLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.RabbitBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Room404;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.ZeroLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.ZeroLevelSub;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.noosa.Game;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Dungeons {

    //游戏主上下文
    private static final Dungeons MAIN = new Dungeons(false);

    //查种 worker 线程的私有上下文
    private static final ThreadLocal<Dungeons> SEARCH_CTX = new ThreadLocal<>();

    public static Dungeons cur() {
        Dungeons s = SEARCH_CTX.get();
        return (s != null) ? s : MAIN;
    }

    //主游戏上下文。仅用于存档读写等永远发生在主线程的路径，
    //避免误把查种 worker 的 ThreadLocal 状态序列化进存档。
    public static Dungeons main() {
        return MAIN;
    }

    public static void enterSearchContext() {
        SEARCH_CTX.set(new Dungeons(true));
    }

    public static void exitSearchContext() {
        SEARCH_CTX.remove();
    }

    public int mobsToChampion;
    public Hero hero;
    public Level level;
    public int depth;
    public int levelId;
    public int CreateId;
    public long seed;
    public final int[] limitedDrops = new int[LimitedDrops.values().length];

    //Generator 生成池的每局状态
    public boolean genUsingFirstDeck;
    public HashMap<Generator.Category, Float> genCategoryProbs;
    public float[][] genCatProbs;

    //Generator 牌堆系统的每局状态：每类别的固定种子与已掉落次数（seed 为 null 表示该类未启用牌堆）
    public Long[] genCatSeeds;
    public int[] genCatDropped;

    //双牌堆类别（如药水/卷轴）本局正使用第 2 套牌堆概率
    public boolean[] genCatUsing2ndProbs;

    //四个任务 NPC 的每局状态
    public final Ghost.Quest ghostQuest = new Ghost.Quest();
    public final Wandmaker.Quest wandmakerQuest = new Wandmaker.Quest();
    public final Blacksmith.Quest blacksmithQuest = new Blacksmith.Quest();
    public final Imp.Quest impQuest = new Imp.Quest();

    //SpecialRoom / SecretRoom 的每局池
    public ArrayList<Class<? extends Room>> spRunSpecials = new ArrayList<>();
    public ArrayList<Class<? extends Room>> spFloorSpecials = new ArrayList<>();
    public int spPitNeededDepth = -1;

    public ArrayList<Class<? extends SecretRoom>> secRunSecrets = new ArrayList<>();
    public int[] secRegionSecretsThisRun = new int[SecretRoom.BASE_REGION_SECRETS_LEN];
    public float[] secRegionSecretsThisRandom = new float[SecretRoom.BASE_REGION_SECRETS_LEN];

    // === 新增：生成链专用上下文标记 ===
    // 当本实例作为当前线程的生成上下文时，为 true
    // 用于区分"游戏主上下文"与"查种子线程上下文"
    public boolean isSearch = false;
    public Dungeons(boolean isSearch) {
        this.isSearch = isSearch;
        reset();
    }

    public void reset() {
        depth = 0;
        CreateId = 0;
        levelId = 0;

        //从 Category 的静态模板克隆每局独立的生成概率数组
        genCategoryProbs = new LinkedHashMap<>();
        Generator.Category[] cats = Generator.Category.values();
        genCatProbs = new float[cats.length][];
        genCatSeeds = new Long[cats.length];
        genCatDropped = new int[cats.length];
        genCatUsing2ndProbs = new boolean[cats.length];
        for (Generator.Category cat : cats) {
            genCatProbs[cat.ordinal()] = (cat.probs != null) ? cat.probs.clone() : null;
        }
    }

    //简易 init：不含 challenges/GameMode 修饰（IDENTIFY 强制挑战、圣诞节彩蛋）
    //与 Dungeon.init 的静态包装（版本/农历/存档清理）——那些由调用方负责
    public void init(String seedCode) {
        mobsToChampion = -1;

        if (seedCode == null || seedCode.isEmpty()){
            seed = DungeonSeed.randomSeed();
        } else{
            seed = DungeonSeed.convertFromText(seedCode);
        }

        Actor.clear();

        Random.pushGenerator( seed );

        //与 Dungeon.init 严格同序：initLabels/initColors/initGems 会消耗种子生成器的随机流，
        //缺少它们会使 initForRun 洗牌与 genUsingFirstDeck 从错误的流位置取值，导致整局生成偏移
        Scroll.initLabels();
        Potion.initColors();
        Ring.initGems();

        SpecialRoom.initForRun();
        SecretRoom.initForRun();
        Generator.fullReset();

        Random.resetGenerators();

        depth = 0;
        CreateId = 0;

        LimitedDrops.reset();

        Ghost.Quest.cur().reset();
        Wandmaker.Quest.cur().reset();
        Blacksmith.Quest.cur().reset();
        Imp.Quest.cur().reset();

        hero = new Hero();
        hero.live();

        Random.pushGenerator( seed );
        GamesInProgress.selectedClass.initHero( hero );
        Random.resetGenerators();
        Dungeon.resetGenerator();
    }

    // === 复制自 Dungeon.newLevel(Level, int, int) ===
    // 注意：内部仍调用 level.create(levelDepth, id)，而 create 内部会读写 Dungeon 的静态字段
    // 这是本阶段的预期行为：Dungeons 只是容器，真正的生成链引用迁移在后续阶段
    public Level newLevel(Level level, int levelDepth, int id){
        this.level = level;
        Actor.clear();

        depth = levelDepth;
        CreateId = id;
        level.create(levelDepth, id);
        if (!isSearch) {
            Game.Seed = seed;
            Game.Challenges = Dungeon.challenges;
            Game.GameMode = Dungeon.GameMode;
            if (depth > Statistics.deepestFloor) {
                Statistics.deepestFloor = levelDepth;

                if (Statistics.qualifiedForNoKilling) {
                    Statistics.completedWithNoKilling = true;
                } else {
                    Statistics.completedWithNoKilling = false;
                }
            }
            if (id / 1000 > Statistics.deepestSub)
                Statistics.deepestSub = id / 1000;

            Statistics.qualifiedForNoKilling = !bossLevel();
        }
        return level;
    }

    // === 复制自 Dungeon.newLevel(int) ===
    public Level newLevel(int id){
        Level level;
        if (id%1000==0)
            level = newZeroLevel(id);
        else if (id%1000!=id)
            level = newSubLevel(id);
        else {
            switch (id) {
                case 1: case 2: case 3: case 4:
                    level = new SewerLevel();
                    break;
                case 5:
                    level = new SewerBossLevel();
                    break;
                case 6: case 7: case 8: case 9:
                    level = new PrisonLevel();
                    break;
                case 10:
                    level = new PrisonBossLevel();
                    break;
                case 11: case 12: case 13: case 14:
                    level = new CavesLevel();
                    break;
                case 15:
                    level = new CavesBossLevel();
                    break;
                case 16: case 17: case 18: case 19:
                    level = new CityLevel();
                    break;
                case 20:
                    level = new CityBossLevel();
                    break;
                case 21: case 22: case 23: case 24:
                    level = new DeepCaveLevel();
                    break;
                case 25:
                    level = new DeepCaveBossLevel();
                    break;
                case 26: case 27: case 28: case 29:
                    level = new HallsLevel();
                    break;
                case 30:
                    level = new HallsBossLevel();
                    break;
                case 31:
                    level = new LastLevel();
                    break;
                default:
                    level = new DeadEndLevel();
            }
        }

        return newLevel(level, id%1000, id);
    }

    // === 复制自 Dungeon.newLevel(int, int) ===
    public Level newLevel(int depth, int sub) {
        return newLevel(depth + sub*1000);
    }

    private Level newZeroLevel(int id){
        Level level;
        switch (id){
            default:
                level = new DeadEndLevel();break;
            case 0:
                level = new ZeroLevel();break;
            case 1000:
                level = new ZeroLevelSub();
                break;
            case 2000:
                level = new Room404();
                break;
            case 3000:
                level = new CoffeeRoom();
                break;
            case 4000:
                level = new Workshop();
                break;
        }
        return level;
    }

    private Level newSubLevel(int id){
        Level level;
        switch(id){
            default:
                level = new DeadEndLevel();break;
            case 1010:
                level = new RabbitBossLevel();break;
            //铁匠任务矿洞子层：对应洞窟主层 11~14
            case 1011: case 1012: case 1013: case 1014:
                level = new MiningLevel();break;
            case 1025:
                level = new LastShopLevel();break;
        }
        return level;
    }

    public int curDepth(){
        return depth;
    }

    // === 复制自 Dungeon.seedToCreate ===
    public long seedToCreate(){
        return seedForLevel(CreateId);
    }

    // === 复制自 Dungeon.seedCurLevel ===
    public long seedCurLevel(){
        return seedForLevel(levelId);
    }

    public long seedCurLevel(Level level){
        return seedForLevel(level.levelId);
    }

    // === 复制自 Dungeon.seedForLevel ===
    public long seedForLevel(int createId){
        Random.pushGenerator( seed );
        //以存档种子为开始的随机数序列
        for (int i = 0; i < createId; i ++) {
            Random.Long(); //we don't care about these values, just need to go through them
        }
        long result = Random.Long();
        //过掉楼层深度个随机数，以获得即将要使用的随机数

        Random.popGenerator();
        return result;
    }

    // === 复制自 Dungeon.shopOnLevel ===
    public boolean shopOnLevel() {
        return depth == 6 || depth == 11 || depth == 16;
    }

    // === 复制自 Dungeon.bossLevel ===
    public boolean bossLevel() {
        return bossLevel( depth );
    }

    public boolean bossLevel( int depth ) {
        return depth == 5 || depth == 10 || depth == 15 || depth == 20 || depth == 25 || depth == 30;
    }

}
