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

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class TargetedCell extends Image {

	//活动标记登记表（含尚未显示到场景的标记），用于持久化与切层清理
	private static final ArrayList<TargetedCell> active = new ArrayList<>();
	//读档后等待场景重建的标记参数
	private static final ArrayList<Integer> pendingPos = new ArrayList<>();
	private static final ArrayList<Integer> pendingColor = new ArrayList<>();
	private static final ArrayList<Float> pendingTime = new ArrayList<>();

	private static final String CELLS = "targeted_cells";
	private static final String POS = "pos";
	private static final String COLOR = "color";
	private static final String TIME = "time";

	public int pos;
	//与 Actor.now() 同一时间轴上的绝对到期时间
	public float time;
	private float alpha;
	private int color;

	public TargetedCell( int pos, int color ) {
		//兼容旧调用：创建即到期，按现实时间淡出
		this( pos, color, Actor.now() );
	}

	public TargetedCell( int pos, int color, float time ) {
		super(Icons.get(Icons.TARGET));

		origin.set( width/2f );

		reset( pos, color, time );

		synchronized (active) {
			active.add( this );
		}
	}

	//创建或重置标记：位置、颜色与到期时间
	public void reset( int pos, int color, float time ) {
		this.pos = pos;
		this.color = color;
		this.time = time;

		alpha = 1f;
		alpha( 1f );
		scale.set( 1f );

		hardlight( color );
		point( DungeonTilemap.tileToWorld( pos ) );
	}

	@Override
	public void update() {

		alpha -= Game.elapsed;

		//未到期时透明度最低保持 0.6，等待玩家操作也不会消失
		if (time >= Actor.now()) {
			alpha = Math.max( alpha, 0.6f );
		}

		if (alpha > 0) {
			alpha( alpha );
			scale.set( (float)Math.pow( alpha, 0.33f ) );
		} else {
			//到期并完成淡出，同时从活动标记登记表移除自身
			killAndErase();
		}
	}

	@Override
	public void killAndErase() {
		synchronized (active) {
			active.remove( this );
		}
		super.killAndErase();
	}

	@Override
	public void destroy() {
		synchronized (active) {
			active.remove( this );
		}
		super.destroy();
	}

	//切层时清理旧场景的标记，保留读档后尚未重建的标记
	public static void clear() {
		TargetedCell[] cells;
		synchronized (active) {
			cells = active.toArray( new TargetedCell[0] );
			active.clear();
		}
		for (TargetedCell cell : cells){
			cell.killAndErase();
		}
	}

	//游戏时间整体减去 min 时，标记的到期时间同步平移
	public static void fixTime( float min ) {
		synchronized (active) {
			for (TargetedCell cell : active){
				cell.time -= min;
			}
			for (int i = 0; i < pendingTime.size(); i++){
				pendingTime.set( i, pendingTime.get( i ) - min );
			}
		}
	}

	//存档：记录活动标记的位置、颜色与到期时间
	public static void store( Bundle bundle ) {
		Bundle cells = new Bundle();
		synchronized (active) {
			int count = active.size() + pendingPos.size();
			int[] positions = new int[count];
			int[] colors = new int[count];
			float[] times = new float[count];

			int i = 0;
			for (TargetedCell cell : active){
				positions[i] = cell.pos;
				colors[i] = cell.color;
				times[i] = cell.time;
				i++;
			}
			for (int j = 0; j < pendingPos.size(); j++, i++){
				positions[i] = pendingPos.get( j );
				colors[i] = pendingColor.get( j );
				times[i] = pendingTime.get( j );
			}

			cells.put( POS, positions );
			cells.put( COLOR, colors );
			cells.put( TIME, times );
		}
		bundle.put( CELLS, cells );
	}

	//读档：恢复标记参数，等待场景重建后重新创建显示对象
	public static void restore( Bundle bundle ) {
		synchronized (active) {
			active.clear();
			pendingPos.clear();
			pendingColor.clear();
			pendingTime.clear();

			//兼容旧存档
			if (bundle == null || !bundle.contains( CELLS )) return;

			Bundle cells = bundle.getBundle( CELLS );
			if (cells == null || cells.isNull()) return;

			int[] positions = cells.getIntArray( POS );
			int[] colors = cells.getIntArray( COLOR );
			float[] times = cells.getFloatArray( TIME );
			if (positions == null || colors == null || times == null) return;

			int count = Math.min( positions.length, Math.min( colors.length, times.length ) );
			for (int i = 0; i < count; i++){
				pendingPos.add( positions[i] );
				pendingColor.add( colors[i] );
				pendingTime.add( times[i] );
			}
		}
	}

	//场景重建时重新创建显示对象
	public static void restoreToScene( Group group ) {
		if (group == null) return;

		//在锁外操作场景，避免与渲染线程形成锁序倒置
		ArrayList<TargetedCell> cells = new ArrayList<>();
		synchronized (active) {
			for (int i = 0; i < pendingPos.size(); i++){
				TargetedCell cell = new TargetedCell( pendingPos.get( i ), pendingColor.get( i ), pendingTime.get( i ) );

				//存档期间已过期的标记从当前透明度继续淡出，完全淡出的直接丢弃
				float overdue = Actor.now() - cell.time;
				if (overdue > 0){
					cell.alpha = 0.6f - overdue;
					if (cell.alpha <= 0){
						cell.alpha = 0f;
						cell.killAndErase();
						continue;
					}
				}

				cells.add( cell );
			}
			pendingPos.clear();
			pendingColor.clear();
			pendingTime.clear();
		}

		for (TargetedCell cell : cells){
			group.add( cell );
		}
	}
}
