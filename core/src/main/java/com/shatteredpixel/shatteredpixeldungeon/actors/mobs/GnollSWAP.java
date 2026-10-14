//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.buffType;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollSWAPSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import java.util.ArrayList;

public class GnollSWAP extends Prowler {
    {
        spriteClass = GnollSWAPSprite.class;
        PASSIVE = new Passive();
        WANDERING = new Wandering();
        state = PASSIVE;
        defenseSkill = 6;
        HP = HT = 24;
        lootChance = 0.0F;
    }

    @Override
    public Notes.Landmark landmark(){
        return Notes.Landmark.GNOLL_SWAP;
    }
    public int damageRoll() {
        return Random.NormalIntRange(1, 10);
    }

    public int attackSkill(Char target) {
        return 15;
    }

    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, 1);
    }

    protected boolean canAttack(Char enemy) {
        if (Dungeon.level.adjacent(pos, enemy.pos)) {
            return true;
        } else {
            if (Dungeon.level.distance(pos, enemy.pos) <= 2) {
                boolean[] passable = BArray.not(Dungeon.level.solid, null);

                for(Char ch : Actor.chars()) {
                    passable[ch.pos] = ch == this;
                }

                PathFinder.buildDistanceMap(enemy.pos, passable, 2);
                if (PathFinder.distance()[pos] <= 2) {
                    return true;
                }
            }

            return super.canAttack(enemy);
        }
    }

    public void rollToDropLoot() {
        super.rollToDropLoot();
        if (!doDrop())
            return;

        ArrayList<Item> items = new ArrayList<>();

        Item i;
        Gold gold = null;
        while (items.size() < 3){
            i = Generator.randomUsingDefaults();
            if (i instanceof Gold){
                if (gold == null) {
                    gold = (Gold) i;
                    items.add(i);
                }
                else
                    gold.quantity(gold.quantity() + i.quantity());
                continue;
            }
            items.add(i);
        }

        Dungeon.level.throwPath(items, pos);
    }

    public void beckon(int cell) {
        if (this.state != this.PASSIVE) {
            super.beckon(cell);
        } else {
            this.target = cell;
        }

    }

    public String description() {
        String desc = super.description();
        if (this.state == this.PASSIVE) {
            desc = desc + "\n\n" + Messages.get(this, "desc_passive");
        } else {
            desc = desc + "\n\n" + Messages.get(this, "desc_aggro");
        }

        return desc;
    }

    private class Passive extends Mob.Wandering {
        private int seenNotifyCooldown;

        private Passive() {
            seenNotifyCooldown = 0;
        }

        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            for(Buff b : buffs()) {
                if (b.type == buffType.NEGATIVE) {
                    state = WANDERING;
                    return true;
                }
            }

            if (fieldOfView[Dungeon.hero().pos] && Dungeon.level.heroFOV[pos]) {
                if (seenNotifyCooldown <= 0) {
                    GLog.p(Messages.get(GnollSWAP.class, "seen_passive"));
                }

                seenNotifyCooldown = 10;
            } else {
                --seenNotifyCooldown;
            }

            if (enemyInFOV && justAlerted) {
                if (Dungeon.level.heroFOV[pos]) {
                    GLog.w(Messages.get(GnollSWAP.class, "seen_aggro"));
                }

                return noticeEnemy();
            } else {
                return continueWandering();
            }
        }
    }

    private class Wandering extends Mob.Wandering {

        protected boolean noticeEnemy() {
            GLog.w(Messages.get(GnollSWAP.class, "seen_aggro"));
            return super.noticeEnemy();
        }
    }
}
