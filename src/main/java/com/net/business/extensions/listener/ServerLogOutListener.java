package com.net.business.extensions.listener;

import com.net.business.beans.BeanManager;
import com.net.business.beans.redis.RedisOperate;
import com.net.business.extensions.core.BaseServerEventHandler;
import com.net.server.core.ISFSEvent;
import com.net.server.core.SFSEventParam;
import com.net.server.entities.User;

/**
 * 用户退出事件监听
 * @author caipeiping
 *
 */
public class ServerLogOutListener extends BaseServerEventHandler {
	private RedisOperate redisOperate;
	
	@SuppressWarnings("unused")
	public void handleServerEvent(ISFSEvent paramISFSEvent) throws Exception {
//		User user = (User)paramISFSEvent.getParameter(SFSEventParam.USER);
//		
//		if (redisOperate==null) {
//			redisOperate=BeanManager.getInstance().getRedisOperate();
//		}
	}

}
