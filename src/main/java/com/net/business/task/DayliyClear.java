package com.net.business.task;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.business.beans.BeanManager;
import com.net.business.beans.redis.RedisOperate;
import com.net.business.util.Constants;

public class DayliyClear implements Runnable {
	private static Logger logger=LoggerFactory.getLogger(DayliyClear.class);
	
	@Override
	public void run() {
		Calendar calendar=Calendar.getInstance();
		int hour=calendar.get(Calendar.HOUR_OF_DAY);
		if (hour==0) {
			logger.info("#########每日清理缓存#######");
			RedisOperate redisOperate=BeanManager.getInstance().getRedisOperate();
			redisOperate.del(Constants.REDIS_FREE_COUNT_KEY);
			redisOperate.del(Constants.REDIS_FREE_MATCHING_COUNT_KEY);
			redisOperate.del(Constants.REDIS_FRIEND_ROOM_COUNT);
			redisOperate.del(Constants.REDIS_MATCHING_ROOM_COUNT);
			logger.info("#########每日清理成功#######");
		}
	}

}
