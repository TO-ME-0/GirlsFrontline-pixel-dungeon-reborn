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

package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.GameMath;
import com.watabou.utils.Random;

public class CrackedSpyglass extends Trinket{

	{
//		image = ItemSpriteSheet.SPYGLASS;
		image = ItemSprite.itemSpriteNeedDraw;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	//额外生成物资的固定数量，等级不再改变它，只改变这些物资的初始清晰度
	public static final int EXTRA_LOOT = 4;

	@Override
	public String statsDesc() {
		if (isIdentified()){
			return Messages.get(this, "stats_desc", Messages.decimalFormat("#.##", 100 * maxOpacity(buffedLvl())));
		} else {
			return Messages.get(this, "typical_stats_desc", Messages.decimalFormat("#.##", 100 * maxOpacity(0)));
		}
	}

	public static float extraLootChance(){
		return extraLootChance(trinketLevel(CrackedSpyglass.class));
	}

	public static float extraLootChance(int level ){
		if (level <= -1){
			return 0;
		} else {
			return EXTRA_LOOT;
		}
	}

	//物资的初始清晰度上限（0/1/2/3 级 → 30%/50%/70%/90%），opacity() 会在 0~该上限之间随机取值
	public static float maxOpacity( int level ){
		if (level <= -1)
			return 1F;
		return Math.min(1F, 0.3F + level * 0.2F);
	}

	public static float opacity() {
		return opacity(trinketLevel(CrackedSpyglass.class));
	}

	public static float opacity( int level ) {
		float opacity = Random.NormalFloat(2);
		if (level <= -1)
			return 1F;
		return opacity * (0.3F + level * 0.2F);
	}

}
