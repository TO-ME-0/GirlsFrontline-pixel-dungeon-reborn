/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.journal.IdentifyKnowledge;
import com.shatteredpixel.shatteredpixeldungeon.journal.IdentifyKnowledge.Category;
import com.shatteredpixel.shatteredpixeldungeon.journal.IdentifyKnowledge.FloorClue;
import com.shatteredpixel.shatteredpixeldungeon.journal.IdentifyKnowledge.ItemClue;
import com.shatteredpixel.shatteredpixeldungeon.journal.IdentifyKnowledge.Mode;
import com.shatteredpixel.shatteredpixeldungeon.journal.IdentifyKnowledge.Origin;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import java.util.Iterator;

//自动鉴定门面：线索登记（tag/tagSingle/tagFloor）、观察事件（onSeen/onRoomSeen/onPickUp）、
//猜测入口（guess/canGuess）与重掷清理（onLabelsRegenerated）。
//知识本体存在 Notes.knowledge，激活条件是“看见”而非“拾取”。
public class AutoIdentify {

	//—— 基础查询 ——

	public static Category categoryOf(Item item) {
		if (item instanceof Potion) return Category.POTION;
		if (item instanceof Scroll) return Category.SCROLL;
		return null;
	}

	public static String labelOf(Item item) {
		if (!(item instanceof ColorItem)) return null;
		return ((ColorItem) item).guessLabel();
	}

	//猜测知识键（异变映射回普通类），非 ColorItem 返回 null
	@SuppressWarnings("unchecked")
	public static Class<? extends Item> keyOf(Item item) {
		if (!(item instanceof ColorItem)) return null;
		Class<?> key = ((ColorItem) item).guessKey();
		return Item.class.isAssignableFrom(key) ? (Class<? extends Item>) key : null;
	}

	//仅支持注册进 handler 的普通药水/卷轴（含异变）；酿造/药剂/占位符无标签，不支持
	public static boolean isSupported(Item item) {
		return categoryOf(item) != null && labelOf(item) != null && keyOf(item) != null;
	}

	//—— 线索登记（生成期 levelId 必须显式传入 level.levelId，不能用 Dungeon.levelId）——

	//实例级 SINGLE：标签与真类都从实例读取
	public static void tagSingle(Item item, int levelId, Origin origin, String cause) {
		if (!isSupported(item)) return;
		tag(item, levelId, origin, Mode.SINGLE, new Class[]{ keyOf(item) }, cause);
	}

	//实例级 ANY_OF / NOT：标签从实例读取，类集合由登记点给出
	public static void tag(Item item, int levelId, Origin origin, Mode mode, Class<? extends Item>[] classes, String cause) {
		if (Notes.knowledge == null || !isSupported(item)) return;
		ItemClue c = new ItemClue();
		c.levelId  = levelId;
		c.category = categoryOf(item);
		c.label    = labelOf(item);
		c.mode     = mode;
		c.classes  = classes;
		c.active   = false;
		c.origin   = origin;
		c.cause    = cause;
		Notes.knowledge.addItemClue(c);
	}

	//楼层池级（无房间范围，如章节配额）：规则已知，立即激活
	public static void tagFloor(int[] levelIds, Category cat, Class<? extends Item>[] classes, int min, int max, Origin origin, String cause) {
		tagFloor(levelIds, cat, classes, min, max, -1, -1, -1, -1, origin, cause);
	}

	//楼层池级（带房间内部范围）：看见房间才激活
	public static void tagFloor(int[] levelIds, Category cat, Class<? extends Item>[] classes, int min, int max, int left, int top, int right, int bottom, Origin origin, String cause) {
		if (Notes.knowledge == null) return;
		FloorClue c = new FloorClue();
		c.levelIds = levelIds;
		c.category = cat;
		c.classes  = classes;
		c.minCount = min;
		c.maxCount = max;
		c.left   = left;
		c.top    = top;
		c.right  = right;
		c.bottom = bottom;
		c.active = left < 0;
		c.origin = origin;
		c.cause  = cause;
		Notes.knowledge.addFloorClue(c);
	}

	//—— 观察事件 ——

	//堆进入视野（或落点本就在视野）：记录标签 + 激活同类别同标签的休眠实例线索
	public static void onSeen(Item item) {
		if (Notes.knowledge == null || !isSupported(item)) return;
		Category cat = categoryOf(item);
		String label = labelOf(item);
		Notes.knowledge.recordSeen(Dungeon.levelId, cat, label);
		Notes.knowledge.activateItemClues(cat, label);
	}

	//拾取只记账，不决定知识是否生效
	public static void onPickUp(Item item) {
		if (Notes.knowledge == null || !isSupported(item)) return;
		Notes.knowledge.recordSeen(Dungeon.levelId, categoryOf(item), labelOf(item));
	}

