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

package com.shatteredpixel.shatteredpixeldungeon.journal;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.ColorItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

//自动鉴定知识库：知识全部挂在类级（Notes.knowledge），不存物品实例。
//三种粒度：实例/标签级 ItemClue、楼层池级 FloorClue（无标签）、类级结果 ClassGuess。
//设计定稿见 docs/AutoIdentify-设计定稿与实施交接.md。
public class IdentifyKnowledge implements Bundlable {

	public enum Category { POTION, SCROLL }
	public enum Mode { SINGLE, ANY_OF, NOT }
	public enum Status { UNKNOWN, PARTIAL, POSITIVE }
	public enum Origin { ROOM, RUN, MOB, SHOP }

	//实例/标签级线索：某个被看见的标签属于/不属于哪些类
	public static class ItemClue implements Bundlable {
		public int levelId;
		public Category category;
		public String label;
		public Mode mode;
		public Class<? extends Item>[] classes;
		public boolean active;
		public Origin origin;
		public String cause;

		private static final String LEVEL_ID = "level_id";
		private static final String CATEGORY = "category";
		private static final String LABEL    = "label";
		private static final String MODE     = "mode";
		private static final String CLASSES  = "classes";
		private static final String ACTIVE   = "active";
		private static final String ORIGIN   = "origin";
		private static final String CAUSE    = "cause";

		@Override
		public void storeInBundle(Bundle bundle) {
			bundle.put(LEVEL_ID, levelId);
			bundle.put(CATEGORY, category);
			bundle.put(LABEL, label);
			bundle.put(MODE, mode);
			bundle.put(CLASSES, classes);
			bundle.put(ACTIVE, active);
			bundle.put(ORIGIN, origin);
			bundle.put(CAUSE, cause);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void restoreFromBundle(Bundle bundle) {
			levelId  = bundle.getInt(LEVEL_ID);
			category = bundle.getEnum(CATEGORY, Category.class);
			label    = bundle.getString(LABEL);
			mode     = bundle.getEnum(MODE, Mode.class);
			classes  = filterClasses(bundle.getClassArray(CLASSES));
			active   = bundle.getBoolean(ACTIVE);
			origin   = bundle.getEnum(ORIGIN, Origin.class);
			cause    = bundle.getString(CAUSE);
		}
	}

	//楼层池级线索（无标签）：某层（或跨层配额）的池子里保证有/有多少个某些类。
	//房间范围用于“看见房间即激活”；-1 表示全层（如章节配额）。
	public static class FloorClue implements Bundlable {
		public int[] levelIds;
		public Category category;
		public Class<? extends Item>[] classes;
		public int minCount = 1;
		public int maxCount = -1;          //-1 表示不限
		public int left = -1, top = -1, right = -1, bottom = -1;
		public boolean active;
		public boolean poolComplete;
		public Origin origin;
		public String cause;

		public boolean hasRect() {
			return left >= 0;
		}

		public boolean covers(int id) {
			if (levelIds == null) return false;
			for (int i : levelIds) if (i == id) return true;
			return false;
		}

		private static final String LEVEL_IDS = "level_ids";
		private static final String CATEGORY  = "category";
		private static final String CLASSES   = "classes";
		private static final String MIN_COUNT = "min_count";
		private static final String MAX_COUNT = "max_count";
		private static final String LEFT      = "left";
		private static final String TOP       = "top";
		private static final String RIGHT     = "right";
		private static final String BOTTOM    = "bottom";
		private static final String ACTIVE    = "active";
		private static final String POOL_DONE = "pool_complete";
		private static final String ORIGIN    = "origin";
		private static final String CAUSE     = "cause";

