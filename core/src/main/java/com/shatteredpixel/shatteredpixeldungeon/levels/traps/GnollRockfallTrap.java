/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGeomancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGuard;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.MiningLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class GnollRockfallTrap extends RockfallTrap {

	@Override
	public void activate() {

		ArrayList<Integer> rockCells = new ArrayList<>();

		//5x5 范围内落石，跳过路障相邻格
		PathFinder.buildDistanceMap( pos, BArray.not( Dungeon.level.solid, null ), 2 );
		for (int i = 0; i < PathFinder.distance().length; i++) {
			if (PathFinder.distance()[i] < Integer.MAX_VALUE) {
				if (Dungeon.level instanceof MiningLevel){
					boolean barricade = false;
					for (int j : PathFinder.NEIGHBOURS9()){
						if (Dungeon.level.map[i+j] == Terrain.BARRICADE){
							barricade = true;
						}
					}
					if (barricade) continue;
				}
				rockCells.add(i);
			}
		}

		boolean seen = false;
		for (int cell : rockCells){

			if (Dungeon.level.heroFOV[ cell ]){
				CellEmitter.get( cell - Dungeon.level.width() ).start(Speck.factory(Speck.ROCK), 0.07f, 10);
				seen = true;
			}

			Char ch = Actor.findChar( cell );

			if (ch != null && ch.isAlive() && !(ch instanceof GnollGeomancer)){
				//mod Trap 无 HazardAssistTracker，删除友军援助追踪

				//伤害比普通落石陷阱低，但无视护甲
				int damage = Random.NormalIntRange(6, 12);
				ch.damage( Math.max(damage, 0) , this);

				//卫士吃满麻痹，其他角色只麻痹一小会
				Buff.prolong(ch, Paralysis.class, ch instanceof GnollGuard ? 10 : 3);

				if (!ch.isAlive() && ch == Dungeon.hero()){
					Dungeon.fail( getClass() );
					GLog.n( Messages.get(this, "ondeath") );
					//mod Trap 无 reclaimed 字段，删除友军魔法击杀徽章判定
				}
			} else if (ch == null
					&& Dungeon.level instanceof MiningLevel
					&& Dungeon.level.traps.get(cell) == null
					&& Dungeon.level.plants.get(cell) == null
					&& Random.Int(2) == 0){
				Level.set( cell, Terrain.MINE_BOULDER );
				GameScene.updateMap(cell);
			}
		}

		if (seen){
			Camera.main.shake(3, 0.7f);
			Sample.INSTANCE.play(Assets.Sounds.ROCKS);
		}

	}

}
