package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Acidic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Aegis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalGuardian;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalSpire;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalWisp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM200;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM201;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DemonSpawner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Disloli;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ELID;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elphelt;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Excutioner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.FungalCore;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.FungalSentry;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.FungalSpinner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gager;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGeomancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollSapper;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Intruder;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Jager;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Jaguar;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.PhantomPiranha;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Pylon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Ripper;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RipperDemon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Scarecrow;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Scorpio;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shielded;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.SksStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Slime;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.SpectralNecromancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.TormentedSpirit;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Typhootin;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Uro;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Yog;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GuardianTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.ui.WndTextNumberInput;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.utils.MobBestiary;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.PointF;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MobPlacer extends TestItem{
    {
        image = ItemSpriteSheet.CANDLE;
        defaultAction = AC_PLACE;
    }

    private static final String AC_PLACE = "place";
    private static final String AC_SET = "set";
    //切换分页模式（阵营分类/图鉴分类），与放置、设置按钮同级
    private static final String AC_FACTION = "faction";

    private int mobTier = 1;
    private int mobIndex = 0;
    private int elite = 0;

    //阵营分类模式开关：true 时按 铁血/正规军/帕拉蒂斯/未启用/矿洞 分页
    private boolean factionMode = false;

    private boolean shouldOverride = false;
    private int HT = 1;
    private int maxPage = 9;
    private int ST = 1;
    private int elite_op = 0;

    private final ArrayList<Class<? extends ChampionEnemy>> eliteBuffs = new ArrayList<>();
    {
        eliteBuffs.add(ChampionEnemy.Blazing.class);
        eliteBuffs.add(ChampionEnemy.AntiMagic.class);
        eliteBuffs.add(ChampionEnemy.Blessed.class);
        eliteBuffs.add(ChampionEnemy.Giant.class);
        eliteBuffs.add(ChampionEnemy.Growing.class);
        eliteBuffs.add(ChampionEnemy.Projecting.class);

        eliteBuffs.add(ChampionEnemy.AllyToRestartOK.class);
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_PLACE);
        actions.add(AC_SET);
        actions.add(AC_FACTION);
        return actions;
    }

    @Override
    public String actionName(String action, Hero hero) {
        //按钮上显示将要切换到的模式名
        if (AC_FACTION.equals(action)) {
            return M.L(MobPlacer.class, factionMode ? "mode_catalog" : "mode_faction");
        }
        return super.actionName(action, hero);
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_PLACE)) {
            GameScene.selectCell(new CellSelector.Listener() {
                @Override
                public void onSelect(final Integer cell) {
                    if(cell != null){
                        if (canPlaceMob(cell)) {
                            try {
                                Mob m = Reflection.newInstance(getMobClass());
                                if (m instanceof SksStatue){
                                    if (m instanceof ArmoredStatue)
                                        m = new ArmoredStatue(false);
                                    else if (m instanceof GuardianTrap.Guardian)
                                        m = new GuardianTrap.Guardian(false);
                                    else
                                        m = new SksStatue(false);
                                }
                                else if(m instanceof Mimic){
                                    m = Mimic.spawnAt(cell, m.getClass());
                                }
                                GameScene.add(m);

                                if( HT > 0 && shouldOverride ){
                                    m.HT = m.HP = HT;
                                }

                                if(elite_op>0){
                                    for(int i=0;i<7;++i){
                                        if((elite_op & (1<<i))>0){
                                            Buff.affect(m, eliteBuffs.get(i));
                                        }
                                    }
                                }
                                ScrollOfTeleportation.appear(m, cell);
                                Dungeon.level.occupyCell(m);
                            } catch (Exception e) {
                                GirlsFrontlinePixelDungeon.reportException(e);
                            }
                        }else{
                            GLog.w(M.L(MobPlacer.class, "forbidden"));
                        }
                    }
                    curUser.next();
                }
                @Override
                public String prompt() {
                    return M.L(MobPlacer.class, "prompt");
                }
            });

        } else if (action.equals(AC_SET)) {
            GameScene.show(new WndSetMob());
        } else if (action.equals(AC_FACTION)) {
            factionMode = !factionMode;
            GLog.p(M.L(MobPlacer.class, factionMode ? "mode_faction" : "mode_catalog"));
            updateQuickslot();
        }
    }

    private boolean canPlaceMob(int cell){
        return Actor.findChar(cell) == null && (!Dungeon.level.solid[cell] || Dungeon.level.map[cell] == Terrain.DOOR || Dungeon.level.map[cell] == Terrain.OPEN_DOOR);
    }


    protected int maxMobIndex(int tier){
        return currentData().get( tier - 1 ).size();
    }

    private Class<? extends Mob> getMobClass(){
        return currentData().get( mobTier - 1 ).get( mobIndex );
    }

    private Class<? extends Mob> getMobClass( int index ){
        return currentData().get( mobTier - 1 ).get( index );
    }

    private LinkedHashMap<Integer, List<Class<? extends Mob>>> currentData(){
        return factionMode ? factionData() : allData;
    }


    @Override
    public void storeInBundle(Bundle b){
        super.storeInBundle(b);
        b.put("mobTier", mobTier);
        b.put("mobIndex", mobIndex);
        b.put("eliteTags", elite);
        b.put("htTags", HT);
        b.put("stTags", ST);
        b.put("maxPage",maxPage);
        b.put("elite_ops", elite_op);
        b.put("mob_shouldOverride",shouldOverride);
        b.put("factionMode", factionMode);
    }

    @Override
    public void restoreFromBundle(Bundle b){
        super.restoreFromBundle(b);
        maxPage = b.getInt("maxPage");
        mobTier = b.getInt("mobTier");
        mobIndex = b.getInt("mobIndex");
        elite = b.getInt("eliteTags");
        HT = b.getInt("htTags");
        ST = b.getInt("stTags");
        elite_op = b.getInt("elite_ops");
        shouldOverride = b.getBoolean("mob_shouldOverride");
        factionMode = b.getBoolean("factionMode");
    }


    private class WndSetMob extends Window{

        private static final int WIDTH = 150;
        private static final int HEIGHT = 180;
        private static final int BTN_SIZE = 18;
        private static final int GAP = 2;

        private RenderedTextBlock selectedPage;
        private ArrayList<IconButton> mobButtons = new ArrayList<>();
        private RenderedTextBlock selectedMob;
        private RenderedTextBlock groupLabel;
        private RedButton btnHealth;
        private RedButton btnBuffs;

        public WndSetMob(){
            super();

            resize(WIDTH, HEIGHT);

            RedButton lhs = new RedButton(Messages.get(WndSetMob.class,"last_page"), 6){
                @Override
                public void onClick(){
                    mobTier--;
                    if(mobTier < 1 || mobTier>maxPage){
                        mobTier = maxPage;
                    }
                    mobIndex = Math.min(mobIndex, maxMobIndex(mobTier) - 1 );
                    refreshImage();
                    updateSelectedMob();
                }
            };
            lhs.setRect(GAP, GAP, 24, 18);
            add(lhs);

            RedButton rhs = new RedButton(Messages.get(WndSetMob.class,"next_page"), 6){
                @Override
                public void onClick(){
                    mobTier++;
                    if(mobTier < 1 || mobTier >maxPage){
                        mobTier = 1;
                    }
                    mobIndex = Math.min(mobIndex, maxMobIndex(mobTier) - 1 );
                    refreshImage();
                    updateSelectedMob();
                }
            };
            rhs.setRect(WIDTH - 24 - GAP,  GAP, 24, 18);
            add(rhs);

            selectedPage = PixelScene.renderTextBlock("", 9);
            PixelScene.align(selectedPage);
            add(selectedPage);

            selectedMob = PixelScene.renderTextBlock("", 9);
            selectedMob.hardlight(0xFFFF44);
            PixelScene.align(selectedMob);
            add(selectedMob);

            //阵营模式下的分组标题（铁血/正规军/帕拉蒂斯/未启用/矿洞）
            groupLabel = PixelScene.renderTextBlock("", 7);
            groupLabel.hardlight(0x88CCFF);
            PixelScene.align(groupLabel);
            add(groupLabel);

            //修改血量按钮（已整合覆盖开关）：输入大于0的数值即覆盖怪物血量，输入0取消覆盖
            btnHealth = new RedButton("", 7) {
                @Override
                protected void onClick() {
                    Game.runOnRenderThread(() -> GameScene.show(new WndTextNumberInput(
                            Messages.get(MobPlacer.class, "custom_title"),
                            Messages.get(MobPlacer.class, "health_desc"),
                            Integer.toString(HT),
                            6, false, Messages.get(MobPlacer.class, "confirm"),
                            Messages.get(MobPlacer.class, "cancel"), false) {
                        @Override
                        public void onSelect(boolean check, String text) {
                            if (check && text.matches("\\d+")) {
                                int value = Integer.parseInt(text);
                                HT = Math.min(value, 666666);
                                //输入0视为关闭血量覆盖
                                shouldOverride = HT > 0;
                                updateHealthBtnText();
                            }
                        }
                    }));
                }
            };
            add(btnHealth);
            updateHealthBtnText();

            //怪物增益按钮：打开子窗口集中勾选精英词条，两种分页模式通用
            btnBuffs = new RedButton(M.L(MobPlacer.class, "select_buffs"), 7) {
                @Override
                protected void onClick() {
                    Game.runOnRenderThread(() -> GameScene.show(new WndSelectBuffs()));
                }
            };
            add(btnBuffs);

            applyMode();
        }

        //血量按钮文本同步：覆盖开启时显示当前数值
        private void updateHealthBtnText(){
            btnHealth.text(Messages.get(MobPlacer.class, "modify_health") + (shouldOverride ? "(" + HT + ")" : ""));
        }

        //切换模式后重建分页：同步页数、钳制索引并刷新图标
        private void applyMode(){
            maxPage = currentData().size();
            mobTier = Math.min(Math.max(mobTier, 1), maxPage);
            mobIndex = Math.min(mobIndex, maxMobIndex(mobTier) - 1);
            if (mobIndex < 0) mobIndex = 0;
            refreshImage();
            updateSelectedMob();
            layout();
        }

        private void updateSelectedMob(){
            int selected = mobTier;
            StringBuilder sb = new StringBuilder();
            for(int i=1;i<=maxPage;++i){
                sb.append((i==selected? "_ ":"- "));
            }
            selectedPage.text(sb.toString());
            selectedPage.maxWidth(WIDTH / 2);
            selectedPage.setPos((WIDTH - selectedPage.width())/2, 5);
            //分组标题跟随翻页刷新
            String key = groupTitleKey();
            groupLabel.text(key.isEmpty() ? "" : M.L(MobPlacer.class, key));
            groupLabel.maxWidth(WIDTH);
            groupLabel.setPos((WIDTH - groupLabel.width())/2, 21);
            updateMobText();
        }

        private void updateMobText(){
            if (maxMobIndex(mobTier) == 0){
                selectedMob.text("-");
                return;
            }
            selectedMob.text( M.L( getMobClass(), "name") );
        }

        private void layout(){
            selectedMob.maxWidth(WIDTH - GAP * 2);
            selectedMob.setPos(GAP, HEIGHT - 34);
            //底部两个功能按钮：修改血量 + 怪物增益
            float btnW = (WIDTH - GAP * 3) / 2f;
            btnHealth.setRect(GAP, HEIGHT - 18, btnW, 16);
            btnBuffs.setRect(btnHealth.right() + GAP, HEIGHT - 18, btnW, 16);
            resize(WIDTH, HEIGHT);
        }

        private void createMobImage() {
            int maxNum = maxMobIndex(mobTier);
            int columns = Math.min(maxNum, 7);

            //每页最多ICONS_PER_PAGE个图标，翻页查看全部，不使用滚动容器
            float left = (WIDTH - (GAP + BTN_SIZE) * columns + GAP) / 2f;
            float top = 30f;

            for (int i = 0; i < maxNum; ++i) {
                final int j = i;
                IconButton btn = new IconButton() {
                    @Override
                    public void onClick() {
                        super.onClick();
                        mobIndex = j;
                        updateMobText();
                    }
                };
                CharSprite sprite;
                try {
                    sprite = Reflection.newInstance(getMobClass(i)).sprite();
                } catch (Exception e) {
                    GLog.w("MobPlacer: failed to create sprite for " + getMobClass(i).getSimpleName() + " - " + e.getMessage());
                    sprite = null;
                }
                btn.icon(sprite);
                if (btn.icon() == null) {
                    continue; //skip mobs whose sprite isn't initialized yet
                }
                float max = Math.max(btn.icon().width(), btn.icon().height());
                btn.icon().scale = new PointF(BTN_SIZE / max, BTN_SIZE / max);
                btn.setRect(left, top, BTN_SIZE, BTN_SIZE);
                left += GAP + BTN_SIZE;

                if ((i + 1) % columns == 0) {
                    left = (WIDTH - (GAP + BTN_SIZE) * columns + GAP) / 2f;
                    top += GAP + BTN_SIZE;
                }

                add(btn);
                mobButtons.add(btn);
            }
        }

        private void clearImage(){
            for(int i=0, len = mobButtons.size();i<len;++i){
                mobButtons.get(i).destroy();
            }
            mobButtons.clear();
        }

        private void refreshImage(){
            clearImage();
            createMobImage();
        }

        @Override
        public void onBackPressed() {
            super.onBackPressed();
        }
    }

    //怪物增益选择子窗口：集中勾选精英词条，勾选状态实时写回 elite_op
    private class WndSelectBuffs extends Window {

        private static final int WIN_W = 120;

        public WndSelectBuffs(){
            super();
            int gap = 2;
            RenderedTextBlock title = PixelScene.renderTextBlock(M.L(MobPlacer.class, "select_buffs"), 9);
            title.hardlight(0xFFFF44);
            PixelScene.align(title);
            title.setPos((WIN_W - title.width())/2, gap);
            add(title);

            float pos = title.bottom() + gap;
            for (int i = 0; i < 7; i++){
                final int bit = 1 << i;
                CheckBox cb = new CheckBox(M.L(MobPlacer.class, "elite_name" + i), 7){
                    @Override
                    public void checked(boolean value) {
                        if (checked != value){
                            checked = value;
                            icon.copy(Icons.get(checked ? Icons.CHECKED : Icons.UNCHECKED));
                            //勾选状态实时写回对应位
                            elite_op = checked ? (elite_op | bit) : (elite_op & ~bit);
                        }
                    }
                };
                cb.checked((elite_op & bit) > 0);
                cb.setRect(gap, pos, WIN_W - gap * 2, 16);
                add(cb);
                pos += 16 + gap;
            }
            resize(WIN_W, (int)pos);
        }
    }


    private static LinkedHashMap<Integer, List<Class<? extends Mob>>> allData = new LinkedHashMap<>();
    static {
        Set<MobBestiary> excludedTypes = EnumSet.of(
                MobBestiary.TRAP,
                MobBestiary.PLANT,
                MobBestiary.ALLY,
                MobBestiary.NEUTRAL
        );
        //List<Class<?>> includedBosses = Arrays.asList(null);
        for(MobBestiary bestiary : MobBestiary.values()){
            if( !excludedTypes.contains( bestiary ) ) {
                List< Class< ? extends Mob > > mobClasses = new ArrayList<>();
                for ( Class<?> cls : bestiary.entities() ) {
                    if ( Mob.class.isAssignableFrom( cls )) {
                        mobClasses.add( ( Class< ? extends Mob >) cls );
                    }
                }
                allData.put( bestiary.ordinal(), new ArrayList<>( mobClasses ) );
            }

        }


    }

    // ===== 阵营分类模式数据 =====
    //分组页索引
    private static final int GROUP_SANGVIS  = 0; //铁血工造
    private static final int GROUP_ARMY     = 1; //正规军
    private static final int GROUP_PARADEUS = 2; //帕拉蒂斯
    private static final int GROUP_DORMANT  = 3; //不在生成表中的非Boss单位
    private static final int GROUP_MINES    = 4; //矿洞任务敌人
    private static final int GROUP_COUNT    = 5;

    //每页最多容纳的图标数（5行×7列），超出自动拆成多页，避免使用滚动容器
    private static final int ICONS_PER_PAGE = 35;
    //factionData页码 → 所属分组（用于显示分组标题）
    private static List<Integer> factionPageGroup = new ArrayList<>();

    //图鉴之外的Boss与休眠单位（实例化探针时若类不可用会被自动跳过）
    private static final Class<?>[] EXTRA_MOBS = {
            Excutioner.class, Uro.class, Elphelt.class, Pylon.class, Gager.class, Disloli.class, YogDzewa.class,
            DemonSpawner.class, RipperDemon.class,
            Acidic.class, Aegis.class, CrystalGuardian.class, CrystalSpire.class, CrystalWisp.class,
            DM100.class, DM200.class, DM201.class, ELID.class, Eye.class,
            FungalCore.class, FungalSentry.class, FungalSpinner.class,
            Intruder.class, Jager.class, Jaguar.class, Necromancer.class,
            PhantomPiranha.class, Ripper.class, Scarecrow.class, Scorpio.class, Shielded.class,
            Skeleton.class, Slime.class, SpectralNecromancer.class, TormentedSpirit.class, Typhootin.class,
            Yog.class
    };

    //惰性初始化：首次打开设置窗口时才构建，避免静态阶段实例化怪物引发崩溃
    private static LinkedHashMap<Integer, List<Class<? extends Mob>>> factionData = null;

    private static LinkedHashMap<Integer, List<Class<? extends Mob>>> factionData(){
        if (factionData == null){
            initFactionData();
        }
        return factionData;
    }

    private static void initFactionData(){
        //先按分组收集，最后再统一拆分成分页
        List<List<Class<? extends Mob>>> raw = new ArrayList<>();
        for (int i = 0; i < GROUP_COUNT; i++){
            raw.add(new ArrayList<>());
        }

        //全集：图鉴已有单位 + Boss + 休眠单位
        LinkedHashSet<Class<?>> universe = new LinkedHashSet<>();
        for (List<Class<? extends Mob>> list : allData.values()){
            universe.addAll(list);
        }
        universe.addAll(Arrays.asList(EXTRA_MOBS));

        //图鉴内单位集合（休眠页只放图鉴之外的单位，避免与经典模式重复）
        HashSet<Class<?>> cataloged = new HashSet<>(universe);
        cataloged.removeAll(Arrays.asList(EXTRA_MOBS));

        //1~30层生成表集合，用于筛出"不在生成表中"的单位
        HashSet<Class<?>> spawned = new HashSet<>();
        try {
            for (int f = 1; f <= 30; f++){
                spawned.addAll(Bestiary.getRotation(f));
            }
        } catch (Exception ignored) {}

        for (Class<?> cls : universe){
            if (!Mob.class.isAssignableFrom(cls)) continue;
            try {
                Mob probe = (Mob) Reflection.newInstance(cls);
                //Boss不允许自由生成：它们的战斗/死亡逻辑硬绑定了各自Boss关卡，放进普通楼层会崩溃
                if (Char.hasProp(probe, Char.Property.BOSS)) continue;
                //按阵营归档
                switch (probe.faction){
                    case SANGVIS_FERRI:
                        raw.get(GROUP_SANGVIS).add((Class<? extends Mob>) cls);
                        break;
                    case REGULAR_ARMY:
                        raw.get(GROUP_ARMY).add((Class<? extends Mob>) cls);
                        break;
                    case PARA_DEUS:
                        raw.get(GROUP_PARADEUS).add((Class<? extends Mob>) cls);
                        break;
                    default:
                        break;
                }
                //不在生成表中的非Boss、且经典模式图鉴里没有的单位，归入休眠页
                if (!spawned.contains(cls)
                        && !cataloged.contains(cls)){
                    raw.get(GROUP_DORMANT).add((Class<? extends Mob>) cls);
                }
            } catch (Exception ignored) {
                //某个类实例化失败时跳过，保证其余单位不受影响
            }
        }

        //矿洞任务敌人单独一组
        raw.get(GROUP_MINES).add(GnollGuard.class);
        raw.get(GROUP_MINES).add(GnollSapper.class);
        raw.get(GROUP_MINES).add(GnollGeomancer.class);

        //每组按每页ICONS_PER_PAGE个拆分为连续页，并记录每页所属分组
        factionData = new LinkedHashMap<>();
        factionPageGroup = new ArrayList<>();
        int page = 0;
        for (int g = 0; g < GROUP_COUNT; g++){
            List<Class<? extends Mob>> group = raw.get(g);
            if (group.isEmpty()){
                factionData.put(page, new ArrayList<>());
                factionPageGroup.add(g);
                page++;
                continue;
            }
            for (int start = 0; start < group.size(); start += ICONS_PER_PAGE){
                factionData.put(page, new ArrayList<>(group.subList(start, Math.min(start + ICONS_PER_PAGE, group.size()))));
                factionPageGroup.add(g);
                page++;
            }
        }
    }

    //当前分组页的标题键名
    private String groupTitleKey(){
        if (!factionMode || factionPageGroup.isEmpty() || mobTier - 1 >= factionPageGroup.size()) return "";
        switch (factionPageGroup.get(mobTier - 1)){
            case GROUP_SANGVIS:  return "group_sangvis";
            case GROUP_ARMY:     return "group_army";
            case GROUP_PARADEUS: return "group_paradeus";
            case GROUP_DORMANT:  return "group_dormant";
            case GROUP_MINES:    return "group_mines";
            default:             return "";
        }
    }
}