package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;


import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elphelt;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestItem;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.AlchemicalCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.Brew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.Elixir;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.ColorItem;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Cypros;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.canScrollRedButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSelectTalent;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSlider;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndWithCanScrollButton;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;
import com.watabou.utils.GameMath;

import java.util.ArrayList;

public class debugBook extends TestItem {
    {
        image = ItemSpriteSheet.SCROLL_HOLDER;
        defaultAction =AC_SetMode;
    }
    private static final String AC_SetMode = "setMode";
    private static final String AC_COPY     = "copy";
    private static final String AC_APPLY = "apply";
    private static Mode mode = Mode.NONE;
    private static final String AC_SET_NUM = "SET";
    enum Mode{
        NONE, EXPERIENCE, STRENGTH, LEVEL, IGNORE, MOB, CHARGE, RESET, COMPLETE, TALENT, WEALTH, COOLDOWN;
        public String modeName(){
            return Messages.get(debugBook.class, name());
        }
    }
    @Override
    public String name(){
        return Messages.get(this, "name") + modeName();
    }
    private String modeName(){
        if (mode == Mode.NONE)
            return "";
        return "——" + mode.modeName();
    }
    private String modeDesc(){
        switch(mode)
        {
            case EXPERIENCE:
                return Messages.get(this, "exp");
            case STRENGTH:
                return Messages.get(this, "str");
            case LEVEL:
                return Messages.get(this, "lvl");
            case IGNORE:
                return Messages.get(this, "ign");
            case MOB:
                return Messages.get(this, "mob_");
            case CHARGE:
                return Messages.get(this, "cha", chaDesc());
            case RESET:
                return Messages.get(this, "res");
            case COMPLETE:
                return Messages.get(this, "comp");
            case TALENT:
                return Messages.get(this, "tal");
            case WEALTH:
                return Messages.get(this, "wea");
            default:
                return "";
        }
    }
    private String chaDesc(){
        switch (workingNum){
            case 1:
                return Messages.get(this,"cha_rem");
            case 2:
                return Messages.get(this,"cha_lock");
            case 0: default:
                return Messages.get(this,"cha_full");
        }
    }
    @Override
    public String desc(){
        String desc =super.desc();
        desc+= modeDesc();
        return desc;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_SetMode);
        if (mode == Mode.RESET)
            actions.add(AC_COPY);
        if(mode != Mode.NONE)
            actions.add(AC_APPLY);
        switch (mode){
            case NONE:
                defaultAction=AC_SetMode;
                break;
            case EXPERIENCE:
            case STRENGTH:
            case LEVEL:
            case MOB:
            case CHARGE:
            case WEALTH:
                actions.add(AC_SET_NUM);
                break;
            default:
                defaultAction=AC_APPLY;
                break;
        }
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        switch (action) {
            case AC_SetMode:
                setMode();
                break;
            case AC_SET_NUM:
                switch (mode) {
                    case EXPERIENCE: setEXP();break;
                    case STRENGTH: setSTR();break;
                    case LEVEL: setLVL();break;
                    case MOB: setMOB();break;
                    case CHARGE: setCHA();break;
                    case WEALTH: setWealth();break;
                }
                break;
            case AC_APPLY:
                switch (mode) {
                    case EXPERIENCE: updateEXP(); break;
                    case STRENGTH: updateSTR(); break;
                    case LEVEL: GameScene.selectItem(itemLVL); break;
                    case IGNORE: GameScene.selectItem(itemIGN); break;
                    case MOB: mobAPPLY(); break;
                    case CHARGE: GameScene.selectItem(itemCHA); break;
                    case RESET: resetLevel(); break;
                    case COMPLETE: complete(); break;
                    case TALENT: setTalent(); break;
                    case WEALTH: WealthKill(); break;
                    case COOLDOWN: GameScene.selectItem(itemCoolDown); break;
                    case NONE: default: break;
                }
                break;
            case AC_COPY:
                try {
                    Bundle bundle = new Bundle();
                    bundle.put( Dungeon.LEVEL, Dungeon.level );
                    FileUtils.bundleToFile(GamesInProgress.depthFile(GamesInProgress.curSlot , Dungeon.cur().depth, Dungeon.cur().branch, true), bundle);
                } catch (Exception e) {
                    GirlsFrontlinePixelDungeon.reportException(e);
                }
                break;
        }
    }

    private int workingNum = 0;
    private void setMode(){
        workingNum = 0;
        ArrayList<canScrollRedButton> buttons = new ArrayList<>();
        for (Mode e : Mode.values()){
            if (e != Mode.NONE)
                buttons.add(new canScrollRedButton(e.modeName()){
                    @Override
                    public void onClick(){
                        super.onClick();
                        modeChange(e);
                    }
                });
        }
        INSTANCE = new WndWithCanScrollButton(buttons);
        GirlsFrontlinePixelDungeon.scene().addToFront(INSTANCE);
    }
    private static WndWithCanScrollButton INSTANCE = null;
    private void modeChange(Mode mode_){
        mode = mode_;
        if (INSTANCE != null)
            INSTANCE.hide();
        switch (mode_){
            case EXPERIENCE: case STRENGTH: case LEVEL: case MOB: case CHARGE: case WEALTH:
                defaultAction = AC_SET_NUM; break;
            default:
                defaultAction = AC_APPLY; break;
        }
        GLog.p(Messages.get(this,"modeText", mode_.modeName()));
        updateQuickslot();
    }

    private void setEXP(){
        Game.runOnRenderThread(()-> GameScene.show(new WndSlider(Messages.get(debugBook.class, "exp_title"), 2, Dungeon.hero().lvl){
            @Override
            public void hide(){
                super.hide();
                workingNum = (int) GameMath.gate(1, num, 40);
                defaultAction = AC_APPLY;
                updateQuickslot();
            }
        }));
    }
    private void updateEXP(){
        if(workingNum-Dungeon.hero().lvl!=0){
            new PotionOfExperience().apply(Dungeon.hero());
            Dungeon.hero().lvl = workingNum;
            Dungeon.hero().attackSkill = 10 + workingNum - 1;
            Dungeon.hero().defenseSkill = 5 + workingNum - 1;
            Sample.INSTANCE.play( Assets.Sounds.READ );
            Dungeon.hero().updateHT( true );
        }else {
            defaultAction = AC_SetMode;
            updateQuickslot();
        }
    }
    private void setSTR(){
        Game.runOnRenderThread(()-> GameScene.show(new WndSlider(Messages.get(debugBook.class, "str_title"), 4, Dungeon.hero().STR){
            @Override
            public void hide(){
                super.hide();
                workingNum = num;
                defaultAction = AC_APPLY;
                updateQuickslot();
            }
        }));
    }
    private void updateSTR(){
        new PotionOfStrength().apply(Dungeon.hero());
        Dungeon.hero().STR = workingNum;
        Sample.INSTANCE.play( Assets.Sounds.READ );
        defaultAction = AC_SetMode;
        updateQuickslot();
    }
    private void setLVL(){
        Game.runOnRenderThread(()-> GameScene.show(new WndSlider(Messages.get(debugBook.class, "lvl_title"), 4){
            @Override
            public void hide(){
                super.hide();
                if (workingNum == num){
                    defaultAction = AC_SetMode;
                    updateQuickslot();
                }
                else {
                    workingNum = num;
                    defaultAction = AC_APPLY;
                    updateQuickslot();
                }
            }
        }));
    }
    protected WndBag.ItemSelector itemLVL = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return Messages.get(debugBook.class, "lvl_select");
        }

        @Override
        public Class<? extends Bag> preferredBag() {
            return Belongings.Backpack.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            return item.isUpgradable()||item instanceof BrokenSeal||item instanceof Artifact;
        }

        @Override
        public void onSelect( Item item ) {
            if(item != null){
                if (item instanceof Artifact){
                    int lvl = Math.min(workingNum, ((Artifact) item).levelCap);
                    int lvlB = lvl-item.level();
                    if (lvlB>=0) {
                        item.upgrade(lvlB);
                    }else {
                        item.degrade(-lvlB);
                    }
                }else {
                    if (item.level()==workingNum){
                        item.level(-workingNum);
                    }else {
                        item.level(workingNum);
                    }
                }
                Sample.INSTANCE.play( Assets.Sounds.READ );
            }
            else{
                defaultAction = AC_SET_NUM;
                updateQuickslot();
            }
        }
    };
    protected WndBag.ItemSelector itemIGN = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return Messages.get(debugBook.class, "ign_select");
        }

        @Override
        public Class<? extends Bag> preferredBag() {
            return Belongings.Backpack.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            return (item instanceof EquipableItem &&!(item instanceof MissileWeapon))||item instanceof Wand||(item instanceof ColorItem &&!(item instanceof Brew)&&!(item instanceof Elixir)&&!(item instanceof AlchemicalCatalyst));
        }

        @Override
        public void onSelect( Item item ) {

            if(item != null){
                if (item instanceof ColorItem){
                    ((ColorItem) item).setIgnore();
                } else if (item instanceof EquipableItem ||item instanceof Wand){
                    item.levelKnown = false;
                    item.cursedKnown = false;
                }
                item.resetGuessingLevel();
                item.BuffLevelPoint = Integer.MIN_VALUE;
                Sample.INSTANCE.play( Assets.Sounds.READ );
            }
            else {
                defaultAction = AC_SetMode;
                updateQuickslot();
            }
        }
    };
    private void setMOB(){
        Game.runOnRenderThread(()-> GameScene.show(new WndSlider(Messages.get(debugBook.class, "mob_title"), 3, Dungeon.mobRan){
            @Override
            public void hide(){
                super.hide();
                workingNum = num;
                defaultAction = AC_APPLY;
                updateQuickslot();
            }
        }));
    }
    private void mobAPPLY(){
        GLog.p(Messages.format("已将怪物精英率调整至 %d %%", workingNum));
        Dungeon.mobRan = workingNum;
        defaultAction = AC_SetMode;
        updateQuickslot();
    }
    private void addButton(ArrayList<canScrollRedButton> buttons, String message, int setNum){
        buttons.add(new canScrollRedButton(message, setNum){

            @Override
            public void onClick(){
                super.onClick();
                if (INSTANCE != null)
                    INSTANCE.hide();
                workingNum = num;
                defaultAction = AC_APPLY;
                updateQuickslot();
            }
        });
    }
    private void setCHA(){
        ArrayList<canScrollRedButton> buttons = new ArrayList<>();
        addButton(buttons, Messages.get(debugBook.class, "cha_full"), 0);
        addButton(buttons, Messages.get(debugBook.class, "cha_rem"), 1);
        addButton(buttons, Messages.get(debugBook.class, "cha_lock"), 2);
        INSTANCE = new WndWithCanScrollButton(buttons){
            @Override
            public void onBackPressed() {
                super.onBackPressed();
                defaultAction = AC_SetMode;
                updateQuickslot();
            }
        };
        GirlsFrontlinePixelDungeon.scene().addToFront(INSTANCE);
    }
    protected WndBag.ItemSelector itemCHA = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return Messages.get(debugBook.class, "cha_select");
        }

        @Override
        public Class<? extends Bag> preferredBag() {
            return Belongings.Backpack.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            return item instanceof ClassArmor||item instanceof Wand||(item instanceof Artifact&&item.getClass()!= ChaliceOfBlood.class)
                    || item instanceof MagesStaff || item instanceof Cypros;
        }

        @Override
        public void onSelect( Item item ) {
            if(item != null){
                switch (workingNum){
                    case 1:
                        lockCharge(item);
                        break;
                    case 2:
                        lockClass(item);
                        break;
                    case 0: default:
                        fullCharge(item);
                        break;
                }
                Sample.INSTANCE.play( Assets.Sounds.READ );
            }
            else {
                defaultAction = AC_SET_NUM;
                updateQuickslot();
            }
        }
    };
    private void fullCharge(Item item){
        Item itemB;
        if (item instanceof MagesStaff){
            itemB = ((MagesStaff) item).wand;
        }
        else if(item instanceof Cypros){
            itemB = ((Cypros) item).wand;
        }
        else {
            itemB = item;
        }

        if (itemB instanceof ClassArmor){
            ((ClassArmor) itemB).charge = 100;
        }
        else if (itemB instanceof Wand){
            ((Wand) itemB).curCharges = ((Wand) itemB).maxCharges;
        }
        else if (itemB instanceof Artifact){
            if(((Artifact) itemB).chargeCap>0)
                ((Artifact) itemB).charge = ((Artifact) itemB).chargeCap;
        }
        updateQuickslot();
    }
    private void lockCharge(Item item){
        Item itemB;
        if (item instanceof MagesStaff){
            itemB = ((MagesStaff) item).wand;
        }
        else if(item instanceof Cypros){
            itemB = ((Cypros) item).wand;
        }else {
            itemB = item;
        }

        if (itemB instanceof ClassArmor){
            if(((ClassArmor) itemB).lockcharge){
                ((ClassArmor) itemB).lockcharge = false;
                ((ClassArmor) itemB).chargeRem = 0;
                GLog.n(Messages.get(this,"cha_singleUnlock"));
                //先解除后清除登记，以免改变当前充能
            }
            else {
                ((ClassArmor) itemB).chargeRem = ((ClassArmor) itemB).charge;
                ((ClassArmor) itemB).lockcharge = true;
                GLog.p(Messages.get(this,"cha_singleLock"));
                //先等级后启动，以免丢失当前充能
            }
        }
        else if (itemB instanceof Wand){
            if(((Wand) itemB).lockcharge){
                ((Wand) itemB).lockcharge = false;
                ((Wand) itemB).chargeRem = 0;
                GLog.n(Messages.get(this,"cha_singleUnlock"));
            }else {
                ((Wand) itemB).chargeRem = ((Wand) itemB).curCharges;
                ((Wand) itemB).lockcharge = true;
                GLog.p(Messages.get(this,"cha_singleLock"));
            }
        }
        else if (itemB instanceof Artifact){
            if(((Artifact) itemB).lockcharge){
                ((Artifact) itemB).lockcharge=false;
                ((Artifact) itemB).chargeRem= 0;
                GLog.n(Messages.get(this,"cha_singleUnlock"));
            }else {
                ((Artifact) itemB).chargeRem= ((Artifact) itemB).charge;
                ((Artifact) itemB).lockcharge=true;
                GLog.p(Messages.get(this,"cha_singleLock"));
            }
        }
        updateQuickslot();
    }
    private void lockClass(Item item){
        Item itemB;
        if (item instanceof MagesStaff){
            itemB = ((MagesStaff) item).wand;
        }
        else if(item instanceof Cypros){
            itemB = ((Cypros) item).wand;
        }
        else {
            itemB = item;
        }

        if (itemB instanceof ClassArmor){
            if(Dungeon.ArmorLock){
                Dungeon.ArmorLock = false;
                GLog.n(Messages.get(this, "cha_armorUnlock"));
            }else {
                Dungeon.ArmorLock = true;
                GLog.p(Messages.get(this, "cha_armorLock"));
            }
        }
        else if (itemB instanceof Wand){
            if(Dungeon.WandLock){
                Dungeon.WandLock = false;
                GLog.n(Messages.get(this, "cha_wandUnlock"));
            }else {
                Dungeon.WandLock = true;
                GLog.p(Messages.get(this, "cha_wandLock"));
            }
        }
        else if (itemB instanceof Artifact){
            if(Dungeon.ArtifactLock){
                Dungeon.ArtifactLock = false;
                GLog.n(Messages.get(this, "cha_artifactUnlock"));
            }else {
                Dungeon.ArtifactLock = true;
                GLog.p(Messages.get(this, "cha_artifactLock"));
            }
        }
        updateQuickslot();
    }
    private void resetLevel(){//mark
        defaultAction = AC_SetMode;
        GLog.p("已重置楼层。");
        InterlevelScene.returnDepth = Dungeon.cur().depth;
        InterlevelScene.returnBranch = 0;
        InterlevelScene.mode = InterlevelScene.Mode.RESET;
        Game.switchScene( InterlevelScene.class );
    }
    private void complete(){
        defaultAction = AC_SetMode;
        Ghost.Quest.cur().complete();
        Ghost.Quest.cur().spawned = true;
        Ghost.Quest.cur().processed = true;
        Wandmaker.Quest.cur().complete();
        Wandmaker.Quest.cur().spawned = true;
        Buff.count(Dungeon.hero(), Elphelt.Finish.class,1);
        Blacksmith.Quest.cur().completed = true;
        Blacksmith.Quest.cur().spawned = true;
        //新版小恶魔任务以满分结算，同时满足商店解锁条件
        Imp.Quest.cur().complete(4000);
        Imp.Quest.cur().spawned = true;
        GLog.p("NPC任务已全部完成。");
    }
    private void setTalent(){
        GirlsFrontlinePixelDungeon.scene().addToFront(new WndSelectTalent());
    }
    private void setWealth(){
        Game.runOnRenderThread(()-> GameScene.show(new WndSlider(Messages.get(debugBook.class, "wealth_title"), 2, workingNum){
            @Override
            public void hide(){
                super.hide();
                workingNum = num;
                defaultAction = AC_APPLY;
                updateQuickslot();
            }
        }));
    }
    private void WealthKill(){
        RingOfWealth.Wealth wealth = Dungeon.hero().buff(RingOfWealth.Wealth.class);
        if (wealth != null) {
            GLog.p(Messages.format("已增加 %d 点击杀数", workingNum));
            ArrayList<Item> items = RingOfWealth.tryForBonusDrop(Dungeon.hero(), workingNum, wealth.ring());
            if (!items.isEmpty())
                RingOfWealth.showFlareForBonusDrop(Dungeon.hero().sprite, wealth.ring(), "");
            for (Item item : items) {
                Dungeon.level.drop(item, Dungeon.hero().pos);
            }
        }
        else {
            defaultAction = AC_SetMode;
            setMode();
        }
    }
    protected WndBag.ItemSelector itemCoolDown = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return Messages.get(debugBook.class, "coolDown_select");
        }

        @Override
        public Class<? extends Bag> preferredBag() {
            return Belongings.Backpack.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            return item.coolDownLeft > 0;
        }

        @Override
        public void onSelect( Item item ) {

            if(item != null){
                item.coolDownLeft = 0;
                Sample.INSTANCE.play( Assets.Sounds.READ );
            }
            else {
                defaultAction = AC_SetMode;
                updateQuickslot();
            }
        }
    };
}