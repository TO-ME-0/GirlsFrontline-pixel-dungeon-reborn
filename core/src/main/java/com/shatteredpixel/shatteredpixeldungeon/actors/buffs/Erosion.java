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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

/**
 * HK416（寄生榴弹转职）专属减益：侵蚀
 *
 * 设计文档要求：
 * - 拥有侵蚀的单位每回合固定受到 [层数*楼层*0.5] 伤害
 * - 拥有侵蚀的单位死亡时造成爆炸：范围3*3，伤害 [12+楼层*1.5]，受护甲减免
 * - 每3回合减少一层
 * - 该buff不会对友军单位和玩家生效
 * - T3-3A 侵蚀继承：被自爆命中的敌人继承 1/3、1/2 或全部层数（可叠加）
 * - T3-5A（未命名天赋）：被附加侵蚀的敌人命中降低 10%/20%/30%
 */
public class Erosion extends Buff {

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	private int stacks = 1;
	private int decayCounter = 0;

	private static final String STACKS = "stacks";
	private static final String DECAY_COUNTER = "decay_counter";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(STACKS, stacks);
		bundle.put(DECAY_COUNTER, decayCounter);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		stacks = bundle.getInt(STACKS);
		decayCounter = bundle.getInt(DECAY_COUNTER);
	}

	public int stacks() {
		return stacks;
	}

	/** 叠加侵蚀层数 */
	public void addStacks(int amount) {
		if (amount <= 0) return;
		stacks += amount;
	}

	@Override
	public int icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public float iconFadePercent() {
		return 0f;
	}

	@Override
	public String toString() {
		return Messages.get(this, "name", stacks);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", stacks, erodedDamage(), explosionDamage());
	}

	/** 每回合侵蚀伤害：层数*楼层*0.5 */
	public int erodedDamage() {
		return Math.max(1, Math.round(stacks * Dungeon.depth() * 0.5f));
	}

	/** 死亡自爆伤害：12+楼层*1.5 */
	public static int explosionDamage() {
		return Math.round(12 + Dungeon.depth() * 1.5f);
	}

	@Override
	public boolean act() {
		//不会对友军单位和玩家生效
		if (target instanceof Hero || target.alignment == Char.Alignment.ALLY) {
			detach();
			return true;
		}

		//每3回合减少一层
		decayCounter++;
		if (decayCounter >= 3) {
			decayCounter = 0;
			stacks--;
			if (stacks <= 0) {
				detach();
				return true;
			}
		}

		//每回合固定伤害
		int dmg = erodedDamage();
		if (dmg > 0 && target.isAlive()) {
			target.damage(dmg, this);
		}

		spend(TICK);
		return true;
	}

	/**
	 * 死亡自爆：在目标死亡时触发（由 Char.die 统一调用）。
	 * 爆炸范围3*3，伤害 12+楼层*1.5，受护甲减免；
	 * T3-3A 侵蚀继承：被自爆命中的敌人继承部分层数。
	 */
	public static void onDeath(Char ch) {
		Erosion erosion = ch.buff(Erosion.class);
		if (erosion == null) return;

		int stacks = erosion.stacks;

		//爆炸特效
		if (Dungeon.level.heroFOV[ch.pos]) {
			CellEmitter.center(ch.pos).burst(BlastParticle.FACTORY, 20);
			for (int n : PathFinder.NEIGHBOURS9()) {
				int c = ch.pos + n;
				if (c >= 0 && c < Dungeon.level.length() && Dungeon.level.heroFOV[c]) {
					CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4);
				}
			}
		}

		//对3*3范围内的单位造成伤害（每个敌人单独计算）
		int inheritedPoints = Dungeon.hero() != null ? Dungeon.hero().pointsInTalent(Talent.HK416_EROSION_INHERIT) : 0;

		for (int n : PathFinder.NEIGHBOURS9()) {
			int c = ch.pos + n;
			if (c < 0 || c >= Dungeon.level.length()) continue;
			Char hit = Actor.findChar(c);
			if (hit == null || !hit.isAlive()) continue;
			//不会对友军单位和玩家生效
			if (hit instanceof Hero || hit.alignment == Char.Alignment.ALLY) continue;

			int dmg = explosionDamage() - hit.drRoll();
			if (dmg > 0) {
				hit.damage(dmg, ch);
			}

			//T3-3A 侵蚀继承：被自爆命中的敌人继承层数（可叠加）
			if (inheritedPoints > 0 && hit.isAlive() && hit.alignment != Char.Alignment.ALLY) {
				int inherited;
				switch (inheritedPoints) {
					case 1: default:
						inherited = stacks / 3; //继承1/3层数
						break;
					case 2:
						inherited = stacks / 2; //继承1/2层数
						break;
					case 3:
						inherited = stacks;     //继承全部层数
						break;
				}
				if (inherited > 0) {
					Erosion e = Buff.affect(hit, Erosion.class);
					e.addStacks(inherited);
				}
			}
		}
	}

	/** T3-5A：被附加侵蚀的敌人命中降低 10%/20%/30%（由 Char.attackSkill 调用） */
	public static float attackSkillFactor(Char ch) {
		Erosion erosion = ch.buff(Erosion.class);
		if (erosion == null || Dungeon.hero() == null) return 1f;
		int pts = Dungeon.hero().pointsInTalent(Talent.HK416_EROSION_WEAKEN);
		if (pts <= 0) return 1f;
		return 1f - 0.1f * pts;
	}
}
