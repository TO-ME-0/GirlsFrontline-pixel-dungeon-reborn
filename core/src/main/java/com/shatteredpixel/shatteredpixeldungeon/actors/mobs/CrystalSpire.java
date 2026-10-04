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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrystalSpireSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CrystalSpire extends Mob {

	{
		//大致对应 +0/1/2/3/4/5 镐需要敲 33/27/23/20/18/16 下
		HP = HT = 300;
		spriteClass = CrystalSpireSprite.class;

		EXP = 20;

		//在其他怪物之后行动，诱使水晶守卫攻击的行为更稳定
		actPriority = MOB_PRIO-1;

		state = PASSIVE;

		alignment = Char.Alignment.NEUTRAL;

		properties.add(Property.IMMOVABLE);
		properties.add(Property.BOSS);
		properties.add(Property.INORGANIC);
		//mod 无 Property.STATIC
	}

	private float abilityCooldown;
	private static final int ABILITY_CD = 15;

	private ArrayList<ArrayList<Integer>> targetedCells = new ArrayList<>();

	@Override
	protected boolean act() {
		//Char 逻辑
		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
			fieldOfView = new boolean[Dungeon.level.length()];
		}
		Dungeon.level.updateFieldOfView( this, fieldOfView );

		throwItems();

		sprite.hideAlert();
		sprite.hideLost();

		//Mob 逻辑
		enemy = Dungeon.hero();

		//尖塔仍能追踪隐身的英雄
		enemySeen = enemy.isAlive() && fieldOfView[enemy.pos];
		//Char/Mob 逻辑结束

		if (!targetedCells.isEmpty()){

			ArrayList<Integer> cellsToAttack = targetedCells.remove(0);

			for (int i : cellsToAttack){

				Char ch = Actor.findChar(i);
				if (ch instanceof CrystalSpire){
					continue; //不会在自己身上生成水晶
				}

				Level.set(i, Terrain.MINE_CRYSTAL);
				GameScene.updateMap(i);

				Splash.at(i, 0xFFFFFF, 5);
			}

			for (int i : cellsToAttack){
				Char ch = Actor.findChar(i);

				if (ch != null && !(ch instanceof CrystalWisp || ch instanceof CrystalSpire)){
					int dmg = Random.NormalIntRange(6, 15);

					//水晶守卫受到的伤害更高
					if (ch instanceof CrystalGuardian) {
						dmg += 12; //18-27 点伤害
						Buff.prolong(ch, Cripple.class, 30f);
					}
					//mod 无 Statistics.questScores，删除扣分
					ch.damage(dmg, new SpireSpike());

					int movePos = i;
					//水晶守卫被推离英雄，其他角色被推离尖塔
					if (ch instanceof CrystalGuardian){
						for (int j : PathFinder.NEIGHBOURS8()){
							if (!Dungeon.level.solid[i+j] && Actor.findChar(i+j) == null &&
									Dungeon.level.trueDistance(i+j, Dungeon.hero().pos) > Dungeon.level.trueDistance(movePos, Dungeon.hero().pos)){
								movePos = i+j;
							}
						}
					} else if (!Char.hasProp(ch, Property.IMMOVABLE)) {
						for (int j : PathFinder.NEIGHBOURS8()){
							if (!Dungeon.level.solid[i+j] && Actor.findChar(i+j) == null &&
									Dungeon.level.trueDistance(i+j, pos) > Dungeon.level.trueDistance(movePos, pos)){
								movePos = i+j;
							}
						}
					}

					if (ch.isAlive()){
						if (movePos != i){
							Actor.add(new Pushing(ch, i, movePos));
							ch.pos = movePos;
							Dungeon.level.occupyCell(ch);
						}
					} else if (ch == Dungeon.hero()){
						GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", name())) );
						Dungeon.fail(getClass());
					}
				}
			}

			Camera.main.shake( 1, 0.7f );
			Sample.INSTANCE.play( Assets.Sounds.SHATTER );

			if (!targetedCells.isEmpty()){
				for (int i : targetedCells.get(0)){
					sprite.parent.add(new TargetedCell(i, 0xFF0000));
				}
			}

		}

		if (hits < 3 || !enemySeen){
			spend(TICK);
			return true;
		} else {

			if (abilityCooldown <= 0){

				if (Random.Int(2) == 0) {
					diamondAOEAttack();
				} else {
					lineAttack();
				}

				for (int i : targetedCells.get(0)){
					sprite.parent.add(new TargetedCell(i, 0xFF0000));
				}

				abilityCooldown += ABILITY_CD;

				//mod Char 无 cooldown()，蓄力固定 1 回合
				spend(TICK);
				Dungeon.hero().interrupt();
			} else {
				abilityCooldown -= 1;
				spend(TICK);
			}

		}

		return true;
	}

	public static class SpireSpike{}

	private void diamondAOEAttack(){
		targetedCells.clear();

		ArrayList<Integer> aoeCells = new ArrayList<>();
		aoeCells.add(Dungeon.hero().pos);
		aoeCells.addAll(spreadDiamondAOE(aoeCells));
		targetedCells.add(new ArrayList<>(aoeCells));

		if (HP < 2*HT/3f){
			aoeCells.addAll(spreadDiamondAOE(aoeCells));
			targetedCells.add(new ArrayList<>(aoeCells));
			if (HP < HT/3f) {
				aoeCells.addAll(spreadDiamondAOE(aoeCells));
				targetedCells.add(aoeCells);
			}
		}
	}

	private ArrayList<Integer> spreadDiamondAOE(ArrayList<Integer> currentCells){
		ArrayList<Integer> spreadCells = new ArrayList<>();
		for (int i : currentCells){
			for (int j : PathFinder.NEIGHBOURS4()){
				if ((!Dungeon.level.solid[i+j] || Dungeon.level.map[i+j] == Terrain.MINE_CRYSTAL)
						&& !spreadCells.contains(i+j) && !currentCells.contains(i+j)){
					spreadCells.add(i+j);
				}
			}
		}
		return spreadCells;
	}

	private void lineAttack(){
		targetedCells.clear();

		ArrayList<Integer> lineCells = new ArrayList<>();
		Ballistica aim = new Ballistica(pos, Dungeon.hero().pos, Ballistica.WONT_STOP);
		for (int i : aim.subPath(1, 7)){
			if (!Dungeon.level.solid[i] || Dungeon.level.map[i] == Terrain.MINE_CRYSTAL){
				lineCells.add(i);
			} else {
				break;
			}
		}

		targetedCells.add(new ArrayList<>(lineCells));
		if (HP < 2*HT/3f){
			lineCells.addAll(spreadDiamondAOE(lineCells));
			targetedCells.add(new ArrayList<>(lineCells));
			if (HP < HT/3f) {
				lineCells.addAll(spreadDiamondAOE(lineCells));
				targetedCells.add(lineCells);
			}
		}
	}

	@Override
	public void beckon(int cell) {
		//什么也不做
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public void damage(int dmg, Object src) {
		if (!(src instanceof Pickaxe) ){
			dmg = 0;
		}
		super.damage(dmg, src);
	}

	@Override
	public boolean isInvulnerable(Class effect) {
		return super.isInvulnerable(effect) || effect != Pickaxe.class;
	}

	@Override
	public void add( Buff buff ) {
		//免疫所有增益与减益：mod 的 add(Buff) 返回 void，拒绝即直接不处理
	}

	private int hits = 0;

	@Override
	public boolean interact(Char c) {
		if (c == Dungeon.hero()){
			final Pickaxe p = Dungeon.hero().belongings.getItem(Pickaxe.class);

			if (p == null){
				return true;
			}

			Dungeon.hero().sprite.attack(pos, new Callback() {
				@Override
				public void call() {
					//使用独立的伤害计算，只受镐子等级和强化方向影响
					//这里假装尖塔是持有者，因此英雄力量或其他装备不参与计算
					int dmg = p.damageRoll(CrystalSpire.this);

					damage(dmg, p);
					abilityCooldown -= dmg/10f;
					sprite.bloodBurstA(Dungeon.hero().sprite.center(), dmg);
					sprite.flash();

					BossHealthBar.bleed(HP <= HT/3);

					if (isAlive()) {
						Sample.INSTANCE.play(Assets.Sounds.SHATTER, 1f, Random.Float(1.15f, 1.25f));
						((CrystalSpireSprite) sprite).updateIdle();
					} else {
						Sample.INSTANCE.play(Assets.Sounds.SHATTER);
						Sample.INSTANCE.playDelayed(Assets.Sounds.ROCKS, 0.1f);
						Camera.main.shake( 3, 0.7f );
						Blacksmith.Quest.cur().beatBoss();

						//mod 无 Bestiary 图鉴，删除 setSeen/countEncounter

						if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
							fieldOfView = new boolean[Dungeon.level.length()];
							Dungeon.level.updateFieldOfView( CrystalSpire.this, fieldOfView );
						}

						for (int i = 0; i < Dungeon.level.length(); i++){
							if (fieldOfView[i] && Dungeon.level.map[i] == Terrain.MINE_CRYSTAL){
								Level.set(i, Terrain.EMPTY);
								GameScene.updateMap(i);
								Splash.at(i, 0xFFFFFF, 5);
							}
						}

						//mod 无 Bestiary.skipCountingEncounters 机制
						for (Char ch : Actor.chars()){
							if (fieldOfView[ch.pos]) {
								if (ch instanceof CrystalGuardian) {
									ch.damage(ch.HT, new SpireSpike());
								}
								if (ch instanceof CrystalWisp) {
									Buff.affect(ch, Blindness.class, 5f);
								}
							}
						}

					}

					hits++;

					if (hits == 1){
						GLog.w(Messages.get(CrystalSpire.class, "warning"));
						Camera.main.shake( 1, 0.7f );
						//mod 无 MINE 音效，敲尖塔统一用 EVOKE
						Sample.INSTANCE.play( Assets.Sounds.EVOKE );
					} else if (hits >= 3) {

						if (hits == 3){
							Sample.INSTANCE.play( Assets.Sounds.ROCKS );
							Camera.main.shake( 3, 0.7f );
							GLog.n(Messages.get(CrystalSpire.class, "alert"));
							BossHealthBar.assignBoss(CrystalSpire.this);

							abilityCooldown = 1; //首次攻击延迟 1 回合
						}

						boolean affectingGuardians = false;
						for (Char ch : Actor.chars()) {
							if (ch instanceof CrystalWisp) {
								if (((CrystalWisp) ch).state != ((CrystalWisp)ch).HUNTING && ((CrystalWisp) ch).target != pos) {
									((CrystalWisp) ch).beckon(pos);
								}
							} else if (ch instanceof CrystalGuardian) {
								if (((CrystalGuardian) ch).state != ((CrystalGuardian)ch).HUNTING && ((CrystalGuardian) ch).target != pos) {
									affectingGuardians = true;
								}
							}
						}

						//建立通往守卫的寻路路线
						//附近的睡眠守卫被减速以争取时间
						//远处已醒的守卫被加速，惩罚玩家吵醒它们
						if (affectingGuardians){
							boolean[] passable = Dungeon.level.passable.clone();
							for (int i = 0; i < Dungeon.level.length(); i++){
								if (Dungeon.level.map[i] == Terrain.MINE_CRYSTAL){
									passable[i] = true;
								}
							}
							PathFinder.buildDistanceMap(pos, passable);

							for (Char ch : Actor.chars()) {
								if (ch instanceof CrystalGuardian){
									if (((CrystalGuardian) ch).state == ((CrystalGuardian) ch).SLEEPING) {

										((CrystalGuardian) ch).aggro(Dungeon.hero());
										((CrystalGuardian) ch).beckon(pos);

										//恰好睡在尖塔附近的守卫会被延迟
										if (PathFinder.distance()[ch.pos] < 20){
											Buff.affect(ch, Paralysis.class, 20-PathFinder.distance()[ch.pos]);
										}

									} else if (((CrystalGuardian) ch).state != ((CrystalGuardian) ch).HUNTING && ((CrystalGuardian) ch).target != pos){
										((CrystalGuardian) ch).beckon(pos);
										if (((CrystalGuardian) ch).state != HUNTING) {
											((CrystalGuardian) ch).aggro(Dungeon.hero());
										}

										//加速已经醒来但离得较远的守卫
										if (PathFinder.distance()[ch.pos] > 8){
											Buff.affect(ch, Haste.class, Math.round((PathFinder.distance()[ch.pos]-8)/2f));
										}
									}
								}
							}
						}
					}

					Invisibility.dispel();
					Dungeon.hero().spendAndNext(p.delayFactor(CrystalSpire.this));
				}
			});
			return false;

		}
		return true;
	}

	public CrystalSpire(){
		super();
		switch (Random.Int(3)){
			case 0: default:
				spriteClass = CrystalSpireSprite.Blue.class;
				break;
			case 1:
				spriteClass = CrystalSpireSprite.Green.class;
				break;
			case 2:
				spriteClass = CrystalSpireSprite.Red.class;
				break;
		}
	}

	@Override
	public float spawningWeight() {
		return 0;
	}

	public static final String SPRITE = "sprite";
	public static final String HITS = "hits";

	public static final String ABILITY_COOLDOWN = "ability_cooldown";
	public static final String TARGETED_CELLS = "targeted_cells";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SPRITE, spriteClass);
		bundle.put(HITS, hits);

		bundle.put(ABILITY_COOLDOWN, abilityCooldown);
		for (int i = 0; i < targetedCells.size(); i++){
			int[] bundleArr = new int[targetedCells.get(i).size()];
			for (int j = 0; j < targetedCells.get(i).size(); j++){
				bundleArr[j] = targetedCells.get(i).get(j);
			}
			bundle.put(TARGETED_CELLS+i, bundleArr);
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		spriteClass = bundle.getClass(SPRITE);
		hits = bundle.getInt(HITS);

		if (hits >= 3){
			BossHealthBar.assignBoss(this);
		}

		abilityCooldown = bundle.getFloat(ABILITY_COOLDOWN);
		targetedCells.clear();
		int i = 0;
		while (bundle.contains(TARGETED_CELLS+i)){
			ArrayList<Integer> targets = new ArrayList<>();
			for (int j : bundle.getIntArray(TARGETED_CELLS+i)){
				targets.add(j);
			}
			targetedCells.add(targets);
			i++;
		}
	}

	{
		immunities.add( Blindness.class );
	}

}
