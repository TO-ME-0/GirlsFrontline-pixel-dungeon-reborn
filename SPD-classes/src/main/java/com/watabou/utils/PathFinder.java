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

import java.util.Arrays;
import java.util.LinkedList;

public class PathFinder {

	//游戏主线程共享的主上下文
	private static final PathFinder MAIN = new PathFinder();

	//查种 worker 线程的私有上下文
	private static final ThreadLocal<PathFinder> SEARCH_CTX = new ThreadLocal<>();

	public static PathFinder cur() {
		PathFinder s = SEARCH_CTX.get();
		return (s != null) ? s : MAIN;
	}

	public static void enterSearchContext() {
		SEARCH_CTX.set(new PathFinder());
	}

	public static void exitSearchContext() {
		SEARCH_CTX.remove();
	}

	private PathFinder() {
	}

	//单向行走方向掩码，位序与 NEIGHBOURS8 一致：NW,N,NE,W,E,SW,S,SE
	//对应 dir 数组顺序
	private static final int[] DIR_BITS = {
			1<<3, 1<<4, 1<<1,
			1<<6,     	1<<0,
			1<<2, 1<<5, 1<<7
	};
	//对应 dirLR 数组顺序
	private static final int[] DIRLR_BITS = {
			1<<0, 1<<3, 1<<5,
			1<<1,     	1<<6,
			1<<2, 1<<4, 1<<7
	};
	//反向扩散时实际移动方向为 -dirLR[i]
	private static final int[] DIRLR_OPP_BITS = {
			1<<7, 1<<4, 1<<2,
			1<<6,     	1<<1,
			1<<5, 1<<3, 1<<0
	};

	//distance 查询结果（buildDistanceMap 后读取）
	public static int[] distance() {
		return cur().distance;
	}

	public void setMapSize( int width, int height ) {

		this.width = width;
		size = width * height;

		distance = new int[size];
		goals = new boolean[size];
		queue = new int[size];

		maxVal = new int[size];
		Arrays.fill(maxVal, Integer.MAX_VALUE);

		dir = new int[]{-1, +1, -width, +width, -width-1, -width+1, +width-1, +width+1};
		dirLR = new int[]{-1-width, -1, -1+width, -width, +width, +1-width, +1, +1+width};

		NEIGHBOURS4 = new int[]{-width, -1, +1, +width};
		NEIGHBOURS8 = new int[]{-width-1, -width, -width+1, -1, +1, +width-1, +width, +width+1};
		NEIGHBOURS9 = new int[]{-width-1, -width, -width+1, -1, 0, +1, +width-1, +width, +width+1};
		NEIGHBOURS16 = new int[]{
				-2*width-2, -2*width-1, -2*width, -2*width+1, -2*width+2,
				-width-2,                                    -width+2,
				-2,                                          +2,
				+width-2,                                    +width+2,
				+2*width-2, +2*width-1, +2*width, +2*width+1, +2*width+2
		};
		NEIGHBOURS25 = new int[]{
				-2*width-2, -2*width-1, -2*width, -2*width+1, -2*width+2,
				-width-2,   -width-1,   -width,   -width+1,   -width+2,
				-2,         -1,          0,        +1,         +2,
				+width-2,   +width-1,    +width,   +width+1,   +width+2,
				+2*width-2, +2*width-1, +2*width, +2*width+1, +2*width+2
		};


		CIRCLE4 = new int[]{-width, +1, +width, -1};
		CIRCLE8 = new int[]{-width-1, -width, -width+1, +1, +width+1, +width, +width-1, -1};
	}

	private int[] distance;
	private int[] maxVal;

	private boolean[] goals;
	private int[] queue;

	private int size = 0;
	private int width = 0;

	private int[] dir;
	private int[] dirLR;

	//performance-light shortcuts for some common pathfinder cases
	//they are in array-access order for increased memory performance
	public static int[] NEIGHBOURS4() {
		return cur().NEIGHBOURS4;
	}
	public static int[] NEIGHBOURS8() {
		return cur().NEIGHBOURS8;
	}
	public static int[] NEIGHBOURS9() {
		return cur().NEIGHBOURS9;
	}
	public static int[] NEIGHBOURS16() {
		return cur().NEIGHBOURS16;
	}
	public static int[] NEIGHBOURS25() {
		return cur().NEIGHBOURS25;
	}

	//similar to their equivalent neighbour arrays, but the order is clockwise.
	//Useful for some logic functions, but is slower due to lack of array-access order.
	public static int[] CIRCLE4() {
		return cur().CIRCLE4;
	}
	public static int[] CIRCLE8() {
		return cur().CIRCLE8;
	}

