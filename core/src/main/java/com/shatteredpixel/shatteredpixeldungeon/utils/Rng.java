package com.shatteredpixel.shatteredpixeldungeon.utils;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.Random;

/**
 * 随机子流。
 *
 * 一个子流 = push 种子 -> 在独立 RNG 上跑一段 -> pop。用法固定为：
 *
 *     try (Rng.Scope s = Rng.substream(seed)) { …原逻辑原样放这里… }
 *
 * 语义分类（名字不同，避免误用）：
 *   substream(seed[, skip])  种子由调用方给出（任务子种子 / 牌堆种子 / seedForDepth 跳数）
 *   nextSubstream()          从当前流取下一个 Long 作种子：楼层内第 k 段独立子流
 *   levelStream(…)           楼层种子，body 直接跑在该种子上（种子回放）
 *
 * 约束：
 *  1. Scope 只能出自本类工厂，且只能出现在 try-with-resources 头部；不得手动 close()；
 *  2. 级别生成路径上禁止使用无种子子流（会让同一颗种子每次求值结果不同）；
 *  3. 子流内部不得调用 Random.resetGenerators()（会清空整栈，使 close() 下溢）；
 *  4. 依赖 java.lang.AutoCloseable（API 19，与 minSdk 完全对齐，零余量）；
 *     若将来下调 minSdk，改为 implements java.io.Closeable（API 1）即可，用法不变；
 *  5. Dungeons.init 的两处 push 是「整栈重置」语义，不属于本模型，保持手写。
 */
public final class Rng {

	/** 已压栈的子流句柄；close() = pop。无状态，全局复用一个实例（零分配）。 */
	public static final class Scope implements AutoCloseable {
		Scope(){}
		@Override public void close(){
			Random.popGenerator();
		}
	}

	private static final Scope HANDLE = new Scope();

	/** 以 seed 开一个子流。 */
	public static Scope substream(long seed){
		return substream(seed, 0);
	}

	/** 以 seed 开子流，并先空转 skip 个 Long（牌堆重放 / seedForDepth 的跳数）。 */
	public static Scope substream(long seed, int skip){
		Random.pushGenerator(seed);
		for (int i = 0; i < skip; i++) Random.Long();
		return HANDLE;
	}

	/** 从当前流取下一个 Long 作种子：楼层内的第 k 段。 */
	public static Scope nextSubstream(){
		return substream(Random.Long());
	}

	// ---------------------------------------------------------------- 楼层

	public static Scope levelStream(){
		return levelStream(Dungeon.depth(), Dungeon.branch());
	}

	public static Scope levelStream(Level level){
		return levelStream(level.levelDepth, level.branch);
	}

	public static Scope levelStream(int depth, int branch){
		return substream(Dungeon.seedForDepth(depth, branch));
	}
}
