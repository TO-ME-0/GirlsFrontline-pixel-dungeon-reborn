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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Random {

	//we store a stack of random number generators, which may be seeded deliberately or randomly.
	//top of the stack is what is currently being used to generate new numbers.
	//the base generator is always created with no seed, and cannot be popped.

	//游戏主上下文：Render / Actor / Interlevel 等游戏本体线程共享同一栈。
	//这些线程之间靠 GameScene 的 wait/notify 串行化，不会并发访问。
	//保留 synchronized 以保证可见性（与原实现语义一致）。
	private static final ArrayDeque<java.util.Random> mainGenerators = new ArrayDeque<>();
	static {
		mainGenerators.push(new java.util.Random());
	}

	//查种线程私有栈：进入查种上下文后，本线程的所有随机操作走该栈，与游戏主上下文隔离。
	private static final ThreadLocal<ArrayDeque<java.util.Random>> searchGenerators = new ThreadLocal<>();

	/** 进入查种上下文：当前线程此后的随机数操作使用独立栈。 */
	public static void enterSearchContext(){
		ArrayDeque<java.util.Random> stack = new ArrayDeque<>();
		stack.push(new java.util.Random());
		searchGenerators.set(stack);
	}

	/** 退出查种上下文：恢复使用游戏主上下文栈。 */
	public static void exitSearchContext(){
		searchGenerators.remove();
	}

	private static ArrayDeque<java.util.Random> searchStack(){
		return searchGenerators.get();
	}

	public static void resetGenerators(){
		ArrayDeque<java.util.Random> s = searchStack();
		if (s != null){
			s.clear();
			s.push(new java.util.Random());
			return;
		}
		synchronized (Random.class){
			mainGenerators.clear();
			mainGenerators.push(new java.util.Random());
		}
	}

	public static void pushGenerator(){
		ArrayDeque<java.util.Random> s = searchStack();
		if (s != null){
			s.push( new java.util.Random() );
			return;
		}
		synchronized (Random.class){
			mainGenerators.push( new java.util.Random() );
		}
	}

    public static void pushGenerator(long seed) {
		ArrayDeque<java.util.Random> s = searchStack();
		if (s != null){
			s.push(new java.util.Random(scrambleSeed(seed)));
			return;
		}
		synchronized (Random.class){
			mainGenerators.push(new java.util.Random(scrambleSeed(seed)));
		}
    }

    private static long scrambleSeed(long seed) {
        seed ^= seed >>> 32;
        seed *= -4710160504952957587L;
        seed ^= seed >>> 29;
        seed *= -4710160504952957587L;
        seed ^= seed >>> 32;
        seed *= -4710160504952957587L;
        seed ^= seed >>> 29;
        return seed;
    }


	public static void popGenerator(){
		ArrayDeque<java.util.Random> s = searchStack();
		if (s != null){
			if (s.size() == 1){
				Game.reportException( new RuntimeException("tried to pop the last random number generator!"));
			} else {
				s.pop();
			}
			return;
		}
		synchronized (Random.class){
			if (mainGenerators.size() == 1){
				Game.reportException( new RuntimeException("tried to pop the last random number generator!"));
			} else {
				mainGenerators.pop();
			}
		}
	}

	//returns a uniformly distributed float in the range [0, 1)
	public static float Float() {
		ArrayDeque<java.util.Random> s = searchStack();
		if (s != null) return s.peek().nextFloat();
		synchronized (Random.class){
			return mainGenerators.peek().nextFloat();
		}
	}

	//returns a uniformly distributed float in the range [0, max)
	public static float Float( float max ) {
		return Float() * max;
	}

	//returns a uniformly distributed float in the range [min, max)
	public static float Float( float min, float max ) {
		return min + Float(max - min);
	}

	//returns a triangularly distributed float in the range [min, max)
	public static float NormalFloat( float min, float max ) {
		return min + NormalFloat(2) * (max - min);
	}
	public static float NormalFloat( int times ){
		if (times == 0)
			return 0;
		float f = 0;
		for (int i = 0; i < times; i++)
			f += Float();
		return f / times;
	}
	public static float SB_Float( float times ){
		switch (Int(6)){
			case 0: return Float();
			case 1: return SB1_Float(times);
			case 2: return SB2_Float(times);
			case 3: return SB3_Float(times);
			case 4: return SB4_Float(times);
			default:return SB_Float(Float(times));
		}
	}
	public static float SB1_Float( float times ){
		float f = Float();
		times -= Float();
		while (times > 0){
			f += Float();
			f /= 2;
			times -= Float();
		}
		return f;
	}
	public static float SB2_Float( float times ){
		return (NormalFloat((int) times) + SB1_Float(times))/2;
	}
	public static float SB3_Float( float times ){
		return NormalFloat( NormalFloat(0, SB2_Float(times)), NormalFloat(SB2_Float(times), 1) );
	}
	public static float SB4_Float( float times ){
		float num = SB3_Float(times);
		while ( times > 0){
			num = (float) Math.pow(num, NormalFloat(0.5F, 2F));
			times -= Float();
		}
		return num;
	}

	//returns a uniformly distributed int in the range [0, max)
	public static int Int( int max ) {
		ArrayDeque<java.util.Random> s = searchStack();
		if (s != null) return max > 0 ? s.peek().nextInt(max) : 0;
		synchronized (Random.class){
			return max > 0 ? mainGenerators.peek().nextInt(max) : 0;
		}
	}

	//returns a uniformly distributed int in the range [min, max)
	public static int Int( int min, int max ) {
		return min + Int(max - min);
	}

	//returns a uniformly distributed int in the range [min, max]
	public static int IntRange( int min, int max ) {
		return min + Int(max - min + 1);
	}

	//returns a triangularly distributed int in the range [min, max]
	public static int NormalIntRange( int min, int max ) {
		return min + (int)(NormalFloat(2) * (max - min + 1));
	}

	//returns a uniformly distributed long in the range [-2^63, 2^63)
	public static long Long() {
		ArrayDeque<java.util.Random> s = searchStack();
		if (s != null) return s.peek().nextLong();
		synchronized (Random.class){
			return mainGenerators.peek().nextLong();
		}
	}

	//returns a uniformly distributed long in the range [0, max)
	public static long Long( long max ) {
		long result = Long();
		if (result < 0) result += Long.MAX_VALUE;
		return result % max;
	}

	//returns an index from chances, the probability of each index is the weight values in changes
	public static int chances( float[] chances ) {
		
		int length = chances.length;
		
		float sum = 0;
		for (int i=0; i < length; i++)
			if (chances[i] > 0)
				sum += chances[i];

		float value = Float( sum );
		sum = 0;
		for (int i=0; i < length; i++) {
			if (chances[i] <= 0)
				continue;
			sum += chances[i];
			if (value < sum) {
				return i;
			}
		}
		
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	//returns a key element from chances, the probability of each key is the weight value it maps to
	public static <K> K chances( HashMap<K,Float> chances ) {
		
		int size = chances.size();

		Object[] values = chances.keySet().toArray();
		float[] probs = new float[size];
		float sum = 0;
		for (int i=0; i < size; i++) {
			probs[i] = chances.get( values[i] );
			sum += probs[i];
		}
		
		if (sum <= 0) {
			return null;
		}
		
		float value = Float( sum );
		
		sum = probs[0];
		for (int i=0; i < size; i++) {
			if (value < sum) {
				return (K)values[i];
			}
			sum += probs[i + 1];
		}
		
		return null;
	}
	
	public static int index( Collection<?> collection ) {
		return Int(collection.size());
	}

	@SafeVarargs
	public static<T> T oneOf(T... array ) {
		return array[Int(array.length)];
	}
	
	public static<T> T element( T[] array ) {
		return element( array, array.length );
	}

	public static<T> T element( T[] array, int max ) {
		return array[Int(max)];
	}
	
	@SuppressWarnings("unchecked")
	public static<T> T element( Collection<? extends T> collection ) {
		int size = collection.size();
		return size > 0 ?
			(T)collection.toArray()[Int( size )] :
			null;
	}

	public static<T> void shuffle( List<?extends T> list){
		ArrayDeque<java.util.Random> s = searchStack();
		if (s != null){
			Collections.shuffle(list, s.peek());
			return;
		}
		synchronized (Random.class){
			Collections.shuffle(list, mainGenerators.peek());
		}
	}
	
	public static<T> void shuffle( T[] array ) {
		for (int i=0; i < array.length - 1; i++) {
			int j = Int( i, array.length );
			if (j != i) {
				T t = array[i];
				array[i] = array[j];
				array[j] = t;
			}
		}
	}
	
	public static<U,V> void shuffle( U[] u, V[]v ) {
		for (int i=0; i < u.length - 1; i++) {
			int j = Int( i, u.length );
			if (j != i) {
				U ut = u[i];
				u[i] = u[j];
				u[j] = ut;
				
				V vt = v[i];
				v[i] = v[j];
				v[j] = vt;
			}
		}
	}
}
