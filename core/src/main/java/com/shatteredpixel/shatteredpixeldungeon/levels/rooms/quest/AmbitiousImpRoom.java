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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.triggers.VaultEntranceTrigger;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.custom.Carpet;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class AmbitiousImpRoom extends SpecialRoom {

	@Override
	public int maxWidth() { return 9; }
	public int minWidth() { return 9; }
	public int maxHeight() { return 9; }
	public int minHeight() { return 9; }

	@Override
	public void paint(Level level) {

		Painter.fill( level, this, Terrain.WALL_DECO );
		Painter.fill( level, this, 1, Terrain.EMPTY );

		Point c = center();

		Painter.set(level, c.x-2, c.y-2, Terrain.REGION_DECO);
		Painter.set(level, c.x+2, c.y-2, Terrain.REGION_DECO);
		Painter.set(level, c.x-2, c.y+2, Terrain.REGION_DECO);
		Painter.set(level, c.x+2, c.y+2, Terrain.REGION_DECO);

		Painter.set(level, c.x-3, c.y-3, Terrain.WALL_DECO);
		Painter.set(level, c.x+3, c.y-3, Terrain.WALL_DECO);
		Painter.set(level, c.x-3, c.y+3, Terrain.WALL_DECO);
		Painter.set(level, c.x+3, c.y+3, Terrain.WALL_DECO);

		Door entrance = entrance();
		Imp npc = new Imp();
		npc.pos = level.pointToCell(c);

		if (entrance.x == left || entrance.x == right){
			npc.pos += Random.IntRange(-1, 1)*level.width();
			npc.pos += entrance.x == left ? -2 : 2;
		} else if (entrance.y == top || entrance.y == bottom){
			npc.pos += Random.IntRange(-1, 1);
			npc.pos += level.width() * (entrance.y == top ? -2 : 2);
		}
		level.mobs.add( npc );

		Painter.drawInside(level, this, entrance, 1, Terrain.EMPTY);
		entrance.set( Door.Type.REGULAR );

		Painter.fill( level, left+1, top+3, 7, 3, Terrain.CUSTOM_DECO_EMPTY);
		Painter.fill( level, left+3, top+1, 3, 7, Terrain.CUSTOM_DECO_EMPTY);

		Carpet carpet = new Carpet();
		carpet.setRect(left+1, top+3, 7, 3);
		level.customTiles.add(carpet);

		carpet = new Carpet();
		carpet.setRect(left+3, top+1, 3, 7);
		level.customTiles.add(carpet);

		QuestEntrance vis = new QuestEntrance();
		vis.pos(c.x - 2, c.y - 2);
		level.customTiles.add(vis);

		EntranceBarrier vis2 = new EntranceBarrier();
		vis2.pos(c.x - 1, c.y - 1);
		level.customTiles.add(vis2);

		WallBanners vis3 = new WallBanners();
		vis3.pos(left+1, top);
		level.customTerrain.add(vis3);

		int entrancePos = level.pointToCell(c);

		//reborn 不用 LevelTransition 踩踏传送，改用触发器（参照 BlacksmithRoom 的矿洞入口）
		level.placeTrigger(new VaultEntranceTrigger().create(entrancePos));

	}

	@Override
	public boolean canPlaceCharacter(Point p, Level l) {
		return false;
	}

	@Override
	public boolean canPlaceTrap(Point p) {
		return false;
	}

	//reborn 的 Point 无静态 distance，本地补欧氏距离（与 4.0 Point.distance 一致）
	private static float pointDist(Point a, Point b){
		float dx = a.x - b.x;
		float dy = a.y - b.y;
		return (float)Math.sqrt(dx*dx + dy*dy);
	}

	@Override
	public boolean canPlaceGrass(Point p) {
		return pointDist(p, center()) >= 5;
	}

	@Override
	public boolean canPlaceWater(Point p) {
		return pointDist(p, center()) >= 5;
	}

	public static class QuestEntrance extends CustomTilemap {

		{
			texture = Assets.Environment.CITY_QUEST;

			tileW = tileH = 5;
		}

		//贴图已拉伸为 384x384（24px 地块）
		final int TEX_WIDTH = 384;

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			v.map(mapSimpleImage(0, 0, TEX_WIDTH), 5);
			return v;
		}

		@Override
		public String name(int tileX, int tileY) {
			return Messages.get(this, "name");
		}

		@Override
		public String desc(int tileX, int tileY) {
			return Messages.get(this, "desc");
		}

		@Override
		public Image image(int tileX, int tileY) {
			//角落图块不提供自定义查看图像
			if ((tileX == 0 || tileX == tileW-1)
					&& (tileY == 0 || tileY == tileH-1)){
				return null;
			} else {
				return super.image(tileX, tileY);
			}
		}
	}

	public static class WallBanners extends CustomTilemap {
		{
			texture = Assets.Environment.CITY_QUEST;

			tileW = 7;
			tileH = 3;
		}

		private final int BANNER_1 = 80;
		private final int BANNER_2 = 81;
		private final int BANNER__BOTTOM = 82;

		@Override
		public void pos(int pos) {
			super.pos(pos);
		}

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			int[] data = new int[tileW*tileH];
			//至多五面旗帜，只要不是门的位置就放
			int cell = tileX + Dungeon.level.width()*tileY;

			if (!Dungeon.level.passable[cell+1]){
				data[1] = BANNER_1 + Random.Int(2);
				data[1+tileW] = BANNER__BOTTOM;
			}

			if (!Dungeon.level.passable[cell+3]) {
				data[3] = BANNER_1 + Random.Int(2);
				if (Dungeon.level.map[cell+3+Dungeon.level.width()] != Terrain.PEDESTAL) {
					data[3 + tileW] = BANNER__BOTTOM;
				}
			}

			if (!Dungeon.level.passable[cell+5]) {
				data[5] = BANNER_1 + Random.Int(2);
				data[5 + tileW] = BANNER__BOTTOM;
			}

			cell += Dungeon.level.width();

			if (!Dungeon.level.passable[cell]) {
				data[7] = BANNER_1 + Random.Int(2);
				data[7 + tileW] = BANNER__BOTTOM;
			}

			if (!Dungeon.level.passable[cell+6]) {
				data[13] = BANNER_1 + Random.Int(2);
				data[13 + tileW] = BANNER__BOTTOM;
			}

			v.map( data, tileW );
			return v;
		}

		@Override
		public Image image(int tileX, int tileY) {
			return null;
		}
	}

	public static class EntranceBarrier extends CustomTilemap {
		{
			texture = Assets.Environment.CITY_QUEST;

			tileW = tileH = 3;
		}

		//贴图已拉伸为 384x384（24px 地块）
		final int TEX_WIDTH = 384;

		@Override
		public Tilemap create() {
			//基本是父类方法的复制，以便在 update 中改变透明度
			if (vis != null && vis.alive) vis.killAndErase();
			vis = new Tilemap(texture, new TextureFilm( texture, SIZE, SIZE )){
				@Override
				protected NoosaScript script() {
					//允许自定义贴图受光照影响
					return NoosaScript.get();
				}

				@Override
				public void update() {
					alpha(0.3f + 0.3f*(float)Math.sin(Game.timeTotal));
					super.update();
				}
			};
			vis.x = tileX*SIZE;
			vis.y = tileY*SIZE;
			vis.map(mapSimpleImage(5, 1, TEX_WIDTH), 3);
			return vis;
		}

		@Override
		public Image image(int tileX, int tileY) {
			return null;
		}
	}
}
