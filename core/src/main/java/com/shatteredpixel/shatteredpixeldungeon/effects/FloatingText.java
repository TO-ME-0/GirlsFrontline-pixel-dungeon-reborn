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

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Stone;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEvasion;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.FerretTuft;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.RenderedText;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;
import com.watabou.utils.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class FloatingText extends RenderedTextBlock {

	private static final float LIFESPAN	= 1f;
	private static final float DISTANCE	= DungeonTilemap.SIZE;

	public static final int ICON_WIDTH = 7;
	public static final int ICON_HEIGHT = 8;
	public static TextureFilm iconFilm = new TextureFilm( Assets.Effects.TEXT_ICONS, ICON_WIDTH, ICON_HEIGHT );

	public static int NO_ICON   = -1;

	//物理伤害图标
	public static int PHYS_DMG          = 0;
	public static int PHYS_DMG_NO_BLOCK = 1;
	public static int MAGIC_DMG         = 2;
	public static int PICK_DMG          = 3;

	//减益/持续伤害图标
	public static int HUNGER    = 5;
	public static int BURNING   = 6;
	public static int SHOCKING  = 7;
	public static int FROST     = 8;
	public static int WATER     = 9;
	public static int BLEEDING  = 10;
	public static int TOXIC     = 11;
	public static int CORROSION = 12;
	public static int POISON    = 13;
	public static int OOZE      = 14;
	public static int DEFERRED  = 15;
	public static int CORRUPTION= 16;
	public static int AMULET    = 17;

	//增益图标
	public static int HEALING   = 18;
	public static int SHIELDING = 19;
	public static int EXPERIENCE= 20;
	public static int STRENGTH  = 21;

	//货币图标
	public static int GOLD      = 23;
	public static int ENERGY    = 24;

	//命中原因图标
	public static int HIT_WEP   = 36;
	public static int HIT_ARM   = 37;
	public static int HIT_BLS   = 38;
	public static int HIT_HEX   = 39;
	public static int HIT_DAZE  = 40;
	public static int HIT_ACC   = 41;
	public static int HIT_EVA   = 42;
	public static int HIT_LIQ   = 43;
	public static int HIT_DANCE = 44;
	public static int HIT_SUPR  = 45;
	public static int HIT_PRES  = 46;
	public static int HIT_MOMEN = 47;

	//破甲类命中图标所在行（图集上为额外一行）

	//闪避原因图标
	public static int MISS_WEP  = 72;
	public static int MISS_ARM  = 73;
	public static int MISS_BLS  = 74;
	public static int MISS_HEX  = 75;
	public static int MISS_DAZE = 76;
	public static int MISS_ACC  = 77;
	public static int MISS_EVA  = 78;
	public static int MISS_LIQ  = 79;
	public static int MISS_DEF  = 80;
	public static int MISS_TUFT = 81;
	public static int MISS_RUN  = 82;

	private Image icon;
	private boolean iconLeft;

	private float timeLeft;

	private int key = -1;

	private static final SparseArray<ArrayList<FloatingText>> stacks = new SparseArray<>();

	public FloatingText() {
		super(9*PixelScene.defaultZoom);
		setHightlighting(false);
	}

	@Override
	public void update() {
		super.update();

		if (timeLeft >= 0) {
			if ((timeLeft -= Game.elapsed) <= 0) {
				kill();
			} else {
				float p = timeLeft / LIFESPAN;
				alpha( p > 0.5f ? 1 : p * 2 );

				float yMove = (DISTANCE / LIFESPAN) * Game.elapsed;
				y -= yMove;
				for (RenderedText t : words){
					t.y -= yMove;
				}

				if (icon != null){
					icon.alpha(p > 0.5f ? 1 : p * 2);
					icon.y -= yMove;
				}
			}
		}
	}

	@Override
	protected synchronized void layout() {
		super.layout();
		if (icon != null){
			if (iconLeft){
				icon.x = left();
			} else {
				icon.x = left() + width() - icon.width();
			}
			icon.x = PixelScene.align(Camera.main, icon.x);
			icon.y = PixelScene.align(Camera.main, top());
		}
	}

	@Override
	public float width() {
		float width = super.width();
		if (icon != null){
			width += icon.width()-0.5f;
		}
		return width;
	}

	@Override
	public void kill() {
		if (key != -1) {
			synchronized (stacks) {
				stacks.get(key).remove(this);
			}
			key = -1;
		}
		super.kill();
	}

	@Override
	public void destroy() {
		kill();
		super.destroy();
	}

	public void reset( float x, float y, String text, int color, int iconIdx, boolean left ) {

		revive();

		zoom( 1 / (float)PixelScene.defaultZoom );

		text( text );
		hardlight( color );

		if (iconIdx != NO_ICON){
			icon = new Image( Assets.Effects.TEXT_ICONS);
			icon.frame(iconFilm.get(iconIdx));
			add(icon);
			iconLeft = left;
			if (iconLeft){
				align(RIGHT_ALIGN);
			}
		} else {
			icon = null;
		}

		setPos(
			PixelScene.align( Camera.main, x - width() / 2),
			PixelScene.align( Camera.main, y - height())
		);

		timeLeft = LIFESPAN;
	}

	/* STATIC METHODS */

	public static void show( float x, float y, String text, int color) {
		show(x, y, -1, text, color, -1, false);
	}

	public static void show( float x, float y, int key, String text, int color) {
		show(x, y, key, text, color, -1, false);
	}

	public static void show( float x, float y, int key, String text, int color, int iconIdx, boolean left ) {
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				FloatingText txt = GameScene.status();
				if (txt != null){
					txt.reset(x, y, text, color, iconIdx, left);
					if (key != -1) push(txt, key);
				}
			}
		});
	}

	private static void push( FloatingText txt, int key ) {

		synchronized (stacks) {
			txt.key = key;

			ArrayList<FloatingText> stack = stacks.get(key);
			if (stack == null) {
				stack = new ArrayList<>();
				stacks.put(key, stack);
			}

			if (stack.size() > 0) {
				FloatingText below = txt;
				int aboveIndex = stack.size() - 1;
				int numBelow = 0;
				while (aboveIndex >= 0) {
					numBelow++;
					FloatingText above = stack.get(aboveIndex);
					if (above.bottom() + 4 > below.top()) {
						above.setPos(above.left(), below.top() - above.height() - 4);

						//reduce remaining time on texts being nudged up, to prevent spam
						above.timeLeft = Math.min(above.timeLeft, LIFESPAN-(numBelow/5f));
						above.timeLeft = Math.max(above.timeLeft, 0);

						below = above;
						aboveIndex--;
					} else {
						break;
					}
				}
			}

			stack.add(txt);
		}
	}

	// *** 以下方法用于计算飘字原因图标：在基础命中/闪避判定之外，标明各项效果造成的影响 ***
	// 这套逻辑确实比较混乱：并非在计算命中/闪避的过程中逐项追踪修正，
	// 而是根据最终掷点结果“倒推”修正来源。
	// 因此这里存在大量重复的逻辑与数值，以及别扭的变通处理。
	// 不过……大体上能用。

	public static int getHitReasonIcon(Char attacker, float accRoll, Char defender, float defRoll){
		HashMap<Integer, Float> hitReasons = new HashMap<>();

		//先处理若干必定命中的情况
		if (accRoll == Char.INFINITE_ACCURACY && attacker.invisible > 0){
			return HIT_SUPR;
		}
		if (defRoll == 0 && defender instanceof Mob && ((Mob) defender).surprisedBy(attacker)){
			return HIT_SUPR;
		}

		KindOfWeapon wep = null;
		if (attacker instanceof Hero) wep = ((Hero) attacker).belongings.weapon();
		if (attacker instanceof Statue) wep = ((Statue)attacker).weapon();

		Armor arm = null;
		if (defender instanceof Hero) arm = ((Hero) defender).belongings.armor();
		if (defender instanceof PrismaticImage) arm = Dungeon.hero().belongings.armor();
		if (defender instanceof ArmoredStatue) arm = ((ArmoredStatue)defender).armor();
		if (defender instanceof DriedRose.GhostHero) arm = ((DriedRose.GhostHero)defender).armor();

		//最后一项检查
		if (defRoll == 0 && arm != null && arm.hasGlyph(Stone.class, defender)){
			return HIT_ARM;
		}

		//命中提升（始终>1）
		if (wep != null && wep.accuracyFactor(attacker) > 1){
			hitReasons.put( HIT_WEP, wep.accuracyFactor(attacker));
		}
		float blessBoost = 1; //多个来源共同影响该图标
		if (attacker.buff(ChampionEnemy.class) != null
			&& attacker.buff(ChampionEnemy.class).evasionAndAccuracyFactor() > 1){
			blessBoost *= attacker.buff(ChampionEnemy.class).evasionAndAccuracyFactor();
		}
		if (attacker.buff(Bless.class) != null) blessBoost *= 1.25f;
		if (blessBoost > 1f) hitReasons.put(HIT_BLS, blessBoost);
		if (RingOfAccuracy.accuracyMultiplier(attacker) > 1)    hitReasons.put(HIT_ACC, RingOfAccuracy.accuracyMultiplier(attacker));
		if (wep instanceof MissileWeapon) {
			if (attacker instanceof Hero
					&& attacker.buff(Momentum.class) != null
					&& attacker.buff(Momentum.class).freerunning()
					&& ((Hero)attacker).hasTalent(Talent.PROJECTILE_MOMENTUM)) {
				hitReasons.put(HIT_MOMEN, 1f + ((Hero) attacker).pointsInTalent(Talent.PROJECTILE_MOMENTUM) / 2f);
			}
		}

		//闪避降低（始终<1）
		if (defender.buff(Hex.class) != null)                   hitReasons.put(HIT_HEX, 0.8f);
		if (RingOfEvasion.evasionMultiplier(defender) < 1)      hitReasons.put(HIT_EVA, RingOfEvasion.evasionMultiplier(defender));
		if (arm != null && arm.evasionFactor(defender, 100) < 100) {
			//护甲的闪避加成本是固定值，这里按百分比折算，确实很别扭
			Armor.testingNoArmDefSkill = true;
			int baseDef = defender.defenseSkill(attacker);
			Armor.testingNoArmDefSkill = false;
			hitReasons.put(HIT_ARM, defender.defenseSkill(attacker)/(float)baseDef);
		}
		//英雄眩晕时闪避减半；怪物眩晕则视为必定命中
		if (defender.paralysed > 0)  {
			if (defender instanceof Hero)   hitReasons.put(HIT_SUPR, 0.5f);
			else                            return HIT_SUPR;
		}

		//按修正幅度从大到小排序
		ArrayList<Integer> sortedReasons = new ArrayList<>(hitReasons.keySet());
		Collections.sort(sortedReasons, new Comparator<Integer>() {
			@Override
			public int compare(Integer a, Integer b) {
				float a1 = hitReasons.get(a) >= 1f ? hitReasons.get(a) : 1 / hitReasons.get(a);
				float b1 = hitReasons.get(b) >= 1f ? hitReasons.get(b) : 1 / hitReasons.get(b);
				return (int)Math.signum(b1 - a1);
			}
		});

		for (Integer reason : sortedReasons){
			if (hitReasons.get(reason) >= 1f) {
				accRoll /= hitReasons.get(reason);
			} else {
				defRoll /= hitReasons.get(reason);
			}
			if (accRoll < defRoll){
				return reason;
			}
		}
		return -1;
	}

	public static int getMissReasonIcon(Char attacker, float accRoll, Char defender, float defRoll){
		HashMap<Integer, Float> missReasons = new HashMap<>();

		KindOfWeapon wep = null;
		if (attacker instanceof Hero) wep = ((Hero) attacker).belongings.weapon();
		if (attacker instanceof Statue) wep = ((Statue)attacker).weapon();

		Armor arm = null;
		if (defender instanceof Hero) arm = ((Hero) defender).belongings.armor();
		if (defender instanceof PrismaticImage) arm = Dungeon.hero().belongings.armor();
		if (defender instanceof ArmoredStatue) arm = ((ArmoredStatue)defender).armor();
		if (defender instanceof DriedRose.GhostHero) arm = ((DriedRose.GhostHero)defender).armor();

		//闪避提升（始终>1）
		float blessBoost = 1; //多个来源共同影响该图标
		if (defender.buff(ChampionEnemy.class) != null
				&& defender.buff(ChampionEnemy.class).evasionAndAccuracyFactor() > 1){
			blessBoost *= defender.buff(ChampionEnemy.class).evasionAndAccuracyFactor();
		}
		if (defender.buff(Bless.class) != null) blessBoost *= 1.25f;
		if (blessBoost > 1f)                                    missReasons.put(MISS_BLS, blessBoost);
		if (FerretTuft.evasionMultiplier() > 1)                 missReasons.put(MISS_TUFT, FerretTuft.evasionMultiplier());
		if (RingOfEvasion.evasionMultiplier(defender) > 1)      missReasons.put(MISS_EVA, RingOfEvasion.evasionMultiplier(defender));
		if (arm != null && arm.evasionFactor(defender, 100) > 100) {
			//护甲的闪避加成本是固定值，这里按百分比折算，确实很别扭
			Armor.testingNoArmDefSkill = true;
			int baseDef = defender.defenseSkill(attacker);
			Armor.testingNoArmDefSkill = false;
			if (defender.buff(Momentum.class) != null){
				//这里稍微开了个挂：闪避强化（aug）也被一并算进去了
				missReasons.put(MISS_RUN, defender.defenseSkill(attacker) / (float) baseDef);
			} else {
				missReasons.put(MISS_ARM, defender.defenseSkill(attacker) / (float) baseDef);
			}
		}

		//命中降低（始终<1）
		if (wep != null && wep.accuracyFactor(attacker) < 1){
			missReasons.put( MISS_WEP, wep.accuracyFactor(attacker));
		}
		if (attacker.buff(Hex.class) != null)                   missReasons.put(MISS_HEX, 0.8f);
		if (RingOfAccuracy.accuracyMultiplier(attacker) < 1)    missReasons.put(MISS_ACC, RingOfAccuracy.accuracyMultiplier(attacker));

		//按修正幅度从大到小排序
		ArrayList<Integer> sortedReasons = new ArrayList<>(missReasons.keySet());
		Collections.sort(sortedReasons, new Comparator<Integer>() {
			@Override
			public int compare(Integer a, Integer b) {
				float a1 = missReasons.get(a) >= 1f ? missReasons.get(a) : 1 / missReasons.get(a);
				float b1 = missReasons.get(b) >= 1f ? missReasons.get(b) : 1 / missReasons.get(b);
				return (int)Math.signum(b1 - a1);
			}
		});

		for (Integer reason : sortedReasons){
			if (missReasons.get(reason) >= 1f) {
				defRoll /= missReasons.get(reason);
			} else {
				accRoll /= missReasons.get(reason);
			}
			if (defRoll < accRoll){
				return reason;
			}
		}
		return -1;
	}
}