	//每次英雄视野更新后调用：激活房间被看见的 FloorClue，并锁存 poolComplete
	public static void onRoomSeen(Level level, boolean[] fov) {
		if (Notes.knowledge == null || fov == null || level.heaps == null) return;
		IdentifyKnowledge k = Notes.knowledge;
		int id = level.levelId;

		for (FloorClue fc : k.floorClues) {
			if (fc.active || !fc.hasRect() || !fc.covers(id)) continue;
			//房间内部格（left+1..right-1, top+1..bottom-1）任一格进入视野即激活
			for (int y = fc.top + 1; y <= fc.bottom - 1 && !fc.active; y++) {
				for (int x = fc.left + 1; x <= fc.right - 1; x++) {
					int cell = x + y * level.width();
					if (cell >= 0 && cell < fov.length && fov[cell]) {
						fc.active = true;
						break;
					}
				}
			}
		}

		//poolComplete：本层所有堆都被看见过则锁存（单调，离开本层后保留）
		if (!k.floorSeen(id).poolComplete) {
			boolean allSeen = true;
			for (Heap h : level.heaps.valueList()) {
				if (!h.seen) {
					allSeen = false;
					break;
				}
			}
			if (allSeen) {
				k.floorSeen(id).poolComplete = true;
				for (FloorClue fc : k.floorClues) {
					if (fc.poolComplete || !fc.covers(id)) continue;
					boolean done = true;
					for (int lid : fc.levelIds) {
						if (!k.floorSeen(lid).poolComplete) {
							done = false;
							break;
						}
					}
					if (done) fc.poolComplete = true;
				}
			}
		}
	}

	//—— 猜测 ——

	//能否对该物品尝试猜测：受支持、未真鉴定、且存在已激活的同类线索
	public static boolean canGuess(Item item) {
		if (Notes.knowledge == null || !isSupported(item)) return false;
		if (item instanceof Potion && ((Potion) item).isKnown()) return false;
		if (item instanceof Scroll && ((Scroll) item).isKnown()) return false;
		Category cat = categoryOf(item);
		String label = labelOf(item);
		for (ItemClue c : Notes.knowledge.itemClues) {
			if (c.active && c.category == cat && label.equals(c.label)) return true;
		}
		for (FloorClue c : Notes.knowledge.floorClues) {
			if (c.active && c.category == cat) return true;
		}
		return false;
	}

	//对目标物品的标签做约束求解（§2.3），结果落盘后返回；不受支持/已真鉴定返回 null
	public static IdentifyKnowledge.SolveOutcome guess(Item item) {
		if (Notes.knowledge == null || !isSupported(item)) return null;
		if (item instanceof Potion && ((Potion) item).isKnown()) return null;
		if (item instanceof Scroll && ((Scroll) item).isKnown()) return null;
		IdentifyKnowledge.SolveOutcome out = Notes.knowledge.solve(categoryOf(item), labelOf(item));
		if (out != null) {
			Notes.knowledge.applyResult(out);
		}
		return out;
	}

	//—— 维护 ——

	//该物品当前正面猜测的依据文本（来源 + 楼层），供信息页展示；无正面猜测时返回空串
	public static String guessEvidence(Item item) {
		if (Notes.knowledge == null) return "";
		IdentifyKnowledge.ClassGuess g = Notes.knowledge.findGuess(keyOf(item));
		if (g == null || g.status != IdentifyKnowledge.Status.POSITIVE) return "";
		StringBuilder sb = new StringBuilder();
		if (g.cause != null && !g.cause.isEmpty()) sb.append(g.cause);
		if (!g.evidenceFloors.isEmpty()) {
			if (sb.length() > 0) sb.append(' ');
			sb.append("依据楼层：");
			boolean first = true;
			for (Integer f : g.evidenceFloors) {
				if (!first) sb.append('、');
				sb.append(f);
				first = false;
			}
		}
		return sb.toString();
	}

	//标签重掷：该类别知识清空，展示缓存同步失效（vetoed 保留）
	public static void onLabelsRegenerated(Category cat) {
		if (Notes.knowledge != null) {
			Notes.knowledge.clearCategory(cat);
		}
		Iterator<Class<? extends ColorItem>> it = Dungeon.guessType.iterator();
		while (it.hasNext()) {
			if (IdentifyKnowledge.categoryOfClass(it.next()) == cat) {
				it.remove();
			}
		}
	}

	//读档后把正面猜测补回展示缓存
	public static void restoreGuessCache() {
		if (Notes.knowledge != null) {
			Notes.knowledge.restoreGuessCache();
		}
	}
}
