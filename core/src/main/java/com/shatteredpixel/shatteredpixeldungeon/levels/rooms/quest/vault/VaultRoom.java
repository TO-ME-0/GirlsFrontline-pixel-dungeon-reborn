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

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.vault;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public abstract class VaultRoom extends StandardRoom {

	//总是“大”房间，内部固定为 9x9 的有效区域

	@Override
	public float[] sizeCatProbs() {
		return new float[]{0, 1, 0};
	}

	@Override
	public int minHeight() { return 11; }
	public int maxHeight() { return 11; }

	@Override
	public int minWidth() { return 11; }
	public int maxWidth() { return 11; }

	// 默认不允许合并，也许以后可以在特定情况下放开？
	@Override
	public boolean canMerge(Level l, Point p, int mergeTerrain) {
		return false;
	}

	//reborn 的 StandardRoom 无 sizeFactor 方法，此处为普通方法，供 VaultLevel.initRooms 直接调用
	public int sizeFactor() {
		return 1;
	}

	private static ArrayList<Class<?extends VaultRoom>> rooms = new ArrayList<>();
	static {
		rooms.add(VaultRingRoom.class);
		rooms.add(VaultCrossRoom.class);
		rooms.add(VaultQuadrantsRoom.class);
		rooms.add(VaultRingsRoom.class);
		rooms.add(VaultEnemyCenterRoom.class);

		rooms.add(VaultHallwayRoom.class);
		rooms.add(VaultLongRingsRoom.class);
		//代币房是必然生成的，在 VaultLevel 中单独添加

		rooms.add(VaultCircleRoom.class);
		rooms.add(VaultAlternatingFireRoom.class);
		rooms.add(VaultLasersRoom.class);
		// 一个简单的敌人宝藏房也是必然生成的
	}

	private static float[] chances = new float[0];

	public static void setupChances(){
		chances = new float[]{2,2,2,2,2, 1,1, 1,1,1};
	}

	public static VaultRoom createRoom(){
		int idx = Random.chances(chances);
		if (idx == -1){
			setupChances();
			idx = Random.chances(chances);
		}
		chances[idx]--;
		return Reflection.newInstance(rooms.get(idx));
	}

}
