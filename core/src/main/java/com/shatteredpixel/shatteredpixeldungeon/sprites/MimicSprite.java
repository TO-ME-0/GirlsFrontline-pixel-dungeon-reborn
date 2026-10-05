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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.watabou.noosa.TextureFilm;

public class MimicSprite extends MobSprite {

	protected Animation advancedHiding;

	protected Animation hiding;

	{
		//adjust shadow slightly to account for 1 empty bottom pixel (used for border while hiding)
		perspectiveRaise    = 5 / 16f; //5 pixels
		shadowWidth         = 1f;
		shadowOffset        = -0.4f;
	}

	protected int texOffset(){
		return 0;
	}

	public MimicSprite() {
		super();

		int c = texOffset();

		texture( Assets.Sprites.MIMIC );

		TextureFilm frames = new TextureFilm( texture, 16, 16 );

		//拟态怪之牙饰品：隐秘拟态怪始终显示完全闭合的宝箱，无细微破绽动画（实现见 MimicTooth）
		advancedHiding = new Animation( 1, true );
		advancedHiding.frames( frames, 0+c);

		hiding = new Animation( 1, true );
		hiding.frames( frames, 0+c, 0+c, 0+c, 0+c, 0+c, 1+c);

		idle = new Animation( 5, true );
		idle.frames( frames, 2+c, 2+c, 2+c, 3+c, 3+c );

		run = new Animation( 10, true );
		run.frames( frames, 2+c, 3+c, 4+c, 5+c, 5+c, 4+c, 3+c );

		attack = new Animation( 10, false );
		attack.frames( frames, 2+c, 6+c, 7+c, 8+c );

		die = new Animation( 5, false );
		die.frames( frames, 9+c, 10+c, 11+c );

		play( idle );
	}
	
	@Override
	public void linkVisuals(Char ch) {
		super.linkVisuals(ch);
		if (ch.alignment == Char.Alignment.NEUTRAL) {
			hideMimic(ch);
		}
	}

	public void hideMimic(){
		hideMimic(null);
	}

	public void hideMimic(Char ch){
		if (ch instanceof Mimic && ((Mimic) ch).stealthy()){
			play(advancedHiding);
		} else {
			play(hiding);
		}
		hideSleep();
	}

	@Override
	protected boolean showPosIndicator() {
		//潜伏（伪装成宝箱）状态下不显示位置图标
		return curAnim != hiding && curAnim != advancedHiding;
	}

	@Override
	public void showSleep() {
		if (curAnim == hiding || curAnim == advancedHiding){
			return;
		}
		super.showSleep();
	}

	public static class Golden extends MimicSprite{
		@Override
		protected int texOffset() {
			return 16;
		}
	}

	public static class Crystal extends MimicSprite{
		@Override
		protected int texOffset() {
			return 32;
		}
	}

	//拟态怪之牙饰品：乌木拟态怪使用 mimic.png 第四排贴图（实现见 MimicTooth）
	public static class Ebony extends MimicSprite{
		@Override
		protected int texOffset() {
			return 48;
		}
	}

}
