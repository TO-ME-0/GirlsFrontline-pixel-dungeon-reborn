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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.custom.utils.CrashHandler;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

public class BusyIndicator extends Image {

	//持续转圈超过该时长（秒）后记录一次卡死日志
	private static final float STUCK_REPORT_DELAY = 120f;

	//本次连续转圈的累计时长
	private float stuckTime = 0f;
	//本次连续转圈是否已经记录过，避免重复写日志
	private boolean reported = false;

	public BusyIndicator() {
		super();
		copy( Icons.BUSY.get() );
		
		origin.set( width / 2, height / 2 );
		angularSpeed = 720;
	}
	
	@Override
	public void update() {
		super.update();

		Hero hero = Dungeon.hero();
		visible = hero != null && hero.isAlive() && !hero.ready;

		//正常的长按休息（长按等待）与深度睡眠会持续转圈，不属于卡死
		if (!visible || hero.resting || hero.buff(MagicalSleep.class) != null) {
			stuckTime = 0f;
			reported = false;
			return;
		}

		//限制单帧步长，避免切后台等导致的超大 delta 被误算作转圈时长
		stuckTime += Math.min(Game.elapsed, 0.5f);
		if (!reported && stuckTime >= STUCK_REPORT_DELAY) {
			reported = true;
			CrashHandler.getInstance().saveHangReport();
		}
	}
}
