/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2018 Evan Debenham
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.DMR;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FieldRation;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class M16 extends DesignatedMarksmanRifle {

    {
        BASE_COOLDOWN_TURNS = 100;
    }

	{
		image = ItemSpriteSheet.M16;

		tier = 2;
		tag = Tag.AR;
        DEF = 3;
        DEFUPGRADE = 1;
        dmgBaseMul = 2;
        dmgBaseDiffer = 1;
        dmgUpgradeMul = 0.8F;
        defaultAction = AC_SKILL;
	}

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_SKILL );
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {

        super.execute(hero, action);
        if (action.equals(AC_SKILL)) {
            //检查是否装备，复制的TimekeepersHourglass
            if (!isEquipped(hero)) {
                GLog.w(Messages.get(this, "must_hold"));
            }
            //检查是否有毒，同上
            else if (cursed) {
                GLog.i(Messages.get(this, "curse"));
            }
            //检查是否超力，灵刀
            else if (hero.STR() < STRReq()) {
                GLog.w(Messages.get(Weapon.class, "too_heav"));
            }
            //检查是否cd，灵刀
            else if (coolDownLeft > 0) {
                GLog.w(Messages.get(this, "cooldown", coolDownLeft));
            }
            //没有进入上述if，即满足全部要求之后，进入此处执行技能
            else {
                Buff.affect(hero, FieldRation.class, FieldRation.DURATION);
                // 消耗固定回合
                hero.spendAndNext(Actor.TICK);
                // 设置冷却时间（固定为基础冷却时间，不受天赋影响）
                coolDownLeft = BASE_COOLDOWN_TURNS;
                // 更新快捷栏显示
                updateQuickslot();
            }
        }
    }

}
