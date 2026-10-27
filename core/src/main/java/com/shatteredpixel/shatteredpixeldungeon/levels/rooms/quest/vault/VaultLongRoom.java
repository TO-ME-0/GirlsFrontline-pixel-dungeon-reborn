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

import com.watabou.utils.Random;

public abstract class VaultLongRoom extends VaultRoom {

	//仅在初始化阶段使用，之后直接看宽高本身
	private boolean wide = Random.Int(2) == 0;

	protected boolean wide(){
		if (width() == height()){
			return wide;
		} else {
			return width() > height();
		}
	}

	@Override
	public int minWidth() {
		return wide() ? 21 : 11;
	}

	@Override
	public int maxWidth() {
		return minWidth();
	}

	@Override
	public int minHeight() {
		return wide() ? 11: 21;
	}

	@Override
	public int maxHeight() {
		return minHeight();
	}

	@Override
	public int sizeFactor() {
		return 2;
	}

}