		@Override
		public void storeInBundle(Bundle bundle) {
			bundle.put(LEVEL_IDS, levelIds);
			bundle.put(CATEGORY, category);
			bundle.put(CLASSES, classes);
			bundle.put(MIN_COUNT, minCount);
			bundle.put(MAX_COUNT, maxCount);
			bundle.put(LEFT, left);
			bundle.put(TOP, top);
			bundle.put(RIGHT, right);
			bundle.put(BOTTOM, bottom);
			bundle.put(ACTIVE, active);
			bundle.put(POOL_DONE, poolComplete);
			bundle.put(ORIGIN, origin);
			bundle.put(CAUSE, cause);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void restoreFromBundle(Bundle bundle) {
			levelIds     = bundle.getIntArray(LEVEL_IDS);
			category     = bundle.getEnum(CATEGORY, Category.class);
			classes      = filterClasses(bundle.getClassArray(CLASSES));
			minCount     = bundle.getInt(MIN_COUNT);
			maxCount     = bundle.getInt(MAX_COUNT);
			left         = bundle.getInt(LEFT);
			top          = bundle.getInt(TOP);
			right        = bundle.getInt(RIGHT);
			bottom       = bundle.getInt(BOTTOM);
			active       = bundle.getBoolean(ACTIVE);
			poolComplete = bundle.getBoolean(POOL_DONE);
			origin       = bundle.getEnum(ORIGIN, Origin.class);
			cause        = bundle.getString(CAUSE);
		}
	}

	//每层曾看见过的标签；poolComplete 为本层堆全部被看见过的单调锁存
	public static class FloorSeen implements Bundlable {
		public int levelId;
		public LinkedHashSet<String> potions = new LinkedHashSet<>();
		public LinkedHashSet<String> scrolls = new LinkedHashSet<>();
		public boolean poolComplete;

		public LinkedHashSet<String> labels(Category cat) {
			return cat == Category.POTION ? potions : scrolls;
		}

		private static final String LEVEL_ID   = "level_id";
		private static final String POTIONS    = "potions";
		private static final String SCROLLS    = "scrolls";
		private static final String POOL_DONE  = "pool_complete";

		@Override
		public void storeInBundle(Bundle bundle) {
			bundle.put(LEVEL_ID, levelId);
			bundle.put(POTIONS, potions.toArray(new String[0]));
			bundle.put(SCROLLS, scrolls.toArray(new String[0]));
			bundle.put(POOL_DONE, poolComplete);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			levelId = bundle.getInt(LEVEL_ID);
			potions.clear();
			for (String s : bundle.getStringArray(POTIONS)) potions.add(s);
			scrolls.clear();
			for (String s : bundle.getStringArray(SCROLLS)) scrolls.add(s);
			poolComplete = bundle.getBoolean(POOL_DONE);
		}
	}

	//类级猜测结果
	public static class ClassGuess implements Bundlable {
		public Class<? extends Item> itemClass;
		public LinkedHashSet<String> possibleLabels = new LinkedHashSet<>();
		public Status status = Status.UNKNOWN;
		public String cause;
		public LinkedHashSet<Integer> evidenceFloors = new LinkedHashSet<>();

		private static final String ITEM_CLASS = "item_class";
		private static final String LABELS     = "labels";
		private static final String STATUS     = "status";
		private static final String CAUSE      = "cause";
		private static final String FLOORS     = "floors";

		@Override
		public void storeInBundle(Bundle bundle) {
			bundle.put(ITEM_CLASS, itemClass);
			bundle.put(LABELS, possibleLabels.toArray(new String[0]));
			bundle.put(STATUS, status);
			bundle.put(CAUSE, cause);
			int[] f = new int[evidenceFloors.size()];
			int i = 0;
			for (Integer fl : evidenceFloors) f[i++] = fl;
			bundle.put(FLOORS, f);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void restoreFromBundle(Bundle bundle) {
			itemClass = bundle.getClass(ITEM_CLASS);
			possibleLabels.clear();
			for (String s : bundle.getStringArray(LABELS)) possibleLabels.add(s);
			status = bundle.getEnum(STATUS, Status.class);
			cause  = bundle.getString(CAUSE);
			evidenceFloors.clear();
			for (int fl : bundle.getIntArray(FLOORS)) evidenceFloors.add(fl);
		}
	}

	public ArrayList<ItemClue> itemClues = new ArrayList<>();
	public ArrayList<FloorClue> floorClues = new ArrayList<>();
	public ArrayList<FloorSeen> floors = new ArrayList<>();
	public ArrayList<ClassGuess> classGuesses = new ArrayList<>();
	//玩家手动撤销过正面猜测的类：求解器不再对这些类给出正面结论
	public LinkedHashSet<Class<? extends Item>> vetoed = new LinkedHashSet<>();

