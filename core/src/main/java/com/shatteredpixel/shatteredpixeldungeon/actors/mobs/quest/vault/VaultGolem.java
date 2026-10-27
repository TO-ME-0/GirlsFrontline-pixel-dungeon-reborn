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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DwarfToken;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ArmoredStatueSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

//reborn 无 Golem 基类，此处将 4.0 Golem 的逻辑直接内联进本类
//由于游荡 AI 状态的改动，目前不会传送自身，只会传送敌人
public class VaultGolem extends Mob {

	{
		//reborn 无 GolemSprite，以 ArmoredStatueSprite 代替
		spriteClass = ArmoredStatueSprite.class;

		HP = HT = 120;
		defenseSkill = 15;

		maxLvl = 30;
		EXP = 0;
		loot = DwarfToken.class;
		lootChance = 1;

		properties.add(Property.INORGANIC);
		properties.add(Property.LARGE);

		HUNTING = new Hunting();
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 25, 30 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 28;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 12);
	}

	@Override
	public float lootChance() {
		return 1;
	}

	@Override
	public Item createLoot() {
		return new DwarfToken();
	}

	private int enemyTeleCooldown = 0;

	private static final String ENEMY_COOLDOWN = "enemy_cooldown";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(ENEMY_COOLDOWN, enemyTeleCooldown);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		enemyTeleCooldown = bundle.getInt( ENEMY_COOLDOWN );
	}

	@Override
	protected boolean act() {
		enemyTeleCooldown--;
		return super.act();
	}

	public void onZapComplete(){
		teleportEnemy();
		next();
	}

	public void teleportEnemy(){
		spend(TICK);

		int bestPos = enemy.pos;
		for (int i : PathFinder.NEIGHBOURS8()){
			if (Dungeon.level.passable[pos + i]
				&& Actor.findChar(pos+i) == null
				&& Dungeon.level.trueDistance(pos+i, enemy.pos) > Dungeon.level.trueDistance(bestPos, enemy.pos)){
				bestPos = pos+i;
			}
		}

		if (enemy.buff(MagicImmune.class) != null){
			bestPos = enemy.pos;
		}

		if (bestPos != enemy.pos){
			ScrollOfTeleportation.appear(enemy, bestPos);
			if (enemy instanceof Hero){
				((Hero) enemy).interrupt();
				Dungeon.observe();
				GameScene.updateFog();
			}
		}

		enemyTeleCooldown = 20;
	}

	private boolean canTele(int target){
		if (enemyTeleCooldown > 0) return false;
		boolean[] solid = Dungeon.level.solid;
		//reborn 无 BArray 工具类，手动反转 solid 得到可通行表（zap 可绕阻挡地形但不能穿墙）
		boolean[] passable = new boolean[solid.length];
		for (int i = 0; i < solid.length; i++) {
			passable[i] = !solid[i];
		}
		PathFinder.buildDistanceMap(target, passable, Dungeon.level.distance(pos, target)+1);
		//zap 可以绕过阻挡地形，但不能穿墙
		if (PathFinder.distance()[pos] == Integer.MAX_VALUE){
			return false;
		}
		return true;
	}

	private class Hunting extends Mob.Hunting {

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (enemyInFOV && !canAttack(enemy) && enemyTeleCooldown <= 0
					&& !Char.hasProp(enemy, Property.IMMOVABLE) && canTele(enemy.pos)
					&& Random.Int(100/Math.max(1,(int)Dungeon.level.distance(pos, enemy.pos))) == 0) {
				teleportEnemy(); //逻辑立即结算（内部已 spend(TICK) 并重置冷却）
				if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
					sprite.zap( enemy.pos ); //纯视觉
				}
				return true;
			}
			return super.act(enemyInFOV, justAlerted);
		}
	}

}
