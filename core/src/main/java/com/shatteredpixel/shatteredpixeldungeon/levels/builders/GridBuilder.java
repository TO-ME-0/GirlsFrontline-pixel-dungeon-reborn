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

package com.shatteredpixel.shatteredpixeldungeon.levels.builders;

import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.watabou.utils.Rect;
import com.watabou.utils.SparseArray;

import java.util.ArrayList;

//从 4.0 原样移植的网格布局器：房间按固定格子尺寸对齐摆放，紧凑且高连通
//reborn 的 LoopBuilder 无法在常规地图尺寸内容纳金库全部 11x11 大房间（placeRoom 死循环超时），故搬回原版方案
public class GridBuilder extends Builder {

	//TODO 后续可改为构造参数而非静态字段
	public static int ROOM_SIZE = 11;

	//每条邻接边会处理两次，因此实际相邻连接率约为 80%
	protected float extraConnectionChance = 0.55f;

	@Override
	public ArrayList<Room> build(ArrayList<Room> rooms) {
		for(Room r : rooms){
			r.setEmpty();
		}

		Room entrance = null;
		Room exit = null;

		for (Room r : rooms){
			if (r.isEntrance()){
				entrance = r;
			} else if (r.isExit()){
				exit = r;
			}
		}

		if (!entrance.forceSize(ROOM_SIZE, ROOM_SIZE)){
			throw new RuntimeException("rigid room sizes for now!");
		}
		entrance.setPos(0, 0);

		ArrayList<Room> toPlace = new ArrayList<>();

		//交错插入，让普通房间之间穿插特殊房间
		ArrayList<Room> multis = new ArrayList<>();
		ArrayList<Room> singles = new ArrayList<>();
		for (Room r : rooms){
			if (r.maxConnections(Room.ALL) == 1){
				singles.add(r);
			} else {
				multis.add(r);
			}
		}

		//按房间总数划定边界
		//（目前假定所有房间为 1x1 格，超大房间允许探出边界，多出来的倍率也已计入）
		int maxWidth = 0, maxHeight = 0;
		while ((maxWidth * maxHeight) < rooms.size()*1.25f){
			if (maxWidth < maxHeight || (maxWidth == maxHeight && Random.Int(2) == 0)){
				maxWidth++;
			} else {
				maxHeight++;
			}
		}

		ArrayList<Room> placed = new ArrayList<>();

		if (!multis.isEmpty()) toPlace.add(multis.remove(0));
		if (!multis.isEmpty()) toPlace.add(multis.remove(0));
		while (!multis.isEmpty() || !singles.isEmpty()){
			if (!multis.isEmpty()) toPlace.add(multis.remove(0));
			if (!multis.isEmpty()) toPlace.add(multis.remove(0));
			if (!multis.isEmpty()) toPlace.add(multis.remove(0));
			if (!singles.isEmpty()) toPlace.add(singles.remove(0));
		}

		toPlace.remove(entrance);
		int entryX;
		int entryY;
		//入口房总是放在边界上（角落概率略高，有意为之）
		switch (Random.Int(4)){
			case 0: default:
				entryX = 0;
				entryY = Random.Int(0, maxHeight);
				break;
			case 1:
				entryX = Random.Int(0, maxWidth);
				entryY = 0;
				break;
			case 2:
				entryX = maxWidth-1;
				entryY = Random.Int(0, maxHeight);
				break;
			case 3:
				entryX = Random.Int(0, maxWidth);
				entryY = maxHeight-1;
				break;
		}
		entrance.setPos(entryX*(ROOM_SIZE-1), entryY*(ROOM_SIZE-1));
		placed.add(entrance);

		//出口房排到最后放
		if (exit != null) {
			toPlace.remove(exit);
			toPlace.add(exit);
		}

		PointF aimCenter = new PointF(maxWidth/2f, maxHeight/2f);

		//用稀疏数组记录格位占用，键为 x + 1000*y，另加 100,100 偏移避免负索引
		//等效限制：-99 < x < 999，-99 < y < 无穷（以房间格为单位）
		SparseArray<Room> gridCells = new SparseArray<>();
		gridCells.put(getIdx(entryX, entryY), entrance);
		int roomPlacementFailures = 0;
		while (!toPlace.isEmpty()) {
			Room r = toPlace.remove(0);
			int cellWidth = 1;
			int cellHeight = 1;
			//TODO 目前只支持严格的倍数尺寸，更灵活的做法是缓冲尺寸不合的房间
			if (!r.forceSize(ROOM_SIZE, ROOM_SIZE)){
				//TODO 目前先尝试更宽的尺寸，或许可以随机化
				if (!r.forceSize(2*ROOM_SIZE-1, 2*ROOM_SIZE-1)) {
					if (!r.forceSize(ROOM_SIZE, 2*ROOM_SIZE-1)) {
						if (!r.forceSize(2*ROOM_SIZE-1, ROOM_SIZE)) {
							throw new RuntimeException("rigid room sizes for now!");
						} else {
							cellWidth = 2; cellHeight = 1;
						}
					} else {
						cellWidth = 1; cellHeight = 2;
					}
				} else {
					cellWidth = cellHeight = 2;
				}
			}
			int tries = 0;
			do {
				r.neigbours.clear();
				tries++;
				if (tries > 100) {
					//暂时放不进去，塞回队列前部
					toPlace.add(Math.min(2, toPlace.size()), r);

					//若持续摆放失败，则整体重来
					roomPlacementFailures++;
					if (roomPlacementFailures > 100){
						return null;
					}
					break;
				}

				Room n;
				int nIdx;
				//前两个放置的房间总是与入口房相连
				if (placed.size() < 3){
					n = entrance;
					nIdx = getIdx(entryX, entryY);
				} else {
					int[] keys = gridCells.keyArray();
					nIdx = keys[Random.Int(keys.length)];
					n = gridCells.get(nIdx, null);
				}
				int rIdx = nIdx;
				float xDiff = aimCenter.x - ((rIdx % 1000) - 100);
				float yDiff = aimCenter.y - ((rIdx / 1000) - 100);
				float dist = (float) Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
				//离中心越远越倾向往中心收拢
				// 距离 0-4 格时收拢概率 67-100%
				//首个房间必定朝中心走
				if (Random.Float(12) < 8+dist || (placed.size() == 1)){
					if (Math.abs(xDiff) >= Math.abs(yDiff)){
						if (xDiff > 0){
							//右移
							rIdx += 1;
						} else {
							//左移
							rIdx -= 1;
						}
					} else {
						if (yDiff > 0){
							//下移
							rIdx += 1000;
						} else {
							//上移
							rIdx -= 1000;
						}
					}
				} else {
					switch (Random.Int(4)) {
						case 0:
							rIdx += 1;
							break;
						case 1:
							rIdx -= 1000;
							break;
						case 2:
							rIdx -= 1;
							break;
						case 3:
							rIdx += 1000;
							break;
					}
				}
				//-100 用于抵消偏移
				int x = (rIdx % 1000) - 100;
				int y = (rIdx / 1000) - 100;
				boolean valid;

				//起始格超出边界则无效
				if (x < 0 || x >= maxHeight || y < 0 || y >= maxHeight) {
					valid = false;

				//起始格被占用则无效
				} else if (gridCells.containsKey(rIdx)) {

					valid = false;

				//大房间需要检查其占据的所有格子
				} else if (cellWidth > 1 || cellHeight > 1) {

					Rect space = findFreeGridSpace(new Point(x, y), gridCells, cellWidth, cellHeight);

					//1x2 或 2x1 的房间必须完全落在界内，更大的房间允许探出一点
					if (cellWidth * cellHeight <= 2) {
						space.left = Math.max(0, space.left);
						space.top = Math.max(0, space.top);
						space.right = Math.min(maxWidth - 1, space.right);
						space.bottom = Math.min(maxHeight - 1, space.bottom);
					}

					//宽高 +1，因为边界是闭区间
					int excessWidth = (space.width() + 1) - cellWidth;
					int excessHeight = (space.height() + 1) - cellHeight;
					valid = excessWidth >= 0 && excessHeight >= 0;
					if (valid) {
						//在可用空间内随机落位
						x = space.left + Random.Int(excessWidth+1);
						y = space.top + Random.Int(excessHeight+1);
						rIdx = getIdx(x, y);
					}
				} else {
					valid = true;
				}

				//同类房间不能共边，网格布局下同色块并排会显得重复
				if (valid){
					r.setPos(x*(ROOM_SIZE-1), y*(ROOM_SIZE-1));
					for (Room other : placed){
						if (other.getClass() == r.getClass()){
							Rect i = r.intersect(other);
							if (i.width() > 0 || i.height() > 0){
								valid = false;
							}
						}
					}
				}

				if (valid){
					if (r.connect(n)) {
						placed.add(r);
						for (int i = 0; i < cellWidth; i++){
							for (int j = 0; j < cellHeight; j++){
								gridCells.put(rIdx + i + j*1000, r);
							}
						}
					}
				}
			} while (!placed.contains(r));
		}

		findNeighbours(rooms);

		for (Room r : rooms){
			for (Room n : r.neigbours){
				if (!n.connected.containsKey(r)
						&& Random.Float() < extraConnectionChance){
					r.connect(n);
				}
			}
		}

		return rooms;
	}

