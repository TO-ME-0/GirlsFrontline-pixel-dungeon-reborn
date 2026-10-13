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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SMG;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.P90FullAuto;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class P90 extends SubMachineGun {

    {
        BASE_COOLDOWN_TURNS = 100;
    }
    {
        image = ItemSpriteSheet.P90;

        tier = 5;
        tag = Tag.SMG;
        DLY = 0.5f;     //极高的射速
        ACC = 1.2f;     //20%命中加成
        DEF = 2;        //可吸收少量伤害
        DEFUPGRADE = 1;
        dmgUpgradeMul = 0.75F;
        dmgBaseMul = 3; //单发伤害较低，以平衡高射速

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
            if (!isEquipped( hero )) {
                GLog.w(Messages.get(this, "must_hold"));
            }
            //检查是否诅咒
            else if (cursed) {
                GLog.i( Messages.get(this, "curse") );
            }
            //检查是否超力
            else if (hero.STR() < STRReq()) {
                GLog.w(Messages.get(Weapon.class, "too_heav"));
            }
            //检查是否cd
            else if (coolDownLeft > 0) {
                GLog.w(Messages.get(this, "cooldown", coolDownLeft));
            }
            //没有进入上述if，即满足全部要求之后，进入此处执行技能
            else {
                Buff.affect(hero, P90FullAuto.class, P90FullAuto.DURATION);
                // 消耗固定回合
                hero.spendAndNext(Actor.TICK);
                // 设置冷却时间（固定为基础冷却时间，不受天赋影响）
                coolDownLeft = BASE_COOLDOWN_TURNS;
                // 更新快捷栏显示
                updateQuickslot();
            }
        }
    }

    @Override
    public float delayFactor( Char owner ) {
        float delay = super.delayFactor( owner );
        //全弹发射生效期间，攻击速度翻倍
        if (owner instanceof Hero && ((Hero) owner).buff(P90FullAuto.class) != null) {
            delay /= 2;
        }
        return delay;
    }

}
