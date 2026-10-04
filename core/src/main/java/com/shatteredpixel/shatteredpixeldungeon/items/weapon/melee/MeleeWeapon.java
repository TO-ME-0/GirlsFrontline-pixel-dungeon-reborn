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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;


import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Random;

public class MeleeWeapon extends Weapon {

    protected float dmgBaseMul = 5;
    protected float dmgBaseDiffer;
    protected float dmgUpgradeDiffer;
    protected float dmgUpgradeMul = 1F;

	@Override
	public int min(int lvl) {
		return  Math.round(minBaseDmg()) +  //base
                Math.round(minUpgrade(lvl));    //level scaling
	}
    public float minBaseDmg(){
        return tier;
    }
    public float minUpgrade(int lvl){
        return lvl;
    }
	@Override
	public int max(int lvl) {
		return  Math.round(maxBaseDmg())+    //base
				Math.round(maxUpgrade(lvl));   //level scaling
	}
    public float maxBaseDmg(){
        return Math.round(dmgBaseMul * (tier+1 + dmgBaseDiffer));
    }
    public float maxUpgrade(int lvl){
        return lvl* dmgUpgradeMul * (tier+1 + dmgUpgradeDiffer);
    }
	public int STRReq(int lvl){
		return STRReq(tier, lvl);
	}

	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		if (overLoad == OverLoad.OVERLOADING)
			overLoadLeft -= 4;
		return super.proc(attacker, defender, damage);
	}
	@Override
	public int damageRoll(Char owner) {
        int damage = augment.damageFactor(super.damageRoll( owner ));

		if (owner instanceof Hero) {
			int exStr = ((Hero)owner).STR() - STRReq();
			if (exStr > 0) {
				//十三叶草饰品：英雄伤害掷骰可被其影响（实现见 Hero.heroDamageIntRange）
				damage += Hero.heroDamageIntRange( 0, exStr );
			}
		}
		
		return damage;
	}
	
	@Override
	public String info() {

		String info = super.info();

		if (levelKnown) {
			info += "\n\n" + Messages.get(MeleeWeapon.class, "stats_known", tier, augment.damageFactor(min()), augment.damageFactor(max()), STRReq());
			if (STRReq() > Dungeon.hero().STR()) {
				info += " " + Messages.get(Weapon.class, "too_heavy");
			} else if (Dungeon.hero().STR() > STRReq()){
				info += " " + Messages.get(Weapon.class, "excess_str", Dungeon.hero().STR() - STRReq());
			}
		} else {
			int lvl = TextGuessingBuffedLevel();
			info += "\n\n" + Messages.get(MeleeWeapon.class, "stats_unknown", tier, min(lvl), max(lvl), STRReq(false));
			if (STRReq(false) > Dungeon.hero().STR()) {
				info += " " + Messages.get(MeleeWeapon.class, "probably_too_heavy");
			}
		}

		String statsInfo = statsInfo();
		if (!statsInfo.equals("")) info += "\n\n" + statsInfo;

		switch (augment) {
			case SPEED:
				info += " " + Messages.get(Weapon.class, "faster");
				break;
			case DAMAGE:
				info += " " + Messages.get(Weapon.class, "stronger");
				break;
			case NONE:
		}

		if (enchantment != null && (cursedKnown || !enchantment.curse())){
			info += "\n\n" + Messages.get(Weapon.class, "enchanted", enchantment.name());
			if (enchantHardened) info += " " + Messages.get(Weapon.class, "enchant_hardened");
			info += " " + Messages.get(enchantment, "desc");
		} else if (enchantHardened){
			info += "\n\n" + Messages.get(Weapon.class, "hardened_no_enchant");
		}

		if (cursed && isEquipped( Dungeon.hero() )) {
			info += "\n\n" + Messages.get(Weapon.class, "cursed_worn");
		} else if (cursedKnown && cursed) {
			info += "\n\n" + Messages.get(Weapon.class, "cursed");
		} else if (!isIdentified() && cursedKnown){
			info += "\n\n" + Messages.get(Weapon.class, "not_cursed");
		}

        if (overLoad != OverLoad.NONE)
            info += "\n\n" + Messages.get(Item.class, overLoad.name(), overLoadLeft);
		return info;
	}
	
	public String statsInfo(){
        if(DEF>0||DEFUPGRADE>0){
            int REM =0;
            if(Dungeon.hero().STR() < this.STRReq()){
                REM -= 2*(this.STRReq() - Dungeon.hero().STR());
            }
            String defInfo;
            if (isIdentified()){
                defInfo = Messages.get(this, "stats_desc",  Math.max(0,DEF + DEFUPGRADE * buffedLvl() + REM));
            } else{
				int lvl = TextGuessingLevel();
                defInfo = Messages.get(this, "typical_stats_desc", Math.max(0,DEF + DEFUPGRADE * lvl + REM) );
            }
            if(DEFUPGRADE>0) {
                defInfo += "通过升级可以使伤害吸收量增长。";
            }
            return defInfo;
        }
		return Messages.get(this, "stats_desc");
	}
	
	@Override
	public int value() {
		int price = 20 * tier;
		if (hasGoodEnchant()) {
			price *= 1.5;
		}
		if (cursedKnown && (cursed || hasCurseEnchant())) {
			price /= 2;
		}
		if (levelKnown && level() > 0) {
			price *= (level() + 1);
		}
		if (price < 1) {
			price = 1;
		}
		return price;
	}

}
