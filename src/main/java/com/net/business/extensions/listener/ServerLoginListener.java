package com.net.business.extensions.listener;

import java.util.HashMap;
import java.util.Map;

import com.net.business.beans.BeanManager;
import com.net.business.beans.redis.RedisOperate;
import com.net.business.extensions.core.BaseServerEventHandler;
import com.net.business.util.Constants;
import com.net.server.core.ISFSEvent;
import com.net.server.core.SFSEventParam;
import com.net.server.entities.User;

public class ServerLoginListener extends BaseServerEventHandler {
	private RedisOperate redisOperate;

	public void handleServerEvent(ISFSEvent paramISFSEvent) throws Exception {
//		User user = (User) paramISFSEvent.getParameter(SFSEventParam.USER);
//
//		if (redisOperate == null) {
//			redisOperate = BeanManager.getInstance().getRedisOperate();
//		}
//		//将用户存入redis
//		Map<String, String> userMap=new HashMap<String, String>();
//		userMap.put("sessionId", user.getSessionId()+"");
//		userMap.put("gold_bean", user.getProperty("gold_bean").toString());
//		userMap.put("picture", user.getProperty("picture").toString());
//		userMap.put("u", user.getName());
//		redisOperate.mapPut(Constants.USER_REDIS_KEY+user.getId(), userMap);
	}

}