	//Bundle 反序列化 Class[] 可能含 null，统一过滤
	@SuppressWarnings("unchecked")
	private static Class<? extends Item>[] filterClasses(Class[] raw) {
		ArrayList<Class<? extends Item>> out = new ArrayList<>();
		if (raw != null) {
			for (Class c : raw) {
				if (c != null && Item.class.isAssignableFrom(c)) {
					out.add((Class<? extends Item>) c);
				}
			}
		}
		return out.toArray(new Class[0]);
	}

	public FloorSeen floorSeen(int levelId) {
		for (FloorSeen f : floors) {
			if (f.levelId == levelId) return f;
		}
		FloorSeen f = new FloorSeen();
		f.levelId = levelId;
		floors.add(f);
		return f;
	}

	public void recordSeen(int levelId, Category cat, String label) {
		if (label == null) return;
		floorSeen(levelId).labels(cat).add(label);
	}

	public boolean hasSeen(int levelId, Category cat, String label) {
		for (FloorSeen f : floors) {
			if (f.levelId == levelId) return f.labels(cat).contains(label);
		}
		return false;
	}

	public void addItemClue(ItemClue clue) {
		itemClues.add(clue);
	}

	public void addFloorClue(FloorClue clue) {
		floorClues.add(clue);
	}

	//看见某标签后，激活同类别同标签的休眠实例线索
	public void activateItemClues(Category cat, String label) {
		for (ItemClue c : itemClues) {
			if (!c.active && c.category == cat && label.equals(c.label)) {
				c.active = true;
			}
		}
	}

	//标签重掷后清空该类别的全部知识；vetoed 是玩家显式撤销，保留
	public void clearCategory(Category cat) {
		for (int i = itemClues.size() - 1; i >= 0; i--) {
			if (itemClues.get(i).category == cat) itemClues.remove(i);
		}
		for (int i = floorClues.size() - 1; i >= 0; i--) {
			if (floorClues.get(i).category == cat) floorClues.remove(i);
		}
		for (FloorSeen f : floors) {
			f.labels(cat).clear();
		}
		for (int i = classGuesses.size() - 1; i >= 0; i--) {
			if (categoryOfClass(classGuesses.get(i).itemClass) == cat) classGuesses.remove(i);
		}
	}

	public static Category categoryOfClass(Class<?> cls) {
		if (cls == null) return null;
		if (Potion.class.isAssignableFrom(cls)) return Category.POTION;
		if (Scroll.class.isAssignableFrom(cls)) return Category.SCROLL;
		return null;
	}

	public ClassGuess findGuess(Class<? extends Item> cls) {
		for (ClassGuess g : classGuesses) {
			if (g.itemClass == cls) return g;
		}
		return null;
	}

	//读档后把正面猜测补回展示缓存
	@SuppressWarnings("unchecked")
	public void restoreGuessCache() {
		for (ClassGuess g : classGuesses) {
			if (g.status == Status.POSITIVE
					&& g.itemClass != null
					&& ColorItem.class.isAssignableFrom(g.itemClass)) {
				Dungeon.guessType.add((Class<? extends ColorItem>) g.itemClass);
			}
		}
	}

	//玩家主动忽略推断结果：撤销该类的正面猜测并加入 vetoed，线索与已见标签本身保留
	public void revokeGuess(Class<? extends Item> cls) {
		if (cls == null) return;
		ClassGuess g = findGuess(cls);
		if (g != null && g.status == Status.POSITIVE) {
			g.status = Status.UNKNOWN;
			g.cause = null;
			g.evidenceFloors.clear();
		}
		vetoed.add(cls);
	}

	//—— 求解器（§2.3）：标签 × 类 约束求解，只在玩家主动猜测时调用 ——

	//一次求解的结果（瞬态，不入档）
	public static class SolveOutcome {
		public Category category;
		public String targetLabel;
		public Status status = Status.UNKNOWN;
		public Class<? extends Item> positiveClass;                       //唯一锁定时的类
		public LinkedHashSet<Class<? extends Item>> possibleClasses = new LinkedHashSet<>(); //目标标签仍可能的类
		public LinkedHashSet<Class<? extends Item>> excludedClasses = new LinkedHashSet<>(); //目标标签已排除的类
		public LinkedHashMap<Class<? extends Item>, LinkedHashSet<String>> labelsByClass = new LinkedHashMap<>(); //全部未知类 → 仍可能标签
		public String cause = "";
		public LinkedHashSet<Integer> evidenceFloors = new LinkedHashSet<>();
	}

