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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PinCushion;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrystalGuardianSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class CrystalGuardian extends Mob{

	{
		spriteClass = CrystalGuardianSprite.class;

		HP = HT = 100;
		defenseSkill = 14;

		EXP = 10;
		maxLvl = -2;

		SLEEPING = new Sleeping();
		state = SLEEPING;

		properties.add(Property.INORGANIC);
		properties.add(Property.MINIBOSS);
	}

	private boolean recovering = false;

	public boolean recovering(){
		return recovering;
	}

	@Override
	protected boolean act() {
		if (recovering){
			if (buff(PinCushion.class) != null){
				buff(PinCushion.class).detach();
			}
			throwItems();
			HP = Math.min(HT, HP+5);
			if (Dungeon.level.heroFOV[pos]) {
				sprite.showStatusWithIcon(CharSprite.POSITIVE, "5", FloatingText.HEALING);
			}
			if (HP == HT){
				recovering = false;
				if (sprite instanceof CrystalGuardianSprite) ((CrystalGuardianSprite) sprite).endCrumple();
			}
			spend(TICK);
			return true;
		}
		return super.act();
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 10, 16 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 20;
	}

	@Override
	public int defenseSkill(Char enemy) {
		if (recovering) return 0;
		else            return super.defenseSkill(enemy);
	}

	@Override
	public boolean surprisedBy(Char enemy, boolean attacking) {
		if (recovering) return false;
		else            return super.surprisedBy(enemy, attacking);
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 10);
	}

	@Override
	public boolean reset() {
		return true;
	}

	//mod 没有 Statistics.questScores 与对应成就追踪，删除 3.3.8 的扣分覆写

	@Override
	public int defenseProc(Char enemy, int damage) {
		if (recovering){
			//在格挡前触发，因此该伤害视为无视格挡
			sprite.showStatusWithIcon(CharSprite.NEGATIVE, Integer.toString(damage), FloatingText.PHYS_DMG_NO_BLOCK);
			HP = Math.max(1, HP-damage);
			damage = -1;
		}

		return super.defenseProc(enemy, damage);
	}

	@Override
	public boolean isAlive() {
		if (HP <= 0){
			HP = 1;

			for (Buff b : buffs()){
				if (!(b instanceof Doom || b instanceof Cripple)) {
					b.detach();
				}
			}

			if (!recovering) {
				recovering = true;
				//mod 无 Bestiary 图鉴计数，删掉 setSeen/countEncounter
				if (sprite != null) ((CrystalGuardianSprite) sprite).crumple();
			}
		}
		return super.isAlive();
	}

	@Override
	public boolean isInvulnerable(Class effect) {
		if (recovering){
			//恢复期间免疫非英雄、非尖塔的角色伤害（防止友军补刀倒地守卫）
			return super.isInvulnerable(effect) || (Char.class.isAssignableFrom(effect) && !com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero.class.isAssignableFrom(effect) && !CrystalSpire.class.isAssignableFrom(effect));
		}
		return super.isInvulnerable(effect);
	}

	public CrystalGuardian(){
		super();
		switch (Random.Int(3)){
			case 0: default:
				spriteClass = CrystalGuardianSprite.Blue.class;
				break;
			case 1:
				spriteClass = CrystalGuardianSprite.Green.class;
				break;
			case 2:
				spriteClass = CrystalGuardianSprite.Red.class;
				break;
		}
	}

	@Override
	public float spawningWeight() {
		return 0;
	}

	@Override
	public float speed() {
		//在封闭空间中移动最多花费 4 回合
		if (!Dungeon.level.openSpace[pos]) {
			return Math.max(0.25f, super.speed() / 4f);
		}
		return super.speed();
	}

	@Override
	public void move(int step, boolean travelling) {
		super.move(step, travelling);
		if (Dungeon.level.map[pos] == Terrain.MINE_CRYSTAL){
			Level.set(pos, Terrain.EMPTY);
			GameScene.updateMap(pos);
			if (Dungeon.level.heroFOV[pos]){
				Splash.at(pos, 0xFFFFFF, 5);
				Sample.INSTANCE.play( Assets.Sounds.SHATTER );
			}
			//踩碎水晶额外消耗一次移动（不受封闭空间影响）
			spend(1/super.speed());
		}
	}

	//mod 寻路体系没有 modifyPassable 钩子：
	//追击时穿越水晶的行为无法注入寻路，守卫将绕开水晶（踩碎水晶的 move 逻辑保留）

	@Override
	public void beckon(int cell) {
		if (state == SLEEPING){
			//什么也不做
		} else {
			super.beckon(cell);
		}
	}

	protected class Sleeping extends Mob.Sleeping{

		@Override
		protected void awaken(boolean enemyInFOV) {
			if (enemyInFOV){
				//看不到实际无法触及的敌人时不会醒来
				PathFinder.buildDistanceMap(enemy.pos, Dungeon.level.passable);
				if (PathFinder.distance()[pos] == Integer.MAX_VALUE){
					return;
				}
			}
			super.awaken(enemyInFOV);
		}
	}

	public static final String SPRITE = "sprite";
	public static final String RECOVERING = "recovering";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SPRITE, spriteClass);
		bundle.put(RECOVERING, recovering);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		spriteClass = bundle.getClass(SPRITE);
		recovering = bundle.getBoolean(RECOVERING);
	}
}
