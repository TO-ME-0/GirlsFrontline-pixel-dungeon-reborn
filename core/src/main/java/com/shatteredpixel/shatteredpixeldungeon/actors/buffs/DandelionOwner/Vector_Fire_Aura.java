package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ColorFlameFactory;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.Card;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.CardAffect;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.CardCalculator;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.CardSelector;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.CommonCard;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.FinalCard;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfDragonsBreath;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;

import java.util.ArrayList;
import java.util.HashMap;

public class Vector_Fire_Aura extends Buff implements ActionIndicator.Action {
    {
        actPriority = MOB_PRIO + 1 ;
        revivePersists = true;
    }
    private final HashMap<Integer, ArrayList<AffectAura>> fireAura = new HashMap<>();
    private int lastPos = -1;
    @Override
    public boolean act(){

        spend(0.1F);
        updateEmitter(false);
        float TICK = hasCard(FinalCard.Vector.G36) ? 0.5F : 1F;
        if (curTime() % TICK < 0.1F){
            for (AffectAura a : getList()){
                int cell = a.cellInLevel(Dungeon.level, target);
                if (outMap(cell))
                    continue;
                Char ch = Actor.findChar(cell);
                if (ch != null && ch.alignment != Char.Alignment.ALLY && !(ch instanceof NPC))
                    effectOnTarget(ch);
            }
        }
        return true;
    }
    @Override
    public void fx( boolean on ){
        if (on)
            updateEmitter(true);
        else
            destroyEmitter();
    }
    private ArrayList<AffectAura> getList(){
        if (!fireAura.containsKey(Dungeon.curFloorCode()) || fireAura.get(Dungeon.curFloorCode()) == null)
            fireAura.put(Dungeon.curFloorCode(), new ArrayList<>());
        return fireAura.get(Dungeon.curFloorCode());
    }
    private static boolean outMap(int cell){
        Point p = Dungeon.level.cellToPoint(cell);
        return p.x <= 0 || p.y <= 0 || p.x >= Dungeon.level.width() - 1 || p.y >= Dungeon.level.height() - 1;
    }
    private void effectOnTarget(Char ch){
        CardAffect.fireAllAffect(ch);
        ch.damage(CardCalculator.fireDamage(true), this);
    }
    private final ArrayList<Emitter> emitters = new ArrayList<>();
    private void destroyEmitter(){
        for (Emitter e : emitters)
            e.on = false;
        emitters.clear();
    }
    private void updateEmitter(boolean strict){
        if (lastPos == target.pos && !strict)
            return;

        destroyEmitter();

        for (AffectAura aura : getList()){
            Emitter e = CellEmitter.get(aura.cellInLevel(Dungeon.level, target));
            e.pour( new ColorFlameFactory(0xAA4488), 0.05f );
            emitters.add(e);
        }

        lastPos = target.pos;
    }
    public boolean contain(int pos){
        for (AffectAura a : getList())
            if (a.cellInLevel(Dungeon.level, target) == pos)
                return true;
        return false;
    }
    public boolean addFireAura(int xAdd, int yAdd){
        ArrayList<AffectAura> auras = getList();
        for (AffectAura a : auras)
            if (a.isSame(xAdd, yAdd))
                return false;
        auras.add(new AffectAura(xAdd, yAdd));
        return true;
    }
    private static final String Affect_Floor = "Affect_Floor_";
    private static final String Affect_Aura = "Affect_Aura";
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        Bundle b = new Bundle();
        for (int i : fireAura.keySet())
            b.put(Affect_Floor + i, fireAura.get(i));
        bundle.put(Affect_Aura, b);
        resetAction();
        lastPos = -1;
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        fireAura.clear();
        Bundle b = bundle.getBundle(Affect_Aura);
        for (String key : b.getKeys())
            fireAura.put(Integer.valueOf(key.split(Affect_Floor)[1]), b.getArrayList(key, AffectAura.class));
        resetAction();
    }

    @Override
    public String actionName() {
        return Messages.get(this, "action_name");
    }
    @Override
    public Image actionIcon() {
        ArrayList<AffectAura> auras = getList();
        if (auras.size() < 5)
            return Icons.Notice(new PotionOfLiquidFlame());
        if (auras.size() < 10 && hasCard(CommonCard.Vector.PP_90))
            return Icons.Notice(new PotionOfDragonsBreath());
        return null;
    }
    @Override
    public void doAction() {
        ArrayList<AffectAura> auras = getList();
        if (auras.size() < 5) {
            GameScene.selectCell(pathSelector);
            return;
        }
        if (auras.size() < 10 && hasCard(CommonCard.Vector.PP_90))
            GameScene.selectCell(diySelector);
    }
    public void resetAction(){
        if (actionIcon() != null)
            ActionIndicator.setAction(this);
        else
            ActionIndicator.clearAction(this);
    }
    private final CellSelector.Listener pathSelector = new CellSelector.Listener() {
        @Override
        public void onSelect(Integer target) {
            if (target != null) {
                int[] path = PathFinder.NEIGHBOURS8();
                for (int i = 0; i < path.length; i++){
                    int j = path[i];
                    int cell = Vector_Fire_Aura.this.target.pos + j;
                    if (target == cell){
                        int[][] start = {{-2,  0}, {-2, -2}, { 0, -2},
                                         {-2,  2},           { 2, -2},
                                         { 0,  2}, { 2,  2}, { 2,  0}};
                        int x = start[i][0], y = start[i][1];
                        for (int k = 0; k < 5; k++){
                            addFireAura(x, y);
                            if (x == -2 && y > -2)
                                y--;
                            else if (y == -2 && x < 2)
                                x++;
                            else if (x == 2 && y < 2)
                                y++;
                            else if (y == 2 && x > -2)
                                x--;
                        }
                        resetAction();
                        updateEmitter(true);
                        return;
                    }
                }
                GLog.i(Messages.get(Vector_Fire_Aura.class, "not_path"));
            }
        }
        @Override
        public String prompt() {
            return Messages.get(Vector_Fire_Aura.class, "select_path");
        }
    };
    private final CellSelector.Listener diySelector = new CellSelector.Listener() {
        @Override
        public void onSelect(Integer target) {
            if (target != null) {
                Point point = Dungeon.level.cellToPoint(target);
                Point hero = Dungeon.level.cellToPoint(Vector_Fire_Aura.this.target.pos);
                int x = point.x - hero.x, y = point.y - hero.y;
                if (Math.abs(x) > 4 || Math.abs(y) > 4){
                    GLog.i(Messages.get(Vector_Fire_Aura.class, "out_side"));
                    return;
                }
                if (Math.abs(x) <= 1 && Math.abs(y) <= 1){
                    GLog.i(Messages.get(Vector_Fire_Aura.class, "Near"));
                    return;
                }
                if (!addFireAura(x, y)) {
                    GLog.i(Messages.get(Vector_Fire_Aura.class, "Same"));
                    return;
                }
                updateEmitter(true);
                resetAction();
            }
        }
        @Override
        public String prompt() {
            return Messages.get(Vector_Fire_Aura.class, "select_diy");
        }
    };
    private static boolean hasCard(Card card){
        return CardSelector.INSTANCE().hasCard(card);
    }
    public static class AffectAura implements Bundlable {
        private int xAdd;
        private int yAdd;
        public AffectAura(){
            this(0, 0);
        }
        public AffectAura(int x, int y){
            xAdd = x;
            yAdd = y;
        }
        public boolean isSame(int x, int y){
            return x == xAdd && y == yAdd;
        }
        public int cellInLevel(Level level, Char hero){
            Point point = level.cellToPoint(hero.pos);
            point.x += xAdd;
            point.y += yAdd;
            return level.pointToCell(point);
        }
        private static final String affect_X_Aura = "affect_X_Aura";
        private static final String affect_Y_Aura = "affect_Y_Aura";
        @Override
        public void storeInBundle(Bundle bundle) {
            bundle.put(affect_X_Aura, xAdd);
            bundle.put(affect_Y_Aura, yAdd);
        }
        @Override
        public void restoreFromBundle(Bundle bundle) {
            xAdd = bundle.getInt(affect_X_Aura);
            yAdd = bundle.getInt(affect_Y_Aura);
        }
    }
}
