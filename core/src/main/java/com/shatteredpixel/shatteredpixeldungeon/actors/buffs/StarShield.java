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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MedicalKit;
import com.watabou.utils.Random;

import java.util.Locale;

public class StarShield extends ShieldBuff {
	
	{
		type = buffType.POSITIVE;
	}

	private int turnsPassed = 0;

	@Override
	public void incShield(int addAmount) {
		//非GSH18角色上限30点；GSH18角色上限500点；GSH18转职行走手术台后无上限
		int curAmount = shielding();
		int newAmount = addAmount + curAmount;
		if(!(target instanceof Hero) || ((Hero)target).heroClass!=HeroClass.GSH18){
			if(newAmount > 30){
				newAmount = 30;
			}
		}else if(((Hero)target).subClass != HeroSubClass.MOBILE_MEDICALTABLE){
			if(newAmount > 500){
				newAmount = 500;
			}
		}

		int incAmount = newAmount - curAmount;
		if(incAmount <= 0){
			return;
		}
		
		int baseAmount = curAmount>30?curAmount:30;
		int overShielding = newAmount - baseAmount;
		if(overShielding > 0){
			Healing healing = target.buff(Healing.class);
			if(healing == null){
				Buff.affect(target, Healing.class).setHeal(overShielding, 0.0f, 1);
			}else{
				healing.increaseHeal(overShielding);
			}
		}

		super.incShield(incAmount);

		// 便携医疗包：每 1 点星之护盾充能 1%
		if (target instanceof Hero){
			Hero hero = (Hero)target;
			MedicalKit.KitRecharge medkitBuff = hero.buff(MedicalKit.KitRecharge.class);
			if (medkitBuff != null){
				// 已装备：正常充能
				medkitBuff.gainCharge(incAmount);
			} else if (hero.hasTalent(Talent.GSH18_FIRST_AID_KIT)){
				// 未装备：急救药箱天赋按概率充能（+1=35%、+2=70%、+3=100%）
				MedicalKit kit = hero.belongings.getItem(MedicalKit.class);
				if (kit != null){
					float prob = Math.min(1f, 0.35f * hero.pointsInTalent(Talent.GSH18_FIRST_AID_KIT));
					if (Random.Float() < prob){
						kit.chargeFromInventory(hero, incAmount);
					}
				}
			}
		}
	}

	@Override
	public boolean act() {
		if (shielding() <= 0) {
			detach();
		} else {
			int heroLevel = (target instanceof Hero) ? ((Hero)target).lvl : 0;

			//分档周期衰减：<10层每5回合1点；10~30层每3回合1点；>30层每2回合1点
			turnsPassed++;
			int interval = shielding() < 10 ? 5 : (shielding() <= 30 ? 3 : 2);
			if (turnsPassed >= interval && shielding() > 0) {
				decShield(1);
				turnsPassed = 0;
			}

			//护盾量超过 10×角色等级 层数时，每回合额外衰减1点（与分档衰减叠加，互不重置计数）
			if (shielding() > 10 * heroLevel) {
				decShield(Math.min(1, shielding()));
			}

			//护盾被本回合衰减清空时立即移除，避免以0值多挂一回合
			if (shielding() <= 0) {
				detach();
			}
		}

		spend(TICK);
		return true;
	}
	
	@Override
	public void fx(boolean on) {
		if (on) target.sprite.add(CharSprite.State.SHIELDED);
		else target.sprite.remove(CharSprite.State.SHIELDED);
	}
	
	@Override
	public int icon() {
		return BuffIndicator.STAR_SHIELD;
	}
	
	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(1f, 1f, 1f); //白色
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(shielding());
	}
	
	@Override
	public String toString() {
		return Messages.get(this, "name");
	}
	
	@Override
	public String desc() {
		return Messages.get(this, "desc", shielding(), turnDecayText());
	}

	//当前每回合实际衰减量：分档周期衰减 + 超限（>10×角色等级）每回合额外1点
	private float turnDecay() {
		int s = shielding();
		float tier = s < 10 ? 1f / 5 : (s <= 30 ? 1f / 3 : 1f / 2);
		int heroLevel = (target instanceof Hero) ? ((Hero)target).lvl : 0;
		if (s > 10 * heroLevel) {
			tier += 1;
		}
		return tier;
	}

	//衰减量可能带小数（如0.33、2.5），整数或末位为0时去掉多余的0
	private String turnDecayText() {
		float decay = turnDecay();
		if (decay == Math.floor(decay)) {
			return String.valueOf((int) decay);
		}
		String txt = String.format(Locale.ROOT, "%.2f", decay);
		if (txt.endsWith("0")) {
			txt = txt.substring(0, txt.length() - 1);
		}
		return txt;
	}

	private static final String TURNS_PASSED = "turns_passed";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TURNS_PASSED, turnsPassed);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		turnsPassed = bundle.getInt(TURNS_PASSED);
	}
}