	private int[] NEIGHBOURS4;
	private int[] NEIGHBOURS8;
	private int[] NEIGHBOURS9;
	private int[] NEIGHBOURS16;
	private int[] NEIGHBOURS25;
	private int[] CIRCLE4;
	private int[] CIRCLE8;

	public static LinkedList<Integer> find( int from, int to, boolean[] passable ) {
		return find( from, to, passable, null );
	}

	public static LinkedList<Integer> find( int from, int to, boolean[] passable, int[] oneWay ) {
		PathFinder p = cur();
		if (oneWay != null && oneWay.length != p.size) oneWay = null;
		return p.find_( from, to, passable, oneWay );
	}

	private LinkedList<Integer> find_( int from, int to, boolean[] passable, int[] oneWay ) {

		if (!buildDistanceMap( from, to, passable, oneWay )) {
			return null;
		}

		LinkedList<Integer> result = new LinkedList<>();
		int s = from;

		// From the starting position we are moving downwards,
		// until we reach the ending point
		do {
			int minD = distance[s];
			int mins = s;

			for (int i=0; i < dir.length; i++) {

				//单向禁止：当前格不能走该方向
				if (oneWay != null && (oneWay[s] & DIR_BITS[i]) != 0) continue;

				int n = s + dir[i];

				int thisD = distance[n];
				if (thisD < minD) {
					minD = thisD;
					mins = n;
				}
			}
			s = mins;
			result.add( s );
		} while (s != to);

		return result;
	}

	public static int getStep( int from, int to, boolean[] passable ) {
		return getStep( from, to, passable, null );
	}

	public static int getStep( int from, int to, boolean[] passable, int[] oneWay ) {
		PathFinder p = cur();
		if (oneWay != null && oneWay.length != p.size) oneWay = null;
		return p.getStep_( from, to, passable, oneWay );
	}

	private int getStep_( int from, int to, boolean[] passable, int[] oneWay ) {

		if (!buildDistanceMap( from, to, passable, oneWay )) {
			return -1;
		}

		// From the starting position we are making one step downwards
		int minD = distance[from];
		int best = from;

		int step, stepD;

		for (int i=0; i < dir.length; i++) {

			//单向禁止：当前格不能走该方向
			if (oneWay != null && (oneWay[from] & DIR_BITS[i]) != 0) continue;

			if ((stepD = distance[step = from + dir[i]]) < minD) {
				minD = stepD;
				best = step;
			}
		}

		return best;
	}

	public static int getStepBack( int cur, int from, boolean[] passable ) {
		return getStepBack( cur, from, passable, null );
	}

	public static int getStepBack( int cur, int from, boolean[] passable, int[] oneWay ) {
		PathFinder p = cur();
		if (oneWay != null && oneWay.length != p.size) oneWay = null;
		return p.getStepBack_( cur, from, passable, oneWay );
	}

	private int getStepBack_( int cur, int from, boolean[] passable, int[] oneWay ) {

		int d = buildEscapeDistanceMap( cur, from, 5, passable, oneWay );
		for (int i=0; i < size; i++) {
			goals[i] = distance[i] == d;
		}
		if (!buildDistanceMap( cur, goals, passable, oneWay )) {
			return -1;
		}

		int s = cur;

		// From the starting position we are making one step downwards
		int minD = distance[s];
		int mins = s;

		for (int i=0; i < dir.length; i++) {

			//单向禁止：当前格不能走该方向
			if (oneWay != null && (oneWay[s] & DIR_BITS[i]) != 0) continue;

			int n = s + dir[i];
			int thisD = distance[n];

			if (thisD < minD) {
				minD = thisD;
				mins = n;
			}
		}

		return mins;
	}

	public static void buildDistanceMap( int to, boolean[] passable, int limit ) {
		cur().buildDistanceMap_(to, passable, limit);
	}

	private void buildDistanceMap_( int to, boolean[] passable, int limit ) {

		System.arraycopy(maxVal, 0, distance, 0, maxVal.length);

		int head = 0;
		int tail = 0;

		// Add to queue
		queue[tail++] = to;
		distance[to] = 0;

		while (head < tail) {

			// Remove from queue
			int step = queue[head++];

			int nextDistance = distance[step] + 1;
			if (nextDistance > limit) {
				return;
			}

			int start = (step % width == 0 ? 3 : 0);
			int end   = ((step+1) % width == 0 ? 3 : 0);
			for (int i = start; i < dirLR.length - end; i++) {

				int n = step + dirLR[i];
				if (n >= 0 && n < size && passable[n] && (distance[n] > nextDistance)) {
					// Add to queue
					queue[tail++] = n;
					distance[n] = nextDistance;
				}

			}
		}
	}

	public static void buildDistanceMap( int to, boolean[] passable ) {
		cur().buildDistanceMap_(to, passable);
	}