	@SuppressWarnings("unchecked")
	private static String labelOfClass(Category cat, Class<? extends Item> cls) {
		if (cat == Category.POTION && Potion.class.isAssignableFrom(cls)) {
			return Potion.labelOf((Class<? extends Potion>) cls);
		}
		if (cat == Category.SCROLL && Scroll.class.isAssignableFrom(cls)) {
			return Scroll.labelOf((Class<? extends Scroll>) cls);
		}
		return null;
	}

	private static LinkedHashSet<Class<? extends Item>> unknownClassesOf(Category cat) {
		LinkedHashSet<Class<? extends Item>> out = new LinkedHashSet<>();
		if (cat == Category.POTION) {
			out.addAll(Potion.unknownClasses());
		} else {
			out.addAll(Scroll.unknownClasses());
		}
		return out;
	}

	private static LinkedHashSet<Class<? extends Item>> asSet(Class<? extends Item>[] classes) {
		LinkedHashSet<Class<? extends Item>> out = new LinkedHashSet<>();
		if (classes != null) {
			for (Class<? extends Item> c : classes) if (c != null) out.add(c);
		}
		return out;
	}

	public SolveOutcome solve(Category cat, String targetLabel) {
		//1. 未知类集合 U 与可用标签集合 L（label↔class 双射，已鉴定类占用自己的标签）
		LinkedHashSet<Class<? extends Item>> unknown = unknownClassesOf(cat);
		//候选表：标签 → 可能类集合
		LinkedHashMap<String, LinkedHashSet<Class<? extends Item>>> cand = new LinkedHashMap<>();
		for (Class<? extends Item> cls : unknown) {
			String label = labelOfClass(cat, cls);
			if (label != null && !cand.containsKey(label)) {
				cand.put(label, new LinkedHashSet<>(unknown));
			}
		}
		if (!cand.containsKey(targetLabel)) return null;

		StringBuilder cause = new StringBuilder();
		LinkedHashSet<Integer> evidenceFloors = new LinkedHashSet<>();

		//2. 应用所有激活的实例/标签级线索
		for (ItemClue c : itemClues) {
			if (!c.active || c.category != cat) continue;
			LinkedHashSet<Class<? extends Item>> cur = cand.get(c.label);
			if (cur == null) continue;
			if (c.mode == Mode.NOT) {
				cur.removeAll(asSet(c.classes));
			} else {
				//SINGLE 与 ANY_OF 都是收窄到给定集合
				cur.retainAll(asSet(c.classes));
			}
			if (c.cause != null && !c.cause.isEmpty()) {
				if (cause.length() > 0) cause.append("\n");
				cause.append(c.cause);
			}
			evidenceFloors.add(c.levelId);
		}
		propagate(cand);

		//3. 应用 FloorClue 存在性约束：S_F 中只剩一个标签可能与 classes 相交时收窄
		ArrayList<FloorClue> enabledFloors = new ArrayList<>();
		ArrayList<LinkedHashSet<String>> enabledSeen = new ArrayList<>();
		for (FloorClue fc : floorClues) {
			if (!fc.active || !fc.poolComplete || fc.category != cat) continue;
			LinkedHashSet<String> seen = new LinkedHashSet<>();
			for (int lid : fc.levelIds) {
				for (FloorSeen f : floors) {
					if (f.levelId == lid) seen.addAll(f.labels(cat));
				}
			}
			seen.retainAll(cand.keySet());
			if (seen.isEmpty()) continue;
			enabledFloors.add(fc);
			enabledSeen.add(seen);

			ArrayList<String> hits = new ArrayList<>();
			for (String l : seen) {
				LinkedHashSet<Class<? extends Item>> cur = cand.get(l);
				if (cur != null && !Collections.disjoint(asSet(fc.classes), cur)) hits.add(l);
			}
			if (hits.size() == 1) {
				cand.get(hits.get(0)).retainAll(asSet(fc.classes));
				propagate(cand);
				if (fc.cause != null && !fc.cause.isEmpty()) {
					if (cause.length() > 0) cause.append("\n");
					cause.append(fc.cause);
				}
				for (int lid : fc.levelIds) evidenceFloors.add(lid);
			}
			//hits.size()==0 说明知识互相矛盾，保守不收窄
		}

		//4. 完美匹配校验（Kuhn）：强制每条候选边后，检查剩余图仍有完美匹配，
		//   且每个启用的 FloorClue 至少被满足一次；不满足则删除该边
		//   先剔除初始就无可满足边的 FloorClue（陈旧/矛盾知识，避免误删全部候选）
		for (int i = enabledFloors.size() - 1; i >= 0; i--) {
			if (!clueHasAnyEdge(cand, enabledFloors.get(i), enabledSeen.get(i))) {
				enabledFloors.remove(i);
				enabledSeen.remove(i);
			}
		}
		boolean changed = true;
		while (changed) {
			changed = false;
			for (String l : new ArrayList<>(cand.keySet())) {
				for (Class<? extends Item> cls : new ArrayList<>(cand.get(l))) {
					if (cand.get(l).size() <= 1) break; //唯一候选保守保留
					LinkedHashMap<String, LinkedHashSet<Class<? extends Item>>> copy = deepCopy(cand);
					copy.get(l).clear();
					copy.get(l).add(cls);
					propagate(copy);
					if (!satisfyClues(copy, enabledFloors, enabledSeen, 0)) {
						cand.get(l).remove(cls);
						propagate(cand);
						changed = true;
					}
				}
			}
		}

		//5. 输出
		SolveOutcome out = new SolveOutcome();
		out.category = cat;
		out.targetLabel = targetLabel;
		out.cause = cause.toString();
		out.evidenceFloors = evidenceFloors;

		LinkedHashSet<Class<? extends Item>> targetCands = cand.get(targetLabel);
		out.possibleClasses.addAll(targetCands);
		for (Class<? extends Item> cls : unknown) {
			if (!targetCands.contains(cls)) out.excludedClasses.add(cls);
		}
		//反转候选表：每个未知类仍可能的标签
		for (Class<? extends Item> cls : unknown) {
			LinkedHashSet<String> ls = new LinkedHashSet<>();
			for (String l : cand.keySet()) {
				if (cand.get(l).contains(cls)) ls.add(l);
			}
			out.labelsByClass.put(cls, ls);
		}

		if (targetCands.size() == 1 && !vetoed.contains(targetCands.iterator().next())) {
			out.status = Status.POSITIVE;
			out.positiveClass = targetCands.iterator().next();
		} else if (!targetCands.isEmpty()) {
			out.status = Status.PARTIAL;
		}
		return out;
	}

