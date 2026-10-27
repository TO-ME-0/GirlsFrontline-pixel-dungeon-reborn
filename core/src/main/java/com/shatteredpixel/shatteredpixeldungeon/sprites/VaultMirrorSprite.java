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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.watabou.noosa.TextureFilm;

public class VaultMirrorSprite extends MobSprite {

	public VaultMirrorSprite () {
		super();

		renderShadow = false;
		//reborn 的 CharSprite 无 visibleOutOfFFOV 字段（4.0 用于让镜子在视野外仍渲染），此处省略

		texture( Assets.Sprites.VAULT_MIRROR );

		TextureFilm frames = new TextureFilm( texture, 16, 23 );

		idle = new Animation( 15, true );
		//先静止 4 秒，然后播放闪光动画
		idle.frames( frames, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				1, 2, 3, 4, 5, 6, 7, 8, 9 );

		run = idle.clone();

		attack = idle.clone();

		die = idle.clone();

		play( idle );
	}

	@Override
	public void turnTo(int from, int to) {
		flipHorizontal = false;
	}

	@Override
	public void link(Char ch) {
		super.link(ch);
		renderShadow = false;
	}

}
