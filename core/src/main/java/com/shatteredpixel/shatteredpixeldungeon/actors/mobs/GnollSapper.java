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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollSapperSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class GnollSapper extends Mob {

	{
		//总是在卫士之后行动，方便风筝它们
		actPriority = Actor.MOB_PRIO-1;

		spriteClass = GnollSapperSprite.class;

		HP = HT = 45;
		defenseSkill = 15;

		EXP = 10;
		maxLvl = -2;

		properties.add(Property.MINIBOSS);

		HUNTING = new Hunting();
		WANDERING = new Wandering();
		state = SLEEPING;
	}

	public int spawnPos;
	private int partnerID = -1;

	private int abilityCooldown = Random.NormalIntRange(4, 6);
	private boolean lastAbilityWasRockfall = false;

	public int throwingRockFromPos = -1;
	public int throwingRockToPos = -1;

	public void linkPartner(Char c){
		losePartner();
		partnerID = c.id();
		if (c instanceof GnollGuard) {
			((GnollGuard) c).linkSapper(this);
		} else if (c instanceof GnollGeomancer){
			((GnollGeomancer) c).linkSapper(this);
		}
	}

	public void losePartner(){
		if (partnerID != -1){
			if (Actor.findById(partnerID) instanceof GnollGuard) {
				((GnollGuard) Actor.findById(partnerID)).loseSapper();
			} else if (Actor.findById(partnerID) instanceof GnollGeomancer) {
				((GnollGeomancer) Actor.findById(partnerID)).loseSapper();
			}
			partnerID = -1;
		}
	}

	public Actor getPartner(){
		return Actor.findById(partnerID);
	}

	@Override
	public void die(Object cause) {
		super.die(cause);
		losePartner();
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 6 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 18;
	}

	@Override
	public void damage(int dmg, Object src) {
		super.damage(dmg, src);
		abilityCooldown -= dmg/10f;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 6);
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public float spawningWeight() {
		return 0;
	}

	@Override
	protected boolean act() {
		if (throwingRockFromPos != -1){

			boolean attacked = Dungeon.level.map[throwingRockFromPos] == Terrain.MINE_BOULDER;

			if (attacked) {
				GnollGeomancer.doRockThrowAttack(this, throwingRockFromPos, throwingRockToPos);
			}

			throwingRockFromPos = -1;
			throwingRockToPos = -1;

			spend(TICK);
			return !attacked;
		} else {
			return super.act();
		}

	}

	public class Hunting extends Mob.Hunting {
		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (!enemyInFOV) {
				if (Dungeon.level.distance(spawnPos, target) > 3){
					//不追击离出生点太远的目标
					target = pos;
				}
				return super.act(enemyInFOV, justAlerted);
			} else {
				enemySeen = true;

				if (getPartner() != null
						&& getPartner() instanceof Mob
						&& ((Mob) getPartner()).alignment != alignment){
					losePartner();
				}

				if (Actor.findById(partnerID) != null
						&& Dungeon.level.distance(pos, enemy.pos) <= 3){
					Mob partner = (Mob) Actor.findById(partnerID);
					if (partner.state == partner.SLEEPING){
						partner.notice();
					}
					if (enemy != partner) {
						partner.target = enemy.pos;
						partner.aggro(enemy);
					}
				}

				if (abilityCooldown-- <= 0){
					boolean targetNextToBarricade = false;
					for (int i : PathFinder.NEIGHBOURS8()){
						if (Dungeon.level.map[enemy.pos+i] == Terrain.BARRICADE
							|| Dungeon.level.map[enemy.pos+i] == Terrain.ENTRANCE){
							targetNextToBarricade = true;
							break;
						}
					}

					// 50/50 掷石或落石，不会连续两次落石
					// 目标贴着路障时总是尝试掷石；无石可掷时总是落石
					Ballistica aim = GnollGeomancer.prepRockThrowAttack(enemy, GnollSapper.this);
					if (aim != null && (targetNextToBarricade || lastAbilityWasRockfall || Random.Int(2) == 0)) {

						lastAbilityWasRockfall = false;
						throwingRockFromPos = aim.sourcePos;
						throwingRockToPos = aim.collisionPos;

						Ballistica warnPath = new Ballistica(aim.sourcePos, aim.collisionPos, Ballistica.STOP_SOLID);
						for (int i : warnPath.subPath(0, warnPath.dist)){
							sprite.parent.add(new TargetedCell(i, 0xFF0000));
						}

						Dungeon.hero().interrupt();
						abilityCooldown = Random.NormalIntRange(4, 6);
						//mod Char 无 cooldown()，蓄力固定 1 回合
						spend(TICK);
						return true;
					} else if (GnollGeomancer.prepRockFallAttack(enemy, GnollSapper.this, 2, true)) {
						lastAbilityWasRockfall = true;
						Dungeon.hero().interrupt();
						spend(TICK);
						abilityCooldown = Random.NormalIntRange(4, 6);
						return true;
					}
				}

				//看到敌人也不会主动靠近，但在攻击距离内会近战
				if (canAttack(enemy)){
					return super.act(enemyInFOV, justAlerted);
				} else {
					spend(TICK);
					return true;
				}
			}
		}
	}

	//mod Wandering 没有 randomDestination 钩子，覆写 continueWandering 使其只在出生点附近徘徊
	public class Wandering extends Mob.Wandering {
		@Override
		protected boolean continueWandering() {
			enemySeen = false;

			int oldPos = pos;
			target = spawnPos;
			if (target != -1 && getCloser( target )) {
				spendMove( 1 / speed() );
				return moveSprite( oldPos, pos );
			} else {
				spendMove( TICK );
			}

			return true;
		}
	}

	private static final String SPAWN_POS = "spawn_pos";
	private static final String PARTNER_ID = "partner_id";

	private static final String ABILITY_COOLDOWN = "ability_cooldown";
	private static final String LAST_ABILITY_WAS_ROCKFALL = "last_ability_was_rockfall";

	private static final String ROCK_FROM_POS = "rock_from_pos";
	private static final String ROCK_TO_POS = "rock_to_pos";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(PARTNER_ID, partnerID);
		bundle.put(SPAWN_POS, spawnPos);

		bundle.put(ABILITY_COOLDOWN, abilityCooldown);
		bundle.put(LAST_ABILITY_WAS_ROCKFALL, lastAbilityWasRockfall);

		bundle.put(ROCK_FROM_POS, throwingRockFromPos);
		bundle.put(ROCK_TO_POS, throwingRockToPos);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		partnerID = bundle.getInt(PARTNER_ID);
		spawnPos = bundle.getInt(SPAWN_POS);

		abilityCooldown = bundle.getInt(ABILITY_COOLDOWN);
		lastAbilityWasRockfall = bundle.getBoolean(LAST_ABILITY_WAS_ROCKFALL);

		throwingRockFromPos = bundle.getInt(ROCK_FROM_POS);
		throwingRockToPos = bundle.getInt(ROCK_TO_POS);
	}
}