	//把求解结果落盘：更新各类的 ClassGuess；POSITIVE 写入展示缓存
	@SuppressWarnings("unchecked")
	public void applyResult(SolveOutcome out) {
		if (out == null) return;
		int total = out.labelsByClass.size();
		for (Class<? extends Item> cls : out.labelsByClass.keySet()) {
			ClassGuess g = findGuess(cls);
			if (g == null) {
				g = new ClassGuess();
				g.itemClass = cls;
				classGuesses.add(g);
			}
			g.possibleLabels = out.labelsByClass.get(cls);
			if (cls == out.positiveClass && out.status == Status.POSITIVE) {
				g.status = Status.POSITIVE;
				g.cause = out.cause;
				g.evidenceFloors = out.evidenceFloors;
			} else if (g.status != Status.POSITIVE) {
				g.status = g.possibleLabels.size() < total ? Status.PARTIAL : Status.UNKNOWN;
			}
		}
		if (out.status == Status.POSITIVE
				&& out.positiveClass != null
				&& ColorItem.class.isAssignableFrom(out.positiveClass)) {
			Dungeon.guessType.add((Class<? extends ColorItem>) out.positiveClass);
		}
	}

	//—— 求解器内部工具 ——

	//单例传播：某标签只剩一个候选则锁定，并从其他标签候选中删除该类，直到不动点
	private static void propagate(LinkedHashMap<String, LinkedHashSet<Class<? extends Item>>> cand) {
		boolean changed = true;
		while (changed) {
			changed = false;
			HashSet<Class<? extends Item>> locked = new HashSet<>();
			for (LinkedHashSet<Class<? extends Item>> s : cand.values()) {
				if (s.size() == 1) locked.add(s.iterator().next());
			}
			for (LinkedHashSet<Class<? extends Item>> s : cand.values()) {
				if (s.size() > 1 && s.removeAll(locked)) changed = true;
			}
		}
	}

