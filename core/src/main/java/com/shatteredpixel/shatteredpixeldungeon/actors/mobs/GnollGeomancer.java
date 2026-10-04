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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DarkGold;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollGeomancerSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class GnollGeomancer extends Mob {

	{
		HP = HT = 150;
		spriteClass = GnollGeomancerSprite.class;

		EXP = 20;

		//和工兵一样，在其他怪物之后行动
		actPriority = MOB_PRIO-1;

		SLEEPING = new Sleeping();
		HUNTING = new Hunting();
		state = SLEEPING;

		//视野用于攻击被自己打开的空地上的英雄
		//英雄退回墙后时它会失去视野并停止攻击，因此它能看穿高草和遮蔽雾
		viewDistance = 12;

		properties.add(Property.BOSS);
		properties.add(Property.IMMOVABLE); //只通过技能位移，否则站定不动
	}

	private int abilityCooldown = Random.NormalIntRange(3, 5);
	private boolean lastAbilityWasRockfall;

	private int[] throwingRocksFromPos = null;
	private int throwingRockToPos = -1; //只需要一个目标点，永远相同

	private int sapperID = -1;
	private int[] sapperSpawns = null;

	@Override
	protected boolean act() {
		if (sapperSpawns == null){
			sapperSpawns = new int[3];
			int i = 0;
			for (Mob m : Dungeon.level.mobs){
				if (m instanceof GnollSapper){
					sapperSpawns[i] = ((GnollSapper) m).spawnPos;
					i++;
				}
				if (i == 3) break;
			}
		}

		if (throwingRocksFromPos != null){
			boolean attacked = false;
			for (int rock : throwingRocksFromPos) {
				if (rock != -1 && Dungeon.level.map[rock] == Terrain.MINE_BOULDER) {
					attacked = true;
					GnollGeomancer.doRockThrowAttack(this, rock, throwingRockToPos);
				}
			}

			throwingRocksFromPos = null;
			throwingRockToPos = -1;

			spend(TICK);
			return !attacked;
		} else {
			return super.act();
		}

	}

	@Override
	public boolean isInvulnerable(Class effect) {
		return super.isInvulnerable(effect)
				|| (buff(RockArmor.class) != null && effect != Pickaxe.class)
				|| hasSapper();
	}

	@Override
	public void add(Buff buff) {
		//睡眠时免疫所有增益与减益（自身专属 buff 除外）；mod 的 add(Buff) 返回 void，拒绝即不处理
		if (!(state == SLEEPING && !(buff instanceof RockArmor || buff instanceof DelayedRockFall))){
			super.add(buff);
		}
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 3, 6 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 20;
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
	public boolean heroShouldInteract() {
		return super.heroShouldInteract() || buff(RockArmor.class) != null;
	}

	@Override
	protected boolean getCloser(int target) {
		return false;
	}

	@Override
	protected boolean getFurther(int target) {
		return false;
	}

	int hits = 0;

	@Override
	public boolean interact(Char c) {
		if (c != Dungeon.hero() || buff(RockArmor.class) == null) {
			return super.interact(c);
		} else {
			final Pickaxe p = Dungeon.hero().belongings.getItem(Pickaxe.class);

			if (p == null){
				return true;
			}

			Dungeon.hero().sprite.attack(pos, new Callback() {
				@Override
				public void call() {
					//使用独立的伤害计算，只受镐子等级和强化方向影响
					//这里假装地卜师是持有者，因此英雄力量或其他装备不参与计算
					int dmg = p.damageRoll(GnollGeomancer.this);

					boolean wasSleeping = state == SLEEPING;

					//保证开头几下不足以砸开石甲
					if (wasSleeping) dmg = Math.min(dmg, 15);

					dmg = Math.min(dmg, buff(RockArmor.class).shielding());

					damage(dmg, p);
					sprite.bloodBurstA(Dungeon.hero().sprite.center(), dmg);
					sprite.flash();

					hits++;
					if (hits == 1){
						GLog.w( Messages.get(GnollGeomancer.this, "warning"));
					} if (hits == 3){
						GLog.n( Messages.get(GnollGeomancer.this, "alert"));
						wasSleeping = false;
						spend(TICK);
						sprite.idle();

						carveRockAndDash();

						state = HUNTING;
						enemy = Dungeon.hero();
						BossHealthBar.assignBoss(GnollGeomancer.this);

						for (Mob m : Dungeon.level.mobs){
							if (m instanceof GnollGuard){
								m.aggro(Dungeon.hero());
								if (!((GnollGuard) m).hasSapper()){
									m.beckon(pos);
								}
							} else if (m instanceof GnollSapper){
								m.aggro(Dungeon.hero());
							}
						}
					}

					if (wasSleeping) {
						state = SLEEPING;
						alerted = false;
					}

					if (buff(RockArmor.class) == null){
						//mod Splash 无 around，用中心点溅射代替
						Splash.at(sprite.center(), 0x555555, 30);
						sprite.idle();
					}

					//mod 无 MINE 音效，镐子敲甲统一用 EVOKE
					Sample.INSTANCE.play(Assets.Sounds.EVOKE, 1f, Random.Float(0.85f, 1.15f));
					Invisibility.dispel();
					Dungeon.hero().spendAndNext(p.delayFactor(GnollGeomancer.this));
				}
			});

			return false;
		}
	}

	@Override
	public void damage(int dmg, Object src) {
		int hpBracket = HT / 3;

		int curbracket = HP / hpBracket;
		if (curbracket == 3) curbracket--; //满血不单独算一档

		inFinalBracket = curbracket == 0;

		super.damage(dmg, src);

		abilityCooldown -= dmg/10f;

		int newBracket =  HP / hpBracket;
		if (newBracket == 3) newBracket--; //满血不单独算一档

		if (newBracket != curbracket) {
			//一次攻击无法跨多个血量档
			if (HP <= (curbracket-1)*hpBracket){
				HP = (curbracket-1)*hpBracket + 1;
			}

			BossHealthBar.bleed(newBracket <= 0);

			carveRockAndDash();
			Buff.affect(this, RockArmor.class).setShield(25);
		}
	}

	private boolean inFinalBracket = false;

	@Override
	public boolean isAlive() {
		//未进入最终血量档时无法死亡，无论受到多少伤害
		return super.isAlive() || !inFinalBracket;
	}

	public void linkSapper( GnollSapper sapper ){
		this.sapperID = sapper.id();
		if (sprite instanceof GnollGeomancerSprite){
			((GnollGeomancerSprite) sprite).setupArmor();
		}
	}

	public boolean hasSapper(){
		return sapperID != -1
				&& Actor.findById(sapperID) instanceof GnollSapper
				&& ((GnollSapper)Actor.findById(sapperID)).isAlive();
	}

	public void loseSapper(){
		if (sapperID != -1){
			sapperID = -1;
			if (sprite instanceof GnollGeomancerSprite){
				((GnollGeomancerSprite) sprite).loseArmor();
			}
		}
	}

	private void carveRockAndDash(){

		//以最近的工兵出生点为目标，优先 16 格内仍存活的工兵
		int closestSapperPos = -1;
		boolean closestisAlive = false;
		for (int i = 0; i < sapperSpawns.length; i++){
			if (sapperSpawns[i] == -1){
				continue;
			}

			if (closestSapperPos == -1) {
				closestSapperPos = sapperSpawns[i];
				for (Mob m : Dungeon.level.mobs){
					if (m instanceof GnollSapper && ((GnollSapper) m).spawnPos == closestSapperPos){
						closestisAlive = true;
						break;
					}
				}
				continue;
			}

			boolean sapperAlive = false;
			for (Mob m : Dungeon.level.mobs){
				if (m instanceof GnollSapper && ((GnollSapper) m).spawnPos == sapperSpawns[i]){
					sapperAlive = true;
					break;
				}
			}

			if ((sapperAlive && !closestisAlive && Dungeon.level.distance(pos, sapperSpawns[i]) <= 16)
					|| Dungeon.level.trueDistance(pos, sapperSpawns[i]) < Dungeon.level.trueDistance(pos, closestSapperPos)) {
				closestSapperPos = sapperSpawns[i];
				closestisAlive = sapperAlive;
			}
		}

		int dashPos = closestSapperPos;

		//最多冲刺 3 次
		if (dashPos == -1){
			return;
		}

		//出生点超过 12 格时，尽量靠近
		Ballistica path = new Ballistica(pos, dashPos, Ballistica.STOP_TARGET);

		if (path.dist > 12){
			dashPos = path.path.get(12);
		}

		if (Actor.findChar(dashPos) != null || Dungeon.level.traps.get(dashPos) != null){
			ArrayList<Integer> candidates = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS8()){
				if (Actor.findChar(dashPos+i) == null && Dungeon.level.traps.get(dashPos+i) == null){
					candidates.add(dashPos+i);
				}
			}
			if (!candidates.isEmpty()) {
				dashPos = Random.element(candidates);
			}
		}

		for (int i = 0; i < sapperSpawns.length; i++){
			if (sapperSpawns[i] == closestSapperPos){
				sapperSpawns[i] = -1;
			}
		}

		path = new Ballistica(pos, dashPos, Ballistica.STOP_TARGET);

		ArrayList<Integer> cells = new ArrayList<>(path.subPath(0, path.dist));
		cells.addAll(spreadDiamondAOE(cells));
		cells.addAll(spreadDiamondAOE(cells));
		cells.addAll(spreadDiamondAOE(cells));

		ArrayList<Integer> exteriorCells = spreadDiamondAOE(cells);

		for (int i : cells){
			if (Dungeon.level.map[i] == Terrain.WALL_DECO){
				Dungeon.level.drop(new DarkGold(), i).sprite.drop();
				Dungeon.level.map[i] = Terrain.EMPTY_DECO;
			} else if (Dungeon.level.solid[i]){
				if (Random.Int(3) == 0){
					Dungeon.level.map[i] = Terrain.MINE_BOULDER;
				} else {
					Dungeon.level.map[i] = Terrain.EMPTY_DECO;
				}
			} else if (Dungeon.level.map[i] == Terrain.HIGH_GRASS || Dungeon.level.map[i] == Terrain.FURROWED_GRASS){
				Dungeon.level.map[i] = Terrain.GRASS;
			}
			CellEmitter.get( i - Dungeon.level.width() ).start(Speck.factory(Speck.ROCK), 0.07f, 10);
		}
		for (int i : exteriorCells){
			if (!Dungeon.level.solid[i]
					&& Dungeon.level.map[i] != Terrain.EMPTY_SP
					&& !Dungeon.level.adjacent(i, Dungeon.level.entrance)
					&& Dungeon.level.traps.get(i) == null
					&& Dungeon.level.plants.get(i) == null
					&& Actor.findChar(i) == null){
				Dungeon.level.map[i] = Terrain.MINE_BOULDER;
			}
		}
		if (Dungeon.level.solid[dashPos]){
			Dungeon.level.map[dashPos] = Terrain.EMPTY_DECO;
		}
		//可能一次改动大量格子，直接重建属性图而不是增量更新
		Dungeon.level.buildFlagMaps();
		Dungeon.level.cleanWalls();
		GameScene.updateMap();
		GameScene.updateFog();
		Dungeon.observe();

		Camera.main.shake(3, 0.7f);
		Sample.INSTANCE.play(Assets.Sounds.ROCKS);

		int oldpos = pos;
		pos = dashPos;
		spend(TICK);
		abilityCooldown = 1;
		Actor.add(new Pushing(this, oldpos, pos));

		if (closestisAlive){
			GnollSapper closest = null;
			for (Mob m : Dungeon.level.mobs){
				if (m instanceof GnollSapper && ((GnollSapper) m).spawnPos == closestSapperPos){
					closest = (GnollSapper) m;
					break;
				}
			}
			if (closest != null){
				Actor guard = closest.getPartner();
				closest.linkPartner(this);
				//工兵离得太远时，把工兵及其卫士传送到地卜师身边
				if (Dungeon.level.distance(closest.pos, dashPos) > 3){
					ArrayList<Integer> candidates = new ArrayList<>();
					for (int i : PathFinder.NEIGHBOURS8()){
						if (!Dungeon.level.solid[dashPos+i]
								&& Dungeon.level.traps.get(dashPos+i) == null
								&& Dungeon.level.plants.get(dashPos+i) == null
								&& Actor.findChar(dashPos+i) == null){
							candidates.add(dashPos+i);
						}
					}

					if (!candidates.isEmpty()){
						int newSapperPos = Random.element(candidates);
						ScrollOfTeleportation.appear(closest, newSapperPos);
						closest.spawnPos = newSapperPos;
						candidates.remove((Integer)newSapperPos);

						if (guard instanceof GnollGuard && !candidates.isEmpty()){
							ScrollOfTeleportation.appear((GnollGuard)guard, Random.element(candidates));
						}

					}
				}
			}
		}
	}

	private ArrayList<Integer> spreadDiamondAOE(ArrayList<Integer> currentCells){
		ArrayList<Integer> spreadCells = new ArrayList<>();
		for (int i : currentCells){
			for (int j : PathFinder.NEIGHBOURS4()){
				if (Dungeon.level.insideMap(i+j) && !spreadCells.contains(i+j) && !currentCells.contains(i+j)){
					spreadCells.add(i+j);
				}
			}
		}
		return spreadCells;
	}

	@Override
	public String description() {
		if (state == SLEEPING){
			return Messages.get(this, "desc_sleeping");
		} else {
			String desc = super.description();
			if (buff(RockArmor.class) != null){
				if (hasSapper()){
					desc += "\n\n" + Messages.get(this, "desc_armor_sapper");
				} else {
					desc += "\n\n" + Messages.get(this, "desc_armor");
				}
			}
			return desc;
		}
	}

	@Override
	public void die(Object cause) {
		super.die(cause);
		Blacksmith.Quest.cur().beatBoss();
		Sample.INSTANCE.playDelayed(Assets.Sounds.ROCKS, 0.1f);
		Camera.main.shake( 3, 0.7f );

		for (int i = 0; i < Dungeon.level.length(); i++){
			if (Dungeon.level.map[i] == Terrain.MINE_BOULDER && Dungeon.level.trueDistance(i, pos) <= 6){
				Level.set(i, Terrain.EMPTY_DECO);
				GameScene.updateMap(i);
				Splash.at(i, 0x555555, 15);
			}
		}
	}

	@Override
	public void beckon(int cell) {
		if (state == SLEEPING){
			//什么也不做
		} else {
			super.beckon(cell);
		}
	}

	private class Sleeping extends Mob.Sleeping {

		@Override
		protected void awaken(boolean enemyInFOV) {
			//什么也不做，采用特殊的觉醒规则（用镐敲甲）
		}
	}

	private class Hunting extends Mob.Hunting {

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (!enemyInFOV){
				//看不到当前目标时，尝试寻找新目标
				enemy = null;
				enemy = chooseEnemy();
				if (enemy != null){
					return act(true, justAlerted);
				} else {
					//找不到就等待
					spend(TICK);
					return true;
				}
			} else {
				enemySeen = true;

				//敌人初次接近、或工兵仍存活时更频繁使用技能
				//但敌人被眩晕时不用，避免把敌人连续晕到死
				if ((Dungeon.level.distance(pos, enemy.pos) > 2 || hasSapper())
						&& buff(RockArmor.class) != null
						&& enemy.buff(Paralysis.class) == null){
					abilityCooldown -= 1f;
				}

				if (hasSapper()){
					((GnollSapper)Actor.findById(sapperID)).aggro(enemy);
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

					//一半概率掷石、一半概率落石，但不会连续两次落石
					//目标贴着路障时总是尝试掷石；无石可掷时总是落石
					int hpBracket = HT / 3;

					int curbracket = HP / hpBracket;
					if (curbracket == 3) curbracket--; //满血不单独算一档

					Ballistica aim = GnollGeomancer.prepRockThrowAttack(enemy, GnollGeomancer.this);
					if (aim != null && (targetNextToBarricade || lastAbilityWasRockfall || Random.Int(2) == 0)) {

						lastAbilityWasRockfall = false;
						throwingRocksFromPos = new int[]{-1, -1, -1};
						throwingRockToPos = aim.collisionPos;

						//根据血量一次最多掷出 3 块石头
						for (int i = 0; i < 3 - curbracket; i++){
							if (aim == null) break;

							throwingRocksFromPos[i] = aim.sourcePos;

							Ballistica warnPath = new Ballistica(aim.sourcePos, aim.collisionPos, Ballistica.STOP_SOLID);
							for (int j : warnPath.subPath(0, warnPath.dist)){
								sprite.parent.add(new TargetedCell(j, 0xFF0000));
							}

							aim = GnollGeomancer.prepRockThrowAttack(enemy, GnollGeomancer.this);
						}

						Dungeon.hero().interrupt();
						abilityCooldown = Random.NormalIntRange(3, 5);
						//mod Char 无 cooldown()，蓄力固定 1 回合
						spend(TICK);
						return true;
					} else if (GnollGeomancer.prepRockFallAttack(enemy, GnollGeomancer.this, 6-2*curbracket, true)) {
						lastAbilityWasRockfall = true;
						Dungeon.hero().interrupt();
						spend(TICK);
						abilityCooldown = Random.NormalIntRange(3, 5);
						return true;
					}
				}

				//不进行普通攻击
				spend(TICK);
				return true;
			}
		}

	}

	//*** 这些方法为 public static，因为工兵也会使用同一套逻辑 ***

	public static Ballistica prepRockThrowAttack( Char target, Char source ){
		ArrayList<Integer> candidateRocks = new ArrayList<>();

		for (int i = 0; i < Dungeon.level.length(); i++){
			if (source.fieldOfView[i] && Dungeon.level.map[i] == Terrain.MINE_BOULDER){
				if (new Ballistica(i, target.pos, Ballistica.PROJECTILE).collisionPos == target.pos){
					candidateRocks.add(i);
				}
			}
		}

		//忽略正在被投掷的岩石
		for (Char ch : Actor.chars()){
			if (ch instanceof GnollGeomancer && ((GnollGeomancer) ch).throwingRocksFromPos != null){
				for (int i : ((GnollGeomancer) ch).throwingRocksFromPos){
					candidateRocks.remove((Integer)i);
				}
			} else if (ch instanceof GnollSapper){
				candidateRocks.remove((Integer)((GnollSapper) ch).throwingRockFromPos);
			}
		}

		if (candidateRocks.isEmpty()){
			return null;
		} else {

			//投出离敌人最近的岩石
			int throwingFromPos = candidateRocks.get(0);
			for (int i : candidateRocks){
				if (Dungeon.level.trueDistance(i, target.pos) < Dungeon.level.trueDistance(throwingFromPos, target.pos)){
					throwingFromPos = i;
				}
			}
			int throwingToPos = target.pos;

			return new Ballistica(throwingFromPos, throwingToPos, Ballistica.PROJECTILE);

		}
	}

	private static int rocksInFlight = 0;
	private static ArrayList<Char> knockedChars = new ArrayList<>();

	public static void doRockThrowAttack( Char source, int from, int to ){

		Level.set(from, Terrain.EMPTY);
		GameScene.updateMap(from);
		source.sprite.attack(from, new Callback() {
			@Override
			public void call() {
				source.sprite.idle();
				//什么也不做
			}
		});

		Ballistica rockPath = new Ballistica(from, to, Ballistica.MAGIC_BOLT);

		Sample.INSTANCE.play(Assets.Sounds.MISS);
		((MissileSprite)source.sprite.parent.recycle( MissileSprite.class )).
				reset( from, rockPath.collisionPos, new GnollGeomancer.Boulder(), new Callback() {
					@Override
					public void call() {
						Splash.at(rockPath.collisionPos, 0x555555, 15);
						Sample.INSTANCE.play(Assets.Sounds.ROCKS);

						Char ch = Actor.findChar(rockPath.collisionPos);
						if (ch == Dungeon.hero()){
							Camera.main.shake( 3, 0.7f );
						} else {
							Camera.main.shake(0.5f, 0.5f);
						}

						if (ch != null && !(ch instanceof GnollGeomancer)){
							ch.damage(Random.NormalIntRange(6, 12), new GnollGeomancer.Boulder());

							//mod 无 Statistics.questScores，删除扣分

							if (ch.isAlive()){
								Buff.prolong( ch, Paralysis.class, ch instanceof GnollGuard ? 10 : 3 );
							} else if (!ch.isAlive() && ch == Dungeon.hero()) {
								Dungeon.fail( source.getClass() );
								GLog.n( Messages.get( GnollGeomancer.class, "rock_kill") );
							}

							//mod WandOfBlastWave 无 6 参重载（无 source 参数）
							if (!knockedChars.contains(ch) && rockPath.path.size() > rockPath.dist+1) {
								Ballistica trajectory = new Ballistica(ch.pos, rockPath.path.get(rockPath.dist + 1), Ballistica.MAGIC_BOLT);
								WandOfBlastWave.throwChar(ch, trajectory, 1, false, false);
								knockedChars.add(ch);
							}
						} else if (ch == null) {
							Dungeon.level.pressCell(rockPath.collisionPos);
						}

						rocksInFlight--;
						if (rocksInFlight <= 0) {
							rocksInFlight = 0;
							source.next();
							knockedChars.clear();
						}
					}
				} );
		rocksInFlight++;
	}

	public static class Boulder extends Item {
		{
			image = ItemSpriteSheet.GEO_BOULDER;
		}
	}

	//整体逻辑类似 DM-300 的落石攻击，但参数更多
	public static boolean prepRockFallAttack( Char target, Char source, int range, boolean avoidBarricades ){
		final int rockCenter = target.pos;

		int safeCell;
		do {
			safeCell = rockCenter + PathFinder.NEIGHBOURS8()[Random.Int(8)];
		} while (safeCell == source.pos
				|| (Dungeon.level.solid[safeCell] && Random.Int(5) != 0)
				|| (Dungeon.level.traps.get(safeCell) != null && Random.Int(5) != 0));

		ArrayList<Integer> rockCells = new ArrayList<>();

		int start = rockCenter - Dungeon.level.width() * range - range;
		int pos;
		for (int y = 0; y < 1+2*range; y++) {
			pos = start + Dungeon.level.width() * y;
			for (int x = 0; x < 1+2*range; x++) {
				if (!Dungeon.level.insideMap(pos)) {
					pos++;
					continue;
				}
				if (avoidBarricades){
					boolean barricade = false;
					for (int j : PathFinder.NEIGHBOURS9()){
						if (Dungeon.level.map[pos+j] == Terrain.BARRICADE
								|| Dungeon.level.map[pos+j] == Terrain.ENTRANCE){
							barricade = true;
						}
					}
					if (barricade){
						pos++;
						continue;
					}
				}
				//加入非实心、非安全格、非地卜师所在格的格子
				if (!Dungeon.level.solid[pos]
						&& pos != safeCell
						&& !(Actor.findChar(pos) instanceof GnollGeomancer)
						&& !(source instanceof GnollGeomancer && Actor.findChar(pos) instanceof GnollSapper)
						&& Random.Int(1+Dungeon.level.distance(rockCenter, pos)/2) == 0) {
					rockCells.add(pos);
				}
				pos++;
			}
		}
		for (int i : rockCells){
			source.sprite.parent.add(new TargetedCell(i, 0xFF0000));
		}
		//mod Char 无 cooldown()，延迟固定 1 回合
		Buff.append(source, GnollRockFall.class, TICK).setRockPositions(rockCells);

		source.sprite.attack(target.pos, new Callback() {
			@Override
			public void call() {
				//什么也不做
				source.sprite.idle();
			}
		});

		return true;
	}

	public static class GnollRockFall extends DelayedRockFall{

		@Override
		public void affectChar(Char ch) {
			ch.damage(Random.NormalIntRange(6, 12), this);
			//mod 无 Statistics.questScores，删除扣分
			if (ch.isAlive()) {
				Buff.prolong(ch, Paralysis.class, ch instanceof GnollGuard ? 10 : 3);
			} else if (ch == Dungeon.hero()){
				Dungeon.fail( target.getClass() );
				GLog.n( Messages.get( GnollGeomancer.class, "rockfall_kill") );
			}
		}

		@Override
		public void affectCell(int cell) {
			if (Dungeon.level.map[cell] != Terrain.EMPTY_SP
					&& !Dungeon.level.adjacent(cell, Dungeon.level.entrance)
					&& Random.Int(3) == 0) {
				Level.set(cell, Terrain.MINE_BOULDER);
				GameScene.updateMap(cell);
			}
		}

	}

	public static class RockArmor extends ShieldBuff { }

	public static final String HITS = "hits";

	private static final String ABILITY_COOLDOWN = "ability_cooldown";
	private static final String LAST_ABILITY_WAS_ROCKFALL = "last_ability_was_rockfall";

	private static final String ROCK_FROM_POS = "rock_from_pos";
	private static final String ROCK_TO_POS = "rock_to_pos";

	private static final String SAPPER_ID = "sapper_id";
	private static final String SAPPER_SPAWNS = "sapper_spawns";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(HITS, hits);

		bundle.put(ABILITY_COOLDOWN, abilityCooldown);
		bundle.put(LAST_ABILITY_WAS_ROCKFALL, lastAbilityWasRockfall);

		if (throwingRocksFromPos != null) {
			bundle.put(ROCK_FROM_POS, throwingRocksFromPos);
		}
		bundle.put(ROCK_TO_POS, throwingRockToPos);

		bundle.put(SAPPER_ID, sapperID);
		if (sapperSpawns != null){
			bundle.put(SAPPER_SPAWNS, sapperSpawns);
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		hits = bundle.getInt(HITS);
		abilityCooldown = bundle.getInt(ABILITY_COOLDOWN);
		lastAbilityWasRockfall = bundle.getBoolean(LAST_ABILITY_WAS_ROCKFALL);

		if (bundle.contains(ROCK_FROM_POS)) {
			throwingRocksFromPos = bundle.getIntArray(ROCK_FROM_POS);
		}
		throwingRockToPos = bundle.getInt(ROCK_TO_POS);

		sapperID = bundle.getInt(SAPPER_ID);
		if (bundle.contains(SAPPER_SPAWNS)) {
			sapperSpawns = bundle.getIntArray(SAPPER_SPAWNS);
		}

		if (hits >= 3){
			BossHealthBar.assignBoss(this);
		}
	}
}
