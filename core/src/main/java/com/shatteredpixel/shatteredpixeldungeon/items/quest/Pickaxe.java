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

package com.shatteredpixel.shatteredpixeldungeon.items.quest;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bat;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.MiningLevel;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class Pickaxe extends MeleeWeapon {

	private static final Glowing BLOODY = new Glowing( 0x550000 );

	{
		image = ItemSpriteSheet.PICKAXE;

		levelKnown = true;
		tier = 3;
		unique = true;
		bones = false;

		//3.3.8 起挖矿改为矿洞内点击岩壁（见 Hero.actMine），不再有物品按钮动作
	}

	public boolean bloodStained = false;

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		//矿洞内不能丢弃或投掷镐，避免把关键道具弄丢
		if (Dungeon.level instanceof MiningLevel){
			actions.remove( AC_DROP );
			actions.remove( AC_THROW );
		}
		return actions;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		if (!bloodStained && defender instanceof Bat) {
			Actor.add(new Actor() {

				{
					actPriority = VFX_PRIO;
				}

				@Override
				protected boolean act() {
					if (!defender.isAlive()){
						bloodStained = true;
						updateQuickslot();
					}

					Actor.remove(this);
					return true;
				}
			});
		}
		return damage;
	}

	private static final String BLOODSTAINED = "bloodStained";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );

		bundle.put( BLOODSTAINED, bloodStained );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );

		bloodStained = bundle.getBoolean( BLOODSTAINED );
	}

	@Override
	public Glowing glowing() {
		return bloodStained ? BLOODY : null;
	}

}