	private static LinkedHashMap<String, LinkedHashSet<Class<? extends Item>>> deepCopy(
			LinkedHashMap<String, LinkedHashSet<Class<? extends Item>>> cand) {
		LinkedHashMap<String, LinkedHashSet<Class<? extends Item>>> copy = new LinkedHashMap<>();
		for (String l : cand.keySet()) {
			copy.put(l, new LinkedHashSet<>(cand.get(l)));
		}
		return copy;
	}

	private static boolean clueHasAnyEdge(LinkedHashMap<String, LinkedHashSet<Class<? extends Item>>> cand,
										  FloorClue fc, LinkedHashSet<String> seen) {
		HashSet<Class<? extends Item>> cls = asSet(fc.classes);
		for (String l : seen) {
			LinkedHashSet<Class<? extends Item>> cur = cand.get(l);
			if (cur != null && !Collections.disjoint(cls, cur)) return true;
		}
		return false;
	}

	//逐条满足 FloorClue（回溯），全部满足后检查完美匹配
	private static boolean satisfyClues(LinkedHashMap<String, LinkedHashSet<Class<? extends Item>>> cand,
										ArrayList<FloorClue> clues,
										ArrayList<LinkedHashSet<String>> seen, int idx) {
		if (idx == clues.size()) return perfectMatch(cand);
		FloorClue fc = clues.get(idx);
		HashSet<Class<? extends Item>> cls = asSet(fc.classes);
		for (String l : seen.get(idx)) {
			LinkedHashSet<Class<? extends Item>> cur = cand.get(l);
			if (cur == null) continue;
			for (Class<? extends Item> c : cls) {
				if (!cur.contains(c)) continue;
				LinkedHashMap<String, LinkedHashSet<Class<? extends Item>>> copy = deepCopy(cand);
				copy.get(l).clear();
				copy.get(l).add(c);
				propagate(copy);
				if (satisfyClues(copy, clues, seen, idx + 1)) return true;
			}
		}
		return false;
	}

	//Kuhn 二分图完美匹配：左标签右类，12×12 规模
	private static boolean perfectMatch(LinkedHashMap<String, LinkedHashSet<Class<? extends Item>>> cand) {
		HashSet<Class<? extends Item>> classes = new HashSet<>();
		for (LinkedHashSet<Class<? extends Item>> s : cand.values()) classes.addAll(s);
		if (classes.size() < cand.size()) return false;
		HashMap<Class<? extends Item>, String> matchR = new HashMap<>();
		for (String l : cand.keySet()) {
			if (!augment(l, cand, matchR, new HashSet<String>())) return false;
		}
		return true;
	}

	private static boolean augment(String l,
								   LinkedHashMap<String, LinkedHashSet<Class<? extends Item>>> cand,
								   HashMap<Class<? extends Item>, String> matchR,
								   HashSet<String> visited) {
		if (visited.contains(l)) return false;
		visited.add(l);
		for (Class<? extends Item> c : cand.get(l)) {
			String owner = matchR.get(c);
			if (owner == null || augment(owner, cand, matchR, visited)) {
				matchR.put(c, l);
				return true;
			}
		}
		return false;
	}

	private static final String ITEM_CLUES    = "item_clues";
	private static final String FLOOR_CLUES   = "floor_clues";
	private static final String FLOORS        = "floors";
	private static final String CLASS_GUESSES = "class_guesses";
	private static final String VETOED        = "vetoed";

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(ITEM_CLUES, itemClues);
		bundle.put(FLOOR_CLUES, floorClues);
		bundle.put(FLOORS, floors);
		bundle.put(CLASS_GUESSES, classGuesses);
		bundle.put(VETOED, vetoed.toArray(new Class[0]));
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		itemClues.clear();
		for (Bundlable b : bundle.getCollection(ITEM_CLUES)) itemClues.add((ItemClue) b);
		floorClues.clear();
		for (Bundlable b : bundle.getCollection(FLOOR_CLUES)) floorClues.add((FloorClue) b);
		floors.clear();
		for (Bundlable b : bundle.getCollection(FLOORS)) floors.add((FloorSeen) b);
		classGuesses.clear();
		for (Bundlable b : bundle.getCollection(CLASS_GUESSES)) classGuesses.add((ClassGuess) b);
		vetoed.clear();
		for (Class c : filterClasses(bundle.getClassArray(VETOED))) vetoed.add(c);
	}
}
