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

package com.watabou.utils;

import com.watabou.noosa.Game;

public class GameMath {
	
	public static float speed( float speed, float acc ) {
		
		if (acc != 0) {
			speed += acc * Game.elapsed;
		}
		
		return speed;
	}

	public static float gate( float var1, float value, float var2 ) {
		float min, max;
		if (var1 < var2) {
			min = var1;
			max = var2;
		}
		else {
			min = var2;
			max = var1;
		}
		if (value < min)
			return min;
		else
			return Math.min(value, max);
	}
	public static int gate( int var1, int value, int var2 ) {
		int min, max;
		if (var1 < var2) {
			min = var1;
			max = var2;
		}
		else {
			min = var2;
			max = var1;
		}
		if (value < min)
			return min;
		else
			return Math.min(value, max);
	}
	public static double gate( double var1, double value, double var2 ) {
		double min, max;
		if (var1 < var2) {
			min = var1;
			max = var2;
		}
		else {
			min = var2;
			max = var1;
		}
		if (value < min)
			return min;
		else
			return Math.min(value, max);
	}
	public static long gate( long var1, long value, long var2 ) {
		long min, max;
		if (var1 < var2) {
			min = var1;
			max = var2;
		}
		else {
			min = var2;
			max = var1;
		}
		if (value < min)
			return min;
		else
			return Math.min(value, max);
	}
}
