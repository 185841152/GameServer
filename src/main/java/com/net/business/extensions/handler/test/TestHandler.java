package com.net.business.extensions.handler.test;

import com.net.business.extensions.core.BaseClientRequestHandler;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;
import com.net.server.entities.User;

public class TestHandler extends BaseClientRequestHandler {

	@Override
	public void handleClientRequest(User user, IGameObject params) throws Exception {
		String name=params.getUtfString("name");
		
		IGameObject result=GameObject.newInstance();
		result.putUtfString("name", "服务器返回"+name);
//		sendResponse(SystemRequest.TEST.getId(), result, user);
		
//		sendResponse(SystemRequest.TEST.getId(), params, user);
		// RedisOperate operate=BeanManager.getInstance().getRedisOperate();
		// operate.set("test", "hello", 0);
		// logger.info("xxoo");
		// validatePVE(user, params);
		// CostItem classUpCost=new CostItem(new Byte("1"), 3, 200);
		// if (commonService==null) {
		// commonService=BeanManager.getInstance().getCommonService();
		// }
		// IGameArray array=commonService.addPlayerResourceOrItems(classUpCost,
		// user.getPlayer());
		// IGameObject result=GameObject.newInstance();
		// result.putGameArray("dp", array);
		//
		// sendResponse(SystemRequest.TEST.getId(), result, user);
		// IGameObject result=GameObject.newInstance();
		// IResponse response = new Response();
		// response.setId(SystemRequest.PLAYER_LEVEL_UP.getId());
		// response.setContent(result);
		// response.setRecipients(user.getSession());
		// response.write(2);
	}

}
