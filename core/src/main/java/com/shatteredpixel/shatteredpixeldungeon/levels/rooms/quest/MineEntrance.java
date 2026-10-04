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

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.CaveRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.triggers.MineExitTrigger;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Image;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class MineEntrance extends CaveRoom {

	@Override
	public float[] sizeCatProbs() {
		return new float[]{1, 0, 0};
	}

	@Override
	public int minWidth() {
		return Math.max(super.minWidth(), 7);
	}

	@Override
	public int minHeight() {
		return Math.max(super.minHeight(), 7);
	}

	@Override
	public boolean isEntrance() {
		return true;
	}

	@Override
	public void paint(Level level) {
		super.paint(level);

		int entrance;
		boolean valid;
		do {
			valid = false;
			entrance = level.pointToCell(random(3));
			for (int i : PathFinder.NEIGHBOURS9()){
				if (level.map[entrance+i] != Terrain.WALL){
					valid = true;
				}
			}
			if (height() == 7 && width() == 7){
				valid = true;
			}
		} while (level.findMob(entrance) != null || !valid);
		Painter.set( level, entrance, Terrain.ENTRANCE );
		//mod 关卡体系没有 LevelTransition，显式记录矿洞入口格（ACCESS 进入时落点也使用它）
		level.entrance = entrance;

		for (int i : PathFinder.NEIGHBOURS8()){
			Painter.set( level, entrance+i, Terrain.EMPTY );
		}

		QuestExit vis = new QuestExit();
		Point e = level.cellToPoint(entrance);
		vis.pos(e.x - 1, e.y - 1);
		level.customTiles.add(vis);

		//mod 用 Trigger 体系：踩入口格经 MineExitTrigger 结算并离开矿洞
		level.placeTrigger(new MineExitTrigger().create(entrance));

		if (Blacksmith.Quest.type() == Blacksmith.Quest.CRYSTAL){
			for (int i = 0; i < width()*height()/2; i ++){
				Point r = random(1);
				if (level.distance(level.pointToCell(r), entrance) > 1
					&& level.map[level.pointToCell(r)] != Terrain.WALL) {
					Painter.set(level, r, Terrain.MINE_CRYSTAL);
				}
			}
		} else if (Blacksmith.Quest.type() == Blacksmith.Quest.GNOLL) {

			//通往非密室的连接有 9/10 概率被实墙封死，否则变成空地
			for (Room n : connected.keySet()){
				if (!(n instanceof SecretRoom) && connected.get(n).type == Door.Type.REGULAR){
					if (Random.Int(10) == 0){
						connected.get(n).set(Door.Type.EMPTY);
					} else {
						connected.get(n).set(Door.Type.WALL);
					}
				}
			}

			ArrayList<Door> doors = new ArrayList<>();
			for (Door d : connected.values()){
				if (d.type == Door.Type.WALL){
					doors.add(d);
				}
			}

			for (Point p : getPoints()){
				int cell = level.pointToCell(p);
				if (level.distance(cell, entrance) > 1 && level.map[cell] == Terrain.EMPTY){
					float dist = 1000;
					for (Door d : doors){
						//mod Point 无静态 distance，内联等价实现
						dist = Math.min(dist, (float)Math.hypot(p.x - d.x, p.y - d.y));
					}
					dist = GameMath.gate(1f, dist-0.5f, 5f);
					float val = Random.Float((float) Math.pow(dist, 2));
					if (val <= 0.75f || dist <= 1) {
						Painter.set(level, cell, Terrain.MINE_BOULDER);
					} else if (val <= 5f && dist <= 3){
						Painter.set(level, cell, Terrain.EMPTY_DECO);
					}
				}
			}

		}
	}

	public static class QuestExit extends CustomTilemap {

		{
			texture = Assets.Environment.CAVES_QUEST;

			tileW = tileH = 3;
		}

		final int TEX_WIDTH = 128;

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			v.map(mapSimpleImage(0, 1, TEX_WIDTH), 3);
			return v;
		}

		@Override
		public String name(int tileX, int tileY) {
			if (tileX == 1 && tileY == 1){
				return Messages.get(this, "name");
			}
			return super.name(tileX, tileY);
		}

		@Override
		public String desc(int tileX, int tileY) {
			if (tileX == 1 && tileY == 1){
				return Messages.get(this, "desc");
			}
			return super.desc(tileX, tileY);
		}

		@Override
		public Image image(int tileX, int tileY) {
			if (tileX == 1 && tileY == 1){
				return super.image(tileX, tileY);
			}
			return null;
		}
	}
}
