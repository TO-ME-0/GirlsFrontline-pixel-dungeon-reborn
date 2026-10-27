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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrystalSpireSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class VaultLaser extends NPC {

	{
		//4.0 原版用 SentrySprite.VaultLaser 内部类，reborn 无对应贴图资源；
		// WardSprite.linkVisuals 会强转 WandOfWarding.Ward 导致 ClassCastException，改用红色水晶尖塔（固定构装体造型）
		spriteClass = CrystalSpireSprite.Red.class;

		properties.add(Char.Property.IMMOVABLE);
	}

	//若以后需要单个激光塔每回合发射多发，可改为 int[][]
	public int[] laserDirs;
	public int laserDirIdx;

	public int curCooldown;
	public int afterShotCooldown;

	public int shotsAfterCooldown = 1;
	private int shotsFired = 0;

	//在某些规律明显的配置中无需预警
	public boolean giveWarning = true;

	//所有激光塔合计至多每 80ms 播放一次音效
	private static long SFXLastPlayed = 0;

	//避免大量哨兵每回合都调用 setSeen
	private boolean seen = false;

	@Override
	protected boolean act() {
		if (!seen && Dungeon.level.heroFOV[pos]){
			Bestiary.setSeen(getClass());
			seen = true;
		}

		curCooldown--;
		if (curCooldown <= 0){

			Ballistica beam = new Ballistica(pos, laserDirs[laserDirIdx], Ballistica.STOP_SOLID);
			boolean visible = false;
			boolean observe = false;
			//reborn 的 subPath 末端为闭区间，与 SPD4.0 的开区间语义相差 1
			for (int cell : beam.subPath(1, beam.dist - 1)){
				if (Dungeon.level.heroFOV[cell]){
					visible = true;
				}
				if (Dungeon.level.flammable[cell]){
					Dungeon.level.destroy( cell );
					observe = true;
					GameScene.updateMap( cell );
				}
				Char ch = Actor.findChar(cell);
				if (ch != null && ch.alignment == Alignment.ALLY){
					ch.damage(Random.NormalIntRange(10, 20), new Eye.DeathGaze());
					if (ch.sprite.visible){
						ch.sprite.flash();
						CellEmitter.center( pos ).burst( PurpleParticle.BURST, Random.IntRange( 1, 2 ) );
					}
					if (ch == Dungeon.hero()){
						Sample.INSTANCE.play( Assets.Sounds.RAY );
						SFXLastPlayed = GirlsFrontlinePixelDungeon.realTime;
						if (Imp.Quest.cur().hazardFreebies > 0){
							Imp.Quest.cur().hazardFreebies--;
						} else {
							Statistics.questScores[3] -= 100;
						}
						if (!ch.isAlive()){
							Dungeon.fail( getClass() );
							GLog.n( Messages.get(this, "ondeath") );
						}
					}
				}
			}
			if (visible){
				sprite.parent.add(new Beam.DeathRay(sprite.center(), DungeonTilemap.raisedTileCenterToWorld(beam.collisionPos)));
				if (SFXLastPlayed+80 < GirlsFrontlinePixelDungeon.realTime) {
					Sample.INSTANCE.play(Assets.Sounds.RAY, 0.5f);
					SFXLastPlayed = GirlsFrontlinePixelDungeon.realTime;
				}
			}

			if (observe){
				Dungeon.observe();
			}

			laserDirIdx++;
			if (laserDirIdx >= laserDirs.length){
				laserDirIdx = 0;
			}

			shotsFired++;
			if (shotsFired < shotsAfterCooldown){
				curCooldown = 1;
			} else {
				shotsFired = 0;
				curCooldown = afterShotCooldown;
			}

		}

		if (curCooldown == 1 && giveWarning){

			Ballistica nextBeam = new Ballistica(pos, laserDirs[laserDirIdx], Ballistica.STOP_SOLID);
			boolean visible = false;
			//reborn 的 subPath 末端为闭区间，与 SPD4.0 的开区间语义相差 1
			for (int cell : nextBeam.subPath(1, nextBeam.dist - 1)){
				if (Dungeon.level.heroFOV[cell]) {
					visible = true;
					break;
				}
			}
			if (visible){
				for (int cell : nextBeam.subPath(1, nextBeam.dist - 1)) {
					sprite.parent.add(new TargetedCell(cell, 0xFF0000));
					if (Actor.findChar(cell) == Dungeon.hero()){
						//主要防止英雄在被瞄准时自动拾取物品
						Dungeon.hero().interrupt();
					}
				}
			}

		}

		throwItems();

		spend(TICK);
		return true;
	}

	@Override
	public boolean isImmune(Class effect) {
		return true;
	}

	@Override
	public boolean isInvulnerable(Class effect) {
		return true;
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public boolean interact(Char c) {
		return true;
	}

	private static final String LASER_DIRS = "laser_dirs";
	private static final String LASER_DIR_IDX = "laser_dir_idx";

	private static final String AFTER_SHOT_COOLDOWN = "after_shot_cooldown";
	private static final String CUR_COOLDOWN = "cur_cooldown";

	private static final String SHOTS = "shots";
	private static final String SHOTS_FIRED = "shots_fired";

	private static final String WARNING = "warning";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LASER_DIRS, laserDirs);
		bundle.put(LASER_DIR_IDX, laserDirIdx);
		bundle.put(AFTER_SHOT_COOLDOWN, afterShotCooldown);
		bundle.put(CUR_COOLDOWN, curCooldown);
		bundle.put(SHOTS, shotsAfterCooldown);
		bundle.put(SHOTS_FIRED, shotsFired);
		bundle.put(WARNING, giveWarning);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		laserDirs = bundle.getIntArray(LASER_DIRS);
		laserDirIdx = bundle.getInt(LASER_DIR_IDX);
		if (bundle.contains(AFTER_SHOT_COOLDOWN)){
			afterShotCooldown = bundle.getInt(AFTER_SHOT_COOLDOWN);
			curCooldown = bundle.getInt(CUR_COOLDOWN);
			shotsAfterCooldown = bundle.getInt(SHOTS);
			shotsFired = bundle.getInt(SHOTS_FIRED);
			giveWarning = bundle.getBoolean(WARNING);
		//3.3.X 旧存档兼容
		} else {
			afterShotCooldown = Random.IntRange(3, 7);
			curCooldown = Random.IntRange(1, afterShotCooldown);
		}
	}

}