	//与 Builder.findFreeSpace 同理，但基于网格坐标与占用表
	//假定起始格为空
	public Rect findFreeGridSpace(Point start, SparseArray<Room> collision, int maxWidth, int maxHeight){
		Rect space = new Rect(start.x, start.y, start.x, start.y);

		//每次各方向扩一格，使形状更接近方形
		boolean expanded = true;
		while (expanded){
			expanded = false;
			//左
			if (space.left > start.x-(maxWidth-1)) {
				boolean valid = true;
				for (int y = space.top; y <= space.bottom; y++) {
					if (collision.containsKey(getIdx(space.left-1, y))) {
						valid = false;
						break;
					}
				}
				if (valid){
					space.left--;
					expanded = true;
				}
			}
			//上
			if (space.top > start.y-(maxHeight-1)){
				boolean valid = true;
				for (int x = space.left; x <= space.right; x++) {
					if (collision.containsKey(getIdx(x, space.top-1))) {
						valid = false;
						break;
					}
				}
				if (valid){
					space.top--;
					expanded = true;
				}
			}
			//右
			if (space.right < start.x+(maxWidth-1)) {
				boolean valid = true;
				for (int y = space.top; y <= space.bottom; y++) {
					if (collision.containsKey(getIdx(space.right+1, y))) {
						valid = false;
						break;
					}
				}
				if (valid){
					space.right++;
					expanded = true;
				}
			}
			//下
			if (space.bottom < start.y+(maxHeight-1)){
				boolean valid = true;
				for (int x = space.left; x <= space.right; x++) {
					if (collision.containsKey(getIdx(x, space.bottom+1))) {
						valid = false;
						break;
					}
				}
				if (valid){
					space.bottom++;
					expanded = true;
				}
			}
		}

		return space;
	}

	private int getIdx(Point p){
		return p.x + 1000*p.y + 100;
	}

	private int getIdx(int x, int y){
		return (x+100) + 1000*(y+100);
	}

}
