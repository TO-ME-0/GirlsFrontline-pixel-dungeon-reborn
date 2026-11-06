package com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Vector_FireBomb;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Vector_FireBomb_Warning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.AttackDMG_Add;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class ThrowingSkill extends SkillItem {
    @Override
    public void onSkill( Hero hero ){
        INSTANCE(ThrowingSelector, SnipeSelector);
    }
    private static boolean isSnipe(){
        return hasCard(RareCard.WA2000.NTW_20);
    }
    private static boolean hasCard( Card card ){
        return CardSelector.INSTANCE().hasCard(card);
    }
    public static boolean Throwing_INSTANCE( int target ) {
        //无法使用hero.spend(-hero.cooldown());
        //因为cast中已经spendAndNext了
        Dungeon.hero().spend(-TIME_TO_THROW);
        new ThrowingBomb().cast(Dungeon.hero(), target);
        if (hasCard(CommonCard.Vector.Beretta_38)) {
            Dungeon.hero().spend(-TIME_TO_THROW);
            new FireBomb(4).cast(Dungeon.hero(), target);
        }
        if (hasCard(CommonCard.Vector.Uzi)) {
            Dungeon.hero().spend(-TIME_TO_THROW);
            new FireBomb(4).cast(Dungeon.hero(), target);
        }
        if (hasCard(RareCard.Vector.PP_19)) {
            Dungeon.hero().spend(-TIME_TO_THROW);
            new FireBomb(8).cast(Dungeon.hero(), target);
        }
        CardAffect.onThrowing();
        updateQuickslot();
        return true;
    }
    public static boolean Snipe_INSTANCE( int target ) {
        if (!Dungeon.level.heroFOV[target]){
            GLog.n(Messages.get(ThrowingSkill.class, "no_sight"));
            return false;
        }
        Char ch = Actor.findChar(target);
        if (ch == null || ch.alignment == Char.Alignment.ALLY || ch instanceof NPC) {
            GLog.n(Messages.get(ThrowingSkill.class, "no_enemy"));
            return false;
        }
        ch.damage(Math.round(new ThrowingBomb().allDamage(target) * 2), ThrowingSkill.class);
        CardAffect.onThrowing();
        updateQuickslot();
        return true;
    }
    public static String prompt = Messages.get(ThrowingSkill.class, "select_target");
    private final CellSelector.Listener ThrowingSelector = new CellSelector.Listener() {
        @Override
        public void onSelect(Integer target) {
            if (target != null) {
                if (!Throwing_INSTANCE(target))
                    return;
                coolDownLeft = 50;
                Dungeon.hero().spendAndNext( 1F );
                updateQuickslot();
            }
        }
        @Override
        public String prompt() {
            return prompt;
        }
    };
    private final CellSelector.Listener SnipeSelector = new CellSelector.Listener() {
        @Override
        public void onSelect(Integer target) {
            if (target != null) {
                if (!Snipe_INSTANCE(target))
                    return;
                Dungeon.hero().spendAndNext( 1F );
                coolDownLeft = 50;
            }
        }
        @Override
        public String prompt() {
            return prompt;
        }
    };
    public static void INSTANCE( CellSelector.Listener Throwing, CellSelector.Listener Snipe ){
        if (isSnipe())
            GameScene.selectCell(Snipe);
        else
            GameScene.selectCell(Throwing);
    }
    public abstract static class Throwing extends Item{
        {
            image = ItemSpriteSheet.SMOKEUmp45;
        }
        protected static int[] throwingPos(){
            return hasCard(CommonCard.UNIVERSAL.FAMAS) ?
                    PathFinder.NEIGHBOURS25() :
                    PathFinder.NEIGHBOURS9();
        }
        @Override
        public int throwPos( Hero user, int dst){
            return new Ballistica( user.pos, dst, Ballistica.STOP_SOLID | Ballistica.STOP_TARGET ).collisionPos;
        }
        @Override
        public abstract void onThrow( int center );
    }
    private static class ThrowingBomb extends Throwing {
        public float allDamage( int center ){
            float dmg = 0;
            int size = 0;
            for (int i : throwingPos()){
                int cell = center + i;
                if (!Dungeon.level.insideMap(cell))
                    continue;
                if (Actor.findChar(cell) != null)
                    size++;
            }
            float minimax = 1;
            float maxMul = 1F;
            if (hasCard(CommonCard.UNIVERSAL.Type56_1))
                minimax *= 0.5F;
            if (hasCard(FinalCard.WA2000.Python)){
                minimax *= 0.5F;
                maxMul += 0.75F;
            }
            for (int i = 0; i < size; i++) {
                float d = CardCalculator.M4A1damageRoll(
                        1 - minimax,
                        maxMul,
                        1 - (Math.min(5, size - 1)) * 0.1F);
                dmg += CardAffect.tryCrit(d, true);
            }
            return dmg;
        }
        @Override
        public void onThrow( int center ) {
            ArrayList<Char> list = new ArrayList<>();
            for (int i : throwingPos()){
                int cell = center + i;
                if (!Dungeon.level.insideMap(cell))
                    continue;
                if (Dungeon.level.flammable[cell]) {
                    Dungeon.level.destroy(cell);
                    GameScene.updateMap(cell);
                }
                Char ch;
                if ((ch = Actor.findChar(cell)) != null)
                    list.add(ch);
            }
            for (Char ch : list)
                //参考 Item.cast() 的 MissileSprite 用法：从爆心 center 飞向每个受击目标
                ((MissileSprite) Dungeon.hero().sprite.parent.recycle(MissileSprite.class))
                        .reset(center, ch.pos, this, null);
            
            for (int i : throwingPos()) {
                int cell = center + i;
                if (!Dungeon.level.insideMap(cell))
                    continue;
                CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 5);
            }
            Dungeon.observe();
            int size = list.size();
            boolean crit = CardAffect.crit();
            for (Char ch : list) {
                if (ch.alignment == Char.Alignment.ALLY || ch instanceof NPC)
                    continue;
                for (int i = 0; i < size; i++) {
                    float minimax = 1;
                    float maxMul = 1F;
                    if (hasCard(CommonCard.UNIVERSAL.Type56_1))
                        minimax *= 0.5F;
                    if (hasCard(FinalCard.WA2000.Python)){
                        minimax *= 0.5F;
                        maxMul += 0.75F;
                    }

                    float dmg = CardCalculator.M4A1damageRoll(1 - minimax, maxMul, 1 - (Math.min(5, size - 1)) * 0.1F);
                    if (ch.isAlive()) {
                        if (crit)
                            ch.damage(CardCalculator.critDamage(dmg, true), ThrowingSkill.class);
                        else
                            ch.damage(Math.round(dmg), ThrowingSkill.class);
                    }
                }
            }
            if (crit && hasCard(CommonCard.WA2000.SV_98))
                Buff.affect(Dungeon.hero(), AttackDMG_Add.SV_98.class, 5F);

            if (hasCard(RareCard.UNIVERSAL.M1887)) {
                size = -1;
                while (list.size() != size) {
                    size = list.size();
                    for (Char ch : list.toArray(new Char[0])) {
                        if (ch.alignment == Char.Alignment.ALLY || ch instanceof NPC)
                            continue;

                        int oldPos = ch.pos;

                        CardAffect.throwChar(ch, center, 2, false, false);

                        if (oldPos != ch.pos)
                            list.remove(ch);
                    }
                }
            }
        }
    }
    public static class FireBomb extends Throwing{
        private float times;
        private int affect_Center = -1;
        public FireBomb(){
            this(0);
        }
        public FireBomb(float times){
            this.times = times;
        }
        @Override
        public void onThrow(int center) {
            affect_Center = center;
            GameScene.add( Blob.seedStrict( center, Math.round(times), Vector_FireBomb.class).add(this) );
            for (int i : throwingPos())
                GameScene.add( Blob.seedStrict( center + i, Math.round(times), Vector_FireBomb_Warning.class) );
        }
        public void affect(){
            times -= 0.5F;
            for (int i : throwingPos()){
                int cell = affect_Center + i;
                Char ch = Actor.findChar(cell);
                if (ch != null && ch.alignment != Char.Alignment.ALLY && !(ch instanceof NPC)){
                    CardAffect.fireAllAffect(ch);
                    ch.damage(CardCalculator.fireDamage(false), this);
                }
            }
        }
        public float times(){
            return times;
        }
        private static final String TIME_LEFT = "TIME_LEFT";
        private static final String AFFECT_CENTER = "AFFECT_CENTER";
        @Override
        public void storeInBundle( Bundle bundle ){
            super.storeInBundle(bundle);
            bundle.put(TIME_LEFT, times);
            bundle.put(AFFECT_CENTER, affect_Center);
        }
        @Override
        public void restoreFromBundle( Bundle bundle ){
            super.restoreFromBundle(bundle);
            times = bundle.getFloat(TIME_LEFT);
            affect_Center = bundle.getInt(AFFECT_CENTER);
        }
    }
}
