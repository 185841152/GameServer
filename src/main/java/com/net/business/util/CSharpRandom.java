package com.net.business.util;

import java.util.Random;

/**
 * C#的随机数算法
 * 
 * @author shangyong
 *
 */
public class CSharpRandom {

	private final int MBIG = Integer.MAX_VALUE;
	private final int MSEED = 161803398;

	private int inext, inextp;
	private int[] SeedArray = new int[56];

	public CSharpRandom(int seed) {
		setSeed(seed);
	}
	
	public CSharpRandom() {
	}
	
	public void setSeed(int seed){
		int ii;
		int mj, mk;

		// 初始化种子数组
		// 该算法是来自于C的数值算法
		mj = MSEED - Math.abs(seed);
		SeedArray[55] = mj;
		mk = 1;
		// 范围[1..55] 是特殊的所以忽略掉0的位置
		for (int i = 1; i < 55; i++) {
			ii = (21 * i) % 55;
			SeedArray[ii] = mk;
			mk = mj - mk;
			if (mk < 0)
				mk += MBIG;
			mj = SeedArray[ii];
		}
		for (int k = 1; k < 5; k++) {
			for (int i = 1; i < 56; i++) {
				SeedArray[i] -= SeedArray[1 + (i + 30) % 55];
				if (SeedArray[i] < 0)
					SeedArray[i] += MBIG;
			}
		}
		inext = 0;
		inextp = 21;
		seed = 1;
	}

	/**
	 * 返回新的随机数 [0..1) 并且重新替换种子列表.
	 * 
	 * @return [0..1)
	 */
	protected double sample() {
		int retVal;
		int locINext = inext;
		int locINextp = inextp;

		if (++locINext >= 56)
			locINext = 1;
		if (++locINextp >= 56)
			locINextp = 1;

		retVal = SeedArray[locINext] - SeedArray[locINextp];

		if (retVal < 0)
			retVal += MBIG;

		SeedArray[locINext] = retVal;

		inext = locINext;
		inextp = locINextp;

		// 分布的随机数
		return (retVal * (1.0 / MBIG));
	}

	/**
	 * @return [0.._int4.MaxValue)
	 */
	public int next() {
		return (int) (sample() * Integer.MAX_VALUE);
	}

	/**
	 * @param minValue
	 * @param maxValue
	 * @return [minValue..maxValue)
	 * @throws Exception
	 */
	public int next(int minValue, int maxValue)  {//throws Exception
//		if (minValue > maxValue) {
//			throw new Exception("[out of range exception] minValue : "
//					+ minValue + ", maxValue : " + maxValue);
//		}

		int range = (maxValue - minValue);
		if (range < 0) {
			long longRange = maxValue - minValue;
			return (int) ((long) (sample() * longRange) + minValue);
		}
		return (int) (sample() * range) + minValue;
	}

	/**
	 * @param maxValue
	 * @return [0..maxValue)
	 * @throws Exception
	 */
	public int next(int maxValue) throws Exception {
		if (maxValue < 0) {
			throw new Exception("[argument exception] maxValue :" + maxValue);
		}
		return (int) (sample() * maxValue);
	}

	/**
	 * @return double[0..1)
	 * @exception none
	 */
	public double NextDouble() {
		return sample();
	}

	/**
	 * 随机产生[0..0x7f]字节数，填充整个byte数组
	 * 
	 * @param buffer 需要填充的数组.
	 * @return void
	 * @throws NullPointerException
	 */
	public void nextBytes(byte[] buffer) {
		if (buffer == null)
			throw new NullPointerException("buffer");
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = (byte) (sample() * (Byte.MAX_VALUE + 1));
		}
	}
	
}
