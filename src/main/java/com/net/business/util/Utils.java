package com.net.business.util;


import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {


	public static double getDistance(int x1, int x2, int y1, int y2) {
		return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
	}

	/**
	 * 在一定的范围内产生一个随机的整数,靠近低范围的一边概率偏大
	 * 
	 * @param min
	 *            最小值
	 * @param max
	 *            最大值
	 * @return
	 */
	public static int randomByScopeBySeed(int min, int max) {
		double r = Math.random();
		int seed = Math.abs(max - min);
		Double scope = seed * (1 - r) < 1 ? 1 : seed * (1 - r);
		return new Random().nextInt(scope.intValue()) + min;
	}
	
	public static int findMaxNumberIndex(int [] nums){
		int value = nums[0];
		int pos = 0;
		for(int i = 1; i < nums.length; i++){
			if(nums[i] > value){
				value = nums[i];
				pos = i;
			}
		}
		return pos;
	}
	
	public static int findMaxNumberIndex(int [] nums,int startIndex){
		int value = nums[startIndex];
		int pos = startIndex;
		int len = nums.length;
		int count=1;
		while(count<len){
			startIndex++;
			if (startIndex>len-1) {
				startIndex=0;
			}
			if(nums[startIndex] > value){
				value = nums[startIndex];
				pos = startIndex;
			}
			count++;
		}
		return pos;
	}
	
	/**
	 * 在一定范围内随机生成一个数字
	 * @param min
	 * @param max
	 * @return
	 */
	public static int randomByScope(int min,int max){
		Random random=new Random();
		return random.nextInt(max-min)+min;
	}

	/**
	 * 在scope以内随机一个值，如果小于传入的概率，返回true
	 * 
	 * @param chance
	 * @return
	 */
	public static boolean randomByChance(int chance, int scope) {
		return new Random().nextInt(scope) < chance;
	}

	/**
	 * 随机产生一个1000以内的整数，并从一个整形的List中找出和他最接近的数
	 * 
	 * @param nums
	 * @return
	 */
	public static int randomWithCloseNumbers(List<Integer> nums, int lookNum) {
		return LookCloseNum(lookNum, nums);
	}

	/**
	 * 随机产生一个1000以内的整数，并从一个整形的List中找出和他最接近的数的位置
	 * 
	 * @param nums
	 * @return
	 */
	public static int randomWithCloseNumbersIndex(List<Integer> nums, int lookNum) {
		return LookCloseNumIndex(lookNum, nums);
	}

	/**
	 * 相对概率，相当于抽奖箱,返回抽中的id，id存放在num中，此时count无效
	 * 
	 * @param maps
	 * @return
	 */
	public static int relativeRandom(List<Odds> vos) {
		Map<Integer, Integer> result = new LinkedHashMap<Integer, Integer>();

		int startseed = 0;
		int endseed = vos.get(0).getOdds();
		int total = 0;
		for (int i = 0; i < vos.size(); i++) {
			Odds info = vos.get(i);

			for (int j = startseed; j < endseed; j++) {
				result.put(j, info.getOddsId());
			}
			startseed += info.getOdds();
			endseed += vos.get(i + 1 >= vos.size() ? i : i + 1).getOdds();
			total += info.getOdds();
		}
		return result.get((int) (Math.random() * total));
	}

	/**
	 * 相对概率，相当于抽奖箱,返回抽中id在list中的下标
	 * 
	 * @param maps
	 * @return
	 */
	public static int relativeRandomReturnIndex(List<Odds> vos) {
		Map<Integer, Integer> result = new LinkedHashMap<Integer, Integer>();

		int startseed = 0;
		int endseed = vos.get(0).getOdds();
		int total = 0;
		for (int i = 0; i < vos.size(); i++) {
			Odds info = vos.get(i);

			for (int j = startseed; j < endseed; j++) {
				result.put(j, i);
			}
			startseed += info.getOdds();
			endseed += vos.get(i + 1 >= vos.size() ? i : i + 1).getOdds();
			total += info.getOdds();
		}
		return result.get((int) (Math.random() * total));
	}

	/**
	 * 从一个整形List集合里面找最接近lookNum的数字
	 * 
	 * @param lookNum
	 * @param nums
	 * @return
	 */
	public static int LookCloseNum(int lookNum, List<Integer> nums) {
		// 差值实始化
		int diffNum = Math.abs(nums.get(0) - lookNum);
		// 最接近的数字
		int result = nums.get(0);

		for (Integer integer : nums) {
			int diffNumTemp = Math.abs(integer - lookNum);
			if (diffNumTemp < diffNum) {
				diffNum = diffNumTemp;
				result = integer;
			}
		}
		return result;
	}

	/**
	 * 从一个整形List集合里面找最接近lookNum的数字的位置
	 * 
	 * @param lookNum
	 * @param nums
	 * @return
	 */
	public static int LookCloseNumIndex(int lookNum, List<Integer> nums) {
		// 差值实始化
		int diffNum = Math.abs(nums.get(0) - lookNum);
		// 最接近的数字
		int index = 0;

		for (int i = 0; i < nums.size(); i++) {
			int diffNumTemp = Math.abs(nums.get(i) - lookNum);
			if (diffNumTemp < diffNum) {
				diffNum = diffNumTemp;
				index = i;
			}
		}
		return index;
	}

	public static int randomNumberWithArray(int... num) {
		if (num == null || num.length <= 0) {
			return 0;
		}
		int seed = new Random().nextInt(num.length);
		return num[seed];
	}

	public static int randomNumberByList(List<Integer> integers) {
		if (integers == null || integers.size() <= 0) {
			return 0;
		}
		int seed = new Random().nextInt(integers.size());
		return integers.get(seed);
	}

	/**
	 * 在一定范围内产生N个不重复的随机数
	 * 
	 * @return
	 */
	public static List<Integer> randomNumbers(int count, int scope) {
		List<Integer> numbers = new ArrayList<Integer>();
		int i = 0;
		while (i < count && i < scope) {
			int number = new Random().nextInt(scope);
			boolean hasRepeat = false;
			for (int j = 0; j < numbers.size(); j++) {
				if (numbers.get(j).intValue() == number) {
					hasRepeat = true;
					break;
				}
			}
			if (!hasRepeat) {
				numbers.add(i, number);
				i++;
			}
		}
		return numbers;
	}

	/**
	 * 随机生成一个min,max之间的一个数
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static int getRandom(int min, int max) {
		Random random = new Random();
		return random.nextInt(max) % (max - min + 1) + min;
	}

	public static double div(double value1, double value2, int scale) throws IllegalAccessException {
		// 如果精确范围小于0，抛出异常信息
		if (scale < 0) {
			throw new IllegalAccessException("精确度不能小于0");
		}
		BigDecimal b1 = new BigDecimal(Double.valueOf(value1));
		BigDecimal b2 = new BigDecimal(Double.valueOf(value2));
		return b1.divide(b2, scale).doubleValue();
	}

	/**
	 * 计算两个日期之间相差的天数
	 * 
	 * @param smdate
	 *            较小的时间
	 * @param bdate
	 *            较大的时间
	 * @return 相差天数
	 * @throws ParseException
	 */
	public static int daysBetween(Date smdate, Date bdate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		smdate = sdf.parse(sdf.format(smdate));
		bdate = sdf.parse(sdf.format(bdate));
		Calendar cal = Calendar.getInstance();
		cal.setTime(smdate);
		long time1 = cal.getTimeInMillis();
		cal.setTime(bdate);
		long time2 = cal.getTimeInMillis();
		long between_days = (time2 - time1) / (1000 * 3600 * 24);

		return Integer.parseInt(String.valueOf(between_days));
	}

}