	private void buildDistanceMap_( int to, boolean[] passable ) {

		System.arraycopy(maxVal, 0, distance, 0, maxVal.length);

		int head = 0;
		int tail = 0;

		// Add to queue
		queue[tail++] = to;
		distance[to] = 0;

		while (head < tail) {

			// Remove from queue
			int step = queue[head++];
			int nextDistance = distance[step] + 1;

			int start = (step % width == 0 ? 3 : 0);
			int end   = ((step+1) % width == 0 ? 3 : 0);
			for (int i = start; i < dirLR.length - end; i++) {

				int n = step + dirLR[i];
				if (n >= 0 && n < size && passable[n] && (distance[n] > nextDistance)) {
					// Add to queue
					queue[tail++] = n;
					distance[n] = nextDistance;
				}

			}
		}
	}

	public static int direction(int pos, Integer target) {
		return cur().direction_(pos, target);
	}

	private int direction_(int pos, Integer target) {
		return 0;
	}

	private boolean buildDistanceMap( int from, int to, boolean[] passable, int[] oneWay ) {

		if (from == to) {
			return false;
		}

		System.arraycopy(maxVal, 0, distance, 0, maxVal.length);

		boolean pathFound = false;

		int head = 0;
		int tail = 0;

		// Add to queue
		queue[tail++] = to;
		distance[to] = 0;

		while (head < tail) {

			// Remove from queue
			int step = queue[head++];
			if (step == from) {
				pathFound = true;
				break;
			}
			int nextDistance = distance[step] + 1;

			int start = (step % width == 0 ? 3 : 0);
			int end   = ((step+1) % width == 0 ? 3 : 0);
			for (int i = start; i < dirLR.length - end; i++) {

				int n = step + dirLR[i];
				if (n < 0 || n >= size) continue;
				//单向禁止：反向扩散的实际移动方向是 n→step
				if (oneWay != null && (oneWay[n] & DIRLR_OPP_BITS[i]) != 0) continue;

				if (n == from || (passable[n] && (distance[n] > nextDistance))) {
					// Add to queue
					queue[tail++] = n;
					distance[n] = nextDistance;
				}

			}
		}

		return pathFound;
	}

	private boolean buildDistanceMap( int from, boolean[] to, boolean[] passable, int[] oneWay ) {

		if (to[from]) {
			return false;
		}

		System.arraycopy(maxVal, 0, distance, 0, maxVal.length);

		boolean pathFound = false;

		int head = 0;
		int tail = 0;

		// Add to queue
		for (int i=0; i < size; i++) {
			if (to[i]) {
				queue[tail++] = i;
				distance[i] = 0;
			}
		}

		while (head < tail) {

			// Remove from queue
			int step = queue[head++];
			if (step == from) {
				pathFound = true;
				break;
			}
			int nextDistance = distance[step] + 1;

			int start = (step % width == 0 ? 3 : 0);
			int end   = ((step+1) % width == 0 ? 3 : 0);
			for (int i = start; i < dirLR.length - end; i++) {

				int n = step + dirLR[i];
				if (n < 0 || n >= size) continue;
				//单向禁止：反向扩散的实际移动方向是 n→step
				if (oneWay != null && (oneWay[n] & DIRLR_OPP_BITS[i]) != 0) continue;

				if (n == from || (passable[n] && (distance[n] > nextDistance))) {
					// Add to queue
					queue[tail++] = n;
					distance[n] = nextDistance;
				}

			}
		}

		return pathFound;
	}

	private int buildEscapeDistanceMap( int cur, int from, int lookAhead, boolean[] passable, int[] oneWay ) {

		System.arraycopy(maxVal, 0, distance, 0, maxVal.length);

		int destDist = Integer.MAX_VALUE;

		int head = 0;
		int tail = 0;

		// Add to queue
		queue[tail++] = from;
		distance[from] = 0;

		int dist = 0;

		while (head < tail) {

			// Remove from queue
			int step = queue[head++];
			dist = distance[step];

			if (dist > destDist) {
				return destDist;
			}

			if (step == cur) {
				destDist = dist + lookAhead;
			}

			int nextDistance = dist + 1;

			int start = (step % width == 0 ? 3 : 0);
			int end   = ((step+1) % width == 0 ? 3 : 0);
			for (int i = start; i < dirLR.length - end; i++) {

				int n = step + dirLR[i];
				if (n < 0 || n >= size) continue;
				//单向禁止：正向扩散的实际移动方向是 step→n
				if (oneWay != null && (oneWay[step] & DIRLR_BITS[i]) != 0) continue;

				if (passable[n] && distance[n] > nextDistance) {
					// Add to queue
					queue[tail++] = n;
					distance[n] = nextDistance;
				}

			}
		}

		return dist;
	}
}
