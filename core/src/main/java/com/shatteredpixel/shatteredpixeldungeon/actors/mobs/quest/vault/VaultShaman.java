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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DwarfToken;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.NecromancerSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

//reborn 无 Shaman 基类，此处将 4.0 Shaman 的逻辑直接内联进本类
public class VaultShaman extends Mob implements Callback {

	{
		//reborn 无 ShamanSprite，以 NecromancerSprite 代替（zap 视觉退化为普通 zap 动画）
		spriteClass = NecromancerSprite.class;

		HP = HT = 35;
		defenseSkill = 18;

		maxLvl = 30;
		EXP = 0;
		loot = DwarfToken.class;
		lootChance = 1;
	}

	@Override
	public int attackSkill( Char target ) {
		return 25;
	}

	@Override
	public float lootChance() {
		return 1;
	}

	@Override
	public Item createLoot() {
		return new DwarfToken();
	}

	int type = Random.Int(5);

	protected void debuff( Char enemy ) {
		switch (type){
			case 0: case 1:
				Buff.prolong( enemy, Weakness.class, Weakness.DURATION );
				break;
			case 2: case 3:
				Buff.prolong( enemy, Vulnerable.class, Vulnerable.DURATION );
				break;
			case 4:
				Buff.prolong( enemy, Hex.class, Hex.DURATION );
				break;
		}
	}

	@Override
	public int damageRoll() {
		//提升近战伤害至蛮兽（无狂暴）水平，因为萨满的近战本就偏弱
		return Random.NormalIntRange( 5, 25 );
	}

	@Override
	public int drRoll() {
		//提升 DR 以抵消英雄高 HP 和溢出力量带来的额外伤害
		return super.drRoll() + 5;
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		return super.canAttack(enemy)
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
	}

	@Override
	protected boolean doAttack(Char enemy ) {

		if (Dungeon.level.adjacent( pos, enemy.pos )
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {

			return super.doAttack( enemy );

		} else {

			//与 DM100 一致的可靠模式：先在 doAttack 里结算伤害，zap 动画仅为视觉；
			//通过 CharSprite.zap(cell, callback) 保证动画结束后回调 call()，
			//不依赖具体 sprite 是否回调 onZapComplete（NecromancerSprite 不会回调本类）
			zap();

			if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
				sprite.zap( enemy.pos, this );
				return false;
			} else {
				return true;
			}
		}
	}

	//用于让抗性判定区分近战与魔法攻击
	public static class EarthenBolt{}

	private void zap() {
		spend( 1f );

		//驱散自身隐身（reborn 的 Invisibility.dispel 仅作用于英雄，改为直接 detach）
		Buff.detach(this, Invisibility.class);
		Char enemy = this.enemy;
		if (hit( this, enemy, true )) {

			if (Random.Int( 2 ) == 0) {
				debuff( enemy );
				if (enemy == Dungeon.hero()) Sample.INSTANCE.play( Assets.Sounds.DEBUFF );
			}

			int dmg = Random.NormalIntRange( 6, 15 );
			enemy.damage( dmg, new EarthenBolt() );

			if (!enemy.isAlive() && enemy == Dungeon.hero()) {
				Dungeon.fail( getClass() );
				GLog.n( Messages.get(Char.class, "kill", name()) );
			}
		} else {
			enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
		}
	}

	public void onZapComplete() {
		zap();
		next();
	}

	@Override
	public void call() {
		next();
	}

	public static final String TYPE = "type";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TYPE, type);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		type = bundle.getInt(TYPE);
	}
}
