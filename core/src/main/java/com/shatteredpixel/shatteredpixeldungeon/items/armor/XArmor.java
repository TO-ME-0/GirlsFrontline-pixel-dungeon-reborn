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

package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.utils.Random;

//X系列外骨骼基类：与同阶IOP外骨骼相比抵挡减半，但闪避会额外判定
public abstract class XArmor extends Armor {

	public XArmor( int tier ) {
		super( tier );
	}

	@Override
	public int DRMax(int lvl){
		if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
			return super.DRMax(lvl);
		}
		//初始抵挡上限为同阶护甲的一半（同阶为tier*2，此处为tier）
		//每级成长为 阶级*0.8，四舍五入取整（同阶成长为tier）
		return Math.max(0, tier + Math.round(tier * 0.8f) * lvl + augment.defenseFactor(lvl));
	}

	@Override
	public int DRMin(int lvl){
		if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
			return super.DRMin(lvl);
		}
		int max = DRMax(lvl);
		if (lvl >= max){
			return (lvl - max);
		} else {
			//抵挡下限的每级成长同样减半（同阶为每级+1，此处每级+0.5向下取整）
			return lvl / 2;
		}
	}

	//基础比同阶护甲多1次闪避判定，每升5级再多1次
	public int extraEvasionRolls(){
		return 1 + buffedLvl() / 5;
	}

	//在Char.hit()中调用：额外进行若干次闪避判定，取对防守方最有利的结果
	//所有修正系数均为正数乘区，先取最大值再乘系数与逐次乘系数等价
	public static float extraEvasionRolls( Char defender, float defRoll, float defStat ){
		if (defender instanceof Hero){
			Armor armor = ((Hero) defender).belongings.armor();
			if (armor instanceof XArmor){
				int extra = ((XArmor) armor).extraEvasionRolls();
				for (int i = 0; i < extra; i++){
					defRoll = Math.max(defRoll, Random.Float(defStat));
				}
			}
		}
		return defRoll;
	}
}
