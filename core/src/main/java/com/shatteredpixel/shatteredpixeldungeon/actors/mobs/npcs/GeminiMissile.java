/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class GeminiMissile extends Geminis {
    @Override
    public int damageRoll() {
        return Random.NormalIntRange( Statistics.deepestFloor/4, Statistics.deepestFloor/2 );
    }

    @Override
    public int attackSkill(Char target) {
        return INFINITE_ACCURACY;
    }

    @Override
    public int defenseSkill(Char enemy) {
        if (enemy instanceof Hero)
            return 0;
        return HT/3;
    }

    @Override
    public void damage(int dmg, Object src) {
        Char mob = null;
        if (src instanceof Char && !(src instanceof Geminis))
            mob = (Char) src;
        else if (WorkingEnemy != null && !(WorkingEnemy instanceof Geminis))
            mob = WorkingEnemy;
        if (mob != null){
            if (miss(mob, 2) && special(mob) || miss(mob, 3))
                dmg = 0;
        }
        WorkingEnemy = null;
        super.damage(dmg, src);
    }
    private boolean special(Char ch){
        return Random.IntRange(
                0, Random.NormalIntRange(
                        Random.Int(
                                0, ch.attackSkill(this)), ch.attackSkill(this)))
                < Random.IntRange(
                0, Random.NormalIntRange(
                        Random.Int(
                                0, defenseSkill(ch)), defenseSkill(ch)));
    }
    private boolean miss(Char enemy, int times){
        boolean miss = Random.Float(enemy.attackSkill(this)) < Random.Float(defenseSkill(enemy));
        if (times <= 0)
            return miss;
        return miss && miss(enemy, times - 1);
    }
    @Override
    public boolean canAttack(Char enemy){
        int distance = Dungeon.level.distance(pos, enemy.pos) ;
        return distance <=3;
    }

    @Override
    public int drRoll() {
        return (Statistics.deepestFloor % 5)*2;
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        for (int i : PathFinder.NEIGHBOURS8()){
            int cell = enemy.pos+i;
            Char ch = Actor.findChar(cell);
            if (ch == null)
                continue;
            if (ch.alignment == alignment || ch instanceof Geminis)
                continue;
            ch.damage(Random.NormalIntRange(0, Dungeon.depth()/5), this);
            if (ch instanceof Mob && twin != null)
                ((Mob) ch).aggro(twin);
        }
        return super.attackProc( enemy, damage );
    }

    @Override
    public int defenseProc(Char enemy, int damage) {
        if (enemy.alignment == this.alignment || enemy instanceof Geminis)
            return 0;
        if (twin == null)
            twin = (Geminis) Actor.findById(twinID);
        if (enemy instanceof Mob && twin != null)
            ((Mob) enemy).aggro(twin);
        return super.defenseProc(enemy, damage);
    }

    @Override
    public float speed() {
        return 1;
    }

}
