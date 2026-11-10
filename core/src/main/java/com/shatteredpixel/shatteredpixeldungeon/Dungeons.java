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
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.utils.Random;

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
    public static Dungeons MAIN() {
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
    public int branch;
    public long seed;
    public final int[] limitedDrops = new int[LimitedDrops.values().length];

    // === 新增：生成链专用上下文标记 ===
    // 当本实例作为当前线程的生成上下文时，为 true
    // 用于区分"游戏主上下文"与"查种子线程上下文"
    public boolean isSearch;

    public final SpecialRoom.State specialRoom = new SpecialRoom.State();
    public final SecretRoom.State secretRoom = new SecretRoom.State();
    public final Generator.State generator = new Generator.State();
    public final Ghost.Quest ghostQuest = new Ghost.Quest();
    public final Wandmaker.Quest wandmakerQuest = new Wandmaker.Quest();
    public final Blacksmith.Quest blacksmithQuest = new Blacksmith.Quest();
    public final Imp.Quest impQuest = new Imp.Quest();

    public Dungeons(boolean isSearch) {
        this.isSearch = isSearch;
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
        branch = 0;

        LimitedDrops.reset();

        Ghost.Quest.reset();
        Wandmaker.Quest.reset();
        Blacksmith.Quest.reset();
        Imp.Quest.reset();

        hero = new Hero();
        hero.live();

        Random.pushGenerator( seed );
        GamesInProgress.selectedClass.initHero( hero );
        Random.resetGenerators();
        Dungeon.resetGenerator();

        //NPC 任务奖励预生成：各任务内部以独立子种子 push/pop，不消耗主随机流；
        //查种器因此在 init 阶段即可读取奖励内容，对仅含 NPC 奖励道具的查询提前剪枝
        Ghost.Quest.cur().genReward();
        Wandmaker.Quest.cur().genReward();
        Blacksmith.Quest.cur().generateRewards();
        Imp.Quest.cur().genReward();
    }

    private Level createLevel(Level level, int levelDepth, int levelBranch){
        this.level = level;
        Actor.clear();

        depth = levelDepth;
        branch = levelBranch;
        level.create(levelDepth, levelBranch);
        return level;
    }

    public Level newLevel(int depth, int branch) {
        return createLevel(Dungeon.getNewLevel(depth, branch), depth, branch);
    }

    public int curDepth(){
        return depth;
    }

    public boolean bossLevel() {
        return bossLevel( depth );
    }

    public boolean bossLevel( int depth ) {
        return depth == 5 || depth == 10 || depth == 15 || depth == 20 || depth == 25 || depth == 30;
    }

}
