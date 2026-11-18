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

package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MagicalFireRoom;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class FrostTrap extends Trap {

	{
		color = WHITE;
		shape = STARS;
	}

	@Override
	public void activate() {
		
		if (Dungeon.level.heroFOV[ pos ]){
			Splash.at( pos, 0xFFB2D6FF, 5);
			Sample.INSTANCE.play( Assets.Sounds.SHATTER );
		}
		
		//火墙（永恒之火）会挡住霜冻扩散：避免大冰霜陷阱的冰越过火墙，冻碎火焰房内的药水
		boolean[] passable = BArray.not( Dungeon.level.solid, null );
		MagicalFireRoom.EternalFire eternalFire = (MagicalFireRoom.EternalFire)Dungeon.level.blobs.get( MagicalFireRoom.EternalFire.class );
		if (eternalFire != null && eternalFire.volume > 0){
			for (int i = 0; i < passable.length; i++){
				if (eternalFire.cur[i] > 0){
					passable[i] = false;
				}
			}
		}

		PathFinder.buildDistanceMap( pos, passable, 2 );
		for (int i = 0; i < PathFinder.distance().length; i++) {
			if (PathFinder.distance()[i] < Integer.MAX_VALUE) {
				GameScene.add(Blob.seed(i, 20, Freezing.class));
			}
		}
	}
}
