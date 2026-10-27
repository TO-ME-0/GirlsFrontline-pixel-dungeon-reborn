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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.quest.vault;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DwarfToken;
import com.shatteredpixel.shatteredpixeldungeon.sprites.UndeadSprite;
import com.watabou.utils.Random;

//reborn 无 Ghoul 基类，此处内联 4.0 Ghoul 的基础属性；
//不内联 partner/生命链接机制，本类不会生成或跟随同伴
// 但仍会与附近的食尸鬼结伴，以模拟食尸鬼同伴共享仇恨的生存行为
public class VaultGhoul extends Mob {

	{
		//reborn 无 GhoulSprite，以 UndeadSprite 代替
		spriteClass = UndeadSprite.class;

		HP = HT = 45;
		defenseSkill = 20;

		maxLvl = 30;
		EXP = 0;
		loot = DwarfToken.class;
		lootChance = 1;

		properties.add(Property.UNDEAD);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 16, 22 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 24;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 4);
	}

	@Override
	protected boolean act() {
		if (state == WANDERING || state == SLEEPING){
			//寻找附近的食尸鬼并被它们的目标吸引，
			// 以模拟食尸鬼同伴共享仇恨的正常行为
			for (Mob m : Dungeon.level.mobs){
				if (m instanceof VaultGhoul
						&& Dungeon.level.distance(pos, m.pos) < 4
						&& m.state == m.HUNTING){ //reborn 无 INVESTIGATING 状态，仅检查 HUNTING
					beckon(m.pos);
					break;
				}
			}
		}
		return super.act();
	}
}
