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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.WelcomeScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PlatformSupport;

public class GirlsFrontlinePixelDungeon extends Game {

	//variable constants for specific older versions of shattered, used for data conversion
	//versions older than v0.9.2b are no longer supported, and data from them is ignored
	public static final int v0_9_2b  = 531;
	public static final int v0_9_3c  = 557; //557 on iOS, 554 on other platforms

	public static final int v1_0_3   = 574;
	public static final int v1_1_2   = 587;
	public static final int v1_2_0   = 609;
	
	//保存上一个场景类，用于返回功能
	public static Class<? extends PixelScene> previousSceneClass;
	
	public GirlsFrontlinePixelDungeon( PlatformSupport platform ) {
		super(sceneClass == null
				? WelcomeScene.class
				: sceneClass
				, platform );

		//Old And New
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff.class,
				"com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.G11" );

		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity.class,
				"com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfElements" );
		//元素合并至韧性。

		//怪物正名后的旧存档类名兼容（旧 Crab 残兽 → 新 Brute 残兽，类文件改名但内容相同）
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Crab" );
		//旧胡蜂（旧类名 Brute）的狂暴 buff → 新 Vespid.VespidRage
		//注意：旧胡蜂本体的类名 Brute 与新残兽重名，无法做别名迁移，旧关卡存档中的胡蜂会被读成残兽（不会崩溃）
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Vespid.VespidRage.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute$BruteRage" );

		//怪物贴图同名化后的旧存档类名兼容
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Striker.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Striker.Focus.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk$Focus" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Prowler.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Manti.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Golem" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Jaeger.RedJaeger.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman$RedShaman" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Jaeger.BlueJaeger.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman$BlueShaman" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Jaeger.PurpleJaeger.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman$PurpleShaman" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.JaegerLock.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.JaegerTrickster.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollTrickster" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Undead.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Ghoul" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Undead.UndeadLifeLink.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Ghoul$GhoulLifeLink" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Archer.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Kentauros" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Uro.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Uro.BombAbility.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu$BombAbility" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Uro.FireAbility.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu$FireAbility" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Uro.ShockerAbility.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu$ShockerAbility" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Disloli.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Disloli.DKGhoul.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing$DKGhoul" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Disloli.DKMonk.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing$DKMonk" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Disloli.DKWarlock.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing$DKWarlock" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Disloli.DKGolem.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing$DKGolem" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Disloli.Summoning.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing$Summoning" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Disloli.KingDamager.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing$KingDamager" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Disloli.DKBarrior.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing$DKBarrior" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Excutioner.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gager.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM300" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gager.FallingRockBuff.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM300$FallingRockBuff" );
		Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.SksStatue.class,
				"com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue" );

	}
	public static void reportException( Throwable tr ) {
		Game.GameMode = Dungeon.GameMode;
		Game.Seed = Dungeon.cur().seed;
		Game.Challenges = Dungeon.challenges;
		Game.reportException(tr);
	}
	public static void saveCrashReport( Throwable tr ){
		Game.GameMode = Dungeon.GameMode;
		Game.Seed = Dungeon.cur().seed;
		Game.Challenges = Dungeon.challenges;
		if (instance != null)
			Game.saveCrashReport(tr);
	}
	@Override
	public void create() {
		super.create();

		updateSystemUI();
		SPDAction.loadBindings();
		
		Music.INSTANCE.enable( SPDSettings.music() );
		Music.INSTANCE.volume( SPDSettings.musicVol()*SPDSettings.musicVol()/100f );
		Sample.INSTANCE.enable( SPDSettings.soundFx() );
		Sample.INSTANCE.volume( SPDSettings.SFXVol()*SPDSettings.SFXVol()/100f );

		Sample.INSTANCE.load( Assets.Sounds.all );
		
	}

	@Override
	public void finish() {
		if (!DeviceCompat.isiOS()) {
			super.finish();
		} else {
			//can't exit on iOS (Apple guidelines), so just go to title screen
			switchScene(TitleScene.class);
		}
	}

	public static void switchNoFade(Class<? extends PixelScene> c){
		switchNoFade(c, null);
	}

	public static void switchNoFade(Class<? extends PixelScene> c, SceneChangeCallback callback) {
		PixelScene.noFade = true;
		switchScene( c, callback );
	}
	
	public static void seamlessResetScene(SceneChangeCallback callback) {
		if (scene() instanceof PixelScene){
			((PixelScene) scene()).saveWindows();
			switchNoFade((Class<? extends PixelScene>) sceneClass, callback );
		} else {
			resetScene();
		}
	}
	
	public static void seamlessResetScene(){
		seamlessResetScene(null);
	}
	
	@Override
	protected void switchScene() {
		//在切换场景之前记录当前场景
		if (scene instanceof PixelScene) {
			previousSceneClass = (Class<? extends PixelScene>) scene.getClass();
		}
		super.switchScene();
		if (scene instanceof PixelScene){
			((PixelScene) scene).restoreWindows();
		}
	}
	
	@Override
	public void resize( int width, int height ) {
		if (width == 0 || height == 0){
			return;
		}

		if (scene instanceof PixelScene &&
				(height != Game.height || width != Game.width)) {
			PixelScene.noFade = true;
			((PixelScene) scene).saveWindows();
		}

		super.resize( width, height );

		updateDisplaySize();

	}
	
	@Override
	public void destroy(){
		super.destroy();
		GameScene.endActorThread();
	}
	
	public void updateDisplaySize(){
		platform.updateDisplaySize();
	}

	public static void updateSystemUI() {
		platform.updateSystemUI();
	}
